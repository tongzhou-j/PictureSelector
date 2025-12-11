package com.luck.picture.lib.config

/**
 * @author：luck
 * @date：2022/3/25 1:41 下午
 * @describe：PermissionEvent
 */
object PermissionEvent {
    val EVENT_SOURCE_DATA: Int = -1
    val EVENT_SYSTEM_SOURCE_DATA: Int = -2
    val EVENT_IMAGE_CAMERA: Int = SelectMimeType.ofImage()
    val EVENT_VIDEO_CAMERA: Int = SelectMimeType.ofVideo()
}
