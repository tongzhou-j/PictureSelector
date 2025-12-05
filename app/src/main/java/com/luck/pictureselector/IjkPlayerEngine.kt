package com.luck.pictureselector

import android.content.Context
import android.view.View
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.engine.VideoPlayerEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnPlayerListener
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author：luck
 * @date：2022/7/1 21:52 上午
 * @describe：IjkPlayerEngine
 */
class IjkPlayerEngine : VideoPlayerEngine<IjkPlayerView> {
    /**
     * 播放状态监听器集
     */
    private val listeners = CopyOnWriteArrayList<OnPlayerListener>()

    override fun onCreateVideoPlayer(context: Context): View {
        return IjkPlayerView(context)
    }

    override fun onStarPlayer(player: IjkPlayerView, media: LocalMedia) {
        val mediaPlayer = player.mediaPlayer
        val config = SelectorProviders.getInstance().selectorConfig
        mediaPlayer?.let {
            it.isLooping = config.isLoopAutoPlay
        }
        player.start(media.availablePath)
    }

    override fun onResume(player: IjkPlayerView) {
        val mediaPlayer = player.mediaPlayer
        mediaPlayer?.start()
    }

    override fun onPause(player: IjkPlayerView) {
        val mediaPlayer = player.mediaPlayer
        mediaPlayer?.pause()
    }

    override fun isPlaying(player: IjkPlayerView): Boolean {
        val mediaPlayer = player.mediaPlayer
        return mediaPlayer != null && mediaPlayer.isPlaying
    }

    override fun addPlayListener(playerListener: OnPlayerListener) {
        if (!listeners.contains(playerListener)) {
            listeners.add(playerListener)
        }
    }

    override fun removePlayListener(playerListener: OnPlayerListener?) {
        if (playerListener != null) {
            listeners.remove(playerListener)
        } else {
            listeners.clear()
        }
    }

    override fun onPlayerAttachedToWindow(player: IjkPlayerView) {
        val mediaPlayer = player.initMediaPlayer()
        mediaPlayer.setOnPreparedListener { mp ->
            mp.start()
            for (i in listeners.indices) {
                val playerListener = listeners[i]
                playerListener.onPlayerReady()
            }
        }
        mediaPlayer.setOnCompletionListener { mp ->
            mp.reset()
            for (i in listeners.indices) {
                val playerListener = listeners[i]
                playerListener.onPlayerEnd()
            }
            player.clearCanvas()
        }

        mediaPlayer.setOnErrorListener { _, what, extra ->
            for (i in listeners.indices) {
                val playerListener = listeners[i]
                playerListener.onPlayerError()
            }
            false
        }
    }

    override fun onPlayerDetachedFromWindow(player: IjkPlayerView) {
        player.release()
    }

    override fun destroy(player: IjkPlayerView) {
        player.release()
    }
}

