package com.luck.picture.lib.interfaces

import androidx.fragment.app.Fragment

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnPermissionDescriptionListener
 */
interface OnPermissionDescriptionListener {
    /**
     * 权限说明
     *
     * @param fragment Fragment实例
     * @param permissions 权限数组
     */
    fun onPermissionDescription(fragment: Fragment, permissions: Array<String>)

    /**
     * 关闭权限说明对话框
     *
     * @param fragment Fragment实例
     */
    fun onDismiss(fragment: Fragment)
}

