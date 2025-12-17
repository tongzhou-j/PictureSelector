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

import android.text.TextUtils
import tv.danmaku.ijk.media.player.IjkMediaMeta

class IjkTrackInfo(private var mStreamMeta: IjkMediaMeta.IjkStreamMeta?) : ITrackInfo {
    private var mTrackType = ITrackInfo.MEDIA_TRACK_TYPE_UNKNOWN

    fun setMediaMeta(streamMeta: IjkMediaMeta.IjkStreamMeta?) {
        mStreamMeta = streamMeta
    }

    override fun getFormat(): IMediaFormat {
        return IjkMediaFormat(mStreamMeta!!)
    }

    override fun getLanguage(): String? {
        return if (mStreamMeta == null || TextUtils.isEmpty(mStreamMeta!!.mLanguage)) {
            "und"
        } else {
            mStreamMeta!!.mLanguage
        }
    }

    override fun getTrackType(): Int {
        return mTrackType
    }

    fun setTrackType(trackType: Int) {
        mTrackType = trackType
    }

    override fun toString(): String {
        return javaClass.simpleName + '{' + getInfoInline() + "}"
    }

    override fun getInfoInline(): String? {
        val out = StringBuilder(128)
        when (mTrackType) {
            ITrackInfo.MEDIA_TRACK_TYPE_VIDEO -> {
                out.append("VIDEO")
                out.append(", ")
                out.append(mStreamMeta!!.getCodecShortNameInline())
                out.append(", ")
                out.append(mStreamMeta!!.getBitrateInline())
                out.append(", ")
                out.append(mStreamMeta!!.getResolutionInline())
            }
            ITrackInfo.MEDIA_TRACK_TYPE_AUDIO -> {
                out.append("AUDIO")
                out.append(", ")
                out.append(mStreamMeta!!.getCodecShortNameInline())
                out.append(", ")
                out.append(mStreamMeta!!.getBitrateInline())
                out.append(", ")
                out.append(mStreamMeta!!.getSampleRateInline())
            }
            ITrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT -> {
                out.append("TIMEDTEXT")
                out.append(", ")
                out.append(mStreamMeta!!.mLanguage)
            }
            ITrackInfo.MEDIA_TRACK_TYPE_SUBTITLE -> {
                out.append("SUBTITLE")
            }
            else -> {
                out.append("UNKNOWN")
            }
        }
        return out.toString()
    }
}

