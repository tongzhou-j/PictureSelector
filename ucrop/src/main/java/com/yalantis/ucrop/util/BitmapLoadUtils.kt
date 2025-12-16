package com.yalantis.ucrop.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.exifinterface.media.ExifInterface
import com.yalantis.ucrop.callback.BitmapLoadCallback
import com.yalantis.ucrop.task.BitmapLoadTask
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import kotlin.jvm.JvmStatic
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 */
object BitmapLoadUtils {
    private const val MAX_BITMAP_SIZE = 100 * 1024 * 1024   // 100 MB
    private const val CONTENT_SCHEME = "content"

    private const val TAG = "BitmapLoadUtils"

    @JvmStatic
    fun decodeBitmapInBackground(
        @NonNull context: Context,
        @NonNull uri: Uri,
        @Nullable outputUri: Uri?,
        requiredWidth: Int,
        requiredHeight: Int,
        loadCallback: BitmapLoadCallback
    ) {
        BitmapLoadTask(context, uri, outputUri, requiredWidth, requiredHeight, loadCallback)
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    @JvmStatic
    fun transformBitmap(@NonNull bitmap: Bitmap, @NonNull transformMatrix: Matrix): Bitmap {
        return try {
            val converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, transformMatrix, true)
            if (!bitmap.sameAs(converted)) {
                converted
            } else {
                bitmap
            }
        } catch (error: OutOfMemoryError) {
            Log.e(TAG, "transformBitmap: ", error)
            bitmap
        }
    }

