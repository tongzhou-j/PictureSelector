package com.luck.picture.lib.adapter.holder

import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.luck.picture.lib.R
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.photoview.OnViewTapListener
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.DoubleUtils
import com.luck.picture.lib.utils.PictureFileUtils
import java.io.IOException

/**
 * @author：luck
 * @date：2021/12/15 5:11 下午
 * @describe：PreviewAudioHolder
 */
class PreviewAudioHolder(itemView: View) : BasePreviewHolder(itemView) {
    private val mHandler = Handler(Looper.getMainLooper())
    lateinit var ivPlayButton: ImageView
    lateinit var tvAudioName: TextView
    lateinit var tvTotalDuration: TextView
    lateinit var tvCurrentTime: TextView
    lateinit var seekBar: SeekBar
    lateinit var ivPlayBack: ImageView
    lateinit var ivPlayFast: ImageView
    private var mPlayer: MediaPlayer? = MediaPlayer()
    private var isPausePlayer = false

    /**
     * 播放计时器
     */
    lateinit var mTickerRunnable: Runnable

    override fun findViews(itemView: View?) {
    }

    override fun loadImage(media: LocalMedia?, maxWidth: Int, maxHeight: Int) {
        tvAudioName.setCompoundDrawablesRelativeWithIntrinsicBounds(
            0,
            R.drawable.ps_ic_audio_play_cover,
            0,
            0
        )
    }

    override fun onClickBackPressed() {
        coverImageView.setOnViewTapListener(object : OnViewTapListener {
            override fun onViewTap(view: View?, x: Float, y: Float) {
                val listener = mPreviewEventListener
                if (listener != null) {
                    listener.onBackPressed()
                }
            }
        })
    }

