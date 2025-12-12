package com.luck.picture.lib.interfaces

import android.view.View

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnItemClickListener
 */
interface OnItemClickListener {
    /**
     * 列表项点击事件
     *
     * @param v 点击的View
     * @param position 位置
     */
    fun onItemClick(v: View, position: Int)
}

