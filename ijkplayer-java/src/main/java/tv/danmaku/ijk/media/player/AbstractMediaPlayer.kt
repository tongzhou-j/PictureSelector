/*
 * Copyright (C) 2013-2014 Bilibili
 * Copyright (C) 2013-2014 Zhang Rui <bbcallen@gmail.com>
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

import tv.danmaku.ijk.media.player.misc.IMediaDataSource

@Suppress("WeakerAccess")
abstract class AbstractMediaPlayer : IMediaPlayer {
    private var mOnPreparedListener: IMediaPlayer.OnPreparedListener? = null
    private var mOnCompletionListener: IMediaPlayer.OnCompletionListener? = null
    private var mOnBufferingUpdateListener: IMediaPlayer.OnBufferingUpdateListener? = null
    private var mOnSeekCompleteListener: IMediaPlayer.OnSeekCompleteListener? = null
    private var mOnVideoSizeChangedListener: IMediaPlayer.OnVideoSizeChangedListener? = null
    private var mOnErrorListener: IMediaPlayer.OnErrorListener? = null
    private var mOnInfoListener: IMediaPlayer.OnInfoListener? = null
    private var mOnTimedTextListener: IMediaPlayer.OnTimedTextListener? = null

    final override fun setOnPreparedListener(listener: IMediaPlayer.OnPreparedListener?) {
        mOnPreparedListener = listener
    }

    final override fun setOnCompletionListener(listener: IMediaPlayer.OnCompletionListener?) {
        mOnCompletionListener = listener
    }

    final override fun setOnBufferingUpdateListener(listener: IMediaPlayer.OnBufferingUpdateListener?) {
        mOnBufferingUpdateListener = listener
    }

    final override fun setOnSeekCompleteListener(listener: IMediaPlayer.OnSeekCompleteListener?) {
        mOnSeekCompleteListener = listener
    }

    final override fun setOnVideoSizeChangedListener(listener: IMediaPlayer.OnVideoSizeChangedListener?) {
        mOnVideoSizeChangedListener = listener
    }

    final override fun setOnErrorListener(listener: IMediaPlayer.OnErrorListener?) {
        mOnErrorListener = listener
    }

    final override fun setOnInfoListener(listener: IMediaPlayer.OnInfoListener?) {
        mOnInfoListener = listener
    }

    final override fun setOnTimedTextListener(listener: IMediaPlayer.OnTimedTextListener?) {
        mOnTimedTextListener = listener
    }

    open fun resetListeners() {
        mOnPreparedListener = null
        mOnBufferingUpdateListener = null
        mOnCompletionListener = null
        mOnSeekCompleteListener = null
        mOnVideoSizeChangedListener = null
        mOnErrorListener = null
        mOnInfoListener = null
        mOnTimedTextListener = null
    }

    protected fun notifyOnPrepared() {
        mOnPreparedListener?.onPrepared(this)
    }

    protected fun notifyOnCompletion() {
        mOnCompletionListener?.onCompletion(this)
    }

    protected fun notifyOnBufferingUpdate(percent: Int) {
        mOnBufferingUpdateListener?.onBufferingUpdate(this, percent)
    }

    protected fun notifyOnSeekComplete() {
        mOnSeekCompleteListener?.onSeekComplete(this)
    }

    protected fun notifyOnVideoSizeChanged(width: Int, height: Int, sarNum: Int, sarDen: Int) {
        mOnVideoSizeChangedListener?.onVideoSizeChanged(this, width, height, sarNum, sarDen)
    }

    protected fun notifyOnError(what: Int, extra: Int): Boolean {
        return mOnErrorListener?.onError(this, what, extra) ?: false
    }

    protected fun notifyOnInfo(what: Int, extra: Int): Boolean {
        return mOnInfoListener?.onInfo(this, what, extra) ?: false
    }

    protected fun notifyOnTimedText(text: IjkTimedText?) {
        mOnTimedTextListener?.onTimedText(this, text)
    }

    override fun setDataSource(mediaDataSource: IMediaDataSource?) {
        throw UnsupportedOperationException()
    }
}

