package com.yalantis.ucrop.util

import android.view.MotionEvent
import androidx.annotation.NonNull
import kotlin.jvm.JvmStatic
import kotlin.math.atan2
import kotlin.math.PI

class RotationGestureDetector(private val listener: OnRotationGestureListener) {
    companion object {
        private const val INVALID_POINTER_INDEX = -1
    }

    private var fX = 0f
    private var fY = 0f
    private var sX = 0f
    private var sY = 0f

    private var mPointerIndex1 = INVALID_POINTER_INDEX
    private var mPointerIndex2 = INVALID_POINTER_INDEX
    private var mAngle = 0f
    private var mIsFirstTouch = false

    val angle: Float
        get() = mAngle

    fun onTouchEvent(@NonNull event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                sX = event.x
                sY = event.y
                mPointerIndex1 = event.findPointerIndex(event.getPointerId(0))
                mAngle = 0f
                mIsFirstTouch = true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                fX = event.x
                fY = event.y
                mPointerIndex2 = event.findPointerIndex(event.getPointerId(event.actionIndex))
                mAngle = 0f
                mIsFirstTouch = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (mPointerIndex1 != INVALID_POINTER_INDEX && mPointerIndex2 != INVALID_POINTER_INDEX && event.pointerCount > mPointerIndex2) {
                    val nfX: Float
                    val nfY: Float
                    val nsX: Float
                    val nsY: Float

                    nsX = event.getX(mPointerIndex1)
                    nsY = event.getY(mPointerIndex1)
                    nfX = event.getX(mPointerIndex2)
                    nfY = event.getY(mPointerIndex2)

                    if (mIsFirstTouch) {
                        mAngle = 0f
                        mIsFirstTouch = false
                    } else {
                        calculateAngleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY)
                    }

                    listener.onRotation(this)

                    fX = nfX
                    fY = nfY
                    sX = nsX
                    sY = nsY
                }
            }
            MotionEvent.ACTION_UP -> {
                mPointerIndex1 = INVALID_POINTER_INDEX
            }
            MotionEvent.ACTION_POINTER_UP -> {
                mPointerIndex2 = INVALID_POINTER_INDEX
            }
        }
        return true
    }

    private fun calculateAngleBetweenLines(
        fx1: Float, fy1: Float, fx2: Float, fy2: Float,
        sx1: Float, sy1: Float, sx2: Float, sy2: Float
    ): Float {
        return calculateAngleDelta(
            Math.toDegrees(atan2((fy1 - fy2).toDouble(), (fx1 - fx2).toDouble())).toFloat(),
            Math.toDegrees(atan2((sy1 - sy2).toDouble(), (sx1 - sx2).toDouble())).toFloat()
        )
    }

    private fun calculateAngleDelta(angleFrom: Float, angleTo: Float): Float {
        mAngle = angleTo % 360.0f - angleFrom % 360.0f

        when {
            mAngle < -180.0f -> mAngle += 360.0f
            mAngle > 180.0f -> mAngle -= 360.0f
        }

        return mAngle
    }

    open class SimpleOnRotationGestureListener : OnRotationGestureListener {
        override fun onRotation(rotationDetector: RotationGestureDetector): Boolean {
            return false
        }
    }

    interface OnRotationGestureListener {
        fun onRotation(rotationDetector: RotationGestureDetector): Boolean
    }
}

