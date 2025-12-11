package com.luck.picture.lib.style

/**
 * @author：luck
 * @date：2021/11/15 4:14 下午
 * @describe：SelectMainStyle
 */
class SelectMainStyle {
    /**
     * 状态栏背景色
     */
    var statusBarColor: Int = 0

    /**
     * 导航栏背景色
     */
    var navigationBarColor: Int = 0

    /**
     * 状态栏字体颜色，非黑即白
     */
    var isDarkStatusBarBlack: Boolean = false

    /**
     * 完成按钮从底部放在右上角
     */
    var isCompleteSelectRelativeTop: Boolean = false

    /**
     * 预览页选择按钮从顶部放在右下角
     */
    var isPreviewSelectRelativeBottom: Boolean = false

    /**
     * 预览页是否显示选择画廊
     */
    var isPreviewDisplaySelectGallery: Boolean = false

    /**
     * 预览页选择按钮MarginRight
     *
     *
     * unit dp
     *
     */
    var previewSelectMarginRight: Int = 0

    /**
     * 预览背景色
     */
    var previewBackgroundColor: Int = 0

    /**
     * 预览页选择按钮文本
     */
    var previewSelectText: String? = null

    /**
     * 预览页选择按钮文本
     */
    var previewSelectTextResId: Int = 0
        private set

    /**
     * 预览页选择按钮字体大小
     */
    var previewSelectTextSize: Int = 0

    /**
     * 预览页选择按钮字体颜色
     */
    var previewSelectTextColor: Int = 0


    /**
     * 勾选样式
     */
    var selectBackground: Int = 0

    /**
     * 预览样式勾选样式
     */
    var previewSelectBackground: Int = 0

    /**
     * 勾选样式是否使用数量类型
     */
    var isSelectNumberStyle: Boolean = false

    /**
     * 预览页勾选样式是否使用数量类型
     */
    var isPreviewSelectNumberStyle: Boolean = false

    /**
     * 列表背景色
     */
    var mainListBackgroundColor: Int = 0

    /**
     * 选择按钮默认文本
     */
    var selectNormalText: String? = null

    /**
     * 选择按钮默认文本
     */
    var selectNormalTextResId: Int = 0
        private set

    /**
     * 选择按钮默认文本字体大小
     */
    var selectNormalTextSize: Int = 0

    /**
     * 选择按钮默认文本字体色值
     */
    var selectNormalTextColor: Int = 0

    /**
     * 选择按钮默认背景
     */
    var selectNormalBackgroundResources: Int = 0

    /**
     * 选择按钮文本
     */
    var selectText: String? = null

    /**
     * 选择按钮文本
     */
    var selectTextResId: Int = 0
        private set

    /**
     * 选择按钮文本字体大小
     */
    var selectTextSize: Int = 0

    /**
     * 选择按钮文本字体色值
     */
    var selectTextColor: Int = 0

    /**
     * 选择按钮选中背景
     */
    var selectBackgroundResources: Int = 0

    /**
     * RecyclerView列表item间隙
     *
     *
     * use unit dp
     *
     */
    var adapterItemSpacingSize: Int = 0

    /**
     * 是否显示左右间距
     */
    var isAdapterItemIncludeEdge: Boolean = false

    /**
     * 勾选样式字体大小
     */
    var adapterSelectTextSize: Int = 0

    /**
     * 勾选按钮点击区域
     *
     *
     * use unit dp
     *
     */
    var adapterSelectClickArea: Int = 0

    /**
     * 勾选样式字体色值
     */
    var adapterSelectTextColor: Int = 0

    /**
     * 勾选样式位置
     * []
     */
    var adapterSelectStyleGravity: IntArray? = null

    /**
     * 资源类型标识
     */
    var adapterDurationDrawableLeft: Int = 0

    /**
     * 时长文字字体大小
     */
    var adapterDurationTextSize: Int = 0

    /**
     * 时长文字颜色
     */
    var adapterDurationTextColor: Int = 0

    /**
     * 时长文字位置
     * []
     */
    var adapterDurationGravity: IntArray? = null

    /**
     * 时长文字阴影背景
     */
    var adapterDurationBackgroundResources: Int = 0

    /**
     * 拍照按钮背景色
     */
    var adapterCameraBackgroundColor: Int = 0

    /**
     * 拍照按钮图标
     */
    var adapterCameraDrawableTop: Int = 0

    /**
     * 拍照按钮文本
     */
    var adapterCameraText: String? = null

    /**
     * 拍照按钮文本
     */
    var adapterCameraTextResId: Int = 0
        private set

    /**
     * 拍照按钮文本字体色值
     */
    var adapterCameraTextColor: Int = 0

    /**
     * 拍照按钮文本字体大小
     */
    var adapterCameraTextSize: Int = 0

    /**
     * 资源图标识的背景
     */
    var adapterTagBackgroundResources: Int = 0

    /**
     * 资源标识的字体大小
     */
    var adapterTagTextSize: Int = 0

    /**
     * 资源标识的字体色值
     */
    var adapterTagTextColor: Int = 0

    /**
     * 资源标识的位置
     * []
     */
    var adapterTagGravity: IntArray? = null

    /**
     * 图片被编辑标识
     */
    var adapterImageEditorResources: Int = 0

    /**
     * 图片被编辑标识位置
     * []
     */
    var adapterImageEditorGravity: IntArray? = null

    /**
     * 预览页画廊边框样式
     */
    var adapterPreviewGalleryFrameResource: Int = 0

    /**
     * 预览页画廊背景色
     */
    var adapterPreviewGalleryBackgroundResource: Int = 0

    /**
     * 预览页画廊item大小
     *
     *
     * use unit dp
     *
     */
    var adapterPreviewGalleryItemSize: Int = 0

    fun setPreviewSelectText(resId: Int) {
        this.previewSelectTextResId = resId
    }

    fun setSelectNormalText(resId: Int) {
        this.selectNormalTextResId = resId
    }

    fun setSelectText(resId: Int) {
        this.selectTextResId = resId
    }

    fun setAdapterCameraText(resId: Int) {
        this.adapterCameraTextResId = resId
    }
}
