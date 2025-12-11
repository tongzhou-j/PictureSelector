package com.luck.picture.lib.loader

import android.content.Context
import android.text.TextUtils
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.SortUtils
import com.luck.picture.lib.utils.ValueOf.toLong
import java.io.File
import java.io.FileFilter
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author：luck
 * @date：2021/11/10 5:40 下午
 * @describe：SandboxFileLoader
 */
object SandboxFileLoader {
    /**
     * 查询应用内部目录的图片
     *
     * @param context    上下文
     * @param sandboxDir 资源目标路径
     */
    fun loadInAppSandboxFolderFile(context: Context?, sandboxDir: String?): LocalMediaFolder? {
        val list = loadInAppSandboxFile(context, sandboxDir)
        var folder: LocalMediaFolder? = null
        if (list != null && list.size > 0) {
            val mutableList = list.map { it as LocalMedia? }.toMutableList()
            SortUtils.sortLocalMediaAddedTime(mutableList)
            val firstMedia = mutableList[0]
            folder = LocalMediaFolder()
            folder.setFolderName(firstMedia?.parentFolderName)
            folder.firstImagePath = firstMedia?.path
            folder.firstMimeType = firstMedia?.mimeType
            folder.bucketId = firstMedia?.bucketId!!
            folder.folderTotalNum = mutableList.size
            val dataList = ArrayList<LocalMedia?>(mutableList)
            folder.setData(dataList)
        }
        return folder
    }


    /**
     * 查询应用内部目录的图片
     *
     * @param context    上下文
     * @param sandboxDir 资源目标路径
     */
    fun loadInAppSandboxFile(context: Context?, sandboxDir: String?): ArrayList<LocalMedia>? {
        if (TextUtils.isEmpty(sandboxDir)) {
            return null
        }
        val list = ArrayList<LocalMedia>()
        val sandboxFile = File(sandboxDir!!)
        if (sandboxFile.exists()) {
            val files = sandboxFile.listFiles(object : FileFilter {
                override fun accept(file: File): Boolean {
                    return !file.isDirectory
                }
            })
            if (files == null) {
                return list
            }
            val config: SelectorConfig =
                SelectorProviders.instance?.selectorConfig ?: SelectorConfig()
            var md: MessageDigest? = null
            try {
                md = MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            for (f in files) {
                val mimeType = MediaUtils.getMimeTypeFromMediaUrl(f.absolutePath)
                val queryOnlyImageList = config.queryOnlyImageList
                val queryOnlyVideoList = config.queryOnlyVideoList
                val queryOnlyAudioList = config.queryOnlyAudioList
                if (config.chooseMode == SelectMimeType.ofImage()) {
                    if (!PictureMimeType.isHasImage(mimeType)) {
                        continue
                    }
                    if (queryOnlyImageList != null && queryOnlyImageList.size > 0 && !queryOnlyImageList.contains(mimeType)) {
                        continue
                    }
                } else if (config.chooseMode == SelectMimeType.ofVideo()) {
                    if (!PictureMimeType.isHasVideo(mimeType)) {
                        continue
                    }
                    if (queryOnlyVideoList != null && queryOnlyVideoList.size > 0 && !queryOnlyVideoList.contains(mimeType)) {
                        continue
                    }
                } else if (config.chooseMode == SelectMimeType.ofAudio()) {
                    if (!PictureMimeType.isHasAudio(mimeType)) {
                        continue
                    }
                    if (queryOnlyAudioList != null && queryOnlyAudioList.size > 0 && !queryOnlyAudioList.contains(mimeType)) {
                        continue
                    }
                }

                if (!config.isGif) {
                    if (PictureMimeType.isHasGif(mimeType)) {
                        continue
                    }
                }
                val absolutePath = f.absolutePath
                val size = f.length()
                if (size <= 0) {
                    continue
                }
                val id: Long
                if (md != null) {
                    md.update(absolutePath.toByteArray())
                    id = BigInteger(1, md.digest()).toLong()
                } else {
                    id = f.lastModified() / 1000
                }
                val bucketId = toLong(sandboxFile.name.hashCode())
                val dateAdded = f.lastModified() / 1000
                val duration: Long
                val width: Int
                val height: Int
                if (PictureMimeType.isHasVideo(mimeType)) {
                    val videoSize = MediaUtils.getVideoSize(context, absolutePath)
                    width = videoSize.width
                    height = videoSize.height
                    duration = videoSize.duration
                } else if (PictureMimeType.isHasAudio(mimeType)) {
                    val audioSize = MediaUtils.getAudioSize(context, absolutePath)
                    width = audioSize.width
                    height = audioSize.height
                    duration = audioSize.duration
                } else {
                    val imageSize = MediaUtils.getImageSize(context, absolutePath)
                    width = imageSize.width
                    height = imageSize.height
                    duration = 0L
                }

                if (PictureMimeType.isHasVideo(mimeType) || PictureMimeType.isHasAudio(mimeType)) {
                    if (config.filterVideoMinSecond > 0 && duration < config.filterVideoMinSecond) {
                        // If you set the minimum number of seconds of video to display
                        continue
                    }
                    if (config.filterVideoMaxSecond > 0 && duration > config.filterVideoMaxSecond) {
                        // If you set the maximum number of seconds of video to display
                        continue
                    }
                    if (duration == 0L) {
                        //If the length is 0, the corrupted video is processed and filtered out
                        continue
                    }
                }
                val media = LocalMedia.create()
                media.id = id
                media.path = absolutePath
                media.realPath = absolutePath
                media.fileName = f.name
                media.parentFolderName = sandboxFile.name
                media.duration = duration
                media.chooseModel = config.chooseMode
                media.mimeType = mimeType
                media.width = width
                media.height = height
                media.size = size
                media.bucketId = bucketId
                media.dateAddedTime = dateAdded
                val onQueryFilterListener = config.onQueryFilterListener
                if (onQueryFilterListener != null) {
                    if (onQueryFilterListener.onFilter(media)) {
                        continue
                    }
                }
                media.sandboxPath = if (SdkVersionUtils.isQ) absolutePath else null
                list.add(media)
            }
        }
        return list
    }
}
