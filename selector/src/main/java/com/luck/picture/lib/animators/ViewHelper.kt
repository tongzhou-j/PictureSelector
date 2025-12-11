package com.luck.picture.lib.animators

import android.view.View
import androidx.core.view.ViewCompat

/**
 * @author：luck
 * @date：2020-04-18 14:13
 * @describe：ViewHelper
 */
object ViewHelper {
    fun clear(v: View) {
        v.setAlpha(1f)
        v.setScaleY(1f)
        v.setScaleX(1f)
        v.setTranslationY(0f)
        v.setTranslationX(0f)
        v.setRotation(0f)
        v.setRotationY(0f)
        v.setRotationX(0f)
        v.setPivotY((v.getMeasuredHeight() / 2).toFloat())
        v.setPivotX((v.getMeasuredWidth() / 2).toFloat())
        ViewCompat.animate(v).setInterpolator(null).setStartDelay(0)
    }
}
