package com.luck.picture.lib.utils

import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.thread.PictureThreadUtils

/**
 * @author：luck
 * @date：2022/1/8 3:29 下午
 * @describe：ToastUtils
 */
object ToastUtils {
    /**
     * show toast content
     *
     * @param context
     * @param text
     */
    fun showToast(context: Context, text: String?) {
        if (isFastDoubleClick && TextUtils.equals(text, ToastUtils.mLastText)) {
            return
        }
        var appContext: Context? = PictureAppMaster.Companion.instance.appContext
        if (appContext == null) {
            appContext = context.applicationContext
        }
        if (PictureThreadUtils.isInUiThread) {
            Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show()
            ToastUtils.mLastText = text
        } else {
            PictureThreadUtils.runOnUiThread(object : Runnable {
                override fun run() {
                    var appContext: Context? =
                        PictureAppMaster.Companion.instance.appContext
                    if (appContext == null) {
                        appContext = context.applicationContext
                    }
                    Toast.makeText(appContext, text, Toast.LENGTH_SHORT).show()
                    ToastUtils.mLastText = text
                }
            })
        }
    }

    private const val TIME: Long = 1000
    private var lastClickTime: Long = 0
    private var mLastText: String? = null

    val isFastDoubleClick: Boolean
        get() {
            val time = System.currentTimeMillis()
            if (time - ToastUtils.lastClickTime < ToastUtils.TIME) {
                return true
            }
            ToastUtils.lastClickTime = time
            return false
        }
}
