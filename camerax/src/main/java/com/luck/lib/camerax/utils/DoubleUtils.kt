package com.luck.lib.camerax.utils

import android.os.SystemClock

/**
 * @author：luck
 * @date：2019-01-04 13:41
 * @describe：DoubleUtils
 */
object DoubleUtils {
    /**
     * Prevent continuous click, jump two pages
     */
    private var lastClickTime: Long = 0
    private const val TIME: Long = 800

    fun isFastDoubleClick(): Boolean {
        val time = SystemClock.elapsedRealtime()
        if (time - lastClickTime < TIME) {
            return true
        }
        lastClickTime = time
        return false
    }
}

