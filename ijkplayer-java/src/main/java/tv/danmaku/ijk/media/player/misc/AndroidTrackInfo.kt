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

package tv.danmaku.ijk.media.player.misc

import android.annotation.TargetApi
import android.media.MediaPlayer
import android.os.Build

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class AndroidTrackInfo(private val mTrackInfo: MediaPlayer.TrackInfo?) : ITrackInfo {
    companion object {
        @JvmStatic
        fun fromMediaPlayer(mp: MediaPlayer): Array<AndroidTrackInfo>? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                fromTrackInfo(mp.trackInfo)
            } else {
                null
            }
        }

        private fun fromTrackInfo(trackInfos: Array<MediaPlayer.TrackInfo>?): Array<AndroidTrackInfo>? {
            if (trackInfos == null) return null

            val androidTrackInfo = Array(trackInfos.size) { i ->
                AndroidTrackInfo(trackInfos[i])
            }
            return androidTrackInfo
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    override fun getFormat(): IMediaFormat {
        if (mTrackInfo == null) {
            throw IllegalStateException("TrackInfo is null")
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            throw IllegalStateException("API level too low")
        }

        val mediaFormat = mTrackInfo!!.format ?: throw IllegalStateException("MediaFormat is null")
        return AndroidMediaFormat(mediaFormat)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getLanguage(): String? {
        return mTrackInfo?.language ?: "und"
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getTrackType(): Int {
        return mTrackInfo?.trackType ?: ITrackInfo.MEDIA_TRACK_TYPE_UNKNOWN
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun toString(): String {
        val out = StringBuilder(128)
        out.append(javaClass.simpleName)
        out.append('{')
        if (mTrackInfo != null) {
            out.append(mTrackInfo.toString())
        } else {
            out.append("null")
        }
        out.append('}')
        return out.toString()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getInfoInline(): String {
        return mTrackInfo?.toString() ?: "null"
    }
}

