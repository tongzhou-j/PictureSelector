package com.luck.pictureselector

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * @author：luck
 * @date：2021/7/14 3:15 PM
 * @describe：ImageLoaderUtils
 */
object ImageLoaderUtils {
    @JvmStatic
    fun assertValidRequest(context: Context): Boolean {
        return when (context) {
            is Activity -> !isDestroy(context)
            is ContextWrapper -> {
                val baseContext = context.baseContext
                if (baseContext is Activity) {
                    !isDestroy(baseContext)
                } else {
                    true
                }
            }
            else -> true
        }
    }

    private fun isDestroy(activity: Activity?): Boolean {
        if (activity == null) {
            return true
        }
        return activity.isFinishing || activity.isDestroyed
    }
}

