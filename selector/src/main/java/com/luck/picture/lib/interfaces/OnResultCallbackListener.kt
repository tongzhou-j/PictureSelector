package com.luck.picture.lib.interfaces

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnResultCallbackListener
 */
interface OnResultCallbackListener<T> {
    /**
     * 返回结果
     *
     * @param result 结果列表
     */
    fun onResult(result: ArrayList<T>)

    /**
     * 取消选择
     */
    fun onCancel()
}

