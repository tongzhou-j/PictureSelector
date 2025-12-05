package com.luck.pictureselector

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.annotation.NonNull
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.engine.VideoPlayerEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnPlayerListener
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author：luck
 * @date：2022/7/1 22:26 下午
 * @describe：ExoPlayerEngine
 */
class ExoPlayerEngine : VideoPlayerEngine<StyledPlayerView> {
    /**
     * 播放状态监听器集
     */
    private val listeners = CopyOnWriteArrayList<OnPlayerListener>()

    override fun onCreateVideoPlayer(context: Context): View {
        val exoPlayer = StyledPlayerView(context)
        exoPlayer.useController = false
        return exoPlayer
    }

    override fun onStarPlayer(exoPlayer: StyledPlayerView, media: LocalMedia) {
        val player = exoPlayer.player
        if (player != null) {
            val path = media.availablePath
            val mediaItem = when {
                PictureMimeType.isContent(path) -> MediaItem.fromUri(Uri.parse(path))
                PictureMimeType.isHasHttp(path) -> MediaItem.fromUri(path)
                else -> MediaItem.fromUri(Uri.fromFile(File(path)))
            }
            val config = SelectorProviders.getInstance().selectorConfig
            player.repeatMode = if (config.isLoopAutoPlay) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    override fun onResume(exoPlayer: StyledPlayerView) {
        exoPlayer.player?.play()
    }

    override fun onPause(exoPlayer: StyledPlayerView) {
        exoPlayer.player?.pause()
    }

    override fun isPlaying(exoPlayer: StyledPlayerView): Boolean {
        val player = exoPlayer.player
        return player != null && player.isPlaying
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

    override fun onPlayerAttachedToWindow(exoPlayer: StyledPlayerView) {
        val player = ExoPlayer.Builder(exoPlayer.context).build()
        exoPlayer.player = player
        player.addListener(mPlayerListener)
    }

    override fun onPlayerDetachedFromWindow(exoPlayer: StyledPlayerView) {
        val player = exoPlayer.player
        if (player != null) {
            player.removeListener(mPlayerListener)
            player.release()
            exoPlayer.player = null
        }
    }

    override fun destroy(exoPlayer: StyledPlayerView) {
        val player = exoPlayer.player
        if (player != null) {
            player.removeListener(mPlayerListener)
            player.release()
        }
    }

    /**
     * ExoPlayer播放状态回调
     */
    private val mPlayerListener = object : Player.Listener {
        override fun onPlayerError(@NonNull error: PlaybackException) {
            for (i in listeners.indices) {
                val playerListener = listeners[i]
                playerListener.onPlayerError()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    for (i in listeners.indices) {
                        val playerListener = listeners[i]
                        playerListener.onPlayerReady()
                    }
                }
                Player.STATE_BUFFERING -> {
                    for (i in listeners.indices) {
                        val playerListener = listeners[i]
                        playerListener.onPlayerLoading()
                    }
                }
                Player.STATE_ENDED -> {
                    for (i in listeners.indices) {
                        val playerListener = listeners[i]
                        playerListener.onPlayerEnd()
                    }
                }
            }
        }
    }
}