    override fun onLongPressDownload(media: LocalMedia?) {
        coverImageView.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                val listener = mPreviewEventListener
                if (listener != null) {
                    listener.onLongPressDownload(media)
                }
                return false
            }
        })
    }

    override fun bindData(media: LocalMedia, position: Int) {
        val path = media.availablePath
        val dataFormat = DateUtils.getYearDataFormat(media.dateAddedTime)
        val fileSize = PictureFileUtils.formatAccurateUnitFileSize(media.size)
        loadImage(media, PictureConfig.UNSET, PictureConfig.UNSET)
        val stringBuilder = StringBuilder()
        stringBuilder.append(media.fileName).append("\n").append(dataFormat).append(" - ")
            .append(fileSize)
        val builder = SpannableStringBuilder(stringBuilder.toString())
        val indexOfStr = dataFormat + " - " + fileSize
        val startIndex = stringBuilder.indexOf(indexOfStr)
        val endOf = startIndex + indexOfStr.length
        builder.setSpan(
            AbsoluteSizeSpan(DensityUtil.dip2px(itemView.context, 12f)),
            startIndex,
            endOf,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        builder.setSpan(
            ForegroundColorSpan(-0x9a9a9b),
            startIndex,
            endOf,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        tvAudioName.text = builder
        tvTotalDuration.text = DateUtils.formatDurationTime(media.duration)
        seekBar.max = media.duration.toInt()
        setBackFastUI(false)
        ivPlayBack.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                slowAudioPlay()
            }
        })

        ivPlayFast.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                fastAudioPlay()
            }
        })

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekBar.progress = progress
                    setCurrentPlayTime(progress)
                    if (isPlaying) {
                        mPlayer!!.seekTo(seekBar.progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val listener = mPreviewEventListener
                if (listener != null) {
                    listener.onBackPressed()
                }
            }
        })
        ivPlayButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                try {
                    if (DoubleUtils.isFastDoubleClick) {
                        return
                    }
                    mPreviewEventListener?.onPreviewVideoTitle(media.fileName)
                    if (isPlaying) {
                        pausePlayer()
                    } else {
                        if (isPausePlayer) {
                            resumePlayer()
                        } else {
                            startPlayer(path)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

        itemView.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(view: View?): Boolean {
                val listener = mPreviewEventListener
                if (listener != null) {
                    listener.onLongPressDownload(media)
                }
                return false
            }
        })
    }

    /**
     * 重新开始播放
     *
     * @param path
     */
    private fun startPlayer(path: String?) {
        try {
            if (PictureMimeType.isContent(path)) {
                mPlayer!!.setDataSource(itemView.context, Uri.parse(path))
            } else {
                mPlayer!!.setDataSource(path)
            }
            mPlayer!!.prepare()
            mPlayer!!.seekTo(seekBar.progress)
            mPlayer!!.start()
            isPausePlayer = false
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override val isPlaying: Boolean
        get() = mPlayer != null && mPlayer!!.isPlaying

    /**
     * 暂停播放
     */
    private fun pausePlayer() {
        mPlayer!!.pause()
        isPausePlayer = true
        playerDefaultUI(false)
        stopUpdateProgress()
    }

    /**
     * 恢复播放
     */
    private fun resumePlayer() {
        mPlayer!!.seekTo(seekBar.progress)
        mPlayer!!.start()
        startUpdateProgress()
        playerIngUI()
    }

    /**
     * 重置播放器
     */
    private fun resetMediaPlayer() {
        isPausePlayer = false
        mPlayer!!.stop()
        mPlayer!!.reset()
    }

    /**
     * 设置当前播放进度
     *
     * @param progress
     */
    private fun setCurrentPlayTime(progress: Int) {
        val time = DateUtils.formatDurationTime(progress.toLong())
        tvCurrentTime.text = time
    }

    /**
     * 快进
     */
    private fun fastAudioPlay() {
        val progress: Long = seekBar.progress + PreviewAudioHolder.Companion.MAX_BACK_FAST_MS
        if (progress >= seekBar.max) {
            seekBar.progress = seekBar.max
        } else {
            seekBar.progress = progress.toInt()
        }
        setCurrentPlayTime(seekBar.progress)
        mPlayer!!.seekTo(seekBar.progress)
    }

    /**
     * 回退
     */
    private fun slowAudioPlay() {
        val progress: Long = seekBar.progress - PreviewAudioHolder.Companion.MAX_BACK_FAST_MS
        if (progress <= 0) {
            seekBar.progress = 0
        } else {
            seekBar.progress = progress.toInt()
        }
        setCurrentPlayTime(seekBar.progress)
        mPlayer!!.seekTo(seekBar.progress)
    }

    /**
     * 播放完成监听
     */
    private val mPlayCompletionListener: MediaPlayer.OnCompletionListener =
        object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                stopUpdateProgress()
                resetMediaPlayer()
                playerDefaultUI(true)
            }
        }

    /**
     * 播放失败监听
     */
    private val mPlayErrorListener: MediaPlayer.OnErrorListener =
        object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                resetMediaPlayer()
                playerDefaultUI(true)
                return false
            }
        }

    /**
     * 资源装载完成
     */
    private val mPlayPreparedListener: MediaPlayer.OnPreparedListener =
        object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                if (mp.isPlaying) {
                    seekBar.max = mp.duration
                    startUpdateProgress()
                    playerIngUI()
                } else {
                    stopUpdateProgress()
                    resetMediaPlayer()
                    playerDefaultUI(true)
                }
            }
        }

    init {
        ivPlayButton = itemView.findViewById<ImageView>(R.id.iv_play_video)
        tvAudioName = itemView.findViewById<TextView>(R.id.tv_audio_name)
        tvCurrentTime = itemView.findViewById<TextView>(R.id.tv_current_time)
        tvTotalDuration = itemView.findViewById<TextView>(R.id.tv_total_duration)
        seekBar = itemView.findViewById<SeekBar>(R.id.music_seek_bar)
        ivPlayBack = itemView.findViewById<ImageView>(R.id.iv_play_back)
        ivPlayFast = itemView.findViewById<ImageView>(R.id.iv_play_fast)
        
        /**
         * 播放计时器
         */
        mTickerRunnable = object : Runnable {
            override fun run() {
                val currentPosition = mPlayer!!.currentPosition.toLong()
                val time = DateUtils.formatDurationTime(currentPosition)
                if (!TextUtils.equals(time, tvCurrentTime.text)) {
                    tvCurrentTime.text = time
                    if (mPlayer!!.duration - currentPosition > PreviewAudioHolder.Companion.MIN_CURRENT_POSITION) {
                        seekBar.progress = currentPosition.toInt()
                    } else {
                        seekBar.progress = mPlayer!!.duration
                    }
                }
                val nextSecondMs: Long =
                    PreviewAudioHolder.Companion.MAX_UPDATE_INTERVAL_MS - currentPosition % PreviewAudioHolder.Companion.MAX_UPDATE_INTERVAL_MS
                mHandler.postDelayed(this, nextSecondMs)
            }
        }
    }

    /**
     * 开始更新播放进度
     */
    private fun startUpdateProgress() {
        mHandler.post(mTickerRunnable)
    }

    /**
     * 停止更新播放进度
     */
    private fun stopUpdateProgress() {
        mHandler.removeCallbacks(mTickerRunnable)
    }

    /**
     * 默认UI样式
     *
     * @param isResetProgress 是否重置进度条
     */
    private fun playerDefaultUI(isResetProgress: Boolean) {
        stopUpdateProgress()
        if (isResetProgress) {
            seekBar.progress = 0
            tvCurrentTime.text = "00:00"
        }
        setBackFastUI(false)
        ivPlayButton.setImageResource(R.drawable.ps_ic_audio_play)
        mPreviewEventListener?.onPreviewVideoTitle(null)
    }

    /**
     * 播放中UI样式
     */
    private fun playerIngUI() {
        startUpdateProgress()
        setBackFastUI(true)
        ivPlayButton.setImageResource(R.drawable.ps_ic_audio_stop)
    }

    /**
     * 设置快进和回退UI样式
     *
     * @param isEnabled
     */
    private fun setBackFastUI(isEnabled: Boolean) {
        ivPlayBack.setEnabled(isEnabled)
        ivPlayFast.setEnabled(isEnabled)
        if (isEnabled) {
            ivPlayBack.setAlpha(1.0f)
            ivPlayFast.setAlpha(1.0f)
        } else {
            ivPlayBack.setAlpha(0.5f)
            ivPlayFast.setAlpha(0.5f)
        }
    }

    override fun onViewAttachedToWindow() {
        isPausePlayer = false
        setMediaPlayerListener()
        playerDefaultUI(true)
    }

    override fun onViewDetachedFromWindow() {
        isPausePlayer = false
        mHandler.removeCallbacks(mTickerRunnable)
        setNullMediaPlayerListener()
        resetMediaPlayer()
        playerDefaultUI(true)
    }

    /**
     * resume and pause play
     */
    override fun resumePausePlay() {
        if (isPlaying) {
            pausePlayer()
        } else {
            resumePlayer()
        }
    }

    override fun release() {
        mHandler.removeCallbacks(mTickerRunnable)
        if (mPlayer != null) {
            setNullMediaPlayerListener()
            mPlayer!!.release()
            mPlayer = null
        }
    }

    /**
     * 设置监听器
     */
    private fun setMediaPlayerListener() {
        mPlayer!!.setOnCompletionListener(mPlayCompletionListener)
        mPlayer!!.setOnErrorListener(mPlayErrorListener)
        mPlayer!!.setOnPreparedListener(mPlayPreparedListener)
    }

    /**
     * 置空监听器
     */
    private fun setNullMediaPlayerListener() {
        mPlayer!!.setOnCompletionListener(null)
        mPlayer!!.setOnErrorListener(null)
        mPlayer!!.setOnPreparedListener(null)
    }

    companion object {
        private val MAX_BACK_FAST_MS = (3 * 1000).toLong()
        private const val MAX_UPDATE_INTERVAL_MS: Long = 1000
        private const val MIN_CURRENT_POSITION: Long = 1000
    }
}
