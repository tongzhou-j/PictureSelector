package com.luck.picture.lib.magical

import android.os.Parcel
import android.os.Parcelable

class ViewParams : Parcelable {
    var left: Int = 0
    var top: Int = 0
    var width: Int = 0
    var height: Int = 0

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.left)
        dest.writeInt(this.top)
        dest.writeInt(this.width)
        dest.writeInt(this.height)
    }

    constructor()

    protected constructor(`in`: Parcel) {
        this.left = `in`.readInt()
        this.top = `in`.readInt()
        this.width = `in`.readInt()
        this.height = `in`.readInt()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ViewParams?> = object : Parcelable.Creator<ViewParams?> {
            override fun createFromParcel(source: Parcel): ViewParams {
                return ViewParams(source)
            }

            override fun newArray(size: Int): Array<ViewParams?> {
                return arrayOfNulls<ViewParams>(size)
            }
        }
    }
}
