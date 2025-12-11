package com.luck.picture.lib.photoview

import android.content.Context
import android.graphics.Matrix
import android.graphics.Matrix.ScaleToFit
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.ImageView
import android.widget.OverScroller
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The component of [com.github.chrisbanes.photoview.PhotoView] which does the work allowing for zooming, scaling, panning, etc.
 * It is made public in case you need to subclass something other than AppCompatImageView and still
 * gain the functionality that [com.github.chrisbanes.photoview.PhotoView] offers
 */
class PhotoViewAttacher(private val mImageView: ImageView) : OnTouchListener,
    View.OnLayoutChangeListener {
    private var mInterpolator: Interpolator = AccelerateDecelerateInterpolator()
    private var mZoomDuration: Int = PhotoViewAttacher.Companion.DEFAULT_ZOOM_DURATION
    private var mMinScale: Float = PhotoViewAttacher.Companion.DEFAULT_MIN_SCALE
    private var mMidScale: Float = PhotoViewAttacher.Companion.DEFAULT_MID_SCALE
    private var mMaxScale: Float = PhotoViewAttacher.Companion.DEFAULT_MAX_SCALE

    private var mAllowParentInterceptOnEdge = true
    private var mBlockParentIntercept = false

    // Gesture Detectors
    private var mGestureDetector: GestureDetector? = null
    private var mScaleDragDetector: CustomGestureDetector? = null

    // These are set so we don't keep allocating them on the heap
    private val mBaseMatrix = Matrix()
    val imageMatrix: Matrix = Matrix()
    private val mSuppMatrix = Matrix()
    private val mDisplayRect = RectF()
    private val mMatrixValues = FloatArray(9)

    // Listeners
    private var mMatrixChangeListener: OnMatrixChangedListener? = null
    private var mPhotoTapListener: OnPhotoTapListener? = null
    private var mOutsidePhotoTapListener: OnOutsidePhotoTapListener? = null
    private var mViewTapListener: OnViewTapListener? = null
    private var mOnClickListener: View.OnClickListener? = null
    private var mLongClickListener: OnLongClickListener? = null
    private var mScaleChangeListener: OnScaleChangedListener? = null
    private var mSingleFlingListener: OnSingleFlingListener? = null
    private var mOnViewDragListener: OnViewDragListener? = null

    private var mCurrentFlingRunnable: FlingRunnable? = null
    private var mHorizontalScrollEdge: Int = PhotoViewAttacher.Companion.HORIZONTAL_EDGE_BOTH
    private var mVerticalScrollEdge: Int = PhotoViewAttacher.Companion.VERTICAL_EDGE_BOTH
    private var mBaseRotation: Float = 0f

    @get:Deprecated("")
    var isZoomEnabled: Boolean = true
        private set
    private var mScaleType = ImageView.ScaleType.FIT_CENTER

    private val onGestureListener: OnGestureListener = object : OnGestureListener {
        override fun onDrag(dx: Float, dy: Float) {
            if (mScaleDragDetector!!.isScaling) {
                return  // Do not drag if we are already scaling
            }
            if (mOnViewDragListener != null) {
                mOnViewDragListener!!.onDrag(dx, dy)
            }
            mSuppMatrix.postTranslate(dx, dy)
            checkAndDisplayMatrix()

            /*
             * Here we decide whether to let the ImageView's parent to start taking
             * over the touch event.
             *
             * First we check whether this function is enabled. We never want the
             * parent to take over if we're scaling. We then check the edge we're
             * on, and the direction of the scroll (i.e. if we're pulling against
             * the edge, aka 'overscrolling', let the parent take over).
             */
            val parent = mImageView.getParent()
            if (mAllowParentInterceptOnEdge && !mScaleDragDetector!!.isScaling && !mBlockParentIntercept) {
                if (mHorizontalScrollEdge == PhotoViewAttacher.Companion.HORIZONTAL_EDGE_BOTH || (mHorizontalScrollEdge == PhotoViewAttacher.Companion.HORIZONTAL_EDGE_LEFT && dx >= 1f)
                    || (mHorizontalScrollEdge == PhotoViewAttacher.Companion.HORIZONTAL_EDGE_RIGHT && dx <= -1f)
                    || (mVerticalScrollEdge == PhotoViewAttacher.Companion.VERTICAL_EDGE_TOP && dy >= 1f)
                    || (mVerticalScrollEdge == PhotoViewAttacher.Companion.VERTICAL_EDGE_BOTTOM && dy <= -1f)
                ) {
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
            } else {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
        }

        override fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float) {
            mCurrentFlingRunnable = FlingRunnable(mImageView.context)
            mCurrentFlingRunnable!!.fling(
                getImageViewWidth(mImageView),
                getImageViewHeight(mImageView), velocityX.toInt(), velocityY.toInt()
            )
            mImageView.post(mCurrentFlingRunnable)
        }

        override fun onScale(scaleFactor: Float, focusX: Float, focusY: Float) {
            onScale(scaleFactor, focusX, focusY, 0f, 0f)
        }

        override fun onScale(
            scaleFactor: Float,
            focusX: Float,
            focusY: Float,
            dx: Float,
            dy: Float
        ) {
            if (this@PhotoViewAttacher.scale < mMaxScale || scaleFactor < 1f) {
                if (mScaleChangeListener != null) {
                    mScaleChangeListener!!.onScaleChange(scaleFactor, focusX, focusY)
                }
                mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
                mSuppMatrix.postTranslate(dx, dy)
                checkAndDisplayMatrix()
            }
        }
    }

    init {
        mImageView.setOnTouchListener(this)
        mImageView.addOnLayoutChangeListener(this)
        if (!mImageView.isInEditMode()) {
            mBaseRotation = 0.0f
            // Create Gesture Detectors...
            mScaleDragDetector = CustomGestureDetector(mImageView.context, onGestureListener)
            mGestureDetector =
                GestureDetector(mImageView.context, object : SimpleOnGestureListener() {
                    // forward long click listener
                    override fun onLongPress(e: MotionEvent) {
                        if (mLongClickListener != null) {
                            mLongClickListener!!.onLongClick(mImageView)
                        }
                    }

                    override fun onFling(
                        e1: MotionEvent?, e2: MotionEvent,
                        velocityX: Float, velocityY: Float
                    ): Boolean {
                        if (mSingleFlingListener != null && e1 != null) {
                            if (this@PhotoViewAttacher.scale > PhotoViewAttacher.Companion.DEFAULT_MIN_SCALE) {
                                return false
                            }
                            if (e1.getPointerCount() > PhotoViewAttacher.Companion.SINGLE_TOUCH
                                || e2.getPointerCount() > PhotoViewAttacher.Companion.SINGLE_TOUCH
                            ) {
                                return false
                            }
                            return mSingleFlingListener!!.onFling(e1, e2, velocityX, velocityY)
                        }
                        return false
                    }
                })
            mGestureDetector!!.setOnDoubleTapListener(object : OnDoubleTapListener {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (mOnClickListener != null) {
                    mOnClickListener!!.onClick(mImageView)
                }
                val displayRect: RectF? = this@PhotoViewAttacher.displayRect
                val x = e.getX()
                val y = e.getY()
                if (mViewTapListener != null) {
                    mViewTapListener!!.onViewTap(mImageView, x, y)
                }
                if (displayRect != null) {
                    // Check to see if the user tapped on the photo
                    if (displayRect.contains(x, y)) {
                        val xResult = ((x - displayRect.left)
                                / displayRect.width())
                        val yResult = ((y - displayRect.top)
                                / displayRect.height())
                        if (mPhotoTapListener != null) {
                            mPhotoTapListener!!.onPhotoTap(mImageView, xResult, yResult)
                        }
                        return true
                    } else {
                        if (mOutsidePhotoTapListener != null) {
                            mOutsidePhotoTapListener!!.onOutsidePhotoTap(mImageView)
                        }
                    }
                }
                return false
            }

            override fun onDoubleTap(ev: MotionEvent): Boolean {
                try {
                    val scale: Float = this@PhotoViewAttacher.scale
                    val x = ev.getX()
                    val y = ev.getY()
                    if (scale < this@PhotoViewAttacher.mediumScale) {
                        setScale(this@PhotoViewAttacher.mediumScale, x, y, true)
                    } else if (scale >= this@PhotoViewAttacher.mediumScale && scale < this@PhotoViewAttacher.maximumScale) {
                        setScale(this@PhotoViewAttacher.maximumScale, x, y, true)
                    } else {
                        setScale(this@PhotoViewAttacher.minimumScale, x, y, true)
                    }
                } catch (e: ArrayIndexOutOfBoundsException) {
                    // Can sometimes happen when getX() and getY() is called
                }
                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                // Wait for the confirmed onDoubleTap() instead
                return false
            }
        })
        }
    }

    fun setOnDoubleTapListener(newOnDoubleTapListener: OnDoubleTapListener?) {
        this.mGestureDetector!!.setOnDoubleTapListener(newOnDoubleTapListener)
    }

    fun setOnScaleChangeListener(onScaleChangeListener: OnScaleChangedListener?) {
        this.mScaleChangeListener = onScaleChangeListener
    }

    fun setOnSingleFlingListener(onSingleFlingListener: OnSingleFlingListener?) {
        this.mSingleFlingListener = onSingleFlingListener
    }

    val displayRect: RectF?
        get() {
            checkMatrixBounds()
            return getDisplayRect(this.drawMatrix)
        }

    fun setDisplayMatrix(finalMatrix: Matrix): Boolean {
        requireNotNull(finalMatrix) { "Matrix cannot be null" }
        if (mImageView.getDrawable() == null) {
            return false
        }
        mSuppMatrix.set(finalMatrix)
        checkAndDisplayMatrix()
        return true
    }

    fun setBaseRotation(degrees: Float) {
        mBaseRotation = degrees % 360
        update()
        setRotationBy(mBaseRotation)
        checkAndDisplayMatrix()
    }

    fun setRotationTo(degrees: Float) {
        mSuppMatrix.setRotate(degrees % 360)
        checkAndDisplayMatrix()
    }

    fun setRotationBy(degrees: Float) {
        mSuppMatrix.postRotate(degrees % 360)
        checkAndDisplayMatrix()
    }

    var minimumScale: Float
        get() = mMinScale
        set(minimumScale) {
            Util.checkZoomLevels(minimumScale, mMidScale, mMaxScale)
            mMinScale = minimumScale
        }

    var mediumScale: Float
        get() = mMidScale
        set(mediumScale) {
            Util.checkZoomLevels(mMinScale, mediumScale, mMaxScale)
            mMidScale = mediumScale
        }

    var maximumScale: Float
        get() = mMaxScale
        set(maximumScale) {
            Util.checkZoomLevels(mMinScale, mMidScale, maximumScale)
            mMaxScale = maximumScale
        }

    var scale: Float
        get() = sqrt(
            (getValue(
                mSuppMatrix,
                Matrix.MSCALE_X
            ).toDouble().pow(2.0).toFloat() + getValue(
                mSuppMatrix,
                Matrix.MSKEW_Y
            ).toDouble().pow(2.0).toFloat()).toDouble()
        ).toFloat()
        set(scale) {
            setScale(scale, false)
        }

    var scaleType: ImageView.ScaleType
        get() = mScaleType
        set(scaleType) {
            if (Util.isSupportedScaleType(scaleType) && scaleType != mScaleType) {
                mScaleType = scaleType
                update()
            }
        }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        // Update our base matrix, as the bounds have changed
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            updateBaseMatrix(mImageView.getDrawable())
        }
    }

    override fun onTouch(v: View?, ev: MotionEvent): Boolean {
        var handled = false
        val imageView = v as? ImageView
        if (this.isZoomEnabled && imageView != null && Util.hasDrawable(imageView)) {
            when (ev.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                    val parent = v!!.getParent()
                    // First, disable the Parent from intercepting the touch
                    // event
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                    // If we're flinging, and the user presses down, cancel
                    // fling
                    cancelFling()
                }

                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    // If the user has zoomed less than min scale, zoom back
                    // to min scale
                    if (this@PhotoViewAttacher.scale < mMinScale) {
                        val rect = this@PhotoViewAttacher.displayRect
                        if (rect != null) {
                            v!!.post(
                                AnimatedZoomRunnable(
                                    this@PhotoViewAttacher.scale, mMinScale,
                                    rect.centerX(), rect.centerY()
                                )
                            )
                            handled = true
                        }
                    } else if (this@PhotoViewAttacher.scale > mMaxScale) {
                        val rect = this@PhotoViewAttacher.displayRect
                        if (rect != null) {
                            v!!.post(
                                AnimatedZoomRunnable(
                                    this@PhotoViewAttacher.scale, mMaxScale,
                                    rect.centerX(), rect.centerY()
                                )
                            )
                            handled = true
                        }
                    }
                }
            }
            // Try the Scale/Drag detector
            val scaleDragDetector = mScaleDragDetector
            if (scaleDragDetector != null) {
                val wasScaling = scaleDragDetector.isScaling
                val wasDragging = scaleDragDetector.isDragging
                handled = scaleDragDetector.onTouchEvent(ev)
                val didntScale = !wasScaling && !scaleDragDetector.isScaling
                val didntDrag = !wasDragging && !scaleDragDetector.isDragging
                mBlockParentIntercept = didntScale && didntDrag
            }
            // Check to see if the user double tapped
            val gestureDetector = mGestureDetector
            if (gestureDetector != null && gestureDetector.onTouchEvent(ev)) {
                handled = true
            }
        }
        return handled
    }

    fun setAllowParentInterceptOnEdge(allow: Boolean) {
        mAllowParentInterceptOnEdge = allow
    }

    fun setScaleLevels(minimumScale: Float, mediumScale: Float, maximumScale: Float) {
        Util.checkZoomLevels(minimumScale, mediumScale, maximumScale)
        mMinScale = minimumScale
        mMidScale = mediumScale
        mMaxScale = maximumScale
    }

    fun setOnLongClickListener(listener: OnLongClickListener?) {
        mLongClickListener = listener
    }

    fun setOnClickListener(listener: View.OnClickListener?) {
        mOnClickListener = listener
    }

    fun setOnMatrixChangeListener(listener: OnMatrixChangedListener?) {
        mMatrixChangeListener = listener
    }

    fun setOnPhotoTapListener(listener: OnPhotoTapListener?) {
        mPhotoTapListener = listener
    }

    fun setOnOutsidePhotoTapListener(mOutsidePhotoTapListener: OnOutsidePhotoTapListener?) {
        this.mOutsidePhotoTapListener = mOutsidePhotoTapListener
    }

    fun setOnViewTapListener(listener: OnViewTapListener?) {
        mViewTapListener = listener
    }

    fun setOnViewDragListener(listener: OnViewDragListener?) {
        mOnViewDragListener = listener
    }

    fun setScale(scale: Float, animate: Boolean) {
        setScale(
            scale,
            ((mImageView.getRight()) / 2).toFloat(),
            ((mImageView.getBottom()) / 2).toFloat(),
            animate
        )
    }

    fun setScale(
        scale: Float, focalX: Float, focalY: Float,
        animate: Boolean
    ) {
        // Check to see if the scale is within bounds
        require(!(scale < mMinScale || scale > mMaxScale)) { "Scale must be within the range of minScale and maxScale" }
        if (animate) {
            mImageView.post(
                AnimatedZoomRunnable(
                    this@PhotoViewAttacher.scale, scale,
                    focalX, focalY
                )
            )
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY)
            checkAndDisplayMatrix()
        }
    }

    /**
     * Set the zoom interpolator
     *
     * @param interpolator the zoom interpolator
     */
    fun setZoomInterpolator(interpolator: Interpolator) {
        mInterpolator = interpolator
    }

    var isZoomable: Boolean
        get() = this.isZoomEnabled
        set(zoomable) {
            this.isZoomEnabled = zoomable
            update()
        }

    fun update() {
        if (this.isZoomEnabled) {
            // Update the base matrix using the current drawable
            updateBaseMatrix(mImageView.getDrawable())
        } else {
            // Reset the Matrix...
            resetMatrix()
        }
    }

    /**
     * Get the display matrix
     *
     * @param matrix target matrix to copy to
     */
    fun getDisplayMatrix(matrix: Matrix) {
        matrix.set(this.drawMatrix)
    }

    /**
     * Get the current support matrix
     */
    fun getSuppMatrix(matrix: Matrix) {
        matrix.set(mSuppMatrix)
    }

    private val drawMatrix: Matrix
        get() {
            imageMatrix.set(mBaseMatrix)
            imageMatrix.postConcat(mSuppMatrix)
            return this.imageMatrix
        }

    fun setZoomTransitionDuration(milliseconds: Int) {
        this.mZoomDuration = milliseconds
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     */
    private fun getValue(matrix: Matrix, whichValue: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[whichValue]
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     */
    private fun resetMatrix() {
        mSuppMatrix.reset()
        setRotationBy(mBaseRotation)
        setImageViewMatrix(this.drawMatrix)
        checkMatrixBounds()
    }

    private fun setImageViewMatrix(matrix: Matrix) {
        mImageView.setImageMatrix(matrix)
        // Call MatrixChangedListener if needed
        if (mMatrixChangeListener != null) {
            val displayRect = getDisplayRect(matrix)
            if (displayRect != null) {
                mMatrixChangeListener!!.onMatrixChanged(displayRect)
            }
        }
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private fun checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(this.drawMatrix)
        }
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     *
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    internal fun getDisplayRect(matrix: Matrix): RectF? {
        val d = mImageView.getDrawable()
        if (d != null) {
            mDisplayRect.set(
                0f, 0f, d.getIntrinsicWidth().toFloat(),
                d.getIntrinsicHeight().toFloat()
            )
            matrix.mapRect(mDisplayRect)
            return mDisplayRect
        }
        return null
    }

    /**
     * Calculate Matrix for FIT_CENTER
     *
     * @param drawable - Drawable being displayed
     */
    private fun updateBaseMatrix(drawable: Drawable?) {
        if (drawable == null) {
            return
        }
        val viewWidth = getImageViewWidth(mImageView).toFloat()
        val viewHeight = getImageViewHeight(mImageView).toFloat()
        val drawableWidth = drawable.getIntrinsicWidth()
        val drawableHeight = drawable.getIntrinsicHeight()
        mBaseMatrix.reset()
        val widthScale = viewWidth / drawableWidth
        val heightScale = viewHeight / drawableHeight
        if (mScaleType == ImageView.ScaleType.CENTER) {
            mBaseMatrix.postTranslate(
                (viewWidth - drawableWidth) / 2f,
                (viewHeight - drawableHeight) / 2f
            )
        } else if (mScaleType == ImageView.ScaleType.CENTER_CROP) {
            val scale = max(widthScale.toDouble(), heightScale.toDouble()).toFloat()
            mBaseMatrix.postScale(scale, scale)
            mBaseMatrix.postTranslate(
                (viewWidth - drawableWidth * scale) / 2f,
                (viewHeight - drawableHeight * scale) / 2f
            )
        } else if (mScaleType == ImageView.ScaleType.CENTER_INSIDE) {
            val scale = min(1.0, min(widthScale.toDouble(), heightScale.toDouble())).toFloat()
            mBaseMatrix.postScale(scale, scale)
            mBaseMatrix.postTranslate(
                (viewWidth - drawableWidth * scale) / 2f,
                (viewHeight - drawableHeight * scale) / 2f
            )
        } else {
            var mTempSrc = RectF(0f, 0f, drawableWidth.toFloat(), drawableHeight.toFloat())
            val mTempDst = RectF(0f, 0f, viewWidth, viewHeight)
            if (mBaseRotation.toInt() % 180 != 0) {
                mTempSrc = RectF(0f, 0f, drawableHeight.toFloat(), drawableWidth.toFloat())
            }
            when (mScaleType) {
                ImageView.ScaleType.FIT_CENTER -> mBaseMatrix.setRectToRect(
                    mTempSrc,
                    mTempDst,
                    ScaleToFit.CENTER
                )

                ImageView.ScaleType.FIT_START -> mBaseMatrix.setRectToRect(
                    mTempSrc,
                    mTempDst,
                    ScaleToFit.START
                )

                ImageView.ScaleType.FIT_END -> mBaseMatrix.setRectToRect(
                    mTempSrc,
                    mTempDst,
                    ScaleToFit.END
                )

                ImageView.ScaleType.FIT_XY -> mBaseMatrix.setRectToRect(
                    mTempSrc,
                    mTempDst,
                    ScaleToFit.FILL
                )

                else -> {}
            }
        }
        resetMatrix()
    }

    private fun checkMatrixBounds(): Boolean {
        val rect = getDisplayRect(this.drawMatrix)
        if (rect == null) {
            return false
        }
        val height = rect.height()
        val width = rect.width()
        var deltaX = 0f
        var deltaY = 0f
        val viewHeight = getImageViewHeight(mImageView)
        if (height <= viewHeight) {
            when (mScaleType) {
                ImageView.ScaleType.FIT_START -> deltaY = -rect.top
                ImageView.ScaleType.FIT_END -> deltaY = viewHeight - height - rect.top
                else -> deltaY = (viewHeight - height) / 2 - rect.top
            }
            mVerticalScrollEdge = PhotoViewAttacher.Companion.VERTICAL_EDGE_BOTH
        } else if (rect.top > 0) {
            mVerticalScrollEdge = PhotoViewAttacher.Companion.VERTICAL_EDGE_TOP
            deltaY = -rect.top
        } else if (rect.bottom < viewHeight) {
            mVerticalScrollEdge = PhotoViewAttacher.Companion.VERTICAL_EDGE_BOTTOM
            deltaY = viewHeight - rect.bottom
        } else {
            mVerticalScrollEdge = PhotoViewAttacher.Companion.VERTICAL_EDGE_NONE
        }
        val viewWidth = getImageViewWidth(mImageView)
        if (width <= viewWidth) {
            when (mScaleType) {
                ImageView.ScaleType.FIT_START -> deltaX = -rect.left
                ImageView.ScaleType.FIT_END -> deltaX = viewWidth - width - rect.left
                else -> deltaX = (viewWidth - width) / 2 - rect.left
            }
            mHorizontalScrollEdge = PhotoViewAttacher.Companion.HORIZONTAL_EDGE_BOTH
        } else if (rect.left > 0) {
            mHorizontalScrollEdge = PhotoViewAttacher.Companion.HORIZONTAL_EDGE_LEFT
            deltaX = -rect.left
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right
            mHorizontalScrollEdge = PhotoViewAttacher.Companion.HORIZONTAL_EDGE_RIGHT
        } else {
            mHorizontalScrollEdge = PhotoViewAttacher.Companion.HORIZONTAL_EDGE_NONE
        }
        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY)
        return true
    }

    private fun getImageViewWidth(imageView: ImageView): Int {
        return imageView.width - imageView.getPaddingLeft() - imageView.getPaddingRight()
    }

    private fun getImageViewHeight(imageView: ImageView): Int {
        return imageView.height - imageView.getPaddingTop() - imageView.getPaddingBottom()
    }

    private fun cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable!!.cancelFling()
            mCurrentFlingRunnable = null
        }
    }

    private inner class AnimatedZoomRunnable(
        private val mZoomStart: Float, private val mZoomEnd: Float,
        private val mFocalX: Float, private val mFocalY: Float
    ) : Runnable {
        private val mStartTime: Long

        init {
            mStartTime = System.currentTimeMillis()
        }

        override fun run() {
            val t = interpolate()
            val scale = mZoomStart + t * (mZoomEnd - mZoomStart)
            val deltaScale: Float = scale / this@PhotoViewAttacher.scale
            onGestureListener.onScale(deltaScale, mFocalX, mFocalY)
            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                Compat.postOnAnimation(mImageView, this)
            }
        }

        fun interpolate(): Float {
            var t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration
            t = min(1.0, t.toDouble()).toFloat()
            t = mInterpolator.getInterpolation(t)
            return t
        }
    }

    private inner class FlingRunnable(context: Context?) : Runnable {
        private val mScroller: OverScroller
        private var mCurrentX = 0
        private var mCurrentY = 0

        init {
            mScroller = OverScroller(context)
        }

        fun cancelFling() {
            mScroller.forceFinished(true)
        }

        fun fling(
            viewWidth: Int, viewHeight: Int, velocityX: Int,
            velocityY: Int
        ) {
            val rect: RectF? = this@PhotoViewAttacher.displayRect
            if (rect == null) {
                return
            }
            val startX = Math.round(-rect.left).toInt()
            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int
            if (viewWidth < rect.width()) {
                minX = 0
                maxX = Math.round(rect.width() - viewWidth).toInt()
            } else {
                maxX = startX
                minX = maxX
            }
            val startY = Math.round(-rect.top).toInt()
            if (viewHeight < rect.height()) {
                minY = 0
                maxY = Math.round(rect.height() - viewHeight).toInt()
            } else {
                maxY = startY
                minY = maxY
            }
            mCurrentX = startX
            mCurrentY = startY
            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(
                    startX, startY, velocityX, velocityY, minX,
                    maxX, minY, maxY, 0, 0
                )
            }
        }

        override fun run() {
            if (mScroller.isFinished()) {
                return  // remaining post that should not be handled
            }
            if (mScroller.computeScrollOffset()) {
                val newX = mScroller.getCurrX()
                val newY = mScroller.getCurrY()
                mSuppMatrix.postTranslate(
                    (mCurrentX - newX).toFloat(),
                    (mCurrentY - newY).toFloat()
                )
                checkAndDisplayMatrix()
                mCurrentX = newX
                mCurrentY = newY
                // Post On animation
                Compat.postOnAnimation(mImageView, this)
            }
        }
    }

    companion object {
        private const val DEFAULT_MAX_SCALE = 3.0f
        private const val DEFAULT_MID_SCALE = 1.75f
        private const val DEFAULT_MIN_SCALE = 1.0f
        private const val DEFAULT_ZOOM_DURATION = 200

        private val HORIZONTAL_EDGE_NONE = -1
        private const val HORIZONTAL_EDGE_LEFT = 0
        private const val HORIZONTAL_EDGE_RIGHT = 1
        private const val HORIZONTAL_EDGE_BOTH = 2
        private val VERTICAL_EDGE_NONE = -1
        private const val VERTICAL_EDGE_TOP = 0
        private const val VERTICAL_EDGE_BOTTOM = 1
        private const val VERTICAL_EDGE_BOTH = 2
        private const val SINGLE_TOUCH = 1
    }
}
