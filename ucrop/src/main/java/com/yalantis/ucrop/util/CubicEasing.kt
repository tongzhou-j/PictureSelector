package com.yalantis.ucrop.util

import kotlin.jvm.JvmStatic

object CubicEasing {
    @JvmStatic
    fun easeOut(time: Float, start: Float, end: Float, duration: Float): Float {
        val t = time / duration - 1.0f
        return end * (t * t * t + 1.0f) + start
    }

    @JvmStatic
    fun easeIn(time: Float, start: Float, end: Float, duration: Float): Float {
        val t = time / duration
        return end * t * t * t + start
    }

    @JvmStatic
    fun easeInOut(time: Float, start: Float, end: Float, duration: Float): Float {
        val t = time / (duration / 2.0f)
        return if (t < 1.0f) {
            end / 2.0f * t * t * t + start
        } else {
            val t2 = t - 2.0f
            end / 2.0f * (t2 * t2 * t2 + 2.0f) + start
        }
    }
}

