package com.yalantis.ucrop.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Nullable

/**
 * Created by Oleksii Shliama [https://github.com/shliama] on 6/24/16.
 */
class AspectRatio(
    @Nullable val aspectRatioTitle: String?,
    val aspectRatioX: Float,
    val aspectRatioY: Float
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(aspectRatioTitle)
        parcel.writeFloat(aspectRatioX)
        parcel.writeFloat(aspectRatioY)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AspectRatio> {
        override fun createFromParcel(parcel: Parcel): AspectRatio {
            return AspectRatio(parcel)
        }

        override fun newArray(size: Int): Array<AspectRatio?> {
            return arrayOfNulls(size)
        }
    }
}

