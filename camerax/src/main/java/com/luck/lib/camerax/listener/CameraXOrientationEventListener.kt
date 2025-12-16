package com.luck.lib.camerax.listener

import android.content.Context
import android.view.OrientationEventListener
import android.view.Surface

/**
 * @author：luck
 * @date：2022/6/4 3:28 下午
 * @describe：CameraXOrientationEventListener
 */
class CameraXOrientationEventListener(
    context: Context,
    private var changedListener: OnOrientationChangedListener?
) : OrientationEventListener(context) {
    private var mRotation = Surface.ROTATION_0

    override fun onOrientationChanged(orientation: Int) {
        if (orientation == ORIENTATION_UNKNOWN) {
            return
        }
        val currentRotation = when {
            orientation > 80 && orientation < 100 -> Surface.ROTATION_270
            orientation > 170 && orientation < 190 -> Surface.ROTATION_180
            orientation > 260 && orientation < 280 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }
        if (mRotation != currentRotation) {
            mRotation = currentRotation
            changedListener?.onOrientationChanged(mRotation)
        }
    }

    interface OnOrientationChangedListener {
        fun onOrientationChanged(orientation: Int)
    }

    fun star() {
        enable()
    }

    fun stop() {
        disable()
    }
}

