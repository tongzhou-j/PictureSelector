package com.luck.picture.lib.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.Size
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luck.picture.lib.utils.SpUtils

/**
 * @author：luck
 * @date：2021/11/18 10:12 上午
 * @describe：PermissionUtil
 */
object PermissionUtil {
    /**
     * 默认未请求授权状态
     */
    const val DEFAULT: Int = 0

    /**
     * 获取权限成功
     */
    const val SUCCESS: Int = 1

    /**
     * 申请权限拒绝, 但是下次申请权限还会弹窗
     */
    const val REFUSE: Int = 2

    /**
     * 申请权限拒绝，并且是永久，不会再弹窗
     */
    const val REFUSE_PERMANENT: Int = 3

    /**
     * Activity Action: Show screen for controlling which apps have access to manage external
     * storage.
     *
     *
     * In some cases, a matching Activity may not exist, so ensure you safeguard against this.
     *
     *
     * If you want to control a specific app's access to manage external storage, use
     * [.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION] instead.
     *
     *
     * Output: Nothing.
     *
     * @see .ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
     */
    const val ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION: String =
        "android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"


    fun hasPermissions(context: Context, @Size(min = 1) vararg perms: String): Boolean {
        if (Build.VERSION.SDK_INT < 23) {
            return true
        }
        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    perm
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun getPermissionStatus(activity: Activity, permission: String): Int {
        val flag = ActivityCompat.checkSelfPermission(activity, permission)
        val should = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        if (should) {
            return REFUSE
        }
        if (flag == PackageManager.PERMISSION_GRANTED) {
            return SUCCESS
        }
        if (!SpUtils.contains(activity, permission)) {
            return DEFAULT
        }
        return REFUSE_PERMANENT
    }

    fun isAllGranted(
        context: Context,
        permissions: Array<String?>,
        grantResults: IntArray
    ): Boolean {
        var isAllGranted = true
        var skipPermissionReject = false
        val targetSdkVersion = context.applicationInfo.targetSdkVersion
        if (targetSdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        PermissionConfig.READ_MEDIA_VISUAL_USER_SELECTED
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    skipPermissionReject = true
                }
            }
        }
        if (grantResults.size > 0) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (skipPermissionReject) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (permissions[i] == PermissionConfig.READ_MEDIA_IMAGES ||
                                permissions[i] == PermissionConfig.READ_MEDIA_VIDEO
                            ) {
                                break
                            }
                        }
                    }
                    isAllGranted = false
                    break
                }
            }
        } else {
            isAllGranted = false
        }
        return isAllGranted
    }


    /**
     * 跳转到系统设置页面
     */
    fun goIntentSetting(fragment: Fragment, requestCode: Int) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", fragment.activity!!.packageName, null)
            intent.data = uri
            fragment.startActivityForResult(intent, requestCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
