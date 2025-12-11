package com.luck.picture.lib.permissions

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.utils.SdkVersionUtils

/**
 * @author：luck
 * @date：2021/12/11 8:24 下午
 * @describe：PermissionConfig
 */
object PermissionConfig {
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    val READ_MEDIA_AUDIO: String = Manifest.permission.READ_MEDIA_AUDIO

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    val READ_MEDIA_IMAGES: String = Manifest.permission.READ_MEDIA_IMAGES

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    val READ_MEDIA_VIDEO: String = Manifest.permission.READ_MEDIA_VIDEO

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    val READ_MEDIA_VISUAL_USER_SELECTED: String =
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    val READ_EXTERNAL_STORAGE: String = Manifest.permission.READ_EXTERNAL_STORAGE
    val WRITE_EXTERNAL_STORAGE: String = Manifest.permission.WRITE_EXTERNAL_STORAGE

    /**
     * 当前申请权限
     */
    var CURRENT_REQUEST_PERMISSION: Array<String?> = arrayOfNulls(0)

    /**
     * 相机权限
     */
    val CAMERA: Array<String?> = arrayOf(Manifest.permission.CAMERA)

    /**
     * 获取外部读取权限
     */
    fun getReadPermissionArray(context: Context, chooseMode: Int): Array<String?> {
        if (SdkVersionUtils.isUPSIDE_DOWN_CAKE) {
            val targetSdkVersion = context.applicationInfo.targetSdkVersion
            if (chooseMode == SelectMimeType.ofImage()) {
                if (targetSdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    return arrayOf(READ_MEDIA_VISUAL_USER_SELECTED, READ_MEDIA_IMAGES)
                } else if (targetSdkVersion == Build.VERSION_CODES.TIRAMISU) {
                    return arrayOf(READ_MEDIA_IMAGES)
                } else {
                    return arrayOf(READ_EXTERNAL_STORAGE)
                }
            } else if (chooseMode == SelectMimeType.ofVideo()) {
                if (targetSdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    return arrayOf(READ_MEDIA_VISUAL_USER_SELECTED, READ_MEDIA_VIDEO)
                } else if (targetSdkVersion == Build.VERSION_CODES.TIRAMISU) {
                    return arrayOf(READ_MEDIA_VIDEO)
                } else {
                    return arrayOf(READ_EXTERNAL_STORAGE)
                }
            } else if (chooseMode == SelectMimeType.ofAudio()) {
                return if (targetSdkVersion >= Build.VERSION_CODES.TIRAMISU)
                    arrayOf(READ_MEDIA_AUDIO)
                else
                    arrayOf(READ_EXTERNAL_STORAGE)
            } else {
                if (targetSdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    return arrayOf(
                        READ_MEDIA_VISUAL_USER_SELECTED,
                        READ_MEDIA_IMAGES,
                        READ_MEDIA_VIDEO
                    )
                } else if (targetSdkVersion == Build.VERSION_CODES.TIRAMISU) {
                    return arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
                } else {
                    return arrayOf(READ_EXTERNAL_STORAGE)
                }
            }
        } else if (SdkVersionUtils.isTIRAMISU) {
            val targetSdkVersion = context.applicationInfo.targetSdkVersion
            if (chooseMode == SelectMimeType.ofImage()) {
                return if (targetSdkVersion >= Build.VERSION_CODES.TIRAMISU)
                    arrayOf(READ_MEDIA_IMAGES)
                else
                    arrayOf(READ_EXTERNAL_STORAGE)
            } else if (chooseMode == SelectMimeType.ofVideo()) {
                return if (targetSdkVersion >= Build.VERSION_CODES.TIRAMISU)
                    arrayOf(READ_MEDIA_VIDEO)
                else
                    arrayOf(READ_EXTERNAL_STORAGE)
            } else if (chooseMode == SelectMimeType.ofAudio()) {
                return if (targetSdkVersion >= Build.VERSION_CODES.TIRAMISU)
                    arrayOf(READ_MEDIA_AUDIO)
                else
                    arrayOf(READ_EXTERNAL_STORAGE)
            } else {
                return if (targetSdkVersion >= Build.VERSION_CODES.TIRAMISU)
                    arrayOf(READ_MEDIA_IMAGES, READ_MEDIA_VIDEO)
                else
                    arrayOf(READ_EXTERNAL_STORAGE)
            }
        }
        return arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
    }
}
