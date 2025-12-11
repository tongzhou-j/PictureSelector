package com.luck.picture.lib.utils

import android.content.Context
import android.content.SharedPreferences
import com.luck.picture.lib.config.PictureConfig

/**
 * @author：luck
 * @date：2022/3/15 6:26 下午
 * @describe：SpUtils
 */
object SpUtils {
    private var pictureSpUtils: SharedPreferences? = null

    private fun getSp(context: Context): SharedPreferences? {
        if (SpUtils.pictureSpUtils == null) {
            SpUtils.pictureSpUtils =
                context.getSharedPreferences(PictureConfig.SP_NAME, Context.MODE_PRIVATE)
        }
        return SpUtils.pictureSpUtils
    }

    fun putString(context: Context, key: String?, value: String?) {
        SpUtils.getSp(context!!)!!.edit().putString(key, value).apply()
    }

    fun putBoolean(context: Context, key: String?, value: Boolean) {
        SpUtils.getSp(context!!)!!.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String?, defValue: Boolean): Boolean {
        return SpUtils.getSp(context!!)!!.getBoolean(key, defValue)
    }

    fun contains(context: Context, key: String?): Boolean {
        return SpUtils.getSp(context!!)!!.contains(key)
    }
}
