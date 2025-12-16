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
import android.media.MediaFormat
import android.os.Build

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
class AndroidMediaFormat(private val mMediaFormat: MediaFormat?) : IMediaFormat {
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getInteger(name: String): Int {
        return mMediaFormat?.getInteger(name) ?: 0
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun getString(name: String): String? {
        return mMediaFormat?.getString(name)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun toString(): String {
        val out = StringBuilder(128)
        out.append(javaClass.name)
        out.append('{')
        if (mMediaFormat != null) {
            out.append(mMediaFormat.toString())
        } else {
            out.append("null")
        }
        out.append('}')
        return out.toString()
    }
}

