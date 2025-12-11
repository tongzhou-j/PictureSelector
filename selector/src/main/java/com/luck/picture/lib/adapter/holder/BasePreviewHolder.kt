package com.luck.picture.lib.adapter.holder

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.R
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.photoview.PhotoView
import com.luck.picture.lib.utils.BitmapUtils
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.MediaUtils

/**
 * @author：luck
 * @date：2021/11/20 3:17 下午
 * @describe：BasePreviewHolder
 */
abstract class BasePreviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected val screenWidth: Int
    protected val screenHeight: Int
    protected val screenAppInHeight: Int
    protected var media: LocalMedia? = null
    protected val selectorConfig: SelectorConfig
    var coverImageView: PhotoView

    /**
     * findViews
     *
     * @param itemView
     */
    protected abstract fun findViews(itemView: View?)

    /**
     * load image cover
     *
     * @param media
     * @param maxWidth
     * @param maxHeight
     */
    protected abstract fun loadImage(media: LocalMedia?, maxWidth: Int, maxHeight: Int)

    /**
     * 点击返回事件
     */
    protected abstract fun onClickBackPressed()

    /**
     * 长按事件
     */
    protected abstract fun onLongPressDownload(media: LocalMedia?)

    /**
     * bind Data
     *
     * @param media
     * @param position
     */
    open fun bindData(media: LocalMedia, position: Int) {
        this.media = media
        val size = getRealSizeFromMedia(media)
        val maxImageSize = BitmapUtils.getMaxImageSize(size[0], size[1])
        loadImage(media, maxImageSize?.get(0) ?: 0, maxImageSize?.get(1) ?: 0)
        setScaleDisplaySize(media)
        setCoverScaleType(media)
        onClickBackPressed()
        onLongPressDownload(media)
    }

    protected fun getRealSizeFromMedia(media: LocalMedia): IntArray {
        if (media.isCut() && media.cropImageWidth > 0 && media.cropImageHeight > 0) {
            return intArrayOf(media.cropImageWidth, media.cropImageHeight)
        } else {
            return intArrayOf(media.width, media.height)
        }
    }

    protected fun setCoverScaleType(media: LocalMedia) {
        if (MediaUtils.isLongImage(media.width, media.height)) {
            coverImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            coverImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }

    protected open fun setScaleDisplaySize(media: LocalMedia) {
        if (!selectorConfig.isPreviewZoomEffect && screenWidth < screenHeight) {
            if (media.width > 0 && media.height > 0) {
                val layoutParams = coverImageView.getLayoutParams() as FrameLayout.LayoutParams
                layoutParams.width = screenWidth
                layoutParams.height = screenAppInHeight
                layoutParams.gravity = Gravity.CENTER
            }
        }
    }

    /**
     * onViewAttachedToWindow
     */
    open fun onViewAttachedToWindow() {
    }

    /**
     * onViewDetachedFromWindow
     */
    open fun onViewDetachedFromWindow() {
    }

    /**
     * resume and pause play
     */
    open fun resumePausePlay() {
    }

    open val isPlaying: Boolean
        /**
         * play ing
         */
        get() = false

    /**
     * release
     */
    open fun release() {
    }

    protected var mPreviewEventListener: OnPreviewEventListener? = null

    init {
        this.selectorConfig = SelectorProviders.Companion.instance?.selectorConfig ?: SelectorConfig()
        this.screenWidth = DensityUtil.getRealScreenWidth(itemView.context)
        this.screenHeight = DensityUtil.getScreenHeight(itemView.context)
        this.screenAppInHeight = DensityUtil.getRealScreenHeight(itemView.context)
        this.coverImageView = itemView.findViewById<PhotoView>(R.id.preview_image)
        findViews(itemView)
    }

    fun setOnPreviewEventListener(listener: OnPreviewEventListener?) {
        this.mPreviewEventListener = listener
    }

    interface OnPreviewEventListener {
        fun onBackPressed()

        fun onPreviewVideoTitle(videoName: String?)

        fun onLongPressDownload(media: LocalMedia?)
    }

    companion object {
        /**
         * 图片
         */
        const val ADAPTER_TYPE_IMAGE: Int = 1

        /**
         * 视频
         */
        const val ADAPTER_TYPE_VIDEO: Int = 2

        /**
         * 音频
         */
        const val ADAPTER_TYPE_AUDIO: Int = 3

        fun generate(parent: ViewGroup, viewType: Int, resource: Int): BasePreviewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(resource, parent, false)
            if (viewType == ADAPTER_TYPE_VIDEO) {
                return PreviewVideoHolder(itemView)
            } else if (viewType == ADAPTER_TYPE_AUDIO) {
                return PreviewAudioHolder(itemView)
            } else {
                return PreviewImageHolder(itemView)
            }
        }
    }
}
