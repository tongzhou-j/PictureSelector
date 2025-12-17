package com.yalantis.ucrop.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.yalantis.ucrop.util.RotationGestureDetector
import kotlin.math.pow

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
class GestureCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : CropImageView(context, attrs, defStyle) {

    companion object {
        private const val DOUBLE_TAP_ZOOM_DURATION = 200
    }

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mRotateDetector: RotationGestureDetector? = null
    private var mGestureDetector: GestureDetector? = null

    private var mMidPntX: Float = 0f
    private var mMidPntY: Float = 0f

    private var mIsRotateEnabled: Boolean = true
    private var mIsScaleEnabled: Boolean = true
    private var mIsGestureEnabled: Boolean = true
    private var mDoubleTapScaleSteps: Int = 5

    fun setScaleEnabled(scaleEnabled: Boolean) {
        mIsScaleEnabled = scaleEnabled
    }

    fun isScaleEnabled(): Boolean {
        return mIsScaleEnabled
    }

    fun setRotateEnabled(rotateEnabled: Boolean) {
        mIsRotateEnabled = rotateEnabled
    }

    fun isRotateEnabled(): Boolean {
        return mIsRotateEnabled
    }

    fun setGestureEnabled(gestureEnabled: Boolean) {
        mIsGestureEnabled = gestureEnabled
    }

    fun isGestureEnabled(): Boolean {
        return mIsGestureEnabled
    }

    fun setDoubleTapScaleSteps(doubleTapScaleSteps: Int) {
        mDoubleTapScaleSteps = doubleTapScaleSteps
    }

    fun getDoubleTapScaleSteps(): Int {
        return mDoubleTapScaleSteps
    }

    /**
     * If it's ACTION_DOWN event - user touches the screen and all current animation must be canceled.
     * If it's ACTION_UP event - user removed all fingers from the screen and current image position must be corrected.
     * If there are more than 2 fingers - update focal point coordinates.
     * Pass the event to the gesture detectors if those are enabled.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            cancelAllAnimations()
        }

        if (event.pointerCount > 1) {
            mMidPntX = (event.getX(0) + event.getX(1)) / 2
            mMidPntY = (event.getY(0) + event.getY(1)) / 2
        }

        if (mIsGestureEnabled) {
            mGestureDetector?.onTouchEvent(event)
        }

        if (mIsScaleEnabled) {
            mScaleDetector?.onTouchEvent(event)
        }

        if (mIsRotateEnabled) {
            mRotateDetector?.onTouchEvent(event)
        }

        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            setImageToWrapCropBounds()
        }
        return true
    }

    override fun init() {
        super.init()
        setupGestureListeners()
    }

    /**
     * This method calculates target scale value for double tap gesture.
     * User is able to zoom the image from min scale value
     * to the max scale value with {@link #mDoubleTapScaleSteps} double taps.
     */
    protected fun getDoubleTapTargetScale(): Float {
        return getCurrentScale() * (getMaxScale() / getMinScale()).pow(1.0f / mDoubleTapScaleSteps)
    }

    private fun setupGestureListeners() {
        mGestureDetector = GestureDetector(context, GestureListener(), null, true)
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        mRotateDetector = RotationGestureDetector(RotateListener())
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            postScale(detector.scaleFactor, mMidPntX, mMidPntY)
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            zoomImageToPosition(getDoubleTapTargetScale(), e.x, e.y, DOUBLE_TAP_ZOOM_DURATION.toLong())
            return super.onDoubleTap(e)
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            postTranslate(-distanceX, -distanceY)
            return true
        }
    }

    private inner class RotateListener : RotationGestureDetector.SimpleOnRotationGestureListener() {
        override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
            postRotate(rotationDetector.angle, mMidPntX, mMidPntY)
            return true
        }
    }
}