    @Deprecated("")
    @JvmStatic
    fun calculateInSampleSize(@NonNull options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width lower or equal to the requested height and width.
            while ((height / inSampleSize) > reqHeight || (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * 计算图片合适压缩比较
     *
     * @param srcWidth  src width
     * @param srcHeight src height
     * @return
     */
    @JvmStatic
    fun computeSize(srcWidth: Int, srcHeight: Int): Int {
        var width = if (srcWidth % 2 == 1) srcWidth + 1 else srcWidth
        var height = if (srcHeight % 2 == 1) srcHeight + 1 else srcHeight

        val longSide = max(width, height)
        val shortSide = min(width, height)

        val scale = shortSide.toFloat() / longSide
        return when {
            scale <= 1 && scale > 0.5625 -> {
                when {
                    longSide < 1664 -> 1
                    longSide < 4990 -> 2
                    longSide > 4990 && longSide < 10240 -> 4
                    else -> longSide / 1280
                }
            }
            scale <= 0.5625 && scale > 0.5 -> {
                if (longSide / 1280 == 0) 1 else longSide / 1280
            }
            else -> ceil(longSide / (1280.0 / scale)).toInt()
        }
    }

    /**
     * Gets the zoom of the image
     *
     * @param context
     * @param mInputUri
     * @return
     */
    @JvmStatic
    fun getMaxImageSize(context: Context, mInputUri: Uri): IntArray {
        if (FileUtils.isHasHttp(mInputUri.toString())) {
            return intArrayOf(0, 0)
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            val stream = context.contentResolver.openInputStream(mInputUri)
            BitmapFactory.decodeStream(stream, null, options)
            options.inSampleSize = computeSize(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        options.inJustDecodeBounds = false

        var decodeSampledBitmap: Bitmap? = null

        var decodeAttemptSuccess = false
        while (!decodeAttemptSuccess) {
            try {
                val stream = context.contentResolver.openInputStream(mInputUri)
                try {
                    decodeSampledBitmap = BitmapFactory.decodeStream(stream, null, options)
                } finally {
                    close(stream)
                }
                if (checkSize(decodeSampledBitmap, options)) continue
                decodeAttemptSuccess = true
            } catch (error: OutOfMemoryError) {
                Log.e(TAG, "doInBackground: BitmapFactory.decodeFileDescriptor: ", error)
                options.inSampleSize *= 2
            } catch (e: IOException) {
                Log.e(TAG, "doInBackground: ImageDecoder.createSource: ", e)
            }
        }
        return if (decodeSampledBitmap == null) {
            intArrayOf(0, 0)
        } else {
            intArrayOf(decodeSampledBitmap.width, decodeSampledBitmap.height)
        }
    }

    @JvmStatic
    fun checkSize(bitmap: Bitmap?, options: BitmapFactory.Options): Boolean {
        val bitmapSize = bitmap?.byteCount ?: 0
        if (bitmapSize > getTotalMemory()) {
            options.inSampleSize *= 2
            return true
        }
        return false
    }

    /**
     * Get total memory of current application
     *
     * @return
     */
    @JvmStatic
    fun getTotalMemory(): Long {
        val totalMemory = Runtime.getRuntime().totalMemory()
        return if (totalMemory > MAX_BITMAP_SIZE.toLong()) MAX_BITMAP_SIZE.toLong() else totalMemory
    }

    @JvmStatic
    fun getExifOrientation(@NonNull context: Context, @NonNull imageUri: Uri): Int {
        var orientation = ExifInterface.ORIENTATION_UNDEFINED
        try {
            val stream = context.contentResolver.openInputStream(imageUri)
            if (stream == null) {
                return orientation
            }
            orientation = ImageHeaderParser(stream).getOrientation()
            close(stream)
        } catch (e: IOException) {
            Log.e(TAG, "getExifOrientation: ${imageUri}", e)
        }
        return orientation
    }

    @JvmStatic
    fun exifToDegrees(exifOrientation: Int): Int {
        val rotation = when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSPOSE -> 90
            ExifInterface.ORIENTATION_ROTATE_180,
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180
            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSVERSE -> 270
            else -> 0
        }
        return rotation
    }

    @JvmStatic
    fun exifToTranslation(exifOrientation: Int): Int {
        val translation = when (exifOrientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
            ExifInterface.ORIENTATION_FLIP_VERTICAL,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE -> -1
            else -> 1
        }
        return translation
    }

    /**
     * This method calculates maximum size of both width and height of bitmap.
     * It is twice the device screen diagonal for default implementation (extra quality to zoom image).
     * Size cannot exceed max texture size.
     *
     * @return - max bitmap size in pixels.
     */
    @SuppressWarnings("SuspiciousNameCombination", "deprecation")
    @JvmStatic
    fun calculateMaxBitmapSize(@NonNull context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        var display: Display? = null
        var width: Int
        var height: Int
        val size = Point()

        if (wm != null) {
            display = wm.defaultDisplay
            display.getSize(size)
        }

        width = size.x
        height = size.y

        // Twice the device screen diagonal as default
        var maxBitmapSize = sqrt((width.toDouble().pow(2) + height.toDouble().pow(2))).toInt()

        // Check for max texture size via Canvas
        val canvas = Canvas()
        val maxCanvasSize = min(canvas.maximumBitmapWidth, canvas.maximumBitmapHeight)
        if (maxCanvasSize > 0) {
            maxBitmapSize = min(maxBitmapSize, maxCanvasSize)
        }

        // Check for max texture size via GL
        val maxTextureSize = EglUtils.getMaxTextureSize()
        if (maxTextureSize > 0) {
            maxBitmapSize = min(maxBitmapSize, maxTextureSize)
        }

        Log.d(TAG, "maxBitmapSize: $maxBitmapSize")
        return maxBitmapSize
    }

    @SuppressWarnings("ConstantConditions")
    @JvmStatic
    fun close(@Nullable c: Closeable?) {
        if (c != null && c is Closeable) { // java.lang.IncompatibleClassChangeError: interface not implemented
            try {
                c.close()
            } catch (e: IOException) {
                // silence
            }
        }
    }

    @JvmStatic
    fun hasContentScheme(uri: Uri?): Boolean {
        return uri != null && CONTENT_SCHEME == uri.scheme
    }
}

