package com.luck.picture.lib.entity

/**
 * @author：luck
 * @date：2021/5/18 7:30 PM
 * @describe：MediaExtraInfo
 */
class MediaExtraInfo {
    /**
     * videoThumbnail
     */
    var videoThumbnail: String? = null

    /**
     * width
     */
    var width: Int = 0

    /**
     * height
     */
    var height: Int = 0

    /**
     * duration
     */
    var duration: Long = 0

    /**
     * orientation
     */
    var orientation: String? = null

    override fun toString(): String {
        return "MediaExtraInfo{" +
                "videoThumbnail='" + videoThumbnail + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", duration=" + duration +
                ", orientation='" + orientation + '\'' +
                '}'
    }
}
