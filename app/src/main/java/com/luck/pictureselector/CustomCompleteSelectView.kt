package com.luck.pictureselector

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Nullable
import com.luck.picture.lib.widget.CompleteSelectView

/**
 * @author：luck
 * @date：2021/12/23 11:54 上午
 * @describe：CustomCompleteSelectView
 */
class CustomCompleteSelectView @JvmOverloads constructor(
    context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CompleteSelectView(context, attrs, defStyleAttr) {

    override fun inflateLayout() {
        LayoutInflater.from(context).inflate(R.layout.ps_custom_complete_selected_layout, this)
    }

    override fun setCompleteSelectViewStyle() {
        super.setCompleteSelectViewStyle()
    }
}

