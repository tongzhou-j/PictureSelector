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

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.ParcelFileDescriptor
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import tv.danmaku.ijk.media.player.annotations.AccessedByNative
import tv.danmaku.ijk.media.player.annotations.CalledByNative
import tv.danmaku.ijk.media.player.misc.IAndroidIO
import tv.danmaku.ijk.media.player.misc.IMediaDataSource
import tv.danmaku.ijk.media.player.misc.ITrackInfo
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo
import tv.danmaku.ijk.media.player.pragma.DebugLog
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.security.InvalidParameterException
import java.util.ArrayList
import java.util.Locale
import java.util.Map

/**
 * @author bbcallen
 *
 *         Java wrapper of ffplay.
 */
class IjkMediaPlayer @JvmOverloads constructor(libLoader: IjkLibLoader? = null) : AbstractMediaPlayer() {
    companion object {
        private val TAG = IjkMediaPlayer::class.java.name

        private const val MEDIA_NOP = 0 // interface test message
        private const val MEDIA_PREPARED = 1
        private const val MEDIA_PLAYBACK_COMPLETE = 2
        private const val MEDIA_BUFFERING_UPDATE = 3
        private const val MEDIA_SEEK_COMPLETE = 4
        private const val MEDIA_SET_VIDEO_SIZE = 5
        private const val MEDIA_TIMED_TEXT = 99
        private const val MEDIA_ERROR = 100
        private const val MEDIA_INFO = 200

        protected const val MEDIA_SET_VIDEO_SAR = 10001

        //----------------------------------------
        // options
        const val IJK_LOG_UNKNOWN = 0
        const val IJK_LOG_DEFAULT = 1

        const val IJK_LOG_VERBOSE = 2
        const val IJK_LOG_DEBUG = 3
        const val IJK_LOG_INFO = 4
        const val IJK_LOG_WARN = 5
        const val IJK_LOG_ERROR = 6
        const val IJK_LOG_FATAL = 7
        const val IJK_LOG_SILENT = 8

        const val OPT_CATEGORY_FORMAT = 1
        const val OPT_CATEGORY_CODEC = 2
        const val OPT_CATEGORY_SWS = 3
        const val OPT_CATEGORY_PLAYER = 4

        const val SDL_FCC_YV12 = 0x32315659 // YV12
        const val SDL_FCC_RV16 = 0x36315652 // RGB565
        const val SDL_FCC_RV32 = 0x32335652 // RGBX8888
        //----------------------------------------

        //----------------------------------------
        // properties
        const val PROP_FLOAT_VIDEO_DECODE_FRAMES_PER_SECOND = 10001
        const val PROP_FLOAT_VIDEO_OUTPUT_FRAMES_PER_SECOND = 10002
        const val FFP_PROP_FLOAT_PLAYBACK_RATE = 10003
        const val FFP_PROP_FLOAT_DROP_FRAME_RATE = 10007

        const val FFP_PROP_INT64_SELECTED_VIDEO_STREAM = 20001
        const val FFP_PROP_INT64_SELECTED_AUDIO_STREAM = 20002
        const val FFP_PROP_INT64_SELECTED_TIMEDTEXT_STREAM = 20011

        const val FFP_PROP_INT64_VIDEO_DECODER = 20003
        const val FFP_PROP_INT64_AUDIO_DECODER = 20004
        const val FFP_PROPV_DECODER_UNKNOWN = 0
        const val FFP_PROPV_DECODER_AVCODEC = 1
        const val FFP_PROPV_DECODER_MEDIACODEC = 2
        const val FFP_PROPV_DECODER_VIDEOTOOLBOX = 3
        const val FFP_PROP_INT64_VIDEO_CACHED_DURATION = 20005
        const val FFP_PROP_INT64_AUDIO_CACHED_DURATION = 20006
        const val FFP_PROP_INT64_VIDEO_CACHED_BYTES = 20007
        const val FFP_PROP_INT64_AUDIO_CACHED_BYTES = 20008
        const val FFP_PROP_INT64_VIDEO_CACHED_PACKETS = 20009
        const val FFP_PROP_INT64_AUDIO_CACHED_PACKETS = 20010
        const val FFP_PROP_INT64_ASYNC_STATISTIC_BUF_BACKWARDS = 20201
        const val FFP_PROP_INT64_ASYNC_STATISTIC_BUF_FORWARDS = 20202
        const val FFP_PROP_INT64_ASYNC_STATISTIC_BUF_CAPACITY = 20203
        const val FFP_PROP_INT64_TRAFFIC_STATISTIC_BYTE_COUNT = 20204
        const val FFP_PROP_INT64_CACHE_STATISTIC_PHYSICAL_POS = 20205
        const val FFP_PROP_INT64_CACHE_STATISTIC_FILE_FORWARDS = 20206
        const val FFP_PROP_INT64_CACHE_STATISTIC_FILE_POS = 20207
        const val FFP_PROP_INT64_CACHE_STATISTIC_COUNT_BYTES = 20208
        const val FFP_PROP_INT64_LOGICAL_FILE_SIZE = 20209
        const val FFP_PROP_INT64_SHARE_CACHE_DATA = 20210
        const val FFP_PROP_INT64_BIT_RATE = 20100
        const val FFP_PROP_INT64_TCP_SPEED = 20200
        const val FFP_PROP_INT64_LATEST_SEEK_LOAD_DURATION = 20300
        const val FFP_PROP_INT64_IMMEDIATE_RECONNECT = 20211
        //----------------------------------------

        /**
         * Default library loader
         * Load them by yourself, if your libraries are not installed at default place.
         */
        private val sLocalLibLoader = object : IjkLibLoader {
            override fun loadLibrary(libName: String) {
                System.loadLibrary(libName)
            }
        }

        @Volatile
        private var mIsLibLoaded = false

        @JvmStatic
        fun loadLibrariesOnce(libLoader: IjkLibLoader?) {
            synchronized(IjkMediaPlayer::class.java) {
                if (!mIsLibLoaded) {
                    val loader = libLoader ?: sLocalLibLoader

                    loader.loadLibrary("ijkffmpeg")
                    loader.loadLibrary("ijksdl")
                    loader.loadLibrary("ijkplayer")
                    mIsLibLoaded = true
                }
            }
        }

        @Volatile
        private var mIsNativeInitialized = false

        private fun initNativeOnce() {
            synchronized(IjkMediaPlayer::class.java) {
                if (!mIsNativeInitialized) {
                    native_init()
                    mIsNativeInitialized = true
                }
            }
        }

        @JvmStatic
        external fun native_profileBegin(libName: String)

        @JvmStatic
        external fun native_profileEnd()

        @JvmStatic
        external fun native_setLogLevel(level: Int)

        @JvmStatic
        external fun _getColorFormatName(mediaCodecColorFormat: Int): String

        private external fun native_init()

        /*
         * Called from native code when an interesting event happens. This method
         * just uses the EventHandler system to post the event back to the main app
         * thread. We use a weak reference to the original IjkMediaPlayer object so
         * that the native code is safe from the object disappearing from underneath
         * it. (This is the cookie passed to native_setup().)
         */
        @CalledByNative
        @JvmStatic
        private fun postEventFromNative(
            weakThiz: Any?,
            what: Int,
            arg1: Int,
            arg2: Int,
            obj: Any?
        ) {
            if (weakThiz == null) return

            @Suppress("UNCHECKED_CAST")
            val mp = (weakThiz as WeakReference<*>).get() as? IjkMediaPlayer ?: return

            if (what == MEDIA_INFO && arg1 == IMediaPlayer.MEDIA_INFO_STARTED_AS_NEXT) {
                // this acquires the wakelock if needed, and sets the client side
                // state
                mp.start()
            }
            mp.mEventHandler?.let {
                val m = it.obtainMessage(what, arg1, arg2, obj)
                it.sendMessage(m)
            }
        }


        @CalledByNative
        @JvmStatic
        private fun onSelectCodec(weakThiz: Any?, mimeType: String?, profile: Int, level: Int): String? {
            if (weakThiz == null || weakThiz !is WeakReference<*>) {
                return null
            }

            @Suppress("UNCHECKED_CAST")
            val weakPlayer = weakThiz as WeakReference<IjkMediaPlayer>
            val player = weakPlayer.get() ?: return null

            var listener = player.mOnMediaCodecSelectListener
            if (listener == null) {
                listener = DefaultMediaCodecSelector.sInstance
            }

            return listener.onMediaCodecSelect(player, mimeType ?: "", profile, level)
        }
    }

