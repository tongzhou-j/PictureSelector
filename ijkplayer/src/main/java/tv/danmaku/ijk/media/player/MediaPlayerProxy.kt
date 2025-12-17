/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.player

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.Surface
import android.view.SurfaceHolder
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import tv.danmaku.ijk.media.player.misc.ITrackInfo
import java.io.FileDescriptor
import java.io.IOException
import java.util.Map

open class MediaPlayerProxy(protected val mBackEndMediaPlayer: IMediaPlayer) : IMediaPlayer {
    fun getInternalMediaPlayer(): IMediaPlayer {
        return mBackEndMediaPlayer
    }

    override fun setDisplay(sh: SurfaceHolder?) {
        mBackEndMediaPlayer.setDisplay(sh)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun setSurface(surface: Surface?) {
        mBackEndMediaPlayer.setSurface(surface)
    }

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    override fun setDataSource(context: Context, uri: Uri) {
        mBackEndMediaPlayer.setDataSource(context, uri)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    override fun setDataSource(context: Context, uri: Uri, headers: Map<String, String>?) {
        mBackEndMediaPlayer.setDataSource(context, uri, headers)
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun setDataSource(fd: FileDescriptor) {
        mBackEndMediaPlayer.setDataSource(fd)
    }

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    override fun setDataSource(path: String) {
        mBackEndMediaPlayer.setDataSource(path)
    }

    override fun setDataSource(mediaDataSource: IMediaDataSource?) {
        mBackEndMediaPlayer.setDataSource(mediaDataSource)
    }

    override fun getDataSource(): String? {
        return mBackEndMediaPlayer.getDataSource()
    }

    @Throws(IllegalStateException::class)
    override fun prepareAsync() {
        mBackEndMediaPlayer.prepareAsync()
    }

    @Throws(IllegalStateException::class)
    override fun start() {
        mBackEndMediaPlayer.start()
    }

    @Throws(IllegalStateException::class)
    override fun stop() {
        mBackEndMediaPlayer.stop()
    }

    @Throws(IllegalStateException::class)
    override fun pause() {
        mBackEndMediaPlayer.pause()
    }

    override fun setScreenOnWhilePlaying(screenOn: Boolean) {
        mBackEndMediaPlayer.setScreenOnWhilePlaying(screenOn)
    }

    override fun getVideoWidth(): Int {
        return mBackEndMediaPlayer.getVideoWidth()
    }

    override fun getVideoHeight(): Int {
        return mBackEndMediaPlayer.getVideoHeight()
    }

    override fun isPlaying(): Boolean {
        return mBackEndMediaPlayer.isPlaying()
    }

    @Throws(IllegalStateException::class)
    override fun seekTo(msec: Long) {
        mBackEndMediaPlayer.seekTo(msec)
    }

    override fun getCurrentPosition(): Long {
        return mBackEndMediaPlayer.getCurrentPosition()
    }

    override fun getDuration(): Long {
        return mBackEndMediaPlayer.getDuration()
    }

    override fun release() {
        mBackEndMediaPlayer.release()
    }

    override fun reset() {
        mBackEndMediaPlayer.reset()
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        mBackEndMediaPlayer.setVolume(leftVolume, rightVolume)
    }

    override fun getAudioSessionId(): Int {
        return mBackEndMediaPlayer.getAudioSessionId()
    }

    override fun getMediaInfo(): MediaInfo? {
        return mBackEndMediaPlayer.getMediaInfo()
    }

    @Deprecated("")
    override fun setLogEnabled(enable: Boolean) {
        // Empty implementation
    }

    @Deprecated("")
    override fun isPlayable(): Boolean {
        return false
    }

    override fun setOnPreparedListener(listener: IMediaPlayer.OnPreparedListener?) {
        if (listener != null) {
            val finalListener = listener
            mBackEndMediaPlayer.setOnPreparedListener(object : IMediaPlayer.OnPreparedListener {
                override fun onPrepared(mp: IMediaPlayer) {
                    finalListener.onPrepared(this@MediaPlayerProxy)
                }
            })
        } else {
            mBackEndMediaPlayer.setOnPreparedListener(null)
        }
    }

    override fun setOnCompletionListener(listener: IMediaPlayer.OnCompletionListener?) {
        if (listener != null) {
            val finalListener = listener
            mBackEndMediaPlayer.setOnCompletionListener(object : IMediaPlayer.OnCompletionListener {
                override fun onCompletion(mp: IMediaPlayer) {
                    finalListener.onCompletion(this@MediaPlayerProxy)
                }
            })
        } else {
            mBackEndMediaPlayer.setOnCompletionListener(null)
        }
    }

    override fun setOnBufferingUpdateListener(listener: IMediaPlayer.OnBufferingUpdateListener?) {
        if (listener != null) {
            val finalListener = listener
            mBackEndMediaPlayer.setOnBufferingUpdateListener(object : IMediaPlayer.OnBufferingUpdateListener {
                override fun onBufferingUpdate(mp: IMediaPlayer, percent: Int) {
                    finalListener.onBufferingUpdate(this@MediaPlayerProxy, percent)
                }
            })
        } else {
            mBackEndMediaPlayer.setOnBufferingUpdateListener(null)
        }
    }

    override fun setOnSeekCompleteListener(listener: IMediaPlayer.OnSeekCompleteListener?) {
        if (listener != null) {
            val finalListener = listener
            mBackEndMediaPlayer.setOnSeekCompleteListener(object : IMediaPlayer.OnSeekCompleteListener {
                override fun onSeekComplete(mp: IMediaPlayer) {
                    finalListener.onSeekComplete(this@MediaPlayerProxy)
                }
            })
        } else {
            mBackEndMediaPlayer.setOnSeekCompleteListener(null)
        }
    }

    override fun setOnVideoSizeChangedListener(listener: IMediaPlayer.OnVideoSizeChangedListener?) {
        if (listener != null) {
            val finalListener = listener
            mBackEndMediaPlayer.setOnVideoSizeChangedListener(object : IMediaPlayer.OnVideoSizeChangedListener {
                override fun onVideoSizeChanged(mp: IMediaPlayer, width: Int, height: Int, sar_num: Int, sar_den: Int) {
                    finalListener.onVideoSizeChanged(this@MediaPlayerProxy, width, height, sar_num, sar_den)
                }
            })
        } else {
            mBackEndMediaPlayer.setOnVideoSizeChangedListener(null)
        }
    }

    override fun setOnErrorListener(listener: IMediaPlayer.OnErrorListener?) {
        if (listener != null) {
            val finalListener = listener
            mBackEndMediaPlayer.setOnErrorListener(object : IMediaPlayer.OnErrorListener {
                override fun onError(mp: IMediaPlayer, what: Int, extra: Int): Boolean {
                    return finalListener.onError(this@MediaPlayerProxy, what, extra)
                }
            })
        } else {
            mBackEndMediaPlayer.setOnErrorListener(null)
        }
    }

    override fun setOnInfoListener(listener: IMediaPlayer.OnInfoListener?) {
        if (listener != null) {
            val finalListener = listener
            mBackEndMediaPlayer.setOnInfoListener(object : IMediaPlayer.OnInfoListener {
                override fun onInfo(mp: IMediaPlayer, what: Int, extra: Int): Boolean {
                    return finalListener.onInfo(this@MediaPlayerProxy, what, extra)
                }
            })
        } else {
            mBackEndMediaPlayer.setOnInfoListener(null)
        }
    }

    override fun setOnTimedTextListener(listener: IMediaPlayer.OnTimedTextListener?) {
        if (listener != null) {
            val finalListener = listener
            mBackEndMediaPlayer.setOnTimedTextListener(object : IMediaPlayer.OnTimedTextListener {
                override fun onTimedText(mp: IMediaPlayer, text: IjkTimedText?) {
                    finalListener.onTimedText(this@MediaPlayerProxy, text)
                }
            })
        } else {
            mBackEndMediaPlayer.setOnTimedTextListener(null)
        }
    }

    override fun setAudioStreamType(streamtype: Int) {
        mBackEndMediaPlayer.setAudioStreamType(streamtype)
    }

    @Deprecated("")
    override fun setKeepInBackground(keepInBackground: Boolean) {
        mBackEndMediaPlayer.setKeepInBackground(keepInBackground)
    }

    override fun getVideoSarNum(): Int {
        return mBackEndMediaPlayer.getVideoSarNum()
    }

    override fun getVideoSarDen(): Int {
        return mBackEndMediaPlayer.getVideoSarDen()
    }

    @Deprecated("")
    override fun setWakeMode(context: Context, mode: Int) {
        mBackEndMediaPlayer.setWakeMode(context, mode)
    }

    override fun getTrackInfo(): Array<ITrackInfo>? {
        return mBackEndMediaPlayer.getTrackInfo()
    }

    override fun setLooping(looping: Boolean) {
        mBackEndMediaPlayer.setLooping(looping)
    }

    override fun isLooping(): Boolean {
        return mBackEndMediaPlayer.isLooping()
    }
}

