package com.luck.picture.lib.loader

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnQueryAlbumListener
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import java.util.Locale
import kotlin.math.max

/**
 * @author：luck
 * @date：2021/11/11 12:53 下午
 * @describe：IBridgeMediaLoader
 */
abstract class IBridgeMediaLoader(
    protected val context: Context?,
    protected val config: SelectorConfig?
) {
    /**
     * query album cover
     *
     * @param bucketId
     */
    abstract fun getAlbumFirstCover(bucketId: Long): String?

    /**
     * query album list
     */
    abstract fun loadAllAlbum(query: OnQueryAllAlbumListener<LocalMediaFolder?>?)

    /**
     * page query specified contents
     *
     * @param bucketId
     * @param page
     * @param pageSize
     */
    abstract fun loadPageMediaData(
        bucketId: Long,
        page: Int,
        pageSize: Int,
        query: OnQueryDataResultListener<LocalMedia?>?
    )


    /**
     * query specified contents
     */
    abstract fun loadOnlyInAppDirAllMedia(query: OnQueryAlbumListener<LocalMediaFolder?>?)


    /**
     * A filter declaring which rows to return,
     * formatted as an SQL WHERE clause (excluding the WHERE itself).
     * Passing null will return all rows for the given URI.
     */
    protected abstract val selection: String?

    /**
     * You may include ?s in selection, which will be replaced by the values from selectionArgs,
     * in the order that they appear in the selection. The values will be bound as Strings.
     */
    protected abstract val selectionArgs: Array<String?>?

    /**
     * How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing null will use the default sort order, which may be unordered.
     */
    protected abstract val sortOrder: String?

    /**
     * parse LocalMedia
     *
     * @param data      Cursor
     * @param isUsePool object pool
     */
    protected abstract fun parseLocalMedia(data: Cursor?, isUsePool: Boolean): LocalMedia?

    protected val durationCondition: String
        /**
         * Get video (maximum or minimum time)
         *
         * @return
         */
        get() {
            val maxS =
                if (this.config!!.filterVideoMaxSecond == 0) Long.Companion.MAX_VALUE else this.config.filterVideoMaxSecond.toLong()
            val minS = max(0L, this.config.filterVideoMinSecond.toLong())
            return String.format(
                Locale.CHINA,
                "%d <%s " + COLUMN_DURATION + " and " + COLUMN_DURATION + " <= %d",
                minS,
                "=",
                maxS
            )
        }

    protected val fileSizeCondition: String
        /**
         * Get media size (maxFileSize or miniFileSize)
         *
         * @return
         */
        get() {
            val maxS =
                if (this.config!!.filterMaxFileSize == 0L) Long.Companion.MAX_VALUE else this.config.filterMaxFileSize
            val minS = max(0L, this.config.filterMinFileSize)
            return String.format(
                Locale.CHINA,
                "%d <%s " + MediaStore.MediaColumns.SIZE + " and " + MediaStore.MediaColumns.SIZE + " <= %d",
                minS,
                "=",
                maxS
            )
        }

    protected val imageMimeTypeCondition: String
        get() {
            val filters = this.config!!.queryOnlyImageList ?: return ""
            val stringBuilder = StringBuilder()
            for (i in filters.indices) {
                val mimeType = filters[i]
                stringBuilder.append(if (i == 0) " AND " else " OR ")
                    .append(MediaStore.MediaColumns.MIME_TYPE).append("='").append(mimeType)
                    .append("'")
            }
            if (!this.config.isGif && !filters.contains(PictureMimeType.ofGIF())) {
                stringBuilder.append(NOT_GIF)
            }
            if (!this.config.isWebp && !filters.contains(PictureMimeType.ofWEBP())) {
                stringBuilder.append(NOT_WEBP)
            }
            if (!this.config.isBmp && !filters.contains(PictureMimeType.ofBMP()) && !filters.contains(
                    PictureMimeType.ofXmsBMP()
                ) && !filters.contains(PictureMimeType.ofWapBMP())
            ) {
                stringBuilder.append(NOT_BMP)
                    .append(NOT_XMS_BMP)
                    .append(NOT_VND_WAP_BMP)
            }
            if (!this.config.isHeic && !filters.contains(PictureMimeType.ofHeic())) {
                stringBuilder.append(NOT_HEIC)
            }

            return stringBuilder.toString()
        }

    protected val videoMimeTypeCondition: String
        get() {
            val filters = this.config!!.queryOnlyVideoList ?: return ""
            val stringBuilder = StringBuilder()
            for (i in filters.indices) {
                val mimeType = filters[i]
                stringBuilder.append(if (i == 0) " AND " else " OR ")
                    .append(MediaStore.MediaColumns.MIME_TYPE).append("='").append(mimeType)
                    .append("'")
            }
            return stringBuilder.toString()
        }

    protected val audioMimeTypeCondition: String
        get() {
            val filters = this.config!!.queryOnlyAudioList ?: return ""
            val stringBuilder = StringBuilder()
            for (i in filters.indices) {
                val mimeType = filters[i]
                stringBuilder.append(if (i == 0) " AND " else " OR ")
                    .append(MediaStore.MediaColumns.MIME_TYPE).append("='").append(mimeType)
                    .append("'")
            }
            return stringBuilder.toString()
        }

    protected val queryUri: Uri? get() = Companion.QUERY_URI
    protected val projection: Array<String?> get() = Companion.PROJECTION
    protected val allProjection: Array<String?> get() = Companion.ALL_PROJECTION
    protected val orderBy: String get() = Companion.ORDER_BY
    protected val tag: String get() = Companion.TAG
    
    companion object {
        @JvmStatic
        protected val TAG: String = IBridgeMediaLoader::class.java.simpleName
        @JvmStatic
        protected val QUERY_URI: Uri? = MediaStore.Files.getContentUri("external")
        @JvmStatic
        protected val ORDER_BY: String = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
        @JvmStatic
        protected val NOT_GIF: String =
            " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/gif')"
        @JvmStatic
        protected val NOT_WEBP: String =
            " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/webp')"
        @JvmStatic
        protected val NOT_BMP: String =
            " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/bmp')"
        @JvmStatic
        protected val NOT_XMS_BMP: String =
            " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/x-ms-bmp')"
        @JvmStatic
        protected val NOT_VND_WAP_BMP: String =
            " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/vnd.wap.wbmp')"
        @JvmStatic
        protected val NOT_HEIC: String =
            " AND (" + MediaStore.MediaColumns.MIME_TYPE + "!='image/heic')"

        internal const val GROUP_BY_BUCKET_Id: String = " GROUP BY (bucket_id"
        internal const val DISTINCT_BUCKET_Id: String = "DISTINCT bucket_id"
        internal const val COLUMN_COUNT: String = "count"
        internal const val COLUMN_BUCKET_ID: String = "bucket_id"
        internal const val COLUMN_DURATION: String = "duration"
        internal const val COLUMN_BUCKET_DISPLAY_NAME: String = "bucket_display_name"
        internal const val COLUMN_ORIENTATION: String = "orientation"
        internal const val MAX_SORT_SIZE: Int = 60

        /**
         * A list of which columns to return. Passing null will return all columns, which is inefficient.
         */
        @JvmStatic
        protected val PROJECTION: Array<String?> = arrayOf<String?>(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            COLUMN_DURATION,
            MediaStore.MediaColumns.SIZE,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            COLUMN_ORIENTATION
        )

        /**
         * A list of which columns to return. Passing null will return all columns, which is inefficient.
         */
        @JvmStatic
        protected val ALL_PROJECTION: Array<String?> = arrayOf<String?>(
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            COLUMN_DURATION,
            MediaStore.MediaColumns.SIZE,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DISPLAY_NAME,
            COLUMN_BUCKET_ID,
            MediaStore.MediaColumns.DATE_ADDED,
            COLUMN_ORIENTATION,
            "COUNT(*) AS " + COLUMN_COUNT
        )
    }
}
