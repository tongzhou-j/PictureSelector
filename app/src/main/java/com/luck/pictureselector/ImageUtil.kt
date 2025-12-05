package com.luck.pictureselector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.luck.picture.lib.utils.DensityUtil

/**
 * @author：luck
 * @date：2022/4/2 5:22 下午
 * @describe：ImageUtil
 */
object ImageUtil {
    /**
     * 设置水印图片在左上角
     *
     * @param context     上下文
     * @param src
     * @param watermark
     * @param paddingLeft
     * @param paddingTop
     * @return
     */
    @JvmStatic
    fun createWaterMaskLeftTop(context: Context, src: Bitmap, watermark: Bitmap, paddingLeft: Int, paddingTop: Int): Bitmap? {
        return createWaterMaskBitmap(src, watermark,
            DensityUtil.dip2px(context, paddingLeft.toFloat()), DensityUtil.dip2px(context, paddingTop.toFloat()))
    }

    /**
     * 设置水印图片到右上角
     *
     * @param context
     * @param src
     * @param watermark
     * @param paddingRight
     * @param paddingTop
     * @return
     */
    @JvmStatic
    fun createWaterMaskRightTop(context: Context, src: Bitmap, watermark: Bitmap, paddingRight: Int, paddingTop: Int): Bitmap? {
        return createWaterMaskBitmap(src, watermark,
            src.width - watermark.width - DensityUtil.dip2px(context, paddingRight.toFloat()),
            DensityUtil.dip2px(context, paddingTop.toFloat()))
    }

    private fun createWaterMaskBitmap(src: Bitmap?, watermark: Bitmap?, paddingLeft: Int, paddingTop: Int): Bitmap? {
        if (src == null) {
            return null
        }
        val width = src.width
        val height = src.height
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(src, 0f, 0f, null)
        watermark?.let {
            canvas.drawBitmap(it, paddingLeft.toFloat(), paddingTop.toFloat(), null)
        }
        canvas.save()
        canvas.restore()
        return newBitmap
    }
}

