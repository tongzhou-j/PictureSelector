/*
 * Copyright (C) 2013 Bilibili
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

package tv.danmaku.ijk.media.player.pragma

import android.util.Log
import java.util.Locale
import kotlin.jvm.JvmStatic

@Suppress("SameParameterValue", "WeakerAccess")
object DebugLog {
    const val ENABLE_ERROR = Pragma.ENABLE_VERBOSE
    const val ENABLE_INFO = Pragma.ENABLE_VERBOSE
    const val ENABLE_WARN = Pragma.ENABLE_VERBOSE
    const val ENABLE_DEBUG = Pragma.ENABLE_VERBOSE
    const val ENABLE_VERBOSE = Pragma.ENABLE_VERBOSE

    @JvmStatic
    fun e(tag: String, msg: String) {
        if (ENABLE_ERROR) {
            Log.e(tag, msg)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: String, tr: Throwable) {
        if (ENABLE_ERROR) {
            Log.e(tag, msg, tr)
        }
    }

    @JvmStatic
    fun efmt(tag: String, fmt: String, vararg args: Any) {
        if (ENABLE_ERROR) {
            val msg = String.format(Locale.US, fmt, *args)
            Log.e(tag, msg)
        }
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        if (ENABLE_INFO) {
            Log.i(tag, msg)
        }
    }

    @JvmStatic
    fun i(tag: String, msg: String, tr: Throwable) {
        if (ENABLE_INFO) {
            Log.i(tag, msg, tr)
        }
    }

    @JvmStatic
    fun ifmt(tag: String, fmt: String, vararg args: Any) {
        if (ENABLE_INFO) {
            val msg = String.format(Locale.US, fmt, *args)
            Log.i(tag, msg)
        }
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        if (ENABLE_WARN) {
            Log.w(tag, msg)
        }
    }

    @JvmStatic
    fun w(tag: String, msg: String, tr: Throwable) {
        if (ENABLE_WARN) {
            Log.w(tag, msg, tr)
        }
    }

    @JvmStatic
    fun wfmt(tag: String, fmt: String, vararg args: Any) {
        if (ENABLE_WARN) {
            val msg = String.format(Locale.US, fmt, *args)
            Log.w(tag, msg)
        }
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        if (ENABLE_DEBUG) {
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun d(tag: String, msg: String, tr: Throwable) {
        if (ENABLE_DEBUG) {
            Log.d(tag, msg, tr)
        }
    }

    @JvmStatic
    fun dfmt(tag: String, fmt: String, vararg args: Any) {
        if (ENABLE_DEBUG) {
            val msg = String.format(Locale.US, fmt, *args)
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun v(tag: String, msg: String) {
        if (ENABLE_VERBOSE) {
            Log.v(tag, msg)
        }
    }

    @JvmStatic
    fun v(tag: String, msg: String, tr: Throwable) {
        if (ENABLE_VERBOSE) {
            Log.v(tag, msg, tr)
        }
    }

    @JvmStatic
    fun vfmt(tag: String, fmt: String, vararg args: Any) {
        if (ENABLE_VERBOSE) {
            val msg = String.format(Locale.US, fmt, *args)
            Log.v(tag, msg)
        }
    }

    @JvmStatic
    fun printStackTrace(e: Throwable) {
        if (ENABLE_WARN) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun printCause(e: Throwable) {
        if (ENABLE_WARN) {
            var cause = e.cause
            val throwable = cause ?: e
            printStackTrace(throwable)
        }
    }
}

