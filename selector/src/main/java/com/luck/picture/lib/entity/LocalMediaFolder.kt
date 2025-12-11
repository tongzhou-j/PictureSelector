package com.luck.picture.lib.entity

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.luck.picture.lib.config.PictureConfig

/**
 * @author：luck
 * @date：2016-12-31 15:21
 * @describe：MediaFolder Entity
 */
class LocalMediaFolder : Parcelable {
    /**
     * folder bucketId
     */
    var bucketId: Long = PictureConfig.ALL.toLong()

    /**
     * folder name
     */
    private var folderName: String? = null

    /**
     * folder first path
     */
    var firstImagePath: String? = null

    /**
     * first data mime type
     */
    var firstMimeType: String? = null

    /**
     * folder total media num
     */
    var folderTotalNum: Int = 0

    /**
     * There are selected resources in the current directory
     */
    var isSelectTag: Boolean = false

    /**
     * current folder data
     *
     *
     * In isPageStrategy mode, there is no data for the first time
     *
     */
    private var data: ArrayList<LocalMedia?>? = ArrayList<LocalMedia?>()

    /**
     * # Internal use
     * setCurrentDataPage
     */
    var currentDataPage: Int = 1

    /**
     * # Internal use
     * is load more
     */
    var isHasMore: Boolean = false


    constructor()


    protected constructor(`in`: Parcel) {
        bucketId = `in`.readLong()
        folderName = `in`.readString()
        firstImagePath = `in`.readString()
        firstMimeType = `in`.readString()
        folderTotalNum = `in`.readInt()
        isSelectTag = `in`.readByte().toInt() != 0
        data = `in`.createTypedArrayList<LocalMedia?>(LocalMedia.Companion.CREATOR)
        currentDataPage = `in`.readInt()
        isHasMore = `in`.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(bucketId)
        dest.writeString(folderName)
        dest.writeString(firstImagePath)
        dest.writeString(firstMimeType)
        dest.writeInt(folderTotalNum)
        dest.writeByte((if (isSelectTag) 1 else 0).toByte())
        dest.writeTypedList<LocalMedia?>(data)
        dest.writeInt(currentDataPage)
        dest.writeByte((if (isHasMore) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getFolderName(): String? {
        return if (TextUtils.isEmpty(folderName)) "unknown" else folderName
    }

    fun setFolderName(folderName: String?) {
        this.folderName = folderName
    }

    fun getData(): ArrayList<LocalMedia?> {
        return (if (data != null) data else java.util.ArrayList<LocalMedia?>())!!
    }

    fun setData(data: ArrayList<LocalMedia?>?) {
        this.data = data
    }

    companion object {
        val CREATOR: Parcelable.Creator<LocalMediaFolder?> =
            object : Parcelable.Creator<LocalMediaFolder?> {
                override fun createFromParcel(`in`: Parcel): LocalMediaFolder {
                    return LocalMediaFolder(`in`)
                }

                override fun newArray(size: Int): Array<LocalMediaFolder?> {
                    return arrayOfNulls<LocalMediaFolder>(size)
                }
            }
    }
}
