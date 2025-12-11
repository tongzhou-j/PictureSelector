package com.luck.picture.lib.adapter.holder

import android.view.Gravity
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.luck.picture.lib.R
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.engine.MediaPlayerEngine
import com.luck.picture.lib.engine.VideoPlayerEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnPlayerListener
import com.luck.picture.lib.photoview.OnViewTapListener
import com.luck.picture.lib.utils.IntentUtils

/**
 * @author：luck
 * @date：2021/12/15 5:12 下午
 * @describe：PreviewVideoHolder
 */
class PreviewVideoHolder(itemView: View) : BasePreviewHolder(itemView) {
    lateinit var ivPlayButton: ImageView
    lateinit var progress: ProgressBar
    lateinit var videoPlayer: View
    private var isPlayed = false

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

    override fun bindData(media: LocalMedia, position: Int) {
        super.bindData(media, position)
        setScaleDisplaySize(media)
        ivPlayButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (selectorConfig.isPauseResumePlay) {
                    dispatchPlay()
                } else {
                    startPlay()
                }
            }
        })
        itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (selectorConfig.isPauseResumePlay) {
                    dispatchPlay()
                } else {
                    val listener = mPreviewEventListener
                    listener?.onBackPressed()
                }
            }
        })
    }

    /**
     * 视频播放状态分发
     */
    private fun dispatchPlay() {
        if (isPlayed) {
            if (isPlaying) {
                onPause()
            } else {
                onResume()
            }
        } else {
            startPlay()
        }
    }

    /**
     * 恢复播放
     */
    private fun onResume() {
        ivPlayButton.visibility = View.GONE
        val engine = selectorConfig.videoPlayerEngine
        @Suppress("UNCHECKED_CAST")
        (engine as? VideoPlayerEngine<View>)?.onResume(videoPlayer)
    }

    /**
     * 暂停播放
     */
    fun onPause() {
        ivPlayButton.visibility = View.VISIBLE
        val engine = selectorConfig.videoPlayerEngine
        @Suppress("UNCHECKED_CAST")
        (engine as? VideoPlayerEngine<View>)?.onPause(videoPlayer)
    }

    /**
     * 是否正在播放中
     */
    override val isPlaying: Boolean
        get() {
            val engine = selectorConfig.videoPlayerEngine
            @Suppress("UNCHECKED_CAST")
            return (engine as? VideoPlayerEngine<View>)?.isPlaying(videoPlayer) ?: false
        }

    /**
     * 外部播放状态监听回调
     */
    private val mPlayerListener: OnPlayerListener = object : OnPlayerListener {
        override fun onPlayerError() {
            playerDefaultUI()
        }

        override fun onPlayerReady() {
            playerIngUI()
        }

        override fun onPlayerLoading() {
            progress.visibility = View.VISIBLE
        }

        override fun onPlayerEnd() {
            playerDefaultUI()
        }
    }

    init {
        ivPlayButton = itemView.findViewById<ImageView>(R.id.iv_play_video)
        progress = itemView.findViewById<ProgressBar>(R.id.progress)
        ivPlayButton.visibility = if (selectorConfig.isPreviewZoomEffect) View.GONE else View.VISIBLE
        if (selectorConfig.videoPlayerEngine == null) {
            selectorConfig.videoPlayerEngine = MediaPlayerEngine()
        }
        val engine = selectorConfig.videoPlayerEngine
        videoPlayer = engine?.onCreateVideoPlayer(itemView.context) ?: throw NullPointerException("onCreateVideoPlayer cannot be empty,Please implement " + VideoPlayerEngine::class.java.canonicalName)
        if (videoPlayer.layoutParams == null) {
            videoPlayer.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        val viewGroup = itemView as ViewGroup
        if (viewGroup.indexOfChild(videoPlayer) != -1) {
            viewGroup.removeView(videoPlayer)
        }
        viewGroup.addView(videoPlayer, 0)
        videoPlayer.visibility = View.GONE
    }

    /**
     * 开始播放视频
     */
    fun startPlay() {
        val currentMedia = media
        if (selectorConfig.isUseSystemVideoPlayer) {
            IntentUtils.startSystemPlayerVideo(itemView.context, currentMedia?.availablePath ?: "")
        } else {
        val engine = selectorConfig.videoPlayerEngine
        if (engine != null && currentMedia != null) {
            progress.visibility = View.VISIBLE
            ivPlayButton.visibility = View.GONE
            val listener = mPreviewEventListener
            listener?.onPreviewVideoTitle(currentMedia.fileName)
            isPlayed = true
            @Suppress("UNCHECKED_CAST")
            (engine as? VideoPlayerEngine<View>)?.onStarPlayer(videoPlayer, currentMedia)
        }
        }
    }

    override fun setScaleDisplaySize(media: LocalMedia) {
        super.setScaleDisplaySize(media)
        if (!selectorConfig.isPreviewZoomEffect && screenWidth < screenHeight) {
            val layoutParams = videoPlayer.layoutParams
            if (layoutParams is FrameLayout.LayoutParams) {
                val playerLayoutParams = layoutParams
                playerLayoutParams.width = screenWidth
                playerLayoutParams.height = screenAppInHeight
                playerLayoutParams.gravity = Gravity.CENTER
            } else if (layoutParams is RelativeLayout.LayoutParams) {
                val playerLayoutParams = layoutParams
                playerLayoutParams.width = screenWidth
                playerLayoutParams.height = screenAppInHeight
                playerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            } else if (layoutParams is LinearLayout.LayoutParams) {
                val playerLayoutParams = layoutParams
                playerLayoutParams.width = screenWidth
                playerLayoutParams.height = screenAppInHeight
                playerLayoutParams.gravity = Gravity.CENTER
            } else if (layoutParams is ConstraintLayout.LayoutParams) {
                val playerLayoutParams = layoutParams
                playerLayoutParams.width = screenWidth
                playerLayoutParams.height = screenAppInHeight
                playerLayoutParams.topToTop = ConstraintSet.PARENT_ID
                playerLayoutParams.bottomToBottom = ConstraintSet.PARENT_ID
            }
        }
    }

    private fun playerDefaultUI() {
        isPlayed = false
        ivPlayButton.visibility = View.VISIBLE
        progress.visibility = View.GONE
        coverImageView.visibility = View.VISIBLE
        videoPlayer.visibility = View.GONE
        val listener = mPreviewEventListener
        listener?.onPreviewVideoTitle(null)
    }

    private fun playerIngUI() {
        progress.visibility = View.GONE
        ivPlayButton.visibility = View.GONE
        coverImageView.visibility = View.GONE
        videoPlayer.visibility = View.VISIBLE
    }

    override fun onViewAttachedToWindow() {
        val engine = selectorConfig.videoPlayerEngine
        @Suppress("UNCHECKED_CAST")
        val typedEngine = engine as? VideoPlayerEngine<View>
        typedEngine?.onPlayerAttachedToWindow(videoPlayer)
        typedEngine?.addPlayListener(mPlayerListener)
    }

    override fun onViewDetachedFromWindow() {
        val engine = selectorConfig.videoPlayerEngine
        @Suppress("UNCHECKED_CAST")
        val typedEngine = engine as? VideoPlayerEngine<View>
        typedEngine?.onPlayerDetachedFromWindow(videoPlayer)
        typedEngine?.removePlayListener(mPlayerListener)
        playerDefaultUI()
    }

    /**
     * resume and pause play
     */
    override fun resumePausePlay() {
        if (isPlaying) {
            onPause()
        } else {
            onResume()
        }
    }

    override fun release() {
        val engine = selectorConfig.videoPlayerEngine
        @Suppress("UNCHECKED_CAST")
        val typedEngine = engine as? VideoPlayerEngine<View>
        typedEngine?.removePlayListener(mPlayerListener)
        typedEngine?.destroy(videoPlayer)
    }
}
