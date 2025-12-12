package com.luck.picture.lib.basic

import android.content.Intent

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：SelectorResult
 */
data class SelectorResult(
    /**
     * 结果码
     */
    val resultCode: Int,
    /**
     * Intent数据
     */
    val intent: Intent?
)

