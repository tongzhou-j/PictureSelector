package com.luck.picture.lib.loader

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import com.luck.picture.lib.R
import com.luck.picture.lib.config.FileSizeUnit
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.entity.MediaData
import com.luck.picture.lib.interfaces.OnQueryAlbumListener
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.thread.PictureThreadUtils
import com.luck.picture.lib.thread.PictureThreadUtils.SimpleTask
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.SortUtils
import com.luck.picture.lib.utils.ValueOf.toInt
import com.luck.picture.lib.utils.ValueOf.toString
import java.io.File

/**
 * @author：luck
 * @date：2020-04-13 15:06
 * @describe：Local media database query class，Support paging
 */
class LocalMediaPageLoader(context: Context?, config: SelectorConfig?) :
    IBridgeMediaLoader(context, config) {
    /**
     * Query conditions in all modes
     *
     * @param timeCondition
     * @param sizeCondition
     * @param queryMimeTypeOptions
     * @return
     */
    private fun getSelectionArgsForAllMediaCondition(
        timeCondition: String?,
        sizeCondition: String?,
        queryImageMimeType: String?,
        queryVideoMimeType: String?
    ): String {
        val stringBuilder = StringBuilder()
        stringBuilder
            .append("(")
            .append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(queryImageMimeType)
            .append(" OR ")
            .append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?").append(queryVideoMimeType)
            .append(" AND ")
            .append(timeCondition)
            .append(")")
            .append(" AND ")
            .append(sizeCondition)
        if (this.isWithAllQuery) {
            return stringBuilder.toString()
        } else {
            return stringBuilder.append(")").append(getGroupByBucketId())
                .toString()
        }
    }
    
    private fun getGroupByBucketId(): String = GROUP_BY_BUCKET_Id

    /**
     * Query conditions in image modes
     *
     * @param fileSizeCondition
     * @param queryMimeTypeOptions
     * @return
     */
    private fun getSelectionArgsForImageMediaCondition(
        fileSizeCondition: String?,
        queryMimeTypeOptions: String?
    ): String {
        val stringBuilder = StringBuilder()
        if (this.isWithAllQuery) {
            return stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeTypeOptions).append(" AND ").append(fileSizeCondition).toString()
        } else {
            return stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                .append("=?")
                .append(queryMimeTypeOptions).append(") AND ").append(fileSizeCondition).append(")")
                .append(getGroupByBucketId()).toString()
        }
    }

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
        val stringBuilder = StringBuilder()
        if (this.isWithAllQuery) {
            return stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(" AND ").append(durationCondition).toString()
        } else {
            return stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                .append("=?").append(queryMimeCondition).append(") AND ").append(durationCondition)
                .append(")").append(getGroupByBucketId()).toString()
        }
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
        val stringBuilder = StringBuilder()
        if (this.isWithAllQuery) {
            return stringBuilder.append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(" AND ").append(durationCondition).toString()
        } else {
            return stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                .append("=?").append(queryMimeCondition).append(") AND ").append(durationCondition)
                .append(")").append(getGroupByBucketId()).toString()
        }
    }

    override fun getAlbumFirstCover(bucketId: Long): String? {
        var data: Cursor? = null
        try {
            val ctx = context ?: return null
            val queryUri = queryUri ?: return null
            if (SdkVersionUtils.isR) {
                val queryArgs = MediaUtils.createQueryArgsBundle(
                    getPageSelection(bucketId),
                    getPageSelectionArgs(bucketId),
                    1,
                    0,
                    sortOrder
                )
                data = ctx.contentResolver.query(
                    queryUri, arrayOf(
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.MediaColumns.MIME_TYPE,
                        MediaStore.MediaColumns.DATA
                    ), queryArgs, null
                )
            } else {
                val orderBy = (sortOrder ?: "") + " limit 1 offset 0"
                data = ctx.contentResolver.query(
                    queryUri, arrayOf(
                        MediaStore.Files.FileColumns._ID,
                        MediaStore.MediaColumns.MIME_TYPE,
                        MediaStore.MediaColumns.DATA
                    ), getPageSelection(bucketId), getPageSelectionArgs(bucketId), orderBy
                )
            }
            if (data != null && data.count > 0) {
                if (data.moveToFirst()) {
                    val id =
                        data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                    val mimeType =
                        data.getString(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE))
                    return if (SdkVersionUtils.isQ) MediaUtils.getRealPathUri(
                        id,
                        mimeType
                    ) else data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                }
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (data != null && !data.isClosed) {
                data.close()
            }
        }
        return null
    }


    override fun loadPageMediaData(
        bucketId: Long,
        page: Int,
        pageSize: Int,
        listener: OnQueryDataResultListener<LocalMedia?>?
    ) {
        PictureThreadUtils.executeByIo<MediaData?>(object : SimpleTask<MediaData?>() {
            override fun doInBackground(): MediaData? {
                var data: Cursor? = null
                try {
                    val ctx = context ?: return MediaData()
                    val uri = queryUri ?: return MediaData()
                    val proj = projection
                    if (SdkVersionUtils.isR) {
                        val queryArgs = MediaUtils.createQueryArgsBundle(
                            getPageSelection(bucketId),
                            getPageSelectionArgs(bucketId),
                            pageSize,
                            (page - 1) * pageSize,
                            sortOrder
                        )
                        data = ctx.contentResolver.query(
                            uri,
                            proj,
                            queryArgs,
                            null
                        )
                    } else {
                        val orderBy =
                            if (page == PictureConfig.ALL) (sortOrder ?: "") else (sortOrder ?: "") + " limit " + pageSize + " offset " + (page - 1) * pageSize
                        data = ctx.contentResolver.query(
                            uri,
                            proj,
                            getPageSelection(bucketId),
                            getPageSelectionArgs(bucketId),
                            orderBy
                        )
                    }
                    if (data != null) {
                        val result = ArrayList<LocalMedia?>()
                        if (data.count > 0) {
                            data.moveToFirst()
                            do {
                                val media = parseLocalMedia(data, false)
                                if (media == null) {
                                    continue
                                }
                                result.add(media)
                            } while (data.moveToNext())
                        }
                        if (bucketId == PictureConfig.ALL.toLong() && page == 1) {
                            val sandboxList = SandboxFileLoader.loadInAppSandboxFile(
                                    context,
                                    config?.sandboxDir
                                )
                            if (sandboxList != null) {
                                val list: MutableList<LocalMedia?> = sandboxList.map { it as LocalMedia? }.toMutableList()
                                result.addAll(list)
                                SortUtils.sortLocalMediaAddedTime(result)
                            }
                        }
                        return MediaData(data.count > 0, result)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i(
                        tag,
                        "loadMedia Page Data Error: " + e.message
                    )
                    return MediaData()
                } finally {
                    if (data != null && !data.isClosed) {
                        data.close()
                    }
                }
                return MediaData()
            }

            override fun onSuccess(result: MediaData?) {
                PictureThreadUtils.cancel(this)
                if (listener != null) {
                    listener.onComplete(
                        if (result?.data != null) result.data else ArrayList<LocalMedia?>(),
                        result?.isHasNextMore ?: false
                    )
                }
            }
        })
    }

    override fun loadOnlyInAppDirAllMedia(query: OnQueryAlbumListener<LocalMediaFolder?>?) {
        PictureThreadUtils.executeByIo<LocalMediaFolder?>(object : SimpleTask<LocalMediaFolder?>() {
            override fun doInBackground(): LocalMediaFolder? {
                return SandboxFileLoader.loadInAppSandboxFolderFile(
                    context,
                    config?.sandboxDir
                )
            }

            override fun onSuccess(result: LocalMediaFolder?) {
                PictureThreadUtils.cancel(this)
                if (query != null) {
                    query.onComplete(result)
                }
            }
        })
    }

    /**
     * Query the local gallery data
     *
     * @param query
     */
    override fun loadAllAlbum(query: OnQueryAllAlbumListener<LocalMediaFolder?>?) {
        PictureThreadUtils.executeByIo<MutableList<LocalMediaFolder?>?>(object :
            SimpleTask<MutableList<LocalMediaFolder?>?>() {
            override fun doInBackground(): MutableList<LocalMediaFolder?>? {
                val ctx = context ?: return ArrayList()
                val uri = queryUri ?: return ArrayList()
                val proj = if (isWithAllQuery) projection else allProjection
                val data = ctx.contentResolver.query(
                    uri,
                    proj,
                    selection,
                    selectionArgs,
                    sortOrder
                )
                try {
                    if (data != null) {
                        val count = data.count
                        var totalCount = 0
                        val mediaFolders: MutableList<LocalMediaFolder> =
                            ArrayList<LocalMediaFolder>()
                        if (count > 0) {
                            if (isWithAllQuery) {
                                val countMap: MutableMap<Long?, Long?> = HashMap<Long?, Long?>()
                                val hashSet: MutableSet<Long?> = HashSet<Long?>()
                                while (data.moveToNext()) {
                                    if (config?.isPageSyncAsCount == true) {
                                        val media = parseLocalMedia(data, true)
                                        if (media == null) {
                                            continue
                                        }
                                        media.recycle()
                                    }
                                    val bucketId =
                                        data.getLong(data.getColumnIndexOrThrow(COLUMN_BUCKET_ID))
                                    var newCount = countMap[bucketId]
                                    if (newCount == null) {
                                        newCount = 1L
                                    } else {
                                        newCount++
                                    }
                                    countMap[bucketId] = newCount

                                    if (hashSet.contains(bucketId)) {
                                        continue
                                    }
                                    val mediaFolder = LocalMediaFolder()
                                    mediaFolder.bucketId = bucketId
                                    val bucketDisplayName = data.getString(
                                        data.getColumnIndexOrThrow(COLUMN_BUCKET_DISPLAY_NAME)
                                    )
                                    val mimeType =
                                        data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
                                    if (!countMap.containsKey(bucketId)) {
                                        continue
                                    }
                                    val size: Long = countMap[bucketId]!!
                                    val id =
                                        data.getLong(data.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                                    mediaFolder.setFolderName(bucketDisplayName)
                                    mediaFolder.folderTotalNum = toInt(size)
                                    mediaFolder.firstImagePath = 
                                        MediaUtils.getRealPathUri(
                                            id,
                                            mimeType
                                        )
                                    mediaFolder.firstMimeType = mimeType
                                    mediaFolders.add(mediaFolder)
                                    hashSet.add(bucketId)
                                }
                                for (mediaFolder in mediaFolders) {
                                    val size = toInt(countMap[mediaFolder.bucketId] ?: 0L)
                                    mediaFolder.folderTotalNum = size
                                    totalCount += size
                                }
                            } else {
                                data.moveToFirst()
                                do {
                                    val url =
                                        data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                                    val bucketDisplayName =
                                        data.getString(data.getColumnIndexOrThrow(COLUMN_BUCKET_DISPLAY_NAME))
                                    val mimeType =
                                        data.getString(data.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
                                    val bucketId =
                                        data.getLong(data.getColumnIndexOrThrow(COLUMN_BUCKET_ID))
                                    val size =
                                        data.getInt(data.getColumnIndexOrThrow(COLUMN_COUNT))
                                    val mediaFolder = LocalMediaFolder()
                                    mediaFolder.bucketId = bucketId
                                    mediaFolder.firstImagePath = url
                                    mediaFolder.setFolderName(bucketDisplayName)
                                    mediaFolder.firstMimeType = mimeType
                                    mediaFolder.folderTotalNum = size
                                    mediaFolders.add(mediaFolder)
                                    totalCount += size
                                } while (data.moveToNext())
                            }
                            // 相机胶卷
                            val allMediaFolder = LocalMediaFolder()
                            val selfFolder = SandboxFileLoader.loadInAppSandboxFolderFile(
                                context,
                                config?.sandboxDir
                            )
                            if (selfFolder != null) {
                                mediaFolders.add(selfFolder)
                                val firstImagePath = selfFolder.firstImagePath
                                val file = File(firstImagePath ?: "")
                                val lastModified = file.lastModified()
                                totalCount += selfFolder.folderTotalNum
                                allMediaFolder.setData(ArrayList<LocalMedia?>())
                                if (data.moveToFirst()) {
                                    allMediaFolder.firstImagePath = 
                                        if (SdkVersionUtils.isQ) getFirstUri(
                                            data
                                        ) else getFirstUrl(data)
                                    allMediaFolder.firstMimeType = 
                                        getFirstCoverMimeType(
                                            data
                                        )
                                    val lastModified2: Long
                                    if (PictureMimeType.isContent(allMediaFolder.firstImagePath)) {
                                        val path = PictureFileUtils.getPath(
                                            context,
                                            Uri.parse(allMediaFolder.firstImagePath ?: "")
                                        )
                                        lastModified2 = File(path ?: "").lastModified()
                                    } else {
                                        lastModified2 =
                                            File(allMediaFolder.firstImagePath ?: "").lastModified()
                                    }
                                    if (lastModified > lastModified2) {
                                        allMediaFolder.firstImagePath = selfFolder.firstImagePath
                                        allMediaFolder.firstMimeType = selfFolder.firstMimeType
                                    }
                                }
                            } else {
                                if (data.moveToFirst()) {
                                    allMediaFolder.firstImagePath = 
                                        if (SdkVersionUtils.isQ) getFirstUri(
                                            data
                                        ) else getFirstUrl(data)
                                    allMediaFolder.firstMimeType = 
                                        getFirstCoverMimeType(
                                            data
                                        )
                                }
                            }
                            if (totalCount == 0) {
                                return mediaFolders.map { it as LocalMediaFolder? }.toMutableList()
                            }
                            val sortableFolders = mediaFolders.map { it as LocalMediaFolder? }.toMutableList()
                            SortUtils.sortFolder(sortableFolders)
                            allMediaFolder.folderTotalNum = totalCount
                            allMediaFolder.bucketId = PictureConfig.ALL.toLong()
                            val folderName: String?
                            if (TextUtils.isEmpty(config?.defaultAlbumName)) {
                                folderName = if (config?.chooseMode == SelectMimeType.ofAudio())
                                    ctx.getString(R.string.ps_all_audio)
                                else
                                    ctx.getString(R.string.ps_camera_roll)
                            } else {
                                folderName = config?.defaultAlbumName
                            }
                            allMediaFolder.setFolderName(folderName)
                            sortableFolders.add(0, allMediaFolder)
                            if (config?.isSyncCover == true) {
                                if (config?.chooseMode == SelectMimeType.ofAll()) {
                                    synchronousFirstCover(sortableFolders)
                                }
                            }
                            return sortableFolders
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i(tag, "loadAllMedia Data Error: " + e.message)
                } finally {
                    if (data != null && !data.isClosed) {
                        data.close()
                    }
                }
                return ArrayList<LocalMediaFolder?>()
            }

            override fun onSuccess(result: MutableList<LocalMediaFolder?>?) {
                PictureThreadUtils.cancel(this)
                LocalMedia.Companion.destroyPool()
                if (query != null) {
                    query.onComplete(result)
                }
            }
        })
    }

    /**
     * Synchronous  First data Cover
     *
     * @param mediaFolders
     */
    private fun synchronousFirstCover(mediaFolders: MutableList<LocalMediaFolder?>) {
        for (i in mediaFolders.indices) {
            val mediaFolder: LocalMediaFolder? = mediaFolders[i]
            if (mediaFolder == null) {
                continue
            }
            val firstCover = getAlbumFirstCover(mediaFolder.bucketId)
            if (TextUtils.isEmpty(firstCover)) {
                continue
            }
            mediaFolder.firstImagePath = firstCover
        }
    }

    private fun getPageSelection(bucketId: Long): String? {
        val durationCondition = durationCondition
        val sizeCondition = fileSizeCondition
        when (config?.chooseMode) {
            SelectMimeType.TYPE_ALL ->                 //  Gets the all
                return getPageSelectionArgsForAllMediaCondition(
                    bucketId,
                    imageMimeTypeCondition,
                    videoMimeTypeCondition,
                    durationCondition,
                    sizeCondition
                )

            SelectMimeType.TYPE_IMAGE ->                 // Gets the image of the specified type
                return getPageSelectionArgsForImageMediaCondition(
                    bucketId,
                    imageMimeTypeCondition,
                    sizeCondition
                )

            SelectMimeType.TYPE_VIDEO ->                 //  Gets the video or video
                return getPageSelectionArgsForVideoMediaCondition(
                    bucketId,
                    videoMimeTypeCondition,
                    durationCondition,
                    sizeCondition
                )

            SelectMimeType.TYPE_AUDIO ->                 //  Gets the video or audio
                return getPageSelectionArgsForAudioMediaCondition(
                    bucketId,
                    audioMimeTypeCondition,
                    durationCondition,
                    sizeCondition
                )
            else -> return null
        }
    }

    private fun getPageSelectionArgs(bucketId: Long): Array<String?>? {
        when (config?.chooseMode) {
            SelectMimeType.TYPE_ALL -> {
                if (bucketId == PictureConfig.ALL.toLong()) {
                    // ofAll
                    return arrayOf(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                    )
                }
                //  Gets the specified album directory
                return arrayOf(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
                    bucketId.toString()
                )
            }

            SelectMimeType.TYPE_IMAGE ->                 // Get photo
                return getSelectionArgsForPageSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                    bucketId
                )

            SelectMimeType.TYPE_VIDEO ->                 // Get video
                return getSelectionArgsForPageSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    bucketId
                )

            SelectMimeType.TYPE_AUDIO ->                 // Get audio
                return getSelectionArgsForPageSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO,
                    bucketId
                )
            else -> return null
        }
    }

    override val selection: String?
        get() {
            val durationCondition = durationCondition
            val fileSizeCondition = fileSizeCondition
            when (config?.chooseMode) {
                SelectMimeType.TYPE_ALL ->                 // Get all, not including audio
                    return getSelectionArgsForAllMediaCondition(
                        durationCondition, fileSizeCondition,
                        imageMimeTypeCondition, videoMimeTypeCondition
                    )

                SelectMimeType.TYPE_IMAGE ->                 // Get Images
                    return getSelectionArgsForImageMediaCondition(
                        fileSizeCondition,
                        imageMimeTypeCondition
                    )

                SelectMimeType.TYPE_VIDEO ->                 // Access to video
                    return getSelectionArgsForVideoMediaCondition(
                        durationCondition,
                        videoMimeTypeCondition
                    )

                SelectMimeType.TYPE_AUDIO ->                 // Access to the audio
                    return getSelectionArgsForAudioMediaCondition(
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
                    return arrayOf(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
                    )

                SelectMimeType.TYPE_IMAGE ->                 // Get photo
                    return arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())

                SelectMimeType.TYPE_VIDEO ->                 // Get video
                    return arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())

                SelectMimeType.TYPE_AUDIO ->                 // Get audio
                    return arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
                else -> return null
            }
        }

    override val sortOrder: String?
        get() {
            return if (TextUtils.isEmpty(config?.sortOrder)) orderBy else config?.sortOrder
        }

    private val isWithAllQuery: Boolean
        /**
         * 查询方式
         */
        get() {
            if (SdkVersionUtils.isQ) {
                return true
            } else {
                return config?.isPageSyncAsCount == true
            }
        }

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
        var mimeType = data.getString(mimeTypeColumn)
        val absolutePath = data.getString(dataColumn)
        val url =
            if (SdkVersionUtils.isQ) MediaUtils.getRealPathUri(id, mimeType ?: "") else absolutePath
        mimeType = if (TextUtils.isEmpty(mimeType)) PictureMimeType.ofJPEG() else mimeType
        if (config?.isFilterInvalidFile == true) {
            if (PictureMimeType.isHasImage(mimeType)) {
                if (!TextUtils.isEmpty(absolutePath) && !PictureFileUtils.isImageFileExists(
                        absolutePath ?: ""
                    )
                ) {
                    return null
                }
            } else {
                if (!PictureFileUtils.isFileExists(absolutePath ?: "")) {
                    return null
                }
            }
        }
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
        val dateAdded = data.getLong(dateAddedColumn)
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

    companion object {
        /**
         * Gets a file of the specified type
         *
         * @param mediaType
         * @return
         */
        private fun getSelectionArgsForPageSingleMediaType(
            mediaType: Int,
            bucketId: Long
        ): Array<String?> {
            return if (bucketId == PictureConfig.ALL.toLong()) arrayOf(mediaType.toString()) else arrayOf(
                mediaType.toString(),
                bucketId.toString()
            )
        }

        /**
         * Get cover uri
         *
         * @param cursor
         * @return
         */
        private fun getFirstUri(cursor: Cursor): String {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
            val mimeType =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
            return MediaUtils.getRealPathUri(id, mimeType ?: "")
        }

        /**
         * Get cover uri mimeType
         *
         * @param cursor
         * @return
         */
        private fun getFirstCoverMimeType(cursor: Cursor): String {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)) ?: ""
        }

        /**
         * Get cover url
         *
         * @param cursor
         * @return
         */
        private fun getFirstUrl(cursor: Cursor): String {
            return cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)) ?: ""
        }

        private fun getPageSelectionArgsForAllMediaCondition(
            bucketId: Long,
            queryImageMimeType: String?,
            queryVideoMimeType: String?,
            durationCondition: String?,
            sizeCondition: String?
        ): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("(")
                .append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryImageMimeType)
                .append(" OR ")
                .append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryVideoMimeType)
                .append(" AND ")
                .append(durationCondition)
                .append(")")
                .append(" AND ")
            if (bucketId == PictureConfig.ALL.toLong()) {
                return stringBuilder.append(sizeCondition).toString()
            } else {
                return stringBuilder.append(IBridgeMediaLoader.COLUMN_BUCKET_ID)
                    .append("=? AND ").append(sizeCondition).toString()
            }
        }

        private fun getPageSelectionArgsForImageMediaCondition(
            bucketId: Long,
            queryMimeCondition: String?,
            sizeCondition: String?
        ): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
            if (bucketId == PictureConfig.ALL.toLong()) {
                return stringBuilder.append(queryMimeCondition).append(") AND ")
                    .append(sizeCondition).toString()
            } else {
                return stringBuilder.append(queryMimeCondition).append(") AND ")
                    .append(IBridgeMediaLoader.COLUMN_BUCKET_ID).append("=? AND ")
                    .append(sizeCondition).toString()
            }
        }

        private fun getPageSelectionArgsForVideoMediaCondition(
            bucketId: Long,
            queryMimeCondition: String?,
            durationCondition: String?,
            sizeCondition: String?
        ): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(" AND ").append(durationCondition)
                .append(") AND ")
            if (bucketId == PictureConfig.ALL.toLong()) {
                return stringBuilder.append(sizeCondition).toString()
            } else {
                return stringBuilder.append(IBridgeMediaLoader.COLUMN_BUCKET_ID)
                    .append("=? AND ").append(sizeCondition).toString()
            }
        }

        private fun getPageSelectionArgsForAudioMediaCondition(
            bucketId: Long,
            queryMimeCondition: String?,
            durationCondition: String?,
            sizeCondition: String?
        ): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE).append("=?")
                .append(queryMimeCondition).append(" AND ").append(durationCondition)
                .append(") AND ")
            if (bucketId == PictureConfig.ALL.toLong()) {
                return stringBuilder.append(sizeCondition).toString()
            } else {
                return stringBuilder.append(IBridgeMediaLoader.COLUMN_BUCKET_ID)
                    .append("=? AND ").append(sizeCondition).toString()
            }
        }
    }
}
