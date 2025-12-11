package com.luck.picture.lib.widget

import android.content.Context
import android.util.AttributeSet
import com.luck.picture.lib.R
import com.luck.picture.lib.utils.StyleUtils

/**
 * @author：luck
 * @date：2021/11/19 4:38 下午
 * @describe：PreviewTitleBar
 */
open class PreviewTitleBar : TitleBar {
    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun setTitleBarStyle() {
        super.setTitleBarStyle()
        val titleBarStyle = config?.selectorStyle?.titleBarStyle ?: return
        if (StyleUtils.checkStyleValidity(titleBarStyle.previewTitleBackgroundColor)) {
            setBackgroundColor(titleBarStyle.previewTitleBackgroundColor)
        } else if (StyleUtils.checkSizeValidity(titleBarStyle.titleBackgroundColor)) {
            setBackgroundColor(titleBarStyle.titleBackgroundColor)
        }
        if (StyleUtils.checkStyleValidity(titleBarStyle.previewTitleLeftBackResource)) {
            ivLeftBack?.setImageResource(titleBarStyle.previewTitleLeftBackResource)
        }
        rlAlbumBg?.setOnClickListener(null)
        viewAlbumClickArea?.setOnClickListener(null)
        val layoutParams = rlAlbumBg?.layoutParams as? LayoutParams
        layoutParams?.removeRule(END_OF)
        layoutParams?.addRule(CENTER_HORIZONTAL)
        rlAlbumBg?.setBackgroundResource(R.drawable.ps_ic_trans_1px)
        tvCancel?.visibility = GONE
        ivArrow?.visibility = GONE
        viewAlbumClickArea?.visibility = GONE
    }
}
