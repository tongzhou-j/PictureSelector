package com.luck.picture.lib.utils

import android.content.Context
import android.os.Environment
import com.luck.picture.lib.config.SelectMimeType

/**
 * @author：luck
 * @date：2022/9/20 7:57 下午
 * @describe：FileDirMap
 */
object FileDirMap {
    private val dirMap = HashMap<Int, String?>()

    fun init(context: Context) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        if (null == dirMap[SelectMimeType.TYPE_IMAGE]) {
            val path: String?
            val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (externalFilesDir != null && externalFilesDir.exists()) {
                path = externalFilesDir.absolutePath
            } else {
                path = context.cacheDir.absolutePath
            }
            dirMap[SelectMimeType.TYPE_IMAGE] = path
        }
        if (null == dirMap[SelectMimeType.TYPE_VIDEO]) {
            val path: String?
            val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            if (externalFilesDir != null && externalFilesDir.exists()) {
                path = externalFilesDir.absolutePath
            } else {
                path = context.cacheDir.absolutePath
            }
            dirMap[SelectMimeType.TYPE_VIDEO] = path
        }
        if (null == dirMap[SelectMimeType.TYPE_AUDIO]) {
            val path: String?
            val externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            if (externalFilesDir != null && externalFilesDir.exists()) {
                path = externalFilesDir.absolutePath
            } else {
                path = context.cacheDir.absolutePath
            }
            dirMap[SelectMimeType.TYPE_AUDIO] = path
        }
    }

    fun getFileDirPath(context: Context, type: Int): String? {
        var dir = dirMap[type]
        if (null == dir) {
            init(context)
            dir = dirMap[type]
        }
        return dir
    }

    fun clear() {
        dirMap.clear()
    }
}
