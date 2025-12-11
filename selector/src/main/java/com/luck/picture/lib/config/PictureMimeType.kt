package com.luck.picture.lib.config

import android.text.TextUtils
import java.util.Locale


/**
 * @author：luck
 * @date：2017-5-24 17:02
 * @describe：PictureMimeType
 */
object PictureMimeType {
    /**
     * isGif
     *
     * @param mimeType
     * @return
     */
    fun isHasGif(mimeType: String?): Boolean {
        return mimeType != null && (mimeType == "image/gif" || mimeType == "image/GIF")
    }

    /**
     * isGif
     *
     * @param url
     * @return
     */
    fun isUrlHasGif(url: String): Boolean {
        return url.lowercase(Locale.getDefault()).endsWith(".gif")
    }

    /**
     * is has image
     *
     * @param url
     * @return
     */
    fun isUrlHasImage(url: String): Boolean {
        return url.lowercase(Locale.getDefault()).endsWith(".jpg")
                || url.lowercase(Locale.getDefault()).endsWith(".jpeg")
                || url.lowercase(Locale.getDefault()).endsWith(".png")
                || url.lowercase(Locale.getDefault()).endsWith(".heic")
    }

    /**
     * isWebp
     *
     * @param mimeType
     * @return
     */
    fun isHasWebp(mimeType: String?): Boolean {
        return mimeType != null && mimeType.equals("image/webp", ignoreCase = true)
    }

    /**
     * isWebp
     *
     * @param url
     * @return
     */
    fun isUrlHasWebp(url: String): Boolean {
        return url.lowercase(Locale.getDefault()).endsWith(".webp")
    }

