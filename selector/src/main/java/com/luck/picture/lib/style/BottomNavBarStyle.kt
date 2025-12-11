package com.luck.picture.lib.style

/**
 * @author：luck
 * @date：2021/11/15 4:15 下午
 * @describe：NavBarbottomStyle
 */
class BottomNavBarStyle {
    /**
     * 底部导航栏背景色
     */
    var bottomNarBarBackgroundColor: Int = 0

    /**
     * 底部预览页NarBar背景色
     */
    var bottomPreviewNarBarBackgroundColor: Int = 0

    /**
     * 底部导航栏高度
     *
     *
     * use unit dp
     *
     */
    var bottomNarBarHeight: Int = 0

    /**
     * 底部预览文本
     */
    var bottomPreviewNormalText: String? = null

    /**
     * 底部预览文本
     */
    var bottomPreviewNormalTextResId: Int = 0
        private set

    /**
     * 底部预览文本字体大小
     */
    var bottomPreviewNormalTextSize: Int = 0

    /**
     * 底部预览文本正常字体色值
     */
    var bottomPreviewNormalTextColor: Int = 0

    /**
     * 底部选中预览文本
     */
    var bottomPreviewSelectText: String? = null

    /**
     * 底部选中预览文本
     */
    var bottomPreviewSelectTextResId: Int = 0
        private set

    /**
     * 底部预览文本选中字体色值
     */
    var bottomPreviewSelectTextColor: Int = 0

    /**
     * 底部编辑文字
     */
    var bottomEditorText: String? = null

    /**
     * 底部编辑文字
     */
    var bottomEditorTextResId: Int = 0
        private set

    /**
     * 底部编辑文字大小
     */
    var bottomEditorTextSize: Int = 0

    /**
     * 底部编辑文字色值
     */
    var bottomEditorTextColor: Int = 0

    /**
     * 底部原图文字DrawableLeft
     */
    var bottomOriginalDrawableLeft: Int = 0

    /**
     * 底部原图文字
     */
    var bottomOriginalText: String? = null

    /**
     * 底部原图文字
     */
    var bottomOriginalTextResId: Int = 0
        private set

    /**
     * 底部原图文字大小
     */
    var bottomOriginalTextSize: Int = 0

    /**
     * 底部原图文字色值
     */
    var bottomOriginalTextColor: Int = 0

    /**
     * 已选数量背景样式
     */
    var bottomSelectNumResources: Int = 0

    /**
     * 已选数量文字大小
     */
    var bottomSelectNumTextSize: Int = 0

    /**
     * 已选数量文字颜色
     */
    var bottomSelectNumTextColor: Int = 0

    /**
     * 是否显示已选数量圆点提醒
     */
    var isCompleteCountTips: Boolean = true


    fun setBottomPreviewNormalText(resId: Int) {
        this.bottomPreviewNormalTextResId = resId
    }

    fun setBottomPreviewSelectText(resId: Int) {
        this.bottomPreviewSelectTextResId = resId
    }

    fun setBottomEditorText(resId: Int) {
        this.bottomEditorTextResId = resId
    }

    fun setBottomOriginalText(resId: Int) {
        this.bottomOriginalTextResId = resId
    }
}
