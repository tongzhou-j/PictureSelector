package com.luck.picture.lib.interfaces

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnQueryAlbumListener
 */
interface OnQueryAlbumListener<T> {
    /**
     * 查询相册完成回调
     *
     * @param folder 相册文件夹
     */
    fun onComplete(folder: T?)
}

