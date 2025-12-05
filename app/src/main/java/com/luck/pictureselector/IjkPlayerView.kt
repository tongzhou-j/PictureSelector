package com.luck.pictureselector

import android.content.Context
import android.graphics.SurfaceTexture
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.luck.picture.lib.config.PictureMimeType
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.IOException

/**
 * @author：luck
 * @date：2022/7/1 23:57 上午
 * @describe：IjkPlayerView
 */
class IjkPlayerView @JvmOverloads constructor(
    @NonNull context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {
    private val textureView: IjkVideoTextureView
    private var _mediaPlayer: IjkMediaPlayer? = null
    private var mVideoRotation: Int = 0

    init {
        textureView = IjkVideoTextureView(context)
        textureView.surfaceTextureListener = this
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER
        textureView.layoutParams = layoutParams
        addView(textureView)
    }

    fun initMediaPlayer(): IjkMediaPlayer {
        if (_mediaPlayer == null) {
            _mediaPlayer = IjkMediaPlayer()
        }
        _mediaPlayer!!.setOnVideoSizeChangedListener { mp: IMediaPlayer, width: Int, height: Int, sarNum: Int, sarDen: Int ->
            textureView.adjustVideoSize(width, height, mVideoRotation)
        }
        _mediaPlayer!!.setOnInfoListener { mp: IMediaPlayer, what: Int, extra: Int ->
            if (what == 10001) {
                mVideoRotation = extra
            }
            false
        }
        _mediaPlayer!!.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
        return _mediaPlayer!!
    }

    val mediaPlayer: IjkMediaPlayer?
        get() = _mediaPlayer

    fun start(path: String) {
        try {
            if (PictureMimeType.isContent(path)) {
                _mediaPlayer!!.setDataSource(context, Uri.parse(path))
            } else {
                _mediaPlayer!!.setDataSource(path)
            }
            val surfaceTexture = textureView.surfaceTexture
            if (surfaceTexture != null) {
                _mediaPlayer!!.setSurface(Surface(surfaceTexture))
            }
            _mediaPlayer!!.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onSurfaceTextureAvailable(@NonNull surface: SurfaceTexture, width: Int, height: Int) {
        _mediaPlayer?.setSurface(Surface(surface))
    }

    override fun onSurfaceTextureSizeChanged(@NonNull surface: SurfaceTexture, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(@NonNull surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(@NonNull surface: SurfaceTexture) {
    }

    class IjkVideoTextureView(context: Context) : TextureView(context) {
        /**
         * 视频宽度
         */
        private var mVideoWidth: Int = 0
        /**
         * 视频高度
         */
        private var mVideoHeight: Int = 0

        /**
         * 视频旋转角度
         */
        private var mVideoRotation: Int = 0

        fun adjustVideoSize(videoWidth: Int, videoHeight: Int, videoRotation: Int) {
            this.mVideoWidth = videoWidth
            this.mVideoHeight = videoHeight
            this.mVideoRotation = videoRotation
            rotation = mVideoRotation.toFloat()
            requestLayout()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var width = getDefaultSize(mVideoWidth, widthMeasureSpec)
            var height = getDefaultSize(mVideoHeight, heightMeasureSpec)
            val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)
            if (mVideoWidth > 0 && mVideoHeight > 0) {
                when {
                    widthSpecMode == View.MeasureSpec.EXACTLY && heightSpecMode == View.MeasureSpec.EXACTLY -> {
                        width = widthSpecSize
                        height = heightSpecSize

                        if (mVideoWidth * height < width * mVideoHeight) {
                            width = height * mVideoWidth / mVideoHeight
                        } else if (mVideoWidth * height > width * mVideoHeight) {
                            height = width * mVideoHeight / mVideoWidth
                        }
                    }
                    widthSpecMode == View.MeasureSpec.EXACTLY -> {
                        width = widthSpecSize
                        height = width * mVideoHeight / mVideoWidth
                        if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                            height = heightSpecSize
                        }
                    }
                    heightSpecMode == View.MeasureSpec.EXACTLY -> {
                        height = heightSpecSize
                        width = height * mVideoWidth / mVideoHeight
                        if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                            width = widthSpecSize
                        }
                    }
                    else -> {
                        width = mVideoWidth
                        height = mVideoHeight
                        if (heightSpecMode == View.MeasureSpec.AT_MOST && height > heightSpecSize) {
                            height = heightSpecSize
                            width = height * mVideoWidth / mVideoHeight
                        }
                        if (widthSpecMode == View.MeasureSpec.AT_MOST && width > widthSpecSize) {
                            width = widthSpecSize
                            height = width * mVideoHeight / mVideoWidth
                        }
                    }
                }
            }
            setMeasuredDimension(width, height)
            if ((mVideoRotation + 180) % 180 != 0) {
                val size = scaleSize(widthSpecSize, heightSpecSize, height, width)
                scaleX = size[0] / height.toFloat()
                scaleY = size[1] / width.toFloat()
            }
        }
    }

    companion object {
        fun scaleSize(textureWidth: Int, textureHeight: Int, realWidth: Int, realHeight: Int): IntArray {
            val deviceRate = textureWidth.toFloat() / textureHeight.toFloat()
            val rate = realWidth.toFloat() / realHeight.toFloat()
            val width: Int
            val height: Int
            if (rate < deviceRate) {
                height = textureHeight
                width = (textureHeight * rate).toInt()
            } else {
                width = textureWidth
                height = (textureWidth / rate).toInt()
            }
            return intArrayOf(width, height)
        }
    }

    fun release() {
        if (_mediaPlayer != null) {
            _mediaPlayer!!.release()
            _mediaPlayer!!.setOnPreparedListener(null)
            _mediaPlayer!!.setOnCompletionListener(null)
            _mediaPlayer!!.setOnErrorListener(null)
            _mediaPlayer!!.setOnInfoListener(null)
            _mediaPlayer = null
        }
    }

    fun clearCanvas() {
    }
}

