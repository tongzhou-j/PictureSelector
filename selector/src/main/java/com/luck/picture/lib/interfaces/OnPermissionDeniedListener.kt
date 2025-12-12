package com.luck.picture.lib.interfaces

import androidx.fragment.app.Fragment

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnPermissionDeniedListener
 */
interface OnPermissionDeniedListener {
    /**
     * 权限被拒绝
     *
     * @param fragment Fragment实例
     * @param permissions 权限数组
     * @param requestType 请求类型
     * @param callback 回调
     */
    fun onDenied(
        fragment: Fragment,
        permissions: Array<String>,
        requestType: Int,
        callback: OnCallbackListener<Boolean>?
    )
}

