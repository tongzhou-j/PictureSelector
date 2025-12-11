package com.luck.picture.lib.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

/**
 * @author：luck
 * @date：2021/11/17 4:42 下午
 * @describe：ActivityCompatHelper
 */
object ActivityCompatHelper {
    private const val MIN_FRAGMENT_COUNT = 1

    fun isDestroy(activity: Activity?): Boolean {
        if (activity == null) {
            return true
        }
        return activity.isFinishing() || activity.isDestroyed()
    }


    /**
     * 验证Fragment是否已存在
     *
     * @param fragmentTag Fragment标签
     * @return
     */
    fun checkFragmentNonExits(activity: FragmentActivity?, fragmentTag: String?): Boolean {
        if (isDestroy(activity)) {
            return false
        }
        val fragment = activity!!.getSupportFragmentManager().findFragmentByTag(fragmentTag)
        return fragment == null
    }


    fun assertValidRequest(context: Context?): Boolean {
        if (context is Activity) {
            val activity = context
            return !isDestroy(activity)
        } else if (context is ContextWrapper) {
            val contextWrapper = context
            if (contextWrapper.getBaseContext() is Activity) {
                val activity = contextWrapper.getBaseContext() as Activity?
                return !isDestroy(activity)
            }
        }
        return true
    }

    /**
     * 验证当前是否是根Fragment
     *
     * @param activity
     * @return
     */
    fun checkRootFragment(activity: FragmentActivity?): Boolean {
        if (isDestroy(activity)) {
            return false
        }
        return activity!!.getSupportFragmentManager()
            .getBackStackEntryCount() == ActivityCompatHelper.MIN_FRAGMENT_COUNT
    }
}
