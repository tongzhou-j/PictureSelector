package com.luck.lib.camerax.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import kotlin.jvm.JvmStatic
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * @author：luck
 * @date：2022/6/19 11:56 上午
 * @describe：BitmapUtils
 */
object BitmapUtils {
    /**
     * 水平镜像
     *
     * @param bmp
     * @return
     */
    @JvmStatic
    fun toHorizontalMirror(bmp: Bitmap): Bitmap {
        val w = bmp.width
        val h = bmp.height
        val matrix = Matrix()
        matrix.postScale(-1F, 1F)
        matrix.postRotate(if (w > h) 90f else 0f)
        return Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true)
    }

    @JvmStatic
    fun computeSize(srcWidth: Int, srcHeight: Int): Int {
        var width = if (srcWidth % 2 == 1) srcWidth + 1 else srcWidth
        var height = if (srcHeight % 2 == 1) srcHeight + 1 else srcHeight

        val longSide = max(width, height)
        val shortSide = min(width, height)

        val scale = shortSide.toFloat() / longSide
        return when {
            scale <= 1 && scale > 0.5625 -> {
                when {
                    longSide < 1664 -> 1
                    longSide < 4990 -> 2
                    longSide > 4990 && longSide < 10240 -> 4
                    else -> longSide / 1280
                }
            }
            scale <= 0.5625 && scale > 0.5 -> {
                if (longSide / 1280 == 0) 1 else longSide / 1280
            }
            else -> {
                ceil(longSide / (1280.0 / scale)).toInt()
            }
        }
    }
}

