package com.luck.picture.lib.interfaces

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnCallbackListener
 */
interface OnCallbackListener<T> {
    /**
     * 回调方法
     *
     * @param data 回调数据
     */
    fun onCall(data: T?)
}

