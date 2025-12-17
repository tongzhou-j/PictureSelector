package com.yalantis.ucrop.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.IntRange
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView
import com.yalantis.ucrop.UCropDevelopConfig
import com.yalantis.ucrop.UCropImageEngine
import com.yalantis.ucrop.callback.BitmapLoadCallback
import com.yalantis.ucrop.model.ExifInfo
import com.yalantis.ucrop.util.BitmapLoadUtils
import com.yalantis.ucrop.util.FastBitmapDrawable
import com.yalantis.ucrop.util.FileUtils
import com.yalantis.ucrop.util.RectUtils
import kotlin.jvm.JvmField
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 * <p/>
 * This class provides base logic to setup the image, transform it with matrix (move, scale, rotate),
 * and methods to get current matrix state.
 */
open class TransformImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    companion object {
        private const val TAG = "TransformImageView"
        private const val RECT_CORNER_POINTS_COORDS = 8
        private const val RECT_CENTER_POINT_COORDS = 2
        private const val MATRIX_VALUES_COUNT = 9
    }

    @JvmField
    protected val mCurrentImageCorners = FloatArray(RECT_CORNER_POINTS_COORDS)
    @JvmField
    protected val mCurrentImageCenter = FloatArray(RECT_CENTER_POINT_COORDS)

    private val mMatrixValues = FloatArray(MATRIX_VALUES_COUNT)

    @JvmField
    protected var mCurrentImageMatrix = Matrix()
    @JvmField
    protected var mThisWidth: Int = 0
    @JvmField
    protected var mThisHeight: Int = 0

    @JvmField
    protected var mTransformImageListener: TransformImageListener? = null

    @JvmField
    protected var mInitialImageCorners: FloatArray? = null
    @JvmField
    protected var mInitialImageCenter: FloatArray? = null

    @JvmField
    protected var mBitmapDecoded: Boolean = false
    @JvmField
    protected var mBitmapLaidOut: Boolean = false

    private var mMaxBitmapSize: Int = 0

    private var mImageInputPath: String? = null
    private var mImageOutputPath: String? = null
    private var mImageInputUri: Uri? = null
    private var mImageOutputUri: Uri? = null
    private var mExifInfo: ExifInfo? = null

    /**
     * Interface for rotation and scale change notifying.
     */
    interface TransformImageListener {
        fun onLoadComplete()
        fun onLoadFailure(@NonNull e: Exception)
        fun onRotate(currentAngle: Float)
        fun onScale(currentScale: Float)
    }

    init {
        init()
    }

    fun setTransformImageListener(transformImageListener: TransformImageListener?) {
        mTransformImageListener = transformImageListener
    }

    override fun setScaleType(scaleType: ScaleType) {
        if (scaleType == ScaleType.MATRIX) {
            super.setScaleType(scaleType)
        } else {
            Log.w(TAG, "Invalid ScaleType. Only ScaleType.MATRIX can be used")
        }
    }

    /**
     * Setter for {@link #mMaxBitmapSize} value.
     * Be sure to call it before {@link #setImageURI(Uri)} or other image setters.
     *
     * @param maxBitmapSize - max size for both width and height of bitmap that will be used in the view.
     */
    fun setMaxBitmapSize(maxBitmapSize: Int) {
        mMaxBitmapSize = maxBitmapSize
    }

    fun getMaxBitmapSize(): Int {
        if (mMaxBitmapSize <= 0) {
            mMaxBitmapSize = BitmapLoadUtils.calculateMaxBitmapSize(context)
        }
        return mMaxBitmapSize
    }

    override fun setImageBitmap(bitmap: Bitmap) {
        setImageDrawable(FastBitmapDrawable(bitmap))
    }

    fun getImageInputPath(): String? {
        return mImageInputPath
    }

    fun getImageOutputPath(): String? {
        return mImageOutputPath
    }

    fun getImageInputUri(): Uri? {
        return mImageInputUri
    }

    fun getImageOutputUri(): Uri? {
        return mImageOutputUri
    }

    fun getExifInfo(): ExifInfo? {
        return mExifInfo
    }

    /**
     * This method takes an Uri as a parameter, then calls method to decode it into Bitmap with specified size.
     *
     * @param imageUri - image Uri
     */
    fun setImageUri(@NonNull imageUri: Uri, @Nullable outputUri: Uri?, isUseCustomBitmap: Boolean) {
        if (UCropDevelopConfig.imageEngine != null && isUseCustomBitmap) {
            useCustomLoaderCrop(imageUri, outputUri)
        } else {
            useDefaultLoaderCrop(imageUri, outputUri)
        }
    }

    /**
     * use uCrop custom loader
     *
     * @param imageUri
     * @param outputUri
     */
    private fun useCustomLoaderCrop(@NonNull imageUri: Uri, @Nullable outputUri: Uri?) {
        val maxImageSize = BitmapLoadUtils.getMaxImageSize(context, imageUri)
        if (maxImageSize[0] > 0 && maxImageSize[1] > 0) {
            UCropDevelopConfig.imageEngine!!.loadImage(context, imageUri, maxImageSize[0], maxImageSize[1], object : UCropImageEngine.OnCallbackListener<Bitmap> {
                override fun onCall(data: Bitmap?) {
                    if (data == null) {
                        useDefaultLoaderCrop(imageUri, outputUri)
                    } else {
                        val copyBitmap = data.copy(data.config, true)
                        setBitmapLoadedResult(copyBitmap, ExifInfo(0, 0, 0), imageUri, outputUri)
                    }
                }
            })
        } else {
            useDefaultLoaderCrop(imageUri, outputUri)
        }
    }

    /**
     * use uCrop default loader
     *
     * @param imageUri
     * @param outputUri
     */
    private fun useDefaultLoaderCrop(@NonNull imageUri: Uri, @Nullable outputUri: Uri?) {
        val maxBitmapSize = getMaxBitmapSize()
        BitmapLoadUtils.decodeBitmapInBackground(context, imageUri, outputUri, maxBitmapSize, maxBitmapSize,
            object : BitmapLoadCallback {
                override fun onBitmapLoaded(@NonNull bitmap: Bitmap, @NonNull exifInfo: ExifInfo, @NonNull imageInputUri: Uri, @Nullable imageOutputUri: Uri?) {
                    setBitmapLoadedResult(bitmap, exifInfo, imageInputUri, imageOutputUri)
                }

                override fun onFailure(@NonNull bitmapWorkerException: Exception) {
                    Log.e(TAG, "onFailure: setImageUri", bitmapWorkerException)
                    mTransformImageListener?.onLoadFailure(bitmapWorkerException)
                }
            })
    }

    /**
     * bitmap loader complete
     *
     * @param bitmap
     * @param exifInfo
     * @param imageInputUri
     * @param imageOutputUri
     */
    fun setBitmapLoadedResult(@NonNull bitmap: Bitmap, @NonNull exifInfo: ExifInfo, @NonNull imageInputUri: Uri, @Nullable imageOutputUri: Uri?) {
        mImageInputUri = imageInputUri
        mImageOutputUri = imageOutputUri
        mImageInputPath = if (FileUtils.isContent(imageInputUri.toString())) {
            imageInputUri.toString()
        } else {
            imageInputUri.path
        }
        mImageOutputPath = imageOutputUri?.let {
            if (FileUtils.isContent(it.toString())) {
                it.toString()
            } else {
                it.path
            }
        }

        mExifInfo = exifInfo

        mBitmapDecoded = true
        setImageBitmap(bitmap)
    }

    /**
     * @return - current image scale value.
     * [1.0f - for original image, 2.0f - for 200% scaled image, etc.]
     */
    fun getCurrentScale(): Float {
        return getMatrixScale(mCurrentImageMatrix)
    }

    /**
     * This method calculates scale value for given Matrix object.
     */
    fun getMatrixScale(@NonNull matrix: Matrix): Float {
        return sqrt(getMatrixValue(matrix, Matrix.MSCALE_X).pow(2) + getMatrixValue(matrix, Matrix.MSKEW_Y).pow(2)).toFloat()
    }

    /**
     * @return - current image rotation angle.
     */
    fun getCurrentAngle(): Float {
        return getMatrixAngle(mCurrentImageMatrix)
    }

    /**
     * This method calculates rotation angle for given Matrix object.
     */
    fun getMatrixAngle(@NonNull matrix: Matrix): Float {
        return (-atan2(getMatrixValue(matrix, Matrix.MSKEW_X), getMatrixValue(matrix, Matrix.MSCALE_X)) * (180 / Math.PI)).toFloat()
    }

    override fun setImageMatrix(matrix: Matrix) {
        super.setImageMatrix(matrix)
        mCurrentImageMatrix.set(matrix)
        updateCurrentImagePoints()
    }

    @Nullable
    fun getViewBitmap(): Bitmap? {
        val drawable = drawable
        return if (drawable == null || drawable !is FastBitmapDrawable) {
            null
        } else {
            (drawable as FastBitmapDrawable).getBitmap()
        }
    }

    /**
     * This method translates current image.
     *
     * @param deltaX - horizontal shift
     * @param deltaY - vertical shift
     */
    fun postTranslate(deltaX: Float, deltaY: Float) {
        if (deltaX != 0f || deltaY != 0f) {
            mCurrentImageMatrix.postTranslate(deltaX, deltaY)
            setImageMatrix(mCurrentImageMatrix)
        }
    }

    /**
     * This method scales current image.
     *
     * @param deltaScale - scale value
     * @param px         - scale center X
     * @param py         - scale center Y
     */
    open fun postScale(deltaScale: Float, px: Float, py: Float) {
        if (deltaScale != 0f) {
            mCurrentImageMatrix.postScale(deltaScale, deltaScale, px, py)
            setImageMatrix(mCurrentImageMatrix)
            mTransformImageListener?.onScale(getMatrixScale(mCurrentImageMatrix))
        }
    }

    /**
     * This method rotates current image.
     *
     * @param deltaAngle - rotation angle
     * @param px         - rotation center X
     * @param py         - rotation center Y
     */
    fun postRotate(deltaAngle: Float, px: Float, py: Float) {
        if (deltaAngle != 0f) {
            mCurrentImageMatrix.postRotate(deltaAngle, px, py)
            setImageMatrix(mCurrentImageMatrix)
            mTransformImageListener?.onRotate(getMatrixAngle(mCurrentImageMatrix))
        }
    }

    protected open fun init() {
        setScaleType(ScaleType.MATRIX)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed || (mBitmapDecoded && !mBitmapLaidOut)) {
            val newLeft = paddingLeft
            val newTop = paddingTop
            val newRight = width - paddingRight
            val newBottom = height - paddingBottom
            mThisWidth = newRight - newLeft
            mThisHeight = newBottom - newTop

            onImageLaidOut()
        }
    }

    /**
     * When image is laid out {@link #mInitialImageCenter} and {@link #mInitialImageCenter}
     * must be set.
     */
    protected open fun onImageLaidOut() {
        val drawable = drawable ?: return

        val w = drawable.intrinsicWidth.toFloat()
        val h = drawable.intrinsicHeight.toFloat()

        Log.d(TAG, String.format("Image size: [%d:%d]", w.toInt(), h.toInt()))

        val initialImageRect = RectF(0f, 0f, w, h)
        mInitialImageCorners = RectUtils.getCornersFromRect(initialImageRect)
        mInitialImageCenter = RectUtils.getCenterFromRect(initialImageRect)

        mBitmapLaidOut = true

        mTransformImageListener?.onLoadComplete()
    }

    /**
     * This method returns Matrix value for given index.
     *
     * @param matrix     - valid Matrix object
     * @param valueIndex - index of needed value. See {@link Matrix#MSCALE_X} and others.
     * @return - matrix value for index
     */
    protected fun getMatrixValue(@NonNull matrix: Matrix, valueIndex: Int): Float {
        matrix.getValues(mMatrixValues)
        return mMatrixValues[valueIndex]
    }

    /**
     * This method logs given matrix X, Y, scale, and angle values.
     * Can be used for debug.
     */
    @Suppress("unused")
    protected fun printMatrix(@NonNull logPrefix: String, @NonNull matrix: Matrix) {
        val x = getMatrixValue(matrix, Matrix.MTRANS_X)
        val y = getMatrixValue(matrix, Matrix.MTRANS_Y)
        val rScale = getMatrixScale(matrix)
        val rAngle = getMatrixAngle(matrix)
        Log.d(TAG, "$logPrefix: matrix: { x: $x, y: $y, scale: $rScale, angle: $rAngle }")
    }

    /**
     * This method updates current image corners and center points that are stored in
     * {@link #mCurrentImageCorners} and {@link #mCurrentImageCenter} arrays.
     * Those are used for several calculations.
     */
    private fun updateCurrentImagePoints() {
        mInitialImageCorners?.let {
            mCurrentImageMatrix.mapPoints(mCurrentImageCorners, it)
        }
        mInitialImageCenter?.let {
            mCurrentImageMatrix.mapPoints(mCurrentImageCenter, it)
        }
    }
}

