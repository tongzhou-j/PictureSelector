package com.luck.picture.lib.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luck.picture.lib.basic.PictureCommonFragment
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.SpUtils

/**
 * @author：luck
 * @date：2021/11/18 10:07 上午
 * @describe：PermissionChecker
 */
class PermissionChecker private constructor() {
    fun requestPermissions(
        fragment: Fragment,
        permissionArray: Array<String?>?,
        callback: PermissionResultCallback?
    ) {
        val groupList: MutableList<Array<String?>?> = ArrayList()
        groupList.add(permissionArray)
        requestPermissions(fragment, groupList, PermissionChecker.Companion.REQUEST_CODE, callback)
    }

    fun requestPermissions(
        fragment: Fragment,
        permissionGroupList: MutableList<Array<String?>?>,
        callback: PermissionResultCallback?
    ) {
        requestPermissions(
            fragment,
            permissionGroupList,
            PermissionChecker.Companion.REQUEST_CODE,
            callback
        )
    }

    private fun requestPermissions(
        fragment: Fragment,
        permissionGroupList: MutableList<Array<String?>?>,
        requestCode: Int,
        permissionResultCallback: PermissionResultCallback?
    ) {
        if (ActivityCompatHelper.isDestroy(fragment.activity)) {
            return
        }
        if (fragment is PictureCommonFragment) {
            if (Build.VERSION.SDK_INT < 23) {
                if (permissionResultCallback != null) {
                    permissionResultCallback.onGranted()
                }
                return
            }
            val activity: Activity? = fragment.activity
            val permissionList: MutableList<String?> = ArrayList()
            for (permissionArray in permissionGroupList) {
                for (permission in permissionArray!!) {
                    if (ContextCompat.checkSelfPermission(
                            activity!!,
                            permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionList.add(permission)
                    }
                }
            }
            if (permissionList.size > 0) {
                fragment.setPermissionsResultAction(permissionResultCallback)
                val requestArray = permissionList.toTypedArray()
                fragment.requestPermissions(requestArray, requestCode)
                ActivityCompat.requestPermissions(activity!!, requestArray, requestCode)
            } else {
                if (permissionResultCallback != null) {
                    permissionResultCallback.onGranted()
                }
            }
        }
    }

    fun onRequestPermissionsResult(
        context: Context,
        permissions: Array<String>,
        grantResults: IntArray?,
        action: PermissionResultCallback
    ) {
        val activity = context as Activity
        for (permission in permissions) {
            val should = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            SpUtils.putBoolean(context, permission, should)
        }
        val permissionsNullable = permissions.map { it as String? }.toTypedArray()
        val grantResultsNonNull = grantResults ?: IntArray(0)
        if (PermissionUtil.isAllGranted(context, permissionsNullable, grantResultsNonNull)) {
            action.onGranted()
        } else {
            action.onDenied()
        }
    }

    companion object {
        private const val REQUEST_CODE = 10086

        private var mInstance: PermissionChecker? = null

        val instance: PermissionChecker
            get() {
                if (PermissionChecker.Companion.mInstance == null) {
                    synchronized(PermissionChecker::class.java) {
                        if (PermissionChecker.Companion.mInstance == null) {
                            PermissionChecker.Companion.mInstance =
                                PermissionChecker()
                        }
                    }
                }
                return PermissionChecker.Companion.mInstance!!
            }


        /**
         * 检查是否有某个权限
         *
         * @param ctx
         * @param permissions
         */
        fun checkSelfPermission(ctx: Context, permissions: Array<String>?): Boolean {
            var isAllGranted = true
            if (permissions != null) {
                for (permission in permissions) {
                    if (ContextCompat.checkSelfPermission(ctx.applicationContext, permission)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        isAllGranted = false
                        break
                    }
                }
            }
            return isAllGranted
        }

        /**
         * 检查读写权限是否存在
         */
        fun isCheckReadStorage(chooseMode: Int, context: Context): Boolean {
            if (SdkVersionUtils.isTIRAMISU) {
                if (chooseMode == SelectMimeType.ofImage()) {
                    return isCheckReadImages(context)
                } else if (chooseMode == SelectMimeType.ofVideo()) {
                    return isCheckReadVideo(context)
                } else if (chooseMode == SelectMimeType.ofAudio()) {
                    return isCheckReadAudio(context)
                } else {
                    return isCheckReadImages(context) && isCheckReadVideo(context)
                }
            } else {
                return isCheckReadExternalStorage(context)
            }
        }


        /**
         * 检查读取图片权限是否存在
         */
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        fun isCheckReadImages(context: Context): Boolean {
            return checkSelfPermission(
                context,
                arrayOf(PermissionConfig.READ_MEDIA_IMAGES)
            )
        }

        /**
         * 检查读取视频权限是否存在
         */
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        fun isCheckReadVideo(context: Context): Boolean {
            return checkSelfPermission(
                context,
                arrayOf(PermissionConfig.READ_MEDIA_VIDEO)
            )
        }

        /**
         * 检查读取音频权限是否存在
         */
        @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
        fun isCheckReadAudio(context: Context): Boolean {
            return checkSelfPermission(
                context,
                arrayOf(PermissionConfig.READ_MEDIA_AUDIO)
            )
        }

        /**
         * 检查写入权限是否存在
         */
        fun isCheckWriteExternalStorage(context: Context): Boolean {
            return checkSelfPermission(
                context,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
        }

        /**
         * 检查读取权限是否存在
         */
        fun isCheckReadExternalStorage(context: Context): Boolean {
            return checkSelfPermission(
                context,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
        }


        /**
         * 检查相机权限是否存在
         */
        fun isCheckCamera(context: Context): Boolean {
            return checkSelfPermission(context, arrayOf(Manifest.permission.CAMERA))
        }

        /**
         * 权限是否已申请
         */
        fun isCheckSelfPermission(context: Context, permissions: Array<String>?): Boolean {
            return checkSelfPermission(context, permissions)
        }
    }
}
