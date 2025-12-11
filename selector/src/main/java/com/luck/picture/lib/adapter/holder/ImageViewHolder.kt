package com.luck.picture.lib.adapter.holder

import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.luck.picture.lib.R
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.StyleUtils

/**
 * @author：luck
 * @date：2021/11/20 3:59 下午
 * @describe：ImageViewHolder
 */
class ImageViewHolder(itemView: View, config: SelectorConfig) :
    BaseRecyclerMediaHolder(itemView, config) {
    private val ivEditor: ImageView
    private val tvMediaTag: TextView

    init {
        tvMediaTag = itemView.findViewById<TextView>(R.id.tv_media_tag)
        ivEditor = itemView.findViewById<ImageView>(R.id.ivEditor)
        val adapterStyle = selectorConfig?.selectorStyle?.selectMainStyle
        val imageEditorRes = adapterStyle?.adapterImageEditorResources ?: 0
        if (StyleUtils.checkStyleValidity(imageEditorRes)) {
            ivEditor.setImageResource(imageEditorRes)
        }
        val editorGravity = adapterStyle?.adapterImageEditorGravity
        if (StyleUtils.checkArrayValidity(editorGravity)) {
            if (ivEditor.getLayoutParams() is RelativeLayout.LayoutParams) {
                (ivEditor.getLayoutParams() as RelativeLayout.LayoutParams).removeRule(
                    RelativeLayout.ALIGN_PARENT_BOTTOM
                )
                for (i in editorGravity!!) {
                    (ivEditor.getLayoutParams() as RelativeLayout.LayoutParams).addRule(i)
                }
            }
        }

        val tagGravity = adapterStyle?.adapterTagGravity
        if (StyleUtils.checkArrayValidity(tagGravity)) {
            if (tvMediaTag.getLayoutParams() is RelativeLayout.LayoutParams) {
                (tvMediaTag.getLayoutParams() as RelativeLayout.LayoutParams).removeRule(
                    RelativeLayout.ALIGN_PARENT_END
                )
                (tvMediaTag.getLayoutParams() as RelativeLayout.LayoutParams).removeRule(
                    RelativeLayout.ALIGN_PARENT_BOTTOM
                )
                for (i in tagGravity!!) {
                    (tvMediaTag.getLayoutParams() as RelativeLayout.LayoutParams).addRule(i)
                }
            }
        }
        val background = adapterStyle?.adapterTagBackgroundResources ?: 0
        if (StyleUtils.checkStyleValidity(background)) {
            tvMediaTag.setBackgroundResource(background)
        }

        val textSize = adapterStyle?.adapterTagTextSize ?: 0
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvMediaTag.setTextSize(textSize.toFloat())
        }

        val textColor = adapterStyle?.adapterTagTextColor ?: 0
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvMediaTag.setTextColor(textColor)
        }
    }


    override fun bindData(media: LocalMedia, position: Int) {
        super.bindData(media, position)
        if (media.isEditorImage() && media.isCut()) {
            ivEditor.setVisibility(View.VISIBLE)
        } else {
            ivEditor.setVisibility(View.GONE)
        }
        tvMediaTag.setVisibility(View.VISIBLE)
        val context = mContext
        if (context != null) {
            if (PictureMimeType.isHasGif(media.mimeType)) {
                tvMediaTag.setText(context.getString(R.string.ps_gif_tag))
            } else if (PictureMimeType.isHasWebp(media.mimeType)) {
                tvMediaTag.setText(context.getString(R.string.ps_webp_tag))
            } else if (MediaUtils.isLongImage(media.width, media.height)) {
                tvMediaTag.setText(context.getString(R.string.ps_long_chart))
            } else {
                tvMediaTag.setVisibility(View.GONE)
            }
        }
    }
}
