package com.luck.picture.lib.interfaces

import android.app.Dialog
import android.content.Context

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnCustomLoadingListener
 */
interface OnCustomLoadingListener {
    /**
     * 创建自定义加载对话框
     *
     * @param context 上下文
     * @return Dialog实例
     */
    fun create(context: Context): Dialog
}

