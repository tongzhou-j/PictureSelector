package com.yalantis.ucrop.model

/**
 * Created by Oleksii Shliama [https://github.com/shliama] on 6/21/16.
 */
class ExifInfo(
    var exifOrientation: Int,
    var exifDegrees: Int,
    var exifTranslation: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExifInfo

        if (exifOrientation != other.exifOrientation) return false
        if (exifDegrees != other.exifDegrees) return false
        return exifTranslation == other.exifTranslation
    }

    override fun hashCode(): Int {
        var result = exifOrientation
        result = 31 * result + exifDegrees
        result = 31 * result + exifTranslation
        return result
    }
}

