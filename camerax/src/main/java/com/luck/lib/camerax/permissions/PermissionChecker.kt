package com.luck.lib.camerax.permissions

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.luck.lib.camerax.PictureCameraActivity

/**
 * @author：luck
 * @date：2021/11/18 10:07 上午
 * @describe：PermissionChecker
 */
class PermissionChecker private constructor() {
    companion object {
        /**
         * 权限设置
         */
        const val PERMISSION_SETTING_CODE = 1102

        /**
         * 录音权限设置
         */
        const val PERMISSION_RECORD_AUDIO_SETTING_CODE = 1103

        private const val REQUEST_CODE = 10086

        @Volatile
        private var mInstance: PermissionChecker? = null

        @JvmStatic
        fun getInstance(): PermissionChecker {
            if (mInstance == null) {
                synchronized(PermissionChecker::class.java) {
                    if (mInstance == null) {
                        mInstance = PermissionChecker()
                    }
                }
            }
            return mInstance!!
        }

        /**
         * 检查是否有某个权限
         *
         * @param ctx
         * @param permissions
         * @return
         */
        @JvmStatic
        fun checkSelfPermission(ctx: Context, permissions: Array<String>): Boolean {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(ctx.applicationContext, permission)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
            return true
        }
    }

    fun requestPermissions(activity: Activity, permissionArray: Array<String>, callback: PermissionResultCallback) {
        val groupList = mutableListOf(permissionArray)
        requestPermissions(activity, groupList, REQUEST_CODE, callback)
    }

    fun requestPermissions(activity: Activity, permissionGroupList: List<Array<String>>, callback: PermissionResultCallback) {
        requestPermissions(activity, permissionGroupList, REQUEST_CODE, callback)
    }

    private fun requestPermissions(
        activity: Activity,
        permissionGroupList: List<Array<String>>,
        requestCode: Int,
        permissionResultCallback: PermissionResultCallback?
    ) {
        if (activity !is PictureCameraActivity) {
            return
        }
        if (Build.VERSION.SDK_INT < 23) {
            permissionResultCallback?.onGranted()
            return
        }
        val permissionList = mutableListOf<String>()
        for (permissionArray in permissionGroupList) {
            for (permission in permissionArray) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission)
                }
            }
        }
        if (permissionList.isNotEmpty()) {
            activity.setPermissionsResultAction(permissionResultCallback)
            ActivityCompat.requestPermissions(activity, permissionList.toTypedArray(), requestCode)
        } else {
            permissionResultCallback?.onGranted()
        }
    }

    fun onRequestPermissionsResult(grantResults: IntArray, action: PermissionResultCallback) {
        if (SimpleXPermissionUtil.isAllGranted(grantResults)) {
            action.onGranted()
        } else {
            action.onDenied()
        }
    }
}