    init {
        initPlayer(libLoader)
    }

    @AccessedByNative
    private var mNativeMediaPlayer: Long = 0

    @AccessedByNative
    private var mNativeMediaDataSource: Long = 0

    @AccessedByNative
    private var mNativeAndroidIO: Long = 0

    @AccessedByNative
    private var mNativeSurfaceTexture: Int = 0

    @AccessedByNative
    private var mListenerContext: Int = 0

    private var mSurfaceHolder: SurfaceHolder? = null
    private var mEventHandler: EventHandler? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var mScreenOnWhilePlaying: Boolean = false
    private var mStayAwake: Boolean = false

    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0
    private var mVideoSarNum: Int = 0
    private var mVideoSarDen: Int = 0

    private var mDataSource: String? = null

    private fun initPlayer(libLoader: IjkLibLoader?) {
        loadLibrariesOnce(libLoader)
        initNativeOnce()

        val looper: Looper? = Looper.myLooper() ?: Looper.getMainLooper()
        mEventHandler = if (looper != null) {
            EventHandler(this, looper)
        } else {
            null
        }

        /*
         * Native setup requires a weak reference to our object. It's easier to
         * create it here than in C++.
         */
        native_setup(WeakReference(this))
    }

    private external fun _setFrameAtTime(
        imgCachePath: String,
        startTime: Long,
        endTime: Long,
        num: Int,
        imgDefinition: Int
    )

    /*
     * Update the IjkMediaPlayer SurfaceTexture. Call after setting a new
     * display surface.
     */
    private external fun _setVideoSurface(surface: Surface?)

