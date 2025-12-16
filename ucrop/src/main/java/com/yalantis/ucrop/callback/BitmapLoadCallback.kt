package com.yalantis.ucrop.callback

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.yalantis.ucrop.model.ExifInfo

interface BitmapLoadCallback {
    fun onBitmapLoaded(
        @NonNull bitmap: Bitmap,
        @NonNull exifInfo: ExifInfo,
        @NonNull imageInputUri: Uri,
        @Nullable imageOutputUri: Uri?
    )

    fun onFailure(@NonNull bitmapWorkerException: Exception)
}

