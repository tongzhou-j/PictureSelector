package com.luck.picture.lib.utils

import android.content.Context
import android.graphics.ColorFilter
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import java.util.regex.Pattern

/**
 * @author：luck
 * @date：2021/11/20 3:27 下午
 * @describe：StyleUtils
 */
object StyleUtils {
    private const val INVALID = 0

    /**
     * 验证样式资源的合法性
     *
     * @param resource
     * @return
     */
    fun checkStyleValidity(resource: Int): Boolean {
        return resource != StyleUtils.INVALID
    }

    /**
     * 验证文本的合法性
     *
     * @param text
     * @return
     */
    fun checkTextValidity(text: String?): Boolean {
        return !TextUtils.isEmpty(text)
    }

    /**
     * 验证文本是否有2个动态匹配符
     *
     * @param text
     * @return
     */
    fun getFormatCount(text: String): Int {
        val pattern = "%[^%]*\\d"
        val compile = Pattern.compile(pattern)
        val matcher = compile.matcher(text)
        var count = 0
        while (matcher.find()) {
            count++
        }
        return count
    }

    /**
     * 验证大小的合法性
     *
     * @param size
     * @return
     */
    fun checkSizeValidity(size: Int): Boolean {
        return size > StyleUtils.INVALID
    }

    /**
     * 验证数组的合法性
     *
     * @param size
     * @return
     */
    fun checkArrayValidity(array: IntArray?): Boolean {
        return array != null && array.size > 0
    }

    /**
     * getColorFilter
     *
     * @param context
     * @param color
     * @return
     */
    fun getColorFilter(context: Context, color: Int): ColorFilter? {
        return BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            ContextCompat.getColor(
                context,
                color
            ), BlendModeCompat.SRC_ATOP
        )
    }
}
