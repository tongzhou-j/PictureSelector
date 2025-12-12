package com.luck.picture.lib.interfaces

import com.luck.picture.lib.entity.LocalMediaFolder

/**
 * @author：luck
 * @date：2020/4/24 11:48 AM
 * @describe：OnAlbumItemClickListener
 */
interface OnAlbumItemClickListener {
    /**
     * 相册列表点击事件
     *
     * @param position 位置
     * @param curFolder 当前相册文件夹
     */
    fun onItemClick(position: Int, curFolder: LocalMediaFolder)
}

