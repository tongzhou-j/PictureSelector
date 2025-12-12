package com.luck.picture.lib.interfaces

import androidx.fragment.app.Fragment

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnCameraInterceptListener
 */
interface OnCameraInterceptListener {
    /**
     * 打开相机
     *
     * @param fragment Fragment实例
     * @param cameraMode 相机模式
     * @param requestCode 请求码
     */
    fun openCamera(fragment: Fragment, cameraMode: Int, requestCode: Int)
}

