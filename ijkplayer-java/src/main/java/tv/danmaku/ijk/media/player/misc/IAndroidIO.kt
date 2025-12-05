/*
 * Copyright (C) 2016 Bilibili
 * Copyright (C) 2016 Raymond Zheng <raymondzheng1412@gmail.com>
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

import java.io.IOException

@Suppress("RedundantThrows")
interface IAndroidIO {
    @Throws(IOException::class)
    fun open(url: String): Int

    @Throws(IOException::class)
    fun read(buffer: ByteArray, size: Int): Int

    @Throws(IOException::class)
    fun seek(offset: Long, whence: Int): Long

    @Throws(IOException::class)
    fun close(): Int
}

