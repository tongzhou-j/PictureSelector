package com.luck.lib.camerax.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.NonNull
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import kotlin.jvm.JvmStatic

/**
 * @author：luck
 * @date：2021/11/18 10:12 上午
 * @describe：SimpleXPermissionUtil
 */
object SimpleXPermissionUtil {

    @JvmStatic
    fun hasPermissions(@NonNull context: Context, @Size(min = 1) @NonNull vararg perms: String): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return true
        }
        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun isAllGranted(grantResults: IntArray): Boolean {
        if (grantResults.isEmpty()) {
            return false
        }
        for (grant in grantResults) {
            if (grant != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * 跳转到系统设置页面
     */
    @JvmStatic
    fun goIntentSetting(activity: Activity, requestCode: Int) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

