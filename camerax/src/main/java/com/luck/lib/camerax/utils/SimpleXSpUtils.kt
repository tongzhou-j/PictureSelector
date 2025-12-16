package com.luck.lib.camerax.utils

import android.content.Context
import android.content.SharedPreferences
import com.luck.lib.camerax.CustomCameraConfig
import kotlin.jvm.JvmStatic

/**
 * @author：luck
 * @date：2022/3/15 6:26 下午
 * @describe：SimpleXSpUtils
 */
object SimpleXSpUtils {
    @Volatile
    private var pictureSpUtils: SharedPreferences? = null

    private fun getSp(context: Context): SharedPreferences {
        if (pictureSpUtils == null) {
            synchronized(SimpleXSpUtils) {
                if (pictureSpUtils == null) {
                    pictureSpUtils = context.getSharedPreferences(CustomCameraConfig.SP_NAME, Context.MODE_PRIVATE)
                }
            }
        }
        return pictureSpUtils!!
    }

    @JvmStatic
    fun putBoolean(context: Context, key: String, value: Boolean) {
        getSp(context).edit().putBoolean(key, value).apply()
    }

    @JvmStatic
    fun getBoolean(context: Context, key: String, defValue: Boolean): Boolean {
        return getSp(context).getBoolean(key, defValue)
    }
}

