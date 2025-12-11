package com.luck.picture.lib.config

/**
 * @author：luck
 * @date：2021/11/23 6:53 下午
 * @describe：SelectMimeType
 */
object SelectMimeType {
    /**
     * GET image or video only
     *
     *
     * excluding Audio
     *
     */
    fun ofAll(): Int {
        return TYPE_ALL
    }

    /**
     * GET image only
     */
    fun ofImage(): Int {
        return TYPE_IMAGE
    }

    /**
     * GET video only
     */
    fun ofVideo(): Int {
        return TYPE_VIDEO
    }

    /**
     * GET audio only
     *
     *
     * # No longer maintain audio related functions,
     * but can continue to use but there will be phone compatibility issues
     *
     *
     * 不再维护音频相关功能，但可以继续使用但会有机型兼容性问题
     */
    fun ofAudio(): Int {
        return TYPE_AUDIO
    }


    const val TYPE_ALL: Int = 0
    const val TYPE_IMAGE: Int = 1
    const val TYPE_VIDEO: Int = 2
    const val TYPE_AUDIO: Int = 3

    /**
     * System image album
     */
    const val SYSTEM_IMAGE: String = "image/*"

    /**
     * System video album
     */
    const val SYSTEM_VIDEO: String = "video/*"

    /**
     * System audio album
     */
    const val SYSTEM_AUDIO: String = "audio/*"

    /**
     * System all image or video album
     */
    val SYSTEM_ALL: String = SYSTEM_IMAGE + "," + SYSTEM_VIDEO
}
