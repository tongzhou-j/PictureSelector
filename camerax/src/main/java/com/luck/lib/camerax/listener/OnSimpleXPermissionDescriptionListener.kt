package com.luck.lib.camerax.listener

import android.content.Context
import android.view.ViewGroup

/**
 * @author：luck
 * @date：2021/12/1 8:48 下午
 * @describe：OnSimpleXPermissionDescriptionListener
 */
interface OnSimpleXPermissionDescriptionListener {
    /**
     * Permission description
     *
     * @param context
     * @param permission
     */
    fun onPermissionDescription(context: Context, viewGroup: ViewGroup, permission: String)

    /**
     * onDismiss
     */
    fun onDismiss(viewGroup: ViewGroup)
}

