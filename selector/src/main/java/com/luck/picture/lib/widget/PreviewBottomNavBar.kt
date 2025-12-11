package com.luck.picture.lib.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.luck.picture.lib.R
import com.luck.picture.lib.utils.StyleUtils

/**
 * @author：luck
 * @date：2021/11/17 10:46 上午
 * @describe：PreviewBottomNavBar
 */
open class PreviewBottomNavBar : BottomNavBar {
    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun handleLayoutUI() {
        tvPreview?.visibility = GONE
        tvImageEditor?.setOnClickListener(this)
        tvImageEditor?.visibility = if (config?.onEditMediaEventListener != null) VISIBLE else GONE
    }

    fun isDisplayEditor(isHasVideo: Boolean) {
        tvImageEditor?.visibility = if (config?.onEditMediaEventListener != null && !isHasVideo) VISIBLE else GONE
    }

    val editor: TextView?
        get() = tvImageEditor

    override fun setBottomNavBarStyle() {
        super.setBottomNavBarStyle()
        val bottomBarStyle = config?.selectorStyle?.bottomBarStyle ?: return
        if (StyleUtils.checkStyleValidity(bottomBarStyle.bottomPreviewNarBarBackgroundColor)) {
            setBackgroundColor(bottomBarStyle.bottomPreviewNarBarBackgroundColor)
        } else if (StyleUtils.checkSizeValidity(bottomBarStyle.bottomNarBarBackgroundColor)) {
            setBackgroundColor(bottomBarStyle.bottomNarBarBackgroundColor)
        }
    }

    override fun onClick(view: View) {
        super.onClick(view)
        if (view.id == R.id.ps_tv_editor) {
            bottomNavBarListener?.onEditImage()
        }
    }
}
