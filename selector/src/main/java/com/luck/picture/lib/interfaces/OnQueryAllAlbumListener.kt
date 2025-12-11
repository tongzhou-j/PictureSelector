package com.luck.picture.lib.interfaces

/**
 * @author：luck
 * @date：2021/12/5 9:41 下午
 * @describe：OnExternalQueryAllAlbumListener
 */
interface OnQueryAllAlbumListener<T> {
    fun onComplete(result: MutableList<T?>?)
}
