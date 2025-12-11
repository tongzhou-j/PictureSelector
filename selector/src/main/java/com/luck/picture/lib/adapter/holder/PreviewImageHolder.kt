package com.luck.picture.lib.adapter.holder

import android.view.View
import android.view.View.OnLongClickListener
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.photoview.OnViewTapListener

/**
 * @author：luck
 * @date：2021/12/15 5:11 下午
 * @describe：PreviewImageHolder
 */
class PreviewImageHolder(itemView: View) : BasePreviewHolder(itemView) {
    override fun findViews(itemView: View?) {
    }

    override fun loadImage(media: LocalMedia?, maxWidth: Int, maxHeight: Int) {
        val imageEngine = selectorConfig.imageEngine
        if (imageEngine != null && media != null) {
            val availablePath = media.availablePath
            if (maxWidth == PictureConfig.UNSET && maxHeight == PictureConfig.UNSET) {
                imageEngine.loadImage(
                    itemView.context,
                    availablePath,
                    coverImageView
                )
            } else {
                imageEngine.loadImage(
                    itemView.context,
                    coverImageView,
                    availablePath,
                    maxWidth,
                    maxHeight
                )
            }
        }
    }

    override fun onClickBackPressed() {
        coverImageView.setOnViewTapListener(object : OnViewTapListener {
            override fun onViewTap(view: View?, x: Float, y: Float) {
                val listener = mPreviewEventListener
                listener?.onBackPressed()
            }
        })
    }

    override fun onLongPressDownload(media: LocalMedia?) {
        coverImageView.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                val listener = mPreviewEventListener
                listener?.onLongPressDownload(media)
                return false
            }
        })
    }
}
