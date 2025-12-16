package com.luck.lib.camerax.listener

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 * @author：luck
 * @date：2022/2/16 9:41 上午
 * @describe：CameraXPreviewViewTouchListener
 */
class CameraXPreviewViewTouchListener(context: Context) : View.OnTouchListener {

    private var mCustomTouchListener: CustomTouchListener? = null

    /**
     * 缩放监听
     */
    private val onScaleGestureListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val delta = detector.scaleFactor
            mCustomTouchListener?.zoom(delta)
            return true
        }
    }

    private val onGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            mCustomTouchListener?.click(e.x, e.y)
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            mCustomTouchListener?.doubleClick(e.x, e.y)
            return true
        }
    }

    private val mGestureDetector: GestureDetector = GestureDetector(context, onGestureListener)
    private val mScaleGestureDetector: ScaleGestureDetector = ScaleGestureDetector(context, onScaleGestureListener)

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        mScaleGestureDetector.onTouchEvent(event)
        if (!mScaleGestureDetector.isInProgress) {
            mGestureDetector.onTouchEvent(event)
        }
        return true
    }


    interface CustomTouchListener {
        /**
         * 放大
         */
        fun zoom(delta: Float)

        /**
         * 点击
         */
        fun click(x: Float, y: Float)

        /**
         * 双击
         */
        fun doubleClick(x: Float, y: Float)
    }

    fun setCustomTouchListener(customTouchListener: CustomTouchListener?) {
        mCustomTouchListener = customTouchListener
    }
}

