package com.luck.picture.lib.style

/**
 * @author：luck
 * @date：2021/11/15 4:15 下午
 * @describe：titleBarStyle
 */
class TitleBarStyle {
    /**
     * 是否隐藏标题栏
     */
    var isHideTitleBar: Boolean = false

    /**
     * 标题栏左边关闭样式
     */
    var titleLeftBackResource: Int = 0

    /**
     * 预览标题栏左边关闭样式
     */
    var previewTitleLeftBackResource: Int = 0

    /**
     * 标题栏默认文案
     */
    var titleDefaultText: String? = null

    /**
     * 标题栏默认文案
     */
    var titleDefaultTextResId: Int = 0
        private set

    /**
     * 标题栏字体大小
     */
    var titleTextSize: Int = 0

    /**
     * 标题栏字体色值
     */
    var titleTextColor: Int = 0

    /**
     * 标题栏背景
     */
    var titleBackgroundColor: Int = 0

    /**
     * 预览标题栏背景
     */
    var previewTitleBackgroundColor: Int = 0

    /**
     * 标题栏高度
     *
     *
     * use  unit dp
     *
     */
    var titleBarHeight: Int = 0

    /**
     * 标题栏专辑背景
     */
    var titleAlbumBackgroundResource: Int = 0

    /**
     * 标题栏位置居左
     */
    var isAlbumTitleRelativeLeft: Boolean = false


    /**
     * 标题栏右边向上图标
     */
    var titleDrawableRightResource: Int = 0

    /**
     * 标题栏右边取消按钮背景
     */
    var titleCancelBackgroundResource: Int = 0

    /**
     * 是否隐藏取消按钮
     */
    var isHideCancelButton: Boolean = false

    /**
     * 外部预览删除
     */
    var previewDeleteBackgroundResource: Int = 0

    /**
     * 标题栏右边默认文本
     */
    var titleCancelText: String? = null

    /**
     * 标题栏右边默认文本
     */
    var titleCancelTextResId: Int = 0
        private set

    /**
     * 标题栏右边文本字体大小
     */
    var titleCancelTextSize: Int = 0

    /**
     * 标题栏右边文本字体色值
     */
    var titleCancelTextColor: Int = 0

    /**
     * 标题栏底部线条色值
     */
    var titleBarLineColor: Int = 0

    /**
     * 是否显示标题栏底部线条
     */
    var isDisplayTitleBarLine: Boolean = false

    fun setTitleDefaultText(resId: Int) {
        this.titleDefaultTextResId = resId
    }

    fun setTitleCancelText(resId: Int) {
        this.titleCancelTextResId = resId
    }
}
