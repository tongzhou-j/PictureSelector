package com.luck.picture.lib.utils

import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import java.lang.Long
import java.util.Collections
import kotlin.Comparator

/**
 * @author：luck
 * @date：2021/11/11 6:11 下午
 * @describe：排序类
 */
object SortUtils {
    /**
     * Sort by the number of files
     *
     * @param imageFolders
     */
    fun sortFolder(imageFolders: MutableList<LocalMediaFolder?>) {
        Collections.sort<LocalMediaFolder?>(
            imageFolders,
            Comparator { lhs: LocalMediaFolder?, rhs: LocalMediaFolder? ->
                if (lhs!!.getData() == null || rhs!!.getData() == null) {
                    return@Comparator 0
                }
                val lSize = lhs.folderTotalNum
                val rSize = rhs.folderTotalNum
                Integer.compare(rSize, lSize)
            })
    }


    /**
     * Sort by the add Time of files
     *
     * @param list
     */
    fun sortLocalMediaAddedTime(list: MutableList<LocalMedia?>) {
        Collections.sort<LocalMedia?>(list, Comparator { lhs: LocalMedia?, rhs: LocalMedia? ->
            val lAddedTime = lhs!!.dateAddedTime
            val rAddedTime = rhs!!.dateAddedTime
            Long.compare(rAddedTime, lAddedTime)
        })
    }
}