    /**
     * Sets the {@link SurfaceHolder} to use for displaying the video portion of
     * the media.
     *
     * Either a surface holder or surface must be set if a display or video sink
     * is needed. Not calling this method or {@link #setSurface(Surface)} when
     * playing back a video will result in only the audio track being played. A
     * null surface holder or surface will result in only the audio track being
     * played.
     *
     * @param sh
     *            the SurfaceHolder to use for video display
     */
    override fun setDisplay(sh: SurfaceHolder?) {
        mSurfaceHolder = sh
        val surface: Surface? = sh?.surface
        _setVideoSurface(surface)
        updateSurfaceScreenOn()
    }

    /**
     * Sets the {@link Surface} to be used as the sink for the video portion of
     * the media. This is similar to {@link #setDisplay(SurfaceHolder)}, but
     * does not support {@link #setScreenOnWhilePlaying(boolean)}. Setting a
     * Surface will un-set any Surface or SurfaceHolder that was previously set.
     * A null surface will result in only the audio track being played.
     *
     * If the Surface sends frames to a {@link SurfaceTexture}, the timestamps
     * returned from {@link SurfaceTexture#getTimestamp()} will have an
     * unspecified zero point. These timestamps cannot be directly compared
     * between different media sources, different instances of the same media
     * source, or multiple runs of the same program. The timestamp is normally
     * monotonically increasing and is unaffected by time-of-day adjustments,
     * but it is reset when the position is set.
     *
     * @param surface
     *            The {@link Surface} to be used for the video portion of the
     *            media.
     */
    override fun setSurface(surface: Surface?) {
        if (mScreenOnWhilePlaying && surface != null) {
            DebugLog.w(
                TAG,
                "setScreenOnWhilePlaying(true) is ineffective for Surface"
            )
        }
        mSurfaceHolder = null
        _setVideoSurface(surface)
        updateSurfaceScreenOn()
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri the Content URI of the data you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    override fun setDataSource(context: Context, uri: Uri) {
        setDataSource(context, uri, null)
    }

    /**
     * Sets the data source as a content Uri.
     *
     * @param context the Context to use when resolving the Uri
     * @param uri the Content URI of the data you want to play
     * @param headers the headers to be sent together with the request for the data
     *                Note that the cross domain redirection is allowed by default, but that can be
     *                changed with key/value pairs through the headers parameter with
     *                "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     *                to disallow or allow cross domain redirection.
     * @throws IllegalStateException if it is called in an invalid state
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    override fun setDataSource(
        context: Context,
        uri: Uri,
        headers: java.util.Map<String, String>?
    ) {
        val scheme = uri.scheme
        if (ContentResolver.SCHEME_FILE == scheme) {
            setDataSource(uri.path ?: "")
            return
        } else if (ContentResolver.SCHEME_CONTENT == scheme
            && Settings.AUTHORITY == uri.authority
        ) {
            // Redirect ringtones to go directly to underlying provider
            val actualUri = RingtoneManager.getActualDefaultRingtoneUri(
                context,
                RingtoneManager.getDefaultType(uri)
            )
            if (actualUri == null) {
                throw FileNotFoundException("Failed to resolve default ringtone")
            }
            setDataSource(context, actualUri, headers)
            return
        }

        var fd: AssetFileDescriptor? = null
        try {
            val resolver = context.contentResolver
            fd = resolver.openAssetFileDescriptor(uri, "r")
            if (fd == null) {
                return
            }
            // Note: using getDeclaredLength so that our behavior is the same
            // as previous versions when the content provider is returning
            // a full file.
            if (fd.declaredLength < 0) {
                setDataSource(fd.fileDescriptor)
            } else {
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.declaredLength)
            }
            return
        } catch (ignored: SecurityException) {
        } catch (ignored: IOException) {
        } finally {
            fd?.close()
        }

        Log.d(TAG, "Couldn't open file on client side, trying server side")

        setDataSource(uri.toString(), headers)
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     *
     * @param path
     *            the path of the file, or the http/rtsp URL of the stream you
     *            want to play
     * @throws IllegalStateException
     *             if it is called in an invalid state
     *
     *             <p>
     *             When <code>path</code> refers to a local file, the file may
     *             actually be opened by a process other than the calling
     *             application. This implies that the pathname should be an
     *             absolute path (as any other process runs with unspecified
     *             current working directory), and that the pathname should
     *             reference a world-readable file.
     */
    override fun setDataSource(path: String) {
        mDataSource = path
        _setDataSource(path, null, null)
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     *
     * @param path the path of the file, or the http/rtsp URL of the stream you want to play
     * @param headers the headers associated with the http request for the stream you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    fun setDataSource(path: String, headers: java.util.Map<String, String>?) {
        if (headers != null && !headers.isEmpty()) {
            val sb = StringBuilder()
            for (entry in headers.entrySet()) {
                sb.append(entry.key)
                sb.append(":")
                val value = entry.value
                if (!value.isNullOrEmpty()) {
                    sb.append(value)
                }
                sb.append("\r\n")
                setOption(OPT_CATEGORY_FORMAT, "headers", sb.toString())
                setOption(
                    OPT_CATEGORY_FORMAT, "protocol_whitelist",
                    "async,cache,crypto,file,http,https,ijkhttphook,ijkinject,ijklivehook,ijklongurl,ijksegment,ijktcphook,pipe,rtp,tcp,tls,udp,ijkurlhook,data"
                )
            }
        }
        setDataSource(path)
    }

    /**
     * Sets the data source (FileDescriptor) to use. It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd the FileDescriptor for the file you want to play
     * @throws IllegalStateException if it is called in an invalid state
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    override fun setDataSource(fd: FileDescriptor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
            var native_fd = -1
            try {
                val f = fd.javaClass.getDeclaredField("descriptor") //NoSuchFieldException
                f.isAccessible = true
                native_fd = f.getInt(fd) //IllegalAccessException
            } catch (e: NoSuchFieldException) {
                throw RuntimeException(e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
            _setDataSourceFd(native_fd)
        } else {
            val pfd = ParcelFileDescriptor.dup(fd)
            try {
                _setDataSourceFd(pfd.fd)
            } finally {
                pfd.close()
            }
        }
    }

    /**
     * Sets the data source (FileDescriptor) to use.  The FileDescriptor must be
     * seekable (N.B. a LocalSocket is not seekable). It is the caller's responsibility
     * to close the file descriptor. It is safe to do so as soon as this call returns.
     *
     * @param fd the FileDescriptor for the file you want to play
     * @param offset the offset into the file where the data to be played starts, in bytes
     * @param length the length in bytes of the data to be played
     * @throws IllegalStateException if it is called in an invalid state
     */
    private fun setDataSource(fd: FileDescriptor, offset: Long, length: Long) {
        // FIXME: handle offset, length
        setDataSource(fd)
    }

    override fun setDataSource(mediaDataSource: IMediaDataSource?) {
        if (mediaDataSource == null) {
            throw IllegalArgumentException("mediaDataSource cannot be null")
        }
        _setDataSource(mediaDataSource)
    }

    fun setAndroidIOCallback(androidIO: IAndroidIO) {
        _setAndroidIOCallback(androidIO)
    }

    private external fun _setDataSource(
        path: String,
        keys: Array<String>?,
        values: Array<String>?
    )

    private external fun _setDataSourceFd(fd: Int)

    private external fun _setDataSource(mediaDataSource: IMediaDataSource)

    private external fun _setAndroidIOCallback(androidIO: IAndroidIO)

    override fun getDataSource(): String? {
        return mDataSource
    }

    override fun prepareAsync() {
        _prepareAsync()
    }

    external fun _prepareAsync()

    override fun start() {
        stayAwake(true)
        _start()
    }

    private external fun _start()

    override fun stop() {
        stayAwake(false)
        _stop()
    }

    private external fun _stop()

    override fun pause() {
        stayAwake(false)
        _pause()
    }

    private external fun _pause()

    @SuppressLint("Wakelock")
    override fun setWakeMode(context: Context, mode: Int) {
        var washeld = false
        if (mWakeLock != null) {
            if (mWakeLock!!.isHeld) {
                washeld = true
                mWakeLock!!.release()
            }
            mWakeLock = null
        }

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(
            mode or PowerManager.ON_AFTER_RELEASE,
            IjkMediaPlayer::class.java.name
        )
        mWakeLock!!.setReferenceCounted(false)
        if (washeld) {
            mWakeLock!!.acquire()
        }
    }

    override fun setScreenOnWhilePlaying(screenOn: Boolean) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                DebugLog.w(
                    TAG,
                    "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder"
                )
            }
            mScreenOnWhilePlaying = screenOn
            updateSurfaceScreenOn()
        }
    }

    @SuppressLint("Wakelock")
    private fun stayAwake(awake: Boolean) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock!!.isHeld) {
                mWakeLock!!.acquire()
            } else if (!awake && mWakeLock!!.isHeld) {
                mWakeLock!!.release()
            }
        }
        mStayAwake = awake
        updateSurfaceScreenOn()
    }

    private fun updateSurfaceScreenOn() {
        mSurfaceHolder?.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake)
    }

    override fun getTrackInfo(): Array<ITrackInfo>? {
        val bundle = getMediaMeta()
        if (bundle == null) return null

        val mediaMeta = IjkMediaMeta.parse(bundle)
        if (mediaMeta == null || mediaMeta.mStreams == null) return null

        val trackInfos = ArrayList<IjkTrackInfo>()
        for (streamMeta in mediaMeta.mStreams) {
            val trackInfo = IjkTrackInfo(streamMeta)
            when {
                streamMeta.mType.equals(IjkMediaMeta.IJKM_VAL_TYPE__VIDEO, ignoreCase = true) -> {
                    trackInfo.setTrackType(ITrackInfo.MEDIA_TRACK_TYPE_VIDEO)
                }
                streamMeta.mType.equals(IjkMediaMeta.IJKM_VAL_TYPE__AUDIO, ignoreCase = true) -> {
                    trackInfo.setTrackType(ITrackInfo.MEDIA_TRACK_TYPE_AUDIO)
                }
                streamMeta.mType.equals(IjkMediaMeta.IJKM_VAL_TYPE__TIMEDTEXT, ignoreCase = true) -> {
                    trackInfo.setTrackType(ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT)
                }
            }
            trackInfos.add(trackInfo)
        }

        return trackInfos.toArray(arrayOfNulls<IjkTrackInfo>(trackInfos.size)) as Array<ITrackInfo>
    }

    // TODO: @Override
    fun getSelectedTrack(trackType: Int): Int {
        return when (trackType) {
            ITrackInfo.MEDIA_TRACK_TYPE_VIDEO -> _getPropertyLong(
                FFP_PROP_INT64_SELECTED_VIDEO_STREAM,
                -1
            ).toInt()
            ITrackInfo.MEDIA_TRACK_TYPE_AUDIO -> _getPropertyLong(
                FFP_PROP_INT64_SELECTED_AUDIO_STREAM,
                -1
            ).toInt()
            ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT -> _getPropertyLong(
                FFP_PROP_INT64_SELECTED_TIMEDTEXT_STREAM,
                -1
            ).toInt()
            else -> -1
        }
    }

    // experimental, should set DEFAULT_MIN_FRAMES and MAX_MIN_FRAMES to 25
    // TODO: @Override
    fun selectTrack(track: Int) {
        _setStreamSelected(track, true)
    }

    // experimental, should set DEFAULT_MIN_FRAMES and MAX_MIN_FRAMES to 25
    // TODO: @Override
    fun deselectTrack(track: Int) {
        _setStreamSelected(track, false)
    }

    private external fun _setStreamSelected(stream: Int, select: Boolean)

    override fun getVideoWidth(): Int {
        return mVideoWidth
    }

    override fun getVideoHeight(): Int {
        return mVideoHeight
    }

    override fun getVideoSarNum(): Int {
        return mVideoSarNum
    }

    override fun getVideoSarDen(): Int {
        return mVideoSarDen
    }

    override external fun isPlaying(): Boolean

    override external fun seekTo(msec: Long)

    override external fun getCurrentPosition(): Long

    override external fun getDuration(): Long

    /**
     * Releases resources associated with this IjkMediaPlayer object. It is
     * considered good practice to call this method when you're done using the
     * IjkMediaPlayer. In particular, whenever an Activity of an application is
     * paused (its onPause() method is called), or stopped (its onStop() method
     * is called), this method should be invoked to release the IjkMediaPlayer
     * object, unless the application has a special need to keep the object
     * around. In addition to unnecessary resources (such as memory and
     * instances of codecs) being held, failure to call this method immediately
     * if a IjkMediaPlayer object is no longer needed may also lead to
     * continuous battery consumption for mobile devices, and playback failure
     * for other applications if no multiple instances of the same codec are
     * supported on a device. Even if multiple instances of the same codec are
     * supported, some performance degradation may be expected when unnecessary
     * multiple instances are used at the same time.
     */
    override fun release() {
        stayAwake(false)
        updateSurfaceScreenOn()
        resetListeners()
        _release()
    }

    private external fun _release()

    override fun reset() {
        stayAwake(false)
        _reset()
        // make sure none of the listeners get called anymore
        mEventHandler?.removeCallbacksAndMessages(null)

        mVideoWidth = 0
        mVideoHeight = 0
    }

    private external fun _reset()

    /**
     * Sets the player to be looping or non-looping.
     *
     * @param looping whether to loop or not
     */
    override fun setLooping(looping: Boolean) {
        val loopCount = if (looping) 0 else 1
        setOption(OPT_CATEGORY_PLAYER, "loop", loopCount.toLong())
        _setLoopCount(loopCount)
    }

    private external fun _setLoopCount(loopCount: Int)

    /**
     * Checks whether the MediaPlayer is looping or non-looping.
     *
     * @return true if the MediaPlayer is currently looping, false otherwise
     */
    override fun isLooping(): Boolean {
        val loopCount = _getLoopCount()
        return loopCount != 1
    }

    private external fun _getLoopCount(): Int

    fun setSpeed(speed: Float) {
        _setPropertyFloat(FFP_PROP_FLOAT_PLAYBACK_RATE, speed)
    }

    fun getSpeed(speed: Float): Float {
        return _getPropertyFloat(FFP_PROP_FLOAT_PLAYBACK_RATE, 0.0f)
    }

    fun getVideoDecoder(): Int {
        return _getPropertyLong(FFP_PROP_INT64_VIDEO_DECODER, FFP_PROPV_DECODER_UNKNOWN.toLong()).toInt()
    }

    fun getVideoOutputFramesPerSecond(): Float {
        return _getPropertyFloat(PROP_FLOAT_VIDEO_OUTPUT_FRAMES_PER_SECOND, 0.0f)
    }

    fun getVideoDecodeFramesPerSecond(): Float {
        return _getPropertyFloat(PROP_FLOAT_VIDEO_DECODE_FRAMES_PER_SECOND, 0.0f)
    }

    fun getVideoCachedDuration(): Long {
        return _getPropertyLong(FFP_PROP_INT64_VIDEO_CACHED_DURATION, 0)
    }

    fun getAudioCachedDuration(): Long {
        return _getPropertyLong(FFP_PROP_INT64_AUDIO_CACHED_DURATION, 0)
    }

    fun getVideoCachedBytes(): Long {
        return _getPropertyLong(FFP_PROP_INT64_VIDEO_CACHED_BYTES, 0)
    }

    fun getAudioCachedBytes(): Long {
        return _getPropertyLong(FFP_PROP_INT64_AUDIO_CACHED_BYTES, 0)
    }

    fun getVideoCachedPackets(): Long {
        return _getPropertyLong(FFP_PROP_INT64_VIDEO_CACHED_PACKETS, 0)
    }

    fun getAudioCachedPackets(): Long {
        return _getPropertyLong(FFP_PROP_INT64_AUDIO_CACHED_PACKETS, 0)
    }

    fun getAsyncStatisticBufBackwards(): Long {
        return _getPropertyLong(FFP_PROP_INT64_ASYNC_STATISTIC_BUF_BACKWARDS, 0)
    }

    fun getAsyncStatisticBufForwards(): Long {
        return _getPropertyLong(FFP_PROP_INT64_ASYNC_STATISTIC_BUF_FORWARDS, 0)
    }

    fun getAsyncStatisticBufCapacity(): Long {
        return _getPropertyLong(FFP_PROP_INT64_ASYNC_STATISTIC_BUF_CAPACITY, 0)
    }

    fun getTrafficStatisticByteCount(): Long {
        return _getPropertyLong(FFP_PROP_INT64_TRAFFIC_STATISTIC_BYTE_COUNT, 0)
    }

    fun getCacheStatisticPhysicalPos(): Long {
        return _getPropertyLong(FFP_PROP_INT64_CACHE_STATISTIC_PHYSICAL_POS, 0)
    }

    fun getCacheStatisticFileForwards(): Long {
        return _getPropertyLong(FFP_PROP_INT64_CACHE_STATISTIC_FILE_FORWARDS, 0)
    }

    fun getCacheStatisticFilePos(): Long {
        return _getPropertyLong(FFP_PROP_INT64_CACHE_STATISTIC_FILE_POS, 0)
    }

    fun getCacheStatisticCountBytes(): Long {
        return _getPropertyLong(FFP_PROP_INT64_CACHE_STATISTIC_COUNT_BYTES, 0)
    }

    fun getFileSize(): Long {
        return _getPropertyLong(FFP_PROP_INT64_LOGICAL_FILE_SIZE, 0)
    }

    fun getBitRate(): Long {
        return _getPropertyLong(FFP_PROP_INT64_BIT_RATE, 0)
    }

    fun getTcpSpeed(): Long {
        return _getPropertyLong(FFP_PROP_INT64_TCP_SPEED, 0)
    }

    fun getSeekLoadDuration(): Long {
        return _getPropertyLong(FFP_PROP_INT64_LATEST_SEEK_LOAD_DURATION, 0)
    }

    private external fun _getPropertyFloat(property: Int, defaultValue: Float): Float
    private external fun _setPropertyFloat(property: Int, value: Float)
    private external fun _getPropertyLong(property: Int, defaultValue: Long): Long
    private external fun _setPropertyLong(property: Int, value: Long)

    fun getDropFrameRate(): Float {
        return _getPropertyFloat(FFP_PROP_FLOAT_DROP_FRAME_RATE, 0.0f)
    }

    override external fun setVolume(leftVolume: Float, rightVolume: Float)

    override external fun getAudioSessionId(): Int

    override fun getMediaInfo(): MediaInfo {
        val mediaInfo = MediaInfo()
        mediaInfo.mMediaPlayerName = "ijkplayer"

        val videoCodecInfo = _getVideoCodecInfo()
        if (!videoCodecInfo.isNullOrEmpty()) {
            val nodes = videoCodecInfo.split(",")
            when {
                nodes.size >= 2 -> {
                    mediaInfo.mVideoDecoder = nodes[0]
                    mediaInfo.mVideoDecoderImpl = nodes[1]
                }
                nodes.size >= 1 -> {
                    mediaInfo.mVideoDecoder = nodes[0]
                    mediaInfo.mVideoDecoderImpl = ""
                }
            }
        }

        val audioCodecInfo = _getAudioCodecInfo()
        if (!audioCodecInfo.isNullOrEmpty()) {
            val nodes = audioCodecInfo.split(",")
            when {
                nodes.size >= 2 -> {
                    mediaInfo.mAudioDecoder = nodes[0]
                    mediaInfo.mAudioDecoderImpl = nodes[1]
                }
                nodes.size >= 1 -> {
                    mediaInfo.mAudioDecoder = nodes[0]
                    mediaInfo.mAudioDecoderImpl = ""
                }
            }
        }

        try {
            mediaInfo.mMeta = IjkMediaMeta.parse(_getMediaMeta())
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return mediaInfo
    }

    override fun setLogEnabled(enable: Boolean) {
        // do nothing
    }

    override fun isPlayable(): Boolean {
        return true
    }

    private external fun _getVideoCodecInfo(): String
    private external fun _getAudioCodecInfo(): String

    fun setOption(category: Int, name: String, value: String) {
        _setOption(category, name, value)
    }

    fun setOption(category: Int, name: String, value: Long) {
        _setOption(category, name, value)
    }

    private external fun _setOption(category: Int, name: String, value: String)
    private external fun _setOption(category: Int, name: String, value: Long)

    fun getMediaMeta(): Bundle? {
        return _getMediaMeta()
    }

    private external fun _getMediaMeta(): Bundle?

    fun getColorFormatName(mediaCodecColorFormat: Int): String {
        return _getColorFormatName(mediaCodecColorFormat)
    }

    override fun setAudioStreamType(streamtype: Int) {
        // do nothing
    }

    override fun setKeepInBackground(keepInBackground: Boolean) {
        // do nothing
    }

    private external fun native_setup(IjkMediaPlayer_this: Any)

    private external fun native_finalize()

    private external fun native_message_loop(IjkMediaPlayer_this: Any)

    @Throws(Throwable::class)
    protected open fun finalize() {
        try {
            // Kotlin 中不需要调用 super.finalize()
        } finally {
            native_finalize()
        }
    }

    fun httphookReconnect() {
        _setPropertyLong(FFP_PROP_INT64_IMMEDIATE_RECONNECT, 1)
    }

    fun setCacheShare(share: Int) {
        _setPropertyLong(FFP_PROP_INT64_SHARE_CACHE_DATA, share.toLong())
    }

    private class EventHandler(
        mp: IjkMediaPlayer,
        looper: Looper
    ) : Handler(looper) {
        private val mWeakPlayer: WeakReference<IjkMediaPlayer> = WeakReference(mp)

        override fun handleMessage(msg: Message) {
            val player = mWeakPlayer.get()
            if (player == null || player.mNativeMediaPlayer == 0L) {
                DebugLog.w(
                    TAG,
                    "IjkMediaPlayer went away with unhandled events"
                )
                return
            }

            when (msg.what) {
                MEDIA_PREPARED -> {
                    player.notifyOnPrepared()
                    return
                }

                MEDIA_PLAYBACK_COMPLETE -> {
                    player.stayAwake(false)
                    player.notifyOnCompletion()
                    return
                }

                MEDIA_BUFFERING_UPDATE -> {
                    var bufferPosition = msg.arg1.toLong()
                    if (bufferPosition < 0) {
                        bufferPosition = 0
                    }

                    var percent: Long = 0
                    val duration = player.getDuration()
                    if (duration > 0) {
                        percent = bufferPosition * 100 / duration
                    }
                    if (percent >= 100) {
                        percent = 100
                    }

                    // DebugLog.efmt(TAG, "Buffer (%d%%) %d/%d",  percent, bufferPosition, duration);
                    player.notifyOnBufferingUpdate(percent.toInt())
                    return
                }

                MEDIA_SEEK_COMPLETE -> {
                    player.notifyOnSeekComplete()
                    return
                }

                MEDIA_SET_VIDEO_SIZE -> {
                    player.mVideoWidth = msg.arg1
                    player.mVideoHeight = msg.arg2
                    player.notifyOnVideoSizeChanged(
                        player.mVideoWidth, player.mVideoHeight,
                        player.mVideoSarNum, player.mVideoSarDen
                    )
                    return
                }

                MEDIA_ERROR -> {
                    DebugLog.e(TAG, "Error (${msg.arg1},${msg.arg2})")
                    if (!player.notifyOnError(msg.arg1, msg.arg2)) {
                        player.notifyOnCompletion()
                    }
                    player.stayAwake(false)
                    return
                }

                MEDIA_INFO -> {
                    when (msg.arg1) {
                        IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                            DebugLog.i(TAG, "Info: MEDIA_INFO_VIDEO_RENDERING_START\n")
                        }
                    }
                    player.notifyOnInfo(msg.arg1, msg.arg2)
                    // No real default action so far.
                    return
                }

                MEDIA_TIMED_TEXT -> {
                    if (msg.obj == null) {
                        player.notifyOnTimedText(null)
                    } else {
                        val text = IjkTimedText(Rect(0, 0, 1, 1), msg.obj as String)
                        player.notifyOnTimedText(text)
                    }
                    return
                }

                MEDIA_NOP -> { // interface test message - ignore
                }

                MEDIA_SET_VIDEO_SAR -> {
                    player.mVideoSarNum = msg.arg1
                    player.mVideoSarDen = msg.arg2
                    player.notifyOnVideoSizeChanged(
                        player.mVideoWidth, player.mVideoHeight,
                        player.mVideoSarNum, player.mVideoSarDen
                    )
                }

                else -> {
                    DebugLog.e(TAG, "Unknown message type ${msg.what}")
                }
            }
        }
    }


    /*
     * ControlMessage
     */

    private var mOnControlMessageListener: OnControlMessageListener? = null
    fun setOnControlMessageListener(listener: OnControlMessageListener?) {
        mOnControlMessageListener = listener
    }

    interface OnControlMessageListener {
        fun onControlResolveSegmentUrl(segment: Int): String
    }

    /*
     * NativeInvoke
     */

    private var mOnNativeInvokeListener: OnNativeInvokeListener? = null
    fun setOnNativeInvokeListener(listener: OnNativeInvokeListener?) {
        mOnNativeInvokeListener = listener
    }

    interface OnNativeInvokeListener {
        val CTRL_WILL_TCP_OPEN: Int
            get() = 0x20001 // NO ARGS
        val CTRL_DID_TCP_OPEN: Int
            get() = 0x20002 // ARG_ERROR, ARG_FAMILIY, ARG_IP, ARG_PORT, ARG_FD

        val CTRL_WILL_HTTP_OPEN: Int
            get() = 0x20003 // ARG_URL, ARG_SEGMENT_INDEX, ARG_RETRY_COUNTER
        val CTRL_WILL_LIVE_OPEN: Int
            get() = 0x20005 // ARG_URL, ARG_RETRY_COUNTER
        val CTRL_WILL_CONCAT_RESOLVE_SEGMENT: Int
            get() = 0x20007 // ARG_URL, ARG_SEGMENT_INDEX, ARG_RETRY_COUNTER

        val EVENT_WILL_HTTP_OPEN: Int
            get() = 0x1 // ARG_URL
        val EVENT_DID_HTTP_OPEN: Int
            get() = 0x2 // ARG_URL, ARG_ERROR, ARG_HTTP_CODE
        val EVENT_WILL_HTTP_SEEK: Int
            get() = 0x3 // ARG_URL, ARG_OFFSET
        val EVENT_DID_HTTP_SEEK: Int
            get() = 0x4 // ARG_URL, ARG_OFFSET, ARG_ERROR, ARG_HTTP_CODE, ARG_FILE_SIZE

        val ARG_URL: String
            get() = "url"
        val ARG_SEGMENT_INDEX: String
            get() = "segment_index"
        val ARG_RETRY_COUNTER: String
            get() = "retry_counter"

        val ARG_ERROR: String
            get() = "error"
        val ARG_FAMILIY: String
            get() = "family"
        val ARG_IP: String
            get() = "ip"
        val ARG_PORT: String
            get() = "port"
        val ARG_FD: String
            get() = "fd"

        val ARG_OFFSET: String
            get() = "offset"
        val ARG_HTTP_CODE: String
            get() = "http_code"
        val ARG_FILE_SIZE: String
            get() = "file_size"

        /*
         * @return true if invoke is handled
         * @throws Exception on any error
         */
        fun onNativeInvoke(what: Int, args: Bundle): Boolean
    }


    /*
     * MediaCodec select
     */

    interface OnMediaCodecSelectListener {
        fun onMediaCodecSelect(mp: IMediaPlayer, mimeType: String, profile: Int, level: Int): String?
    }

    private var mOnMediaCodecSelectListener: OnMediaCodecSelectListener? = null
    fun setOnMediaCodecSelectListener(listener: OnMediaCodecSelectListener?) {
        mOnMediaCodecSelectListener = listener
    }

    override fun resetListeners() {
        super.resetListeners()
        mOnMediaCodecSelectListener = null
    }


    class DefaultMediaCodecSelector : OnMediaCodecSelectListener {
        companion object {
            @JvmStatic
            val sInstance = DefaultMediaCodecSelector()
        }

        @Suppress("deprecation")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun onMediaCodecSelect(mp: IMediaPlayer, mimeType: String, profile: Int, level: Int): String? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                return null
            }

            if (mimeType.isNullOrEmpty()) {
                return null
            }

            Log.i(TAG, String.format(Locale.US, "onSelectCodec: mime=%s, profile=%d, level=%d", mimeType, profile, level))
            val candidateCodecList = ArrayList<IjkMediaCodecInfo>()
            val numCodecs = MediaCodecList.getCodecCount()
            for (i in 0 until numCodecs) {
                val codecInfo = MediaCodecList.getCodecInfoAt(i)
                Log.d(TAG, String.format(Locale.US, "  found codec: %s", codecInfo.name))
                if (codecInfo.isEncoder) {
                    continue
                }

                val types = codecInfo.supportedTypes
                if (types == null) {
                    continue
                }

                for (type in types) {
                    if (type.isNullOrEmpty()) {
                        continue
                    }

                    Log.d(TAG, String.format(Locale.US, "    mime: %s", type))
                    if (!type.equals(mimeType, ignoreCase = true)) {
                        continue
                    }

                    val candidate = IjkMediaCodecInfo.setupCandidate(codecInfo, mimeType)
                    if (candidate == null) {
                        continue
                    }

                    candidateCodecList.add(candidate)
                    Log.i(TAG, String.format(Locale.US, "candidate codec: %s rank=%d", codecInfo.name, candidate.mRank))
                    candidate.dumpProfileLevels(mimeType)
                }
            }

            if (candidateCodecList.isEmpty()) {
                return null
            }

            var bestCodec = candidateCodecList[0]

            for (codec in candidateCodecList) {
                if (codec.mRank > bestCodec.mRank) {
                    bestCodec = codec
                }
            }

            if (bestCodec.mRank < IjkMediaCodecInfo.RANK_LAST_CHANCE) {
                Log.w(TAG, String.format(Locale.US, "unaccetable codec: %s", bestCodec.mCodecInfo?.name ?: "unknown"))
                return null
            }

            Log.i(TAG, String.format(Locale.US, "selected codec: %s rank=%d", bestCodec.mCodecInfo?.name ?: "unknown", bestCodec.mRank))
            return bestCodec.mCodecInfo?.name
        }
    }
}

