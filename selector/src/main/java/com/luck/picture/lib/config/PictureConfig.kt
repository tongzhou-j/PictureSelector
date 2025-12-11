package com.luck.picture.lib.config

import com.luck.picture.lib.BuildConfig

/**
 * @author：luck
 * @data：2017/5/24 1:00
 * @describe : constant
 */
object PictureConfig {
    const val SP_NAME: String = "PictureSpUtils"

    private val KEY = BuildConfig.LIBRARY_PACKAGE_NAME

    const val EXTRA_RESULT_SELECTION: String = "extra_result_media"

    val EXTRA_PICTURE_SELECTOR_CONFIG: String = PictureConfig.KEY + ".PictureSelectorConfig"

    const val CAMERA_FACING: String = "android.intent.extras.CAMERA_FACING"

    val EXTRA_ALL_FOLDER_SIZE: String = PictureConfig.KEY + ".all_folder_size"


    const val EXTRA_QUICK_CAPTURE: String = "android.intent.extra.quickCapture"

    val EXTRA_EXTERNAL_PREVIEW: String = PictureConfig.KEY + ".external_preview"

    val EXTRA_DISPLAY_CAMERA: String = PictureConfig.KEY + ".display_camera"

    val EXTRA_BOTTOM_PREVIEW: String = PictureConfig.KEY + ".bottom_preview"

    val EXTRA_CURRENT_ALBUM_NAME: String = PictureConfig.KEY + ".current_album_name"

    val EXTRA_CURRENT_PAGE: String = PictureConfig.KEY + ".current_page"

    val EXTRA_CURRENT_BUCKET_ID: String = PictureConfig.KEY + ".current_bucketId"

    val EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE: String =
        PictureConfig.KEY + ".external_preview_display_delete"

    val EXTRA_PREVIEW_CURRENT_POSITION: String = PictureConfig.KEY + ".current_preview_position"

    val EXTRA_PREVIEW_CURRENT_ALBUM_TOTAL: String = PictureConfig.KEY + ".current_album_total"

    val EXTRA_CURRENT_CHOOSE_MODE: String = PictureConfig.KEY + ".current_choose_mode"

    val EXTRA_MODE_TYPE_SOURCE: String = PictureConfig.KEY + ".mode_type_source"

    const val MAX_PAGE_SIZE: Int = 60

    const val MIN_PAGE_SIZE: Int = 10

    const val CAMERA_BEFORE: Int = 1


    const val DEFAULT_SPAN_COUNT: Int = 4

    const val REQUEST_CAMERA: Int = 909

    const val CHOOSE_REQUEST: Int = 188

    const val REQUEST_GO_SETTING: Int = 1102

    val ALL: Int = -1

    val UNSET: Int = -1

    const val MODE_TYPE_SYSTEM_SOURCE: Int = 1
    const val MODE_TYPE_EXTERNAL_PREVIEW_SOURCE: Int = 2
}
