/*
 * Copyright (C) 2006 Bilibili
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
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
import android.media.AudioManager
import android.media.MediaDataSource
import android.media.MediaPlayer
import android.media.TimedText
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.view.Surface
import android.view.SurfaceHolder
import tv.danmaku.ijk.media.player.misc.AndroidTrackInfo
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import tv.danmaku.ijk.media.player.misc.ITrackInfo
import tv.danmaku.ijk.media.player.pragma.DebugLog
import java.io.FileDescriptor
import java.io.IOException
import java.lang.ref.WeakReference

class AndroidMediaPlayer : AbstractMediaPlayer() {
    private val mInternalMediaPlayer: MediaPlayer
    private val mInternalListenerAdapter: AndroidMediaPlayerListenerHolder
    private var mDataSource: String? = null
    private var mMediaDataSource: MediaDataSource? = null

    private val mInitLock = Any()
    private var mIsReleased: Boolean = false

    companion object {
        private var sMediaInfo: MediaInfo? = null
    }

    init {
        synchronized(mInitLock) {
            mInternalMediaPlayer = MediaPlayer()
        }
        mInternalMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mInternalListenerAdapter = AndroidMediaPlayerListenerHolder(this)
        attachInternalListeners()
    }

    fun getInternalMediaPlayer(): MediaPlayer {
        return mInternalMediaPlayer
    }

    override fun setDisplay(sh: SurfaceHolder?) {
        synchronized(mInitLock) {
            if (!mIsReleased) {
                mInternalMediaPlayer.setDisplay(sh)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun setSurface(surface: Surface?) {
        mInternalMediaPlayer.setSurface(surface)
    }

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    override fun setDataSource(context: Context, uri: Uri) {
        mInternalMediaPlayer.setDataSource(context, uri)
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    override fun setDataSource(context: Context, uri: Uri, headers: java.util.Map<String, String>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && headers != null) {
            val headersMap = java.util.HashMap<String, String>()
            for (entry in headers.entrySet()) {
                headersMap.put(entry.key, entry.value)
            }
            @Suppress("DEPRECATION")
            mInternalMediaPlayer.setDataSource(context, uri, headersMap)
        } else {
            mInternalMediaPlayer.setDataSource(context, uri)
        }
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun setDataSource(fd: FileDescriptor) {
        mInternalMediaPlayer.setDataSource(fd)
    }

    @Throws(IOException::class, IllegalArgumentException::class, SecurityException::class, IllegalStateException::class)
    override fun setDataSource(path: String) {
        mDataSource = path

        val uri = Uri.parse(path)
        val scheme = uri.scheme
        if (!scheme.isNullOrEmpty() && scheme.equals("file", ignoreCase = true)) {
            mInternalMediaPlayer.setDataSource(uri.path)
        } else {
            mInternalMediaPlayer.setDataSource(path)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun setDataSource(mediaDataSource: IMediaDataSource?) {
        if (mediaDataSource == null) {
            throw IllegalArgumentException("mediaDataSource cannot be null")
        }
        releaseMediaDataSource()

        mMediaDataSource = MediaDataSourceProxy(mediaDataSource)
        mInternalMediaPlayer.setDataSource(mMediaDataSource)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private class MediaDataSourceProxy(private val mMediaDataSource: IMediaDataSource) : MediaDataSource() {
        @Throws(IOException::class)
        override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
            return mMediaDataSource.readAt(position, buffer, offset, size)
        }

        @Throws(IOException::class)
        override fun getSize(): Long {
            return mMediaDataSource.getSize()
        }

        @Throws(IOException::class)
        override fun close() {
            mMediaDataSource.close()
        }
    }

    override fun getDataSource(): String? {
        return mDataSource
    }

    private fun releaseMediaDataSource() {
        mMediaDataSource?.let {
            try {
                it.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mMediaDataSource = null
        }
    }

    @Throws(IllegalStateException::class)
    override fun prepareAsync() {
        mInternalMediaPlayer.prepareAsync()
    }

    @Throws(IllegalStateException::class)
    override fun start() {
        mInternalMediaPlayer.start()
    }

    @Throws(IllegalStateException::class)
    override fun stop() {
        mInternalMediaPlayer.stop()
    }

    @Throws(IllegalStateException::class)
    override fun pause() {
        mInternalMediaPlayer.pause()
    }

    override fun setScreenOnWhilePlaying(screenOn: Boolean) {
        mInternalMediaPlayer.setScreenOnWhilePlaying(screenOn)
    }

    override fun getTrackInfo(): Array<ITrackInfo> {
        val trackInfos = AndroidTrackInfo.fromMediaPlayer(mInternalMediaPlayer) ?: return emptyArray()
        return Array(trackInfos.size) { trackInfos[it] }
    }

    override fun getVideoWidth(): Int {
        return mInternalMediaPlayer.videoWidth
    }

    override fun getVideoHeight(): Int {
        return mInternalMediaPlayer.videoHeight
    }

    override fun getVideoSarNum(): Int {
        return 1
    }

    override fun getVideoSarDen(): Int {
        return 1
    }

    override fun isPlaying(): Boolean {
        return try {
            mInternalMediaPlayer.isPlaying
        } catch (e: IllegalStateException) {
            DebugLog.printStackTrace(e)
            false
        }
    }

    @Throws(IllegalStateException::class)
    override fun seekTo(msec: Long) {
        mInternalMediaPlayer.seekTo(msec.toInt())
    }

    override fun getCurrentPosition(): Long {
        return try {
            mInternalMediaPlayer.currentPosition.toLong()
        } catch (e: IllegalStateException) {
            DebugLog.printStackTrace(e)
            0L
        }
    }

    override fun getDuration(): Long {
        return try {
            mInternalMediaPlayer.duration.toLong()
        } catch (e: IllegalStateException) {
            DebugLog.printStackTrace(e)
            0L
        }
    }

    override fun release() {
        mIsReleased = true
        mInternalMediaPlayer.release()
        releaseMediaDataSource()
        resetListeners()
        attachInternalListeners()
    }

    override fun reset() {
        try {
            mInternalMediaPlayer.reset()
        } catch (e: IllegalStateException) {
            DebugLog.printStackTrace(e)
        }
        releaseMediaDataSource()
        resetListeners()
        attachInternalListeners()
    }

    override fun setLooping(looping: Boolean) {
        mInternalMediaPlayer.isLooping = looping
    }

    override fun isLooping(): Boolean {
        return mInternalMediaPlayer.isLooping
    }

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        mInternalMediaPlayer.setVolume(leftVolume, rightVolume)
    }

    override fun getAudioSessionId(): Int {
        return mInternalMediaPlayer.audioSessionId
    }

    override fun getMediaInfo(): MediaInfo {
        if (sMediaInfo == null) {
            val module = MediaInfo()

            module.mVideoDecoder = "android"
            module.mVideoDecoderImpl = "HW"

            module.mAudioDecoder = "android"
            module.mAudioDecoderImpl = "HW"

            sMediaInfo = module
        }

        return sMediaInfo!!
    }

    override fun setLogEnabled(enable: Boolean) {
    }

    override fun isPlayable(): Boolean {
        return true
    }

    /*--------------------
     * misc
     */
    override fun setWakeMode(context: Context, mode: Int) {
        mInternalMediaPlayer.setWakeMode(context, mode)
    }

    override fun setAudioStreamType(streamtype: Int) {
        mInternalMediaPlayer.setAudioStreamType(streamtype)
    }

    override fun setKeepInBackground(keepInBackground: Boolean) {
    }

    /*--------------------
     * Listeners adapter
     */
    private fun attachInternalListeners() {
        mInternalMediaPlayer.setOnPreparedListener(mInternalListenerAdapter)
        mInternalMediaPlayer.setOnBufferingUpdateListener(mInternalListenerAdapter)
        mInternalMediaPlayer.setOnCompletionListener(mInternalListenerAdapter)
        mInternalMediaPlayer.setOnSeekCompleteListener(mInternalListenerAdapter)
        mInternalMediaPlayer.setOnVideoSizeChangedListener(mInternalListenerAdapter)
        mInternalMediaPlayer.setOnErrorListener(mInternalListenerAdapter)
        mInternalMediaPlayer.setOnInfoListener(mInternalListenerAdapter)
        mInternalMediaPlayer.setOnTimedTextListener(mInternalListenerAdapter)
    }

    private class AndroidMediaPlayerListenerHolder(mp: AndroidMediaPlayer) :
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnTimedTextListener {
        
        private val mWeakMediaPlayer: WeakReference<AndroidMediaPlayer> = WeakReference(mp)

        override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            val self = mWeakMediaPlayer.get()
            return self != null && self.notifyOnInfo(what, extra)
        }

        override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
            val self = mWeakMediaPlayer.get()
            return self != null && self.notifyOnError(what, extra)
        }

        override fun onVideoSizeChanged(mp: MediaPlayer, width: Int, height: Int) {
            val self = mWeakMediaPlayer.get() ?: return
            self.notifyOnVideoSizeChanged(width, height, 1, 1)
        }

        override fun onSeekComplete(mp: MediaPlayer) {
            val self = mWeakMediaPlayer.get() ?: return
            self.notifyOnSeekComplete()
        }

        override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
            val self = mWeakMediaPlayer.get() ?: return
            self.notifyOnBufferingUpdate(percent)
        }

        override fun onCompletion(mp: MediaPlayer) {
            val self = mWeakMediaPlayer.get() ?: return
            self.notifyOnCompletion()
        }

        override fun onPrepared(mp: MediaPlayer) {
            val self = mWeakMediaPlayer.get() ?: return
            self.notifyOnPrepared()
        }

        override fun onTimedText(mp: MediaPlayer, text: TimedText?) {
            val self = mWeakMediaPlayer.get() ?: return

            val ijkText = text?.let {
                IjkTimedText(it.bounds, it.text)
            }

            self.notifyOnTimedText(ijkText)
        }
    }
}
