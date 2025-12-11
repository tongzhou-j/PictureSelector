package com.luck.picture.lib.loader

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.text.TextUtils
import com.luck.picture.lib.R
import com.luck.picture.lib.config.FileSizeUnit
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnQueryAlbumListener
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.thread.PictureThreadUtils
import com.luck.picture.lib.thread.PictureThreadUtils.SimpleTask
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.SortUtils

/**
 * @author：luck
 * @data：2016/12/31 19:12
 * @describe: Local media database query class
 */
class LocalMediaLoader(context: Context?, config: SelectorConfig?) :
    IBridgeMediaLoader(context, config) {
    override fun loadAllAlbum(query: OnQueryAllAlbumListener<LocalMediaFolder?>?) {
        PictureThreadUtils.executeByIo<MutableList<LocalMediaFolder?>?>(object :
            SimpleTask<MutableList<LocalMediaFolder?>?>() {
            override fun doInBackground(): MutableList<LocalMediaFolder?>? {
                val imageFolders: MutableList<LocalMediaFolder> = ArrayList<LocalMediaFolder>()
                val ctx = context ?: return ArrayList<LocalMediaFolder?>()
                val queryUri = queryUri ?: return ArrayList<LocalMediaFolder?>()
                val data = ctx.contentResolver.query(
                    queryUri, projection,
                    selection, selectionArgs, sortOrder
                )
                try {
                    if (data != null) {
                        val allImageFolder = LocalMediaFolder()
                        val latelyImages = ArrayList<LocalMedia?>()
                        val count = data.count
                        if (count > 0) {
                            data.moveToFirst()
                            do {
                                val media = parseLocalMedia(data, false)
                                if (media == null) {
                                    continue
                                }
                                val folder = getImageFolder(
                                    media.path,
                                    media.mimeType, media.parentFolderName, imageFolders
                                )
                                folder.bucketId = media.bucketId
                                val folderData = folder.getData()
                                folderData.add(media)
                                folder.setData(folderData)
                                folder.folderTotalNum = folder.folderTotalNum + 1
                                latelyImages.add(media)
                                val imageNum = allImageFolder.folderTotalNum
                                allImageFolder.folderTotalNum = imageNum + 1
                            } while (data.moveToNext())

                            val selfFolder = SandboxFileLoader.loadInAppSandboxFolderFile(
                                context,
                                config?.sandboxDir
                            )
                            if (selfFolder != null) {
                                imageFolders.add(selfFolder)
                                allImageFolder.folderTotalNum = allImageFolder.folderTotalNum + selfFolder.folderTotalNum
                                allImageFolder.setData(selfFolder.getData())
                                selfFolder.getData()?.let { latelyImages.addAll(0, it) }
                                if (IBridgeMediaLoader.MAX_SORT_SIZE > selfFolder.folderTotalNum) {
                                    if (latelyImages.size > IBridgeMediaLoader.MAX_SORT_SIZE) {
                                        SortUtils.sortLocalMediaAddedTime(
                                            latelyImages.subList(
                                                0,
                                                IBridgeMediaLoader.MAX_SORT_SIZE
                                            )
                                        )
                                    } else {
                                        SortUtils.sortLocalMediaAddedTime(latelyImages)
                                    }
                                }
                            }

                            if (latelyImages.size > 0) {
                                val sortableFolders = imageFolders.map { it as LocalMediaFolder? }.toMutableList()
                                SortUtils.sortFolder(sortableFolders)
                                sortableFolders.add(0, allImageFolder)
                                allImageFolder.firstImagePath = latelyImages[0]?.path
                                allImageFolder.firstMimeType = latelyImages[0]?.mimeType
                                val folderName: String?
                                if (TextUtils.isEmpty(config?.defaultAlbumName)) {
                                    folderName =
                                        if (config?.chooseMode == SelectMimeType.ofAudio())
                                            ctx.getString(R.string.ps_all_audio)
                                        else
                                            ctx.getString(R.string.ps_camera_roll)
                                } else {
                                    folderName = config?.defaultAlbumName
                                }
                                allImageFolder.setFolderName(folderName)
                                allImageFolder.bucketId = PictureConfig.ALL.toLong()
                                allImageFolder.setData(latelyImages)
                                return sortableFolders
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (data != null && !data.isClosed) {
                        data.close()
                    }
                }
                return imageFolders.map { it as LocalMediaFolder? }.toMutableList()
            }

            override fun onSuccess(result: MutableList<LocalMediaFolder?>?) {
                PictureThreadUtils.cancel(this)
                if (query != null) {
                    query.onComplete(result)
                }
            }
        })
    }


    override fun loadOnlyInAppDirAllMedia(listener: OnQueryAlbumListener<LocalMediaFolder?>?) {
        PictureThreadUtils.executeByIo<LocalMediaFolder?>(object : SimpleTask<LocalMediaFolder?>() {
            override fun doInBackground(): LocalMediaFolder? {
                return SandboxFileLoader.loadInAppSandboxFolderFile(
                    context,
                    config?.sandboxDir
                )
            }

            override fun onSuccess(result: LocalMediaFolder?) {
                PictureThreadUtils.cancel(this)
                if (listener != null) {
                    listener.onComplete(result)
                }
            }
        })
    }

    override fun loadPageMediaData(
        bucketId: Long,
        page: Int,
        pageSize: Int,
        query: OnQueryDataResultListener<LocalMedia?>?
    ) {
    }

    override fun getAlbumFirstCover(bucketId: Long): String? {
        return null
    }

    override val selection: String?
        get() {
            val durationCondition = durationCondition
            val fileSizeCondition = fileSizeCondition
            when (config?.chooseMode) {
            SelectMimeType.TYPE_ALL ->                 // Get all, not including audio
                    return Companion.getSelectionArgsForAllMediaCondition(
                    durationCondition,
                    fileSizeCondition,
                        imageMimeTypeCondition,
                        videoMimeTypeCondition
                )

            SelectMimeType.TYPE_IMAGE ->                 // Gets the image
                    return Companion.getSelectionArgsForImageMediaCondition(
                    fileSizeCondition,
                        imageMimeTypeCondition
                )

            SelectMimeType.TYPE_VIDEO ->                 // Access to video
                    return Companion.getSelectionArgsForVideoMediaCondition(
                    durationCondition,
                        videoMimeTypeCondition
                )

            SelectMimeType.TYPE_AUDIO ->                 // Access to the audio
                    return Companion.getSelectionArgsForAudioMediaCondition(
                    durationCondition,
                        audioMimeTypeCondition
                )
                else -> return null
            }
    }

    override val selectionArgs: Array<String?>?
        get() {
            when (config?.chooseMode) {
            SelectMimeType.TYPE_ALL ->                 // Get all
                    return arrayOf<String?>(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                )

            SelectMimeType.TYPE_IMAGE ->                 // Get photo
                    return arrayOf<String?>(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())

            SelectMimeType.TYPE_VIDEO ->                 // Get video
                    return arrayOf<String?>(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

            SelectMimeType.TYPE_AUDIO ->                 // Get audio
                    return arrayOf<String?>(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
                else -> return null
            }
    }

    override val sortOrder: String?
        get() = if (TextUtils.isEmpty(config?.sortOrder)) orderBy else config?.sortOrder

    override fun parseLocalMedia(data: Cursor?, isUsePool: Boolean): LocalMedia? {
        if (data == null) return null
        val proj = projection
        val idColumn = data.getColumnIndexOrThrow(proj[0]!!)
        val dataColumn = data.getColumnIndexOrThrow(proj[1]!!)
        val mimeTypeColumn = data.getColumnIndexOrThrow(proj[2]!!)
        val widthColumn = data.getColumnIndexOrThrow(proj[3]!!)
        val heightColumn = data.getColumnIndexOrThrow(proj[4]!!)
        val durationColumn = data.getColumnIndexOrThrow(proj[5]!!)
        val sizeColumn = data.getColumnIndexOrThrow(proj[6]!!)
        val folderNameColumn =
            data.getColumnIndexOrThrow(proj[7]!!)
        val fileNameColumn = data.getColumnIndexOrThrow(proj[8]!!)
        val bucketIdColumn = data.getColumnIndexOrThrow(proj[9]!!)
        val dateAddedColumn =
            data.getColumnIndexOrThrow(proj[10]!!)
        val orientationColumn =
            data.getColumnIndexOrThrow(proj[11]!!)
        val id = data.getLong(idColumn)
        val dateAdded = data.getLong(dateAddedColumn)
        var mimeType = data.getString(mimeTypeColumn)
        val absolutePath = data.getString(dataColumn)
        val url =
            if (SdkVersionUtils.isQ) MediaUtils.getRealPathUri(id, mimeType ?: "") else absolutePath
        mimeType = if (TextUtils.isEmpty(mimeType)) PictureMimeType.ofJPEG() else mimeType
        // Here, it is solved that some models obtain mimeType and return the format of image / *,
        // which makes it impossible to distinguish the specific type, such as mi 8,9,10 and other models
        if (mimeType.endsWith("image/*")) {
            mimeType = MediaUtils.getMimeTypeFromMediaUrl(absolutePath ?: "")
            if (config?.isGif != true) {
                if (PictureMimeType.isHasGif(mimeType)) {
                    return null
                }
            }
        }

        if (mimeType.endsWith("image/*")) {
            return null
        }

        if (config?.isWebp != true) {
            if (mimeType.startsWith(PictureMimeType.ofWEBP())) {
                return null
            }
        }
        if (config?.isBmp != true) {
            if (PictureMimeType.isHasBmp(mimeType)) {
                return null
            }
        }
        if (config?.isHeic != true) {
            if (PictureMimeType.isHasHeic(mimeType)) {
                return null
            }
        }

        var width = data.getInt(widthColumn)
        var height = data.getInt(heightColumn)
        val orientation = data.getInt(orientationColumn)
        if (orientation == 90 || orientation == 270) {
            width = data.getInt(heightColumn)
            height = data.getInt(widthColumn)
        }
        val duration = data.getLong(durationColumn)
        val size = data.getLong(sizeColumn)
        val folderName = data.getString(folderNameColumn)
        var fileName = data.getString(fileNameColumn)
        val bucketId = data.getLong(bucketIdColumn)
        if (TextUtils.isEmpty(fileName)) {
            fileName = PictureMimeType.getUrlToFileName(absolutePath ?: "")
        }
        if (config?.isFilterSizeDuration == true && size > 0 && size < FileSizeUnit.KB) {
            // Filter out files less than 1KB
            return null
        }
        if (PictureMimeType.isHasVideo(mimeType) || PictureMimeType.isHasAudio(mimeType)) {
            if (config?.filterVideoMinSecond ?: 0 > 0 && duration < (config?.filterVideoMinSecond ?: 0)) {
                // If you set the minimum number of seconds of video to display
                return null
            }
            if (config?.filterVideoMaxSecond ?: 0 > 0 && duration > (config?.filterVideoMaxSecond ?: 0)) {
                // If you set the maximum number of seconds of video to display
                return null
            }
            if (config?.isFilterSizeDuration == true && duration <= 0) {
                //If the length is 0, the corrupted video is processed and filtered out
                return null
            }
        }
        val media: LocalMedia =
            if (isUsePool) LocalMedia.obtain() else LocalMedia.create()
        media.id = id
        media.bucketId = bucketId
        media.path = url
        media.realPath = absolutePath
        media.fileName = fileName
        media.parentFolderName = folderName
        media.duration = duration
        media.chooseModel = config?.chooseMode ?: 0
        media.mimeType = mimeType
        media.width = width
        media.height = height
        media.size = size
        media.dateAddedTime = dateAdded
        val onQueryFilterListener = config?.onQueryFilterListener
        if (onQueryFilterListener != null) {
            if (onQueryFilterListener.onFilter(media)) {
                return null
            }
        }
        return media
    }

    /**
     * Create folder
     *
     * @param firstPath
     * @param firstMimeType
     * @param imageFolders
     * @param folderName
     * @return
     */
    private fun getImageFolder(
        firstPath: String?,
        firstMimeType: String?,
        folderName: String?,
        imageFolders: MutableList<LocalMediaFolder>
    ): LocalMediaFolder {
        for (folder in imageFolders) {
            // Under the same folder, return yourself, otherwise create a new folder
            val name = folder.getFolderName()
            if (TextUtils.isEmpty(name)) {
                continue
            }
            if (TextUtils.equals(name, folderName)) {
                return folder
            }
        }
        val newFolder = LocalMediaFolder()
        newFolder.setFolderName(folderName)
        newFolder.firstImagePath = firstPath
        newFolder.firstMimeType = firstMimeType
        imageFolders.add(newFolder)
        return newFolder
    }

    companion object {
        /**
         * Video mode conditions
         *
         * @param durationCondition
         * @param queryMimeCondition
         * @return
         */
        private fun getSelectionArgsForVideoMediaCondition(
            durationCondition: String?,
            queryMimeCondition: String?
        ): String {
            return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + durationCondition
        }

        /**
         * Audio mode conditions
         *
         * @param durationCondition
         * @param queryMimeCondition
         * @return
         */
        private fun getSelectionArgsForAudioMediaCondition(
            durationCondition: String?,
            queryMimeCondition: String?
        ): String {
            return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + durationCondition
        }

        /**
         * Query conditions in all modes
         *
         * @param timeCondition
         * @param sizeCondition
         * @param queryImageMimeType
         * @param queryVideoMimeType
         */
        private fun getSelectionArgsForAllMediaCondition(
            timeCondition: String?,
            sizeCondition: String?,
            queryImageMimeType: String?,
            queryVideoMimeType: String?
        ): String {
            return "(" +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryImageMimeType + " OR " +
                    MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryVideoMimeType + " AND " +
                    timeCondition + ") AND " +
                    sizeCondition
        }

        /**
         * Query conditions in image modes
         *
         * @param fileSizeCondition
         * @param queryMimeCondition
         * @return
         */
        private fun getSelectionArgsForImageMediaCondition(
            fileSizeCondition: String?,
            queryMimeCondition: String?
        ): String {
            return MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + queryMimeCondition + " AND " + fileSizeCondition
        }
    }
}
