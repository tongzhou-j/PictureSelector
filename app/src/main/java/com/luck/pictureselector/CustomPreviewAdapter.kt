package com.luck.pictureselector

import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.luck.picture.lib.adapter.PicturePreviewAdapter
import com.luck.picture.lib.adapter.holder.BasePreviewHolder
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.photoview.OnViewTapListener
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.luck.picture.lib.utils.MediaUtils
import kotlin.math.max

/**
 * @author：luck
 * @date：2022/2/21 4:17 下午
 * @describe：CustomPreviewAdapter
 */
class CustomPreviewAdapter : PicturePreviewAdapter() {
    @NonNull
    override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): BasePreviewHolder {
        return if (viewType == BasePreviewHolder.ADAPTER_TYPE_IMAGE) {
            // 这里以重写自定义图片预览为例
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.ps_custom_preview_image, parent, false)
            CustomPreviewImageHolder(itemView)
        } else {
            super.onCreateViewHolder(parent, viewType)
        }
    }

    class CustomPreviewImageHolder(@NonNull itemView: View) : BasePreviewHolder(itemView) {
        private var subsamplingScaleImageView: SubsamplingScaleImageView? = null

        override fun findViews(itemView: View) {
            subsamplingScaleImageView = itemView.findViewById(R.id.big_preview_image)
        }

        override fun loadImage(media: LocalMedia, maxWidth: Int, maxHeight: Int) {
            if (!ActivityCompatHelper.assertValidRequest(itemView.context)) {
                return
            }
            Glide.with(itemView.context)
                .asBitmap()
                .load(media.availablePath)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        @NonNull resource: Bitmap,
                        @Nullable transition: Transition<in Bitmap>?
                    ) {
                        if (MediaUtils.isLongImage(resource.width, resource.height)) {
                            subsamplingScaleImageView?.visibility = View.VISIBLE
                            val scale = max(
                                screenWidth / resource.width.toFloat(),
                                screenHeight / resource.height.toFloat()
                            )
                            subsamplingScaleImageView?.setImage(
                                ImageSource.cachedBitmap(resource),
                                ImageViewState(scale, PointF(0f, 0f), 0)
                            )
                        } else {
                            subsamplingScaleImageView?.visibility = View.GONE
                            coverImageView.setImageBitmap(resource)
                        }
                    }

                    override fun onLoadFailed(@Nullable errorDrawable: Drawable?) {
                    }

                    override fun onLoadCleared(@Nullable placeholder: Drawable?) {
                    }
                })
        }

        override fun onClickBackPressed() {
            if (MediaUtils.isLongImage(media.width, media.height)) {
                subsamplingScaleImageView?.setOnClickListener { v ->
                    mPreviewEventListener?.onBackPressed()
                }
            } else {
                coverImageView.setOnViewTapListener(object : OnViewTapListener {
                    override fun onViewTap(view: View, x: Float, y: Float) {
                        mPreviewEventListener?.onBackPressed()
                    }
                })
            }
        }

        override fun onLongPressDownload(media: LocalMedia) {
            if (MediaUtils.isLongImage(media.width, media.height)) {
                subsamplingScaleImageView?.setOnLongClickListener { view ->
                    mPreviewEventListener?.onLongPressDownload(media)
                    false
                }
            } else {
                coverImageView.setOnLongClickListener { view ->
                    mPreviewEventListener?.onLongPressDownload(media)
                    false
                }
            }
        }
    }
}

