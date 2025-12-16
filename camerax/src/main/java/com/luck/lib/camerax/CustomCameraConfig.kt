package com.luck.lib.camerax

import com.luck.lib.camerax.listener.OnSimpleXPermissionDeniedListener
import com.luck.lib.camerax.listener.OnSimpleXPermissionDescriptionListener
import kotlin.jvm.JvmField

/**
 * @author：luck
 * @date：2021/11/29 7:14 下午
 * @describe：CustomCameraConfig
 */
object CustomCameraConfig {
    /**
     * 两者都可以
     */
    const val BUTTON_STATE_BOTH = 0

    /**
     * 只能拍照
     */
    const val BUTTON_STATE_ONLY_CAPTURE = 1

    /**
     * 只能录像
     */
    const val BUTTON_STATE_ONLY_RECORDER = 2

    /**
     * 默认最大录制时间
     */
    const val DEFAULT_MAX_RECORD_VIDEO = 60 * 1000 + 500

    /**
     * 默认最小录制时间
     */
    const val DEFAULT_MIN_RECORD_VIDEO = 1500

    const val SP_NAME = "PictureSpUtils"

    /**
     * 图片加载引擎
     */
    @JvmField
    var imageEngine: CameraImageEngine? = null

    /**
     * 自定义权限说明
     */
    @JvmField
    var explainListener: OnSimpleXPermissionDescriptionListener? = null

    /**
     * 权限拒绝回调
     */
    @JvmField
    var deniedListener: OnSimpleXPermissionDeniedListener? = null

    /**
     * 释放监听器
     */
    fun destroy() {
        imageEngine = null
        explainListener = null
        deniedListener = null
    }
}

