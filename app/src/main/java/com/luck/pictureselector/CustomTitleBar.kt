package com.luck.pictureselector

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.luck.picture.lib.widget.TitleBar

/**
 * @author：luck
 * @date：2021/11/17 10:45 上午
 * @describe：CustomTitleBar
 */
class CustomTitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TitleBar(context, attrs, defStyleAttr), View.OnClickListener {

    override fun getTitleCancelView(): TextView {
        return tvCancel
    }

    override fun inflateLayout() {
        inflate(context, R.layout.ps_custom_title_bar, this)
    }

    override fun setTitleBarStyle() {
        super.setTitleBarStyle()
    }
}

