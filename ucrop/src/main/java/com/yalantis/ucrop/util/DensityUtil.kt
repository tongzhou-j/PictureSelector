package com.yalantis.ucrop.util

import android.content.Context
import android.content.res.Resources
import kotlin.jvm.JvmStatic

/**
 * @author：luck
 * @date：2021/11/17 11:48 上午
 * @describe：DensityUtil
 */
object DensityUtil {
    /**
     * dp2px
     */
    @JvmStatic
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.applicationContext.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 获取状态栏高度
     */
    @JvmStatic
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
        val result = if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
        return result.takeIf { it != 0 } ?: dip2px(context, 26f)
    }
}