    /**
     * isVideo
     *
     * @param mimeType
     * @return
     */
    fun isHasVideo(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_VIDEO)
    }

    /**
     * isVideo
     *
     * @param url
     * @return
     */
    fun isUrlHasVideo(url: String): Boolean {
        return url.lowercase(Locale.getDefault()).endsWith(".mp4")
    }

    /**
     * isAudio
     *
     * @param mimeType
     * @return
     */
    fun isHasAudio(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_AUDIO)
    }

    /**
     * isAudio
     *
     * @param url
     * @return
     */
    fun isUrlHasAudio(url: String): Boolean {
        return url.lowercase(Locale.getDefault())
            .endsWith(".amr") || url.lowercase(Locale.getDefault()).endsWith(".mp3")
    }

    /**
     * isImage
     *
     * @param mimeType
     * @return
     */
    fun isHasImage(mimeType: String?): Boolean {
        return mimeType != null && mimeType.startsWith(MIME_TYPE_PREFIX_IMAGE)
    }

    /**
     * isHasBmp
     *
     * @param mimeType
     * @return
     */
    fun isHasBmp(mimeType: String?): Boolean {
        if (TextUtils.isEmpty(mimeType)) {
            return false
        }
        return mimeType!!.startsWith(ofBMP())
                || mimeType.startsWith(ofXmsBMP())
                || mimeType.startsWith(ofWapBMP())
    }

    /**
     * isHasHeic
     *
     * @param mimeType
     * @return
     */
    fun isHasHeic(mimeType: String?): Boolean {
        if (TextUtils.isEmpty(mimeType)) {
            return false
        }
        return mimeType!!.startsWith(ofHeic())
    }

    /**
     * Determine if it is JPG.
     *
     * @param is image file mimeType
     */
    fun isJPEG(mimeType: String?): Boolean {
        if (TextUtils.isEmpty(mimeType)) {
            return false
        }
        return mimeType!!.startsWith(MIME_TYPE_JPEG) || mimeType.startsWith(PictureMimeType.MIME_TYPE_JPG)
    }

    /**
     * Determine if it is JPG.
     *
     * @param is image file mimeType
     */
    fun isJPG(mimeType: String?): Boolean {
        if (TextUtils.isEmpty(mimeType)) {
            return false
        }
        return mimeType!!.startsWith(PictureMimeType.MIME_TYPE_JPG)
    }


    /**
     * is Network image
     *
     * @param path
     * @return
     */
    fun isHasHttp(path: String?): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        return path!!.startsWith("http") || path.startsWith("https")
    }

    /**
     * Is it the same type
     *
     * @param oldMimeType 已选的资源类型
     * @param newMimeType 当次选中的资源类型
     * @return
     */
    fun isMimeTypeSame(oldMimeType: String?, newMimeType: String?): Boolean {
        if (TextUtils.isEmpty(oldMimeType)) {
            return true
        }
        return getMimeType(oldMimeType) == getMimeType(newMimeType)
    }

    /**
     * Picture or video
     *
     * @return
     */
    fun getMimeType(mimeType: String?): Int {
        if (TextUtils.isEmpty(mimeType)) {
            return SelectMimeType.TYPE_IMAGE
        }
        if (mimeType!!.startsWith(MIME_TYPE_PREFIX_VIDEO)) {
            return SelectMimeType.TYPE_VIDEO
        } else if (mimeType.startsWith(MIME_TYPE_PREFIX_AUDIO)) {
            return SelectMimeType.TYPE_AUDIO
        } else {
            return SelectMimeType.TYPE_IMAGE
        }
    }

    /**
     * Get source suffix
     *
     * @param mineType
     * @return
     */
    fun getLastSourceSuffix(mineType: String): String {
        try {
            return mineType.substring(mineType.lastIndexOf("/")).replace("/", ".")
        } catch (e: Exception) {
            e.printStackTrace()
            return JPG
        }
    }

    /**
     * Get url to file name
     *
     * @param path
     * @return
     */
    fun getUrlToFileName(path: String): String {
        var result = ""
        try {
            val lastIndexOf = path.lastIndexOf("/")
            if (lastIndexOf != -1) {
                result = path.substring(lastIndexOf + 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * is content://
     *
     * @param url
     * @return
     */
    fun isContent(url: String?): Boolean {
        if (TextUtils.isEmpty(url)) {
            return false
        }
        return url!!.startsWith("content://")
    }


    fun ofPNG(): String {
        return PictureMimeType.MIME_TYPE_PNG
    }

    fun ofJPEG(): String {
        return MIME_TYPE_JPEG
    }

    fun ofBMP(): String {
        return PictureMimeType.MIME_TYPE_BMP
    }


    fun ofXmsBMP(): String {
        return PictureMimeType.MIME_TYPE_XMS_BMP
    }

    fun ofWapBMP(): String {
        return PictureMimeType.MIME_TYPE_WAP_BMP
    }

    fun ofHeic(): String {
        return PictureMimeType.MIME_TYPE_HEIC
    }

    fun ofGIF(): String {
        return PictureMimeType.MIME_TYPE_GIF
    }

    fun ofWEBP(): String {
        return PictureMimeType.MIME_TYPE_WEBP
    }

    fun of3GP(): String {
        return PictureMimeType.MIME_TYPE_3GP
    }

    fun ofMP4(): String {
        return PictureMimeType.MIME_TYPE_MP4
    }

    fun ofMPEG(): String {
        return PictureMimeType.MIME_TYPE_MPEG
    }

    fun ofAVI(): String {
        return PictureMimeType.MIME_TYPE_AVI
    }


    const val MIME_TYPE_IMAGE: String = "image/jpeg"
    const val MIME_TYPE_VIDEO: String = "video/mp4"
    const val MIME_TYPE_AUDIO: String = "audio/mpeg"
    const val MIME_TYPE_AUDIO_AMR: String = "audio/amr"

    const val MIME_TYPE_PREFIX_IMAGE: String = "image"
    const val MIME_TYPE_PREFIX_VIDEO: String = "video"
    const val MIME_TYPE_PREFIX_AUDIO: String = "audio"

    private const val MIME_TYPE_PNG = "image/png"
    const val MIME_TYPE_JPEG: String = "image/jpeg"
    private const val MIME_TYPE_JPG = "image/jpg"
    private const val MIME_TYPE_BMP = "image/bmp"
    private const val MIME_TYPE_XMS_BMP = "image/x-ms-bmp"
    private const val MIME_TYPE_WAP_BMP = "image/vnd.wap.wbmp"
    private const val MIME_TYPE_GIF = "image/gif"
    private const val MIME_TYPE_WEBP = "image/webp"
    private const val MIME_TYPE_HEIC = "image/heic"

    private const val MIME_TYPE_3GP = "video/3gp"
    private const val MIME_TYPE_MP4 = "video/mp4"
    private const val MIME_TYPE_MPEG = "video/mpeg"
    private const val MIME_TYPE_AVI = "video/avi"


    const val JPEG: String = ".jpeg"

    const val JPG: String = ".jpg"

    const val PNG: String = ".png"

    const val WEBP: String = ".webp"

    const val GIF: String = ".gif"

    const val BMP: String = ".bmp"

    const val AMR: String = ".amr"

    const val WAV: String = ".wav"

    const val MP3: String = ".mp3"

    const val MP4: String = ".mp4"

    const val AVI: String = ".avi"

    const val JPEG_Q: String = "image/jpeg"

    const val PNG_Q: String = "image/png"

    const val MP4_Q: String = "video/mp4"

    const val AVI_Q: String = "video/avi"

    const val AMR_Q: String = "audio/amr"

    const val WAV_Q: String = "audio/x-wav"

    const val MP3_Q: String = "audio/mpeg"

    const val DCIM: String = "DCIM/Camera"

    const val CAMERA: String = "Camera"
}
