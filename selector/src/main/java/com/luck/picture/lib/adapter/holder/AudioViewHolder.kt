package com.luck.picture.lib.adapter.holder

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.luck.picture.lib.R
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.StyleUtils

/**
 * @author：luck
 * @date：2021/11/20 3:59 下午
 * @describe：AudioViewHolder
 */
class AudioViewHolder(itemView: View, config: SelectorConfig) :
    BaseRecyclerMediaHolder(itemView, config) {
    private val tvDuration: TextView

    init {
        tvDuration = itemView.findViewById<TextView>(R.id.tv_duration)
        val adapterStyle = selectorConfig?.selectorStyle?.selectMainStyle
        val drawableLeft = adapterStyle?.adapterDurationDrawableLeft ?: 0
        if (StyleUtils.checkStyleValidity(drawableLeft)) {
            tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, 0, 0, 0)
        }
        val textSize = adapterStyle?.adapterDurationTextSize ?: 0
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvDuration.setTextSize(textSize.toFloat())
        }
        val textColor = adapterStyle?.adapterDurationTextColor ?: 0
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvDuration.setTextColor(textColor)
        }

        val shadowBackground = adapterStyle?.adapterDurationBackgroundResources ?: 0
        if (StyleUtils.checkStyleValidity(shadowBackground)) {
            tvDuration.setBackgroundResource(shadowBackground)
        }

        val durationGravity = adapterStyle?.adapterDurationGravity
        if (StyleUtils.checkArrayValidity(durationGravity)) {
            if (tvDuration.layoutParams is RelativeLayout.LayoutParams) {
                (tvDuration.layoutParams as RelativeLayout.LayoutParams).removeRule(
                    RelativeLayout.ALIGN_PARENT_BOTTOM
                )
                if (durationGravity != null) {
                    for (i in durationGravity) {
                        (tvDuration.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                    }
                }
            }
        }
    }

    override fun bindData(media: LocalMedia, position: Int) {
        super.bindData(media, position)
        tvDuration.setText(DateUtils.formatDurationTime(media.duration))
    }

    override fun loadCover(path: String?) {
        ivPicture?.setImageResource(R.drawable.ps_audio_placeholder)
    }
}
