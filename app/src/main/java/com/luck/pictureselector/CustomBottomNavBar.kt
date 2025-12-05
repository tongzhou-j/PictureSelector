package com.luck.pictureselector

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.luck.picture.lib.widget.BottomNavBar

/**
 * @author：luck
 * @date：2021/11/17 10:46 上午
 * @describe：CustomBottomNavBar
 */
class CustomBottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BottomNavBar(context, attrs, defStyleAttr), View.OnClickListener {

    override fun inflateLayout() {
        inflate(context, R.layout.ps_custom_bottom_nav_bar, this)
    }
}

