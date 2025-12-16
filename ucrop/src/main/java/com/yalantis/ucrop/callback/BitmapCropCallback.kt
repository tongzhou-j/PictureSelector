package com.yalantis.ucrop.callback

import android.net.Uri
import androidx.annotation.NonNull

interface BitmapCropCallback {
    fun onBitmapCropped(@NonNull resultUri: Uri, offsetX: Int, offsetY: Int, imageWidth: Int, imageHeight: Int)

    fun onCropFailure(@NonNull t: Throwable)
}

