package com.yalantis.ucrop.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.RectF
import android.graphics.Region
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.NonNull
import com.yalantis.ucrop.R
import com.yalantis.ucrop.callback.OverlayViewChangeListener
import com.yalantis.ucrop.util.DensityUtil
import com.yalantis.ucrop.util.RectUtils
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 * <p/>
 * This view is used for drawing the overlay on top of the image. It may have frame, crop guidelines and dimmed area.
 * This must have LAYER_TYPE_SOFTWARE to draw itself properly.
 */
class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    companion object {
        private const val SMOOTH_CENTER_DURATION = 1000L
        const val FREESTYLE_CROP_MODE_DISABLE = 0
        const val FREESTYLE_CROP_MODE_ENABLE = 1
        const val FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH = 2

        const val DEFAULT_SHOW_CROP_FRAME = true
        const val DEFAULT_SHOW_CROP_GRID = true
        const val DEFAULT_CIRCLE_DIMMED_LAYER = false
        const val DEFAULT_FREESTYLE_CROP_MODE = FREESTYLE_CROP_MODE_DISABLE
        const val DEFAULT_CROP_GRID_ROW_COUNT = 2
        const val DEFAULT_CROP_GRID_COLUMN_COUNT = 2
    }

    private val mCropViewRect = RectF()
    private val mTempRect = RectF()

    @JvmField
    protected var mThisWidth: Int = 0
    @JvmField
    protected var mThisHeight: Int = 0
    @JvmField
    protected var mCropGridCorners: FloatArray? = null
    @JvmField
    protected var mCropGridCenter: FloatArray? = null

    private var mCropGridRowCount: Int = 0
    private var mCropGridColumnCount: Int = 0
    private var mTargetAspectRatio: Float = 0f
    private var mGridPoints: FloatArray? = null
    private var mShowCropFrame: Boolean = false
    private var mShowCropGrid: Boolean = false
    private var mCircleDimmedLayer: Boolean = false
    private var mDimmedColor: Int = 0
    private val mCircularPath = Path()
    private val mDimmedStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mCropGridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mCropFramePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mCropFrameCornersPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    @FreestyleMode
    private var mFreestyleCropMode: Int = DEFAULT_FREESTYLE_CROP_MODE
    private var mPreviousTouchX: Float = -1f
    private var mPreviousTouchY: Float = -1f
    private var mCurrentTouchCornerIndex: Int = -1
    private var mTouchPointThreshold: Int = 0
    private var mCropRectMinSize: Int = 0
    private var mCropRectCornerTouchAreaLineLength: Int = 0
    private var isDragCenter: Boolean = false
    private var mCallback: OverlayViewChangeListener? = null
    private var smoothAnimator: ValueAnimator? = null
    private var mShouldSetupCropBounds: Boolean = false

    init {
        mTouchPointThreshold = resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_rect_corner_touch_threshold)
        mCropRectMinSize = resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_rect_min_size)
        mCropRectCornerTouchAreaLineLength = resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_rect_corner_touch_area_line_length)
        init()
    }

    fun getOverlayViewChangeListener(): OverlayViewChangeListener? {
        return mCallback
    }

    fun setOverlayViewChangeListener(callback: OverlayViewChangeListener?) {
        mCallback = callback
    }

    @NonNull
    fun getCropViewRect(): RectF {
        return mCropViewRect
    }

    @Deprecated("Please use the new method {@link #getFreestyleCropMode() getFreestyleCropMode} method as we have more than 1 freestyle crop mode.")
    fun isFreestyleCropEnabled(): Boolean {
        return mFreestyleCropMode == FREESTYLE_CROP_MODE_ENABLE
    }

    /**
     * Crop and drag automatically center
     *
     * @param isDragCenter
     */
    fun setDragSmoothToCenter(isDragCenter: Boolean) {
        this.isDragCenter = isDragCenter
    }

    @Deprecated("Please use the new method {@link #setFreestyleCropMode setFreestyleCropMode} method as we have more than 1 freestyle crop mode.")
    fun setFreestyleCropEnabled(freestyleCropEnabled: Boolean) {
        mFreestyleCropMode = if (freestyleCropEnabled) FREESTYLE_CROP_MODE_ENABLE else FREESTYLE_CROP_MODE_DISABLE
    }

    @FreestyleMode
    fun getFreestyleCropMode(): Int {
        return mFreestyleCropMode
    }

    fun setFreestyleCropMode(@FreestyleMode mFreestyleCropMode: Int) {
        this.mFreestyleCropMode = mFreestyleCropMode
        postInvalidate()
    }

    /**
     * Setter for {@link #mCircleDimmedLayer} variable.
     *
     * @param circleDimmedLayer - set it to true if you want dimmed layer to be an circle
     */
    fun setCircleDimmedLayer(circleDimmedLayer: Boolean) {
        mCircleDimmedLayer = circleDimmedLayer
    }

    /**
     * Setter for crop grid rows count.
     * Resets {@link #mGridPoints} variable because it is not valid anymore.
     */
    fun setCropGridRowCount(@IntRange(from = 0) cropGridRowCount: Int) {
        mCropGridRowCount = cropGridRowCount
        mGridPoints = null
    }

    /**
     * Setter for crop grid columns count.
     * Resets {@link #mGridPoints} variable because it is not valid anymore.
     */
    fun setCropGridColumnCount(@IntRange(from = 0) cropGridColumnCount: Int) {
        mCropGridColumnCount = cropGridColumnCount
        mGridPoints = null
    }

    /**
     * Setter for {@link #mShowCropFrame} variable.
     *
     * @param showCropFrame - set to true if you want to see a crop frame rectangle on top of an image
     */
    fun setShowCropFrame(showCropFrame: Boolean) {
        mShowCropFrame = showCropFrame
    }

    /**
     * Setter for {@link #mShowCropGrid} variable.
     *
     * @param showCropGrid - set to true if you want to see a crop grid on top of an image
     */
    fun setShowCropGrid(showCropGrid: Boolean) {
        mShowCropGrid = showCropGrid
    }

    /**
     * Setter for {@link #mDimmedColor} variable.
     *
     * @param dimmedColor - desired color of dimmed area around the crop bounds
     */
    fun setDimmedColor(@ColorInt dimmedColor: Int) {
        mDimmedColor = dimmedColor
    }

    /**
     * Setter for {@link #mDimmedStrokeColor} variable.
     *
     * @param circleStrokeColor - desired color of dimmed area around the crop bounds
     */
    fun setCircleStrokeColor(@ColorInt circleStrokeColor: Int) {
        mDimmedStrokePaint.color = circleStrokeColor
    }

    /**
     * Setter for crop frame stroke width
     */
    fun setCropFrameStrokeWidth(@IntRange(from = 0) width: Int) {
        mCropFramePaint.strokeWidth = width.toFloat()
    }

    /**
     * Setter for crop grid stroke width
     */
    fun setCropGridStrokeWidth(@IntRange(from = 0) width: Int) {
        mCropGridPaint.strokeWidth = width.toFloat()
    }

    /**
     * Setter for crop grid stroke width
     *
     * @param width
     */
    fun setDimmedStrokeWidth(@IntRange(from = 0) width: Int) {
        mDimmedStrokePaint.strokeWidth = width.toFloat()
    }

    /**
     * Setter for crop frame color
     */
    fun setCropFrameColor(@ColorInt color: Int) {
        mCropFramePaint.color = color
    }

    /**
     * Setter for crop grid color
     */
    fun setCropGridColor(@ColorInt color: Int) {
        mCropGridPaint.color = color
    }

    /**
     * This method sets aspect ratio for crop bounds.
     *
     * @param targetAspectRatio - aspect ratio for image crop (e.g. 1.77(7) for 16:9)
     */
    fun setTargetAspectRatio(targetAspectRatio: Float) {
        mTargetAspectRatio = targetAspectRatio
        if (mThisWidth > 0) {
            setupCropBounds()
            postInvalidate()
        } else {
            mShouldSetupCropBounds = true
        }
    }

    /**
     * This method setups crop bounds rectangles for given aspect ratio and view size.
     * {@link #mCropViewRect} is used to draw crop bounds - uses padding.
     */
    fun setupCropBounds() {
        var height = (mThisWidth / mTargetAspectRatio).toInt()
        if (height > mThisHeight) {
            val width = (mThisHeight * mTargetAspectRatio).toInt()
            val halfDiff = (mThisWidth - width) / 2
            mCropViewRect.set((paddingLeft + halfDiff).toFloat(), paddingTop.toFloat(),
                (paddingLeft + width + halfDiff).toFloat(), (paddingTop + mThisHeight).toFloat())
        } else {
            val halfDiff = (mThisHeight - height) / 2
            mCropViewRect.set(paddingLeft.toFloat(), (paddingTop + halfDiff).toFloat(),
                (paddingLeft + mThisWidth).toFloat(), (paddingTop + height + halfDiff).toFloat())
        }

        mCallback?.onCropRectUpdated(mCropViewRect)

        updateGridPoints()
    }

    private fun updateGridPoints() {
        mCropGridCorners = RectUtils.getCornersFromRect(mCropViewRect)
        mCropGridCenter = RectUtils.getCenterFromRect(mCropViewRect)

        mGridPoints = null
        mCircularPath.reset()
        mCircularPath.addCircle(mCropViewRect.centerX(), mCropViewRect.centerY(),
            min(mCropViewRect.width(), mCropViewRect.height()) / 2f, Path.Direction.CW)
    }

    protected fun init() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            val newLeft = paddingLeft
            val newTop = paddingTop
            val newRight = width - paddingRight
            val newBottom = height - paddingBottom
            mThisWidth = newRight - newLeft
            mThisHeight = newBottom - newTop

            if (mShouldSetupCropBounds) {
                mShouldSetupCropBounds = false
                setTargetAspectRatio(mTargetAspectRatio)
            }
        }
    }

    /**
     * Along with image there are dimmed layer, crop bounds and crop guidelines that must be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawDimmedLayer(canvas)
        drawCropGrid(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mCropViewRect.isEmpty || mFreestyleCropMode == FREESTYLE_CROP_MODE_DISABLE) {
            return false
        }

        var x = event.x
        var y = event.y

        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            mCurrentTouchCornerIndex = getCurrentTouchIndex(x, y)
            val shouldHandle = mCurrentTouchCornerIndex != -1
            if (!shouldHandle) {
                mPreviousTouchX = -1f
                mPreviousTouchY = -1f
            } else if (mPreviousTouchX < 0) {
                mPreviousTouchX = x
                mPreviousTouchY = y
            }
            return shouldHandle
        }

        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
            if (event.pointerCount == 1 && mCurrentTouchCornerIndex != -1) {

                x = min(max(x, paddingLeft.toFloat()), (width - paddingRight).toFloat())
                y = min(max(y, paddingTop.toFloat()), (height - paddingBottom).toFloat())

                updateCropViewRect(x, y)

                mPreviousTouchX = x
                mPreviousTouchY = y

                return true
            }
        }

        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            mPreviousTouchX = -1f
            mPreviousTouchY = -1f
            mCurrentTouchCornerIndex = -1

            mCallback?.onCropRectUpdated(mCropViewRect)

            if (isDragCenter) {
                smoothToCenter()
            }
        }

        return false
    }

    /**
     * * The order of the corners is:
     * 0------->1
     * ^        |
     * |   4    |
     * |        v
     * 3<-------2
     */
    private fun updateCropViewRect(touchX: Float, touchY: Float) {
        mTempRect.set(mCropViewRect)

        when (mCurrentTouchCornerIndex) {
            // resize rectangle
            0 -> {
                mTempRect.set(touchX, touchY, mCropViewRect.right, mCropViewRect.bottom)
            }
            1 -> {
                mTempRect.set(mCropViewRect.left, touchY, touchX, mCropViewRect.bottom)
            }
            2 -> {
                mTempRect.set(mCropViewRect.left, mCropViewRect.top, touchX, touchY)
            }
            3 -> {
                mTempRect.set(touchX, mCropViewRect.top, mCropViewRect.right, touchY)
            }
            // move rectangle
            4 -> {
                mTempRect.offset(touchX - mPreviousTouchX, touchY - mPreviousTouchY)
                if (mTempRect.left > getLeft() && mTempRect.top > getTop()
                    && mTempRect.right < getRight() && mTempRect.bottom < getBottom()) {
                    mCropViewRect.set(mTempRect)
                    updateGridPoints()
                    postInvalidate()
                }
                return
            }
        }

        val changeHeight = mTempRect.height() >= mCropRectMinSize
        val changeWidth = mTempRect.width() >= mCropRectMinSize
        mCropViewRect.set(
            if (changeWidth) mTempRect.left else mCropViewRect.left,
            if (changeHeight) mTempRect.top else mCropViewRect.top,
            if (changeWidth) mTempRect.right else mCropViewRect.right,
            if (changeHeight) mTempRect.bottom else mCropViewRect.bottom)

        if (changeHeight || changeWidth) {
            updateGridPoints()
            postInvalidate()
        }
    }

    /**
     * * The order of the corners in the float array is:
     * 0------->1
     * ^        |
     * |   4    |
     * |        v
     * 3<-------2
     *
     * @return - index of corner that is being dragged
     */
    private fun getCurrentTouchIndex(touchX: Float, touchY: Float): Int {
        var closestPointIndex = -1
        var closestPointDistance = mTouchPointThreshold.toDouble()
        mCropGridCorners?.let { corners ->
            for (i in 0 until 8 step 2) {
                val distanceToCorner = sqrt((touchX - corners[i]).toDouble().pow(2) + (touchY - corners[i + 1]).toDouble().pow(2))
                if (distanceToCorner < closestPointDistance) {
                    closestPointDistance = distanceToCorner
                    closestPointIndex = i / 2
                }
            }
        }

        if (mFreestyleCropMode == FREESTYLE_CROP_MODE_ENABLE && closestPointIndex < 0 && mCropViewRect.contains(touchX, touchY)) {
            return 4
        }

        return closestPointIndex
    }

    /**
     * This method draws dimmed area around the crop bounds.
     *
     * @param canvas - valid canvas object
     */
    protected fun drawDimmedLayer(@NonNull canvas: Canvas) {
        canvas.save()
        if (mCircleDimmedLayer) {
            canvas.clipPath(mCircularPath, Region.Op.DIFFERENCE)
        } else {
            canvas.clipRect(mCropViewRect, Region.Op.DIFFERENCE)
        }
        canvas.drawColor(mDimmedColor)
        canvas.restore()

        if (mCircleDimmedLayer) { // Draw 1px stroke to fix antialias
            canvas.drawCircle(mCropViewRect.centerX(), mCropViewRect.centerY(),
                min(mCropViewRect.width(), mCropViewRect.height()) / 2f, mDimmedStrokePaint)
        }
    }

    /**
     * This method draws crop bounds (empty rectangle)
     * and crop guidelines (vertical and horizontal lines inside the crop bounds) if needed.
     *
     * @param canvas - valid canvas object
     */
    protected fun drawCropGrid(@NonNull canvas: Canvas) {
        if (mShowCropGrid) {
            if (mGridPoints == null && !mCropViewRect.isEmpty) {

                mGridPoints = FloatArray((mCropGridRowCount) * 4 + (mCropGridColumnCount) * 4)

                var index = 0
                for (i in 0 until mCropGridRowCount) {
                    mGridPoints!![index++] = mCropViewRect.left
                    mGridPoints!![index++] = (mCropViewRect.height() * (((i + 1.0f) / (mCropGridRowCount + 1)))) + mCropViewRect.top
                    mGridPoints!![index++] = mCropViewRect.right
                    mGridPoints!![index++] = (mCropViewRect.height() * (((i + 1.0f) / (mCropGridRowCount + 1)))) + mCropViewRect.top
                }

                for (i in 0 until mCropGridColumnCount) {
                    mGridPoints!![index++] = (mCropViewRect.width() * (((i + 1.0f) / (mCropGridColumnCount + 1)))) + mCropViewRect.left
                    mGridPoints!![index++] = mCropViewRect.top
                    mGridPoints!![index++] = (mCropViewRect.width() * (((i + 1.0f) / (mCropGridColumnCount + 1)))) + mCropViewRect.left
                    mGridPoints!![index++] = mCropViewRect.bottom
                }
            }

            mGridPoints?.let {
                canvas.drawLines(it, mCropGridPaint)
            }
        }

        if (mShowCropFrame) {
            canvas.drawRect(mCropViewRect, mCropFramePaint)
        }

        if (mFreestyleCropMode != FREESTYLE_CROP_MODE_DISABLE) {
            canvas.save()

            mTempRect.set(mCropViewRect)
            mTempRect.inset(mCropRectCornerTouchAreaLineLength.toFloat(), -mCropRectCornerTouchAreaLineLength.toFloat())
            canvas.clipRect(mTempRect, Region.Op.DIFFERENCE)

            mTempRect.set(mCropViewRect)
            mTempRect.inset(-mCropRectCornerTouchAreaLineLength.toFloat(), mCropRectCornerTouchAreaLineLength.toFloat())
            canvas.clipRect(mTempRect, Region.Op.DIFFERENCE)

            canvas.drawRect(mCropViewRect, mCropFrameCornersPaint)

            canvas.restore()
        }
    }

    /**
     * This method extracts all needed values from the styled attributes.
     * Those are used to configure the view.
     */
    @Suppress("DEPRECATION")
    fun processStyledAttributes(@NonNull a: TypedArray) {
        mCircleDimmedLayer = a.getBoolean(R.styleable.ucrop_UCropView_ucrop_circle_dimmed_layer, DEFAULT_CIRCLE_DIMMED_LAYER)
        mDimmedColor = a.getColor(R.styleable.ucrop_UCropView_ucrop_dimmed_color,
            resources.getColor(R.color.ucrop_color_default_dimmed))
        mDimmedStrokePaint.color = mDimmedColor
        mDimmedStrokePaint.style = Paint.Style.STROKE
        mDimmedStrokePaint.strokeWidth = DensityUtil.dip2px(context, 1f).toFloat()

        initCropFrameStyle(a)
        mShowCropFrame = a.getBoolean(R.styleable.ucrop_UCropView_ucrop_show_frame, DEFAULT_SHOW_CROP_FRAME)

        initCropGridStyle(a)
        mShowCropGrid = a.getBoolean(R.styleable.ucrop_UCropView_ucrop_show_grid, DEFAULT_SHOW_CROP_GRID)
    }

    /**
     * This method setups Paint object for the crop bounds.
     */
    @Suppress("DEPRECATION")
    private fun initCropFrameStyle(@NonNull a: TypedArray) {
        val cropFrameStrokeSize = a.getDimensionPixelSize(R.styleable.ucrop_UCropView_ucrop_frame_stroke_size,
            resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_frame_stoke_width))
        val cropFrameColor = a.getColor(R.styleable.ucrop_UCropView_ucrop_frame_color,
            resources.getColor(R.color.ucrop_color_default_crop_frame))
        mCropFramePaint.strokeWidth = cropFrameStrokeSize.toFloat()
        mCropFramePaint.color = cropFrameColor
        mCropFramePaint.style = Paint.Style.STROKE

        mCropFrameCornersPaint.strokeWidth = (cropFrameStrokeSize * 3).toFloat()
        mCropFrameCornersPaint.color = cropFrameColor
        mCropFrameCornersPaint.style = Paint.Style.STROKE
    }

    /**
     * This method setups Paint object for the crop guidelines.
     */
    @Suppress("DEPRECATION")
    private fun initCropGridStyle(@NonNull a: TypedArray) {
        val cropGridStrokeSize = a.getDimensionPixelSize(R.styleable.ucrop_UCropView_ucrop_grid_stroke_size,
            resources.getDimensionPixelSize(R.dimen.ucrop_default_crop_grid_stoke_width))
        val cropGridColor = a.getColor(R.styleable.ucrop_UCropView_ucrop_grid_color,
            resources.getColor(R.color.ucrop_color_default_crop_grid))
        mCropGridPaint.strokeWidth = cropGridStrokeSize.toFloat()
        mCropGridPaint.color = cropGridColor

        mCropGridRowCount = a.getInt(R.styleable.ucrop_UCropView_ucrop_grid_row_count, DEFAULT_CROP_GRID_ROW_COUNT)
        mCropGridColumnCount = a.getInt(R.styleable.ucrop_UCropView_ucrop_grid_column_count, DEFAULT_CROP_GRID_COLUMN_COUNT)
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(FREESTYLE_CROP_MODE_DISABLE, FREESTYLE_CROP_MODE_ENABLE, FREESTYLE_CROP_MODE_ENABLE_WITH_PASS_THROUGH)
    annotation class FreestyleMode

    /**
     * 平滑移动至中心
     */
    private fun smoothToCenter() {
        val centerPoint = Point((right + left) / 2, (top + bottom) / 2)
        val offsetY = (centerPoint.y - mCropViewRect.centerY()).toInt()
        val offsetX = (centerPoint.x - mCropViewRect.centerX()).toInt()
        val before = RectF(mCropViewRect)
        val after = RectF(mCropViewRect)
        after.offset(offsetX.toFloat(), offsetY.toFloat())
        smoothAnimator?.cancel()
        smoothAnimator = ValueAnimator.ofFloat(0f, 1f)
        smoothAnimator!!.duration = SMOOTH_CENTER_DURATION
        smoothAnimator!!.interpolator = OvershootInterpolator(1.0f)
        smoothAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mCallback?.onCropRectUpdated(mCropViewRect)
            }
        })

        var lastAnimationValue = 0f
        smoothAnimator!!.addUpdateListener { animation ->
            val x = offsetX * animation.animatedValue as Float
            val y = offsetY * animation.animatedValue as Float
            mCropViewRect.set(RectF(
                before.left + x,
                before.top + y,
                before.right + x,
                before.bottom + y
            ))
            updateGridPoints()
            postInvalidate()
            mCallback?.postTranslate(
                offsetX * (animation.animatedValue as Float - lastAnimationValue),
                offsetY * (animation.animatedValue as Float - lastAnimationValue)
            )
            lastAnimationValue = animation.animatedValue as Float
        }
        smoothAnimator!!.start()
    }
}

