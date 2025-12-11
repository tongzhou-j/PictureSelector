package com.luck.picture.lib.engine

import android.content.Context
import android.media.MediaPlayer
import android.view.View
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnPlayerListener
import com.luck.picture.lib.widget.MediaPlayerView
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author：luck
 * @date：2022/7/1 22:09 下午
 * @describe：MediaPlayerEngine
 */
class MediaPlayerEngine : VideoPlayerEngine<MediaPlayerView?> {
    /**
     * 播放状态监听器集
     */
    private val listeners = CopyOnWriteArrayList<OnPlayerListener>()

    override fun onCreateVideoPlayer(context: Context?): View? {
        return if (context != null) MediaPlayerView(context) else null
    }

    override fun onStarPlayer(player: MediaPlayerView?, media: LocalMedia?) {
        if (player == null || media == null) return
        val availablePath = media.availablePath
        val mediaPlayer = player.mediaPlayer
        val surfaceView = player.getSurfaceView()
        surfaceView.setZOrderOnTop(PictureMimeType.isHasHttp(availablePath))
        val config: SelectorConfig? = SelectorProviders.instance?.selectorConfig
        mediaPlayer?.setLooping(config?.isLoopAutoPlay ?: false)
        player.start(availablePath)
    }

    override fun onResume(player: MediaPlayerView?) {
        player?.mediaPlayer?.start()
    }

    override fun onPause(player: MediaPlayerView?) {
        player?.mediaPlayer?.pause()
    }

    override fun isPlaying(player: MediaPlayerView?): Boolean {
        return player?.mediaPlayer?.isPlaying ?: false
    }

    override fun addPlayListener(playerListener: OnPlayerListener?) {
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

    override fun onPlayerAttachedToWindow(player: MediaPlayerView?) {
        if (player == null) return
        val mediaPlayer = player.initMediaPlayer()
        mediaPlayer?.setOnPreparedListener(object : MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                mp.start()
                for (i in listeners.indices) {
                    val playerListener = listeners[i]
                    playerListener?.onPlayerReady()
                }
            }
        })
        mediaPlayer?.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer) {
                mp.reset()
                for (i in listeners.indices) {
                    val playerListener = listeners[i]
                    playerListener?.onPlayerEnd()
                }
                player.clearCanvas()
            }
        })
        mediaPlayer?.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                for (i in listeners.indices) {
                    val playerListener = listeners[i]
                    playerListener?.onPlayerError()
                }
                return false
            }
        })
    }

    override fun onPlayerDetachedFromWindow(player: MediaPlayerView?) {
        player?.release()
    }

    override fun destroy(player: MediaPlayerView?) {
        player?.release()
    }
}
