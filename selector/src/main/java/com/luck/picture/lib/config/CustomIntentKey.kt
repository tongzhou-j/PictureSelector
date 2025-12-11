package com.luck.picture.lib.config

/**
 * @author：luck
 * @date：2021/12/1 6:49 下午
 * @describe：CustomIntentKey
 */
object CustomIntentKey {
    /**
     * 自定义数据
     */
    const val EXTRA_CUSTOM_EXTRA_DATA: String = "customExtraData"

    /**
     * 输出的路径
     */
    const val EXTRA_OUT_PUT_PATH: String = "outPutPath"

    /**
     * 图片宽度
     */
    const val EXTRA_IMAGE_WIDTH: String = "imageWidth"

    /**
     * 图片高度
     */
    const val EXTRA_IMAGE_HEIGHT: String = "imageHeight"

    /**
     * 图片X轴偏移量
     */
    const val EXTRA_OFFSET_X: String = "offsetX"

    /**
     * 图片Y轴偏移量
     */
    const val EXTRA_OFFSET_Y: String = "offsetY"

    /**
     * 图片旋转比例
     */
    const val EXTRA_ASPECT_RATIO: String = "aspectRatio"

    /**
     * uCrop的裁剪输出路径Key
     */
    const val EXTRA_OUTPUT_URI: String = "com.yalantis.ucrop.OutputUri"
}
