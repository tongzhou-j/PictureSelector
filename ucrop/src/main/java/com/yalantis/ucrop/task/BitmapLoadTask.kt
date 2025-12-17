package com.yalantis.ucrop.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.yalantis.ucrop.OkHttpClientStore
import com.yalantis.ucrop.callback.BitmapLoadCallback
import com.yalantis.ucrop.model.ExifInfo
import com.yalantis.ucrop.util.BitmapLoadUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSource
import okio.Okio
import okio.Sink
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference

/**
 * Creates and returns a Bitmap for a given Uri(String url).
 * inSampleSize is calculated based on requiredWidth property. However can be adjusted if OOM occurs.
 * If any EXIF config is found - bitmap is transformed properly.
 */
class BitmapLoadTask(
    @NonNull context: Context,
    @NonNull inputUri: Uri,
    @Nullable outputUri: Uri?,
    requiredWidth: Int,
    requiredHeight: Int,
    loadCallback: BitmapLoadCallback
) : AsyncTask<Void?, Void?, BitmapLoadTask.BitmapWorkerResult>() {

    companion object {
        private const val TAG = "BitmapWorkerTask"
    }

    private val mContext: WeakReference<Context> = WeakReference(context)
    private var mInputUri: Uri = inputUri
    private val mOutputUri: Uri? = outputUri
    private val mRequiredWidth: Int = requiredWidth
    private val mRequiredHeight: Int = requiredHeight
    private val mBitmapLoadCallback: BitmapLoadCallback = loadCallback

    class BitmapWorkerResult {
        var mBitmapResult: Bitmap? = null
        var mExifInfo: ExifInfo? = null
        var mBitmapWorkerException: Exception? = null

        constructor(@NonNull bitmapResult: Bitmap, @NonNull exifInfo: ExifInfo) {
            mBitmapResult = bitmapResult
            mExifInfo = exifInfo
        }

        constructor(@NonNull bitmapWorkerException: Exception) {
            mBitmapWorkerException = bitmapWorkerException
        }
    }

    @NonNull
    override fun doInBackground(vararg params: Void?): BitmapWorkerResult {
        val context = mContext.get()
            ?: return BitmapWorkerResult(NullPointerException("context is null"))

        if (mInputUri == null) {
            return BitmapWorkerResult(NullPointerException("Input Uri cannot be null"))
        }

        try {
            processInputUri()
        } catch (e: Exception) {
            return BitmapWorkerResult(e)
        }

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            val stream = context.contentResolver.openInputStream(mInputUri)
            BitmapFactory.decodeStream(stream, null, options)
            options.inSampleSize = BitmapLoadUtils.computeSize(options.outWidth, options.outHeight)
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
                    if (options.outWidth == -1 || options.outHeight == -1) {
                        return BitmapWorkerResult(IllegalArgumentException("Bounds for bitmap could not be retrieved from the Uri: [$mInputUri]"))
                    }
                } finally {
                    BitmapLoadUtils.close(stream)
                }
                if (BitmapLoadUtils.checkSize(decodeSampledBitmap, options)) continue
                decodeAttemptSuccess = true
            } catch (error: OutOfMemoryError) {
                Log.e(TAG, "doInBackground: BitmapFactory.decodeFileDescriptor: ", error)
                options.inSampleSize *= 2
            } catch (e: IOException) {
                Log.e(TAG, "doInBackground: ImageDecoder.createSource: ", e)
                return BitmapWorkerResult(IllegalArgumentException("Bitmap could not be decoded from the Uri: [$mInputUri]", e))
            }
        }

        if (decodeSampledBitmap == null) {
            return BitmapWorkerResult(IllegalArgumentException("Bitmap could not be decoded from the Uri: [$mInputUri]"))
        }

        val exifOrientation = BitmapLoadUtils.getExifOrientation(context, mInputUri)
        val exifDegrees = BitmapLoadUtils.exifToDegrees(exifOrientation)
        val exifTranslation = BitmapLoadUtils.exifToTranslation(exifOrientation)

        val exifInfo = ExifInfo(exifOrientation, exifDegrees, exifTranslation)

        val matrix = Matrix()
        if (exifDegrees != 0) {
            matrix.preRotate(exifDegrees.toFloat())
        }
        if (exifTranslation != 1) {
            matrix.postScale(exifTranslation.toFloat(), 1f)
        }
        return if (!matrix.isIdentity) {
            BitmapWorkerResult(BitmapLoadUtils.transformBitmap(decodeSampledBitmap, matrix), exifInfo)
        } else {
            BitmapWorkerResult(decodeSampledBitmap, exifInfo)
        }
    }

    @Throws(NullPointerException::class, IOException::class)
    private fun processInputUri() {
        val inputUriScheme = mInputUri.scheme
        Log.d(TAG, "Uri scheme: $inputUriScheme")
        when {
            "http" == inputUriScheme || "https" == inputUriScheme -> {
                try {
                    downloadFile(mInputUri, mOutputUri)
                } catch (e: Exception) {
                    Log.e(TAG, "Downloading failed", e)
                    throw e
                }
            }
            "file" != inputUriScheme && "content" != inputUriScheme -> {
                Log.e(TAG, "Invalid Uri scheme $inputUriScheme")
                throw IllegalArgumentException("Invalid Uri scheme$inputUriScheme")
            }
        }
    }

    @Throws(NullPointerException::class, IOException::class)
    private fun downloadFile(@NonNull inputUri: Uri, @Nullable outputUri: Uri?) {
        Log.d(TAG, "downloadFile")

        if (outputUri == null) {
            throw NullPointerException("Output Uri is null - cannot download image")
        }

        val context = mContext.get()
            ?: throw NullPointerException("Context is null")

        val client: OkHttpClient = OkHttpClientStore.INSTANCE.getClient()

        var source: BufferedSource? = null
        var sink: Sink? = null
        var response: Response? = null
        try {
            val request = Request.Builder()
                .url(inputUri.toString())
                .build()
            response = client.newCall(request).execute()
            source = response.body()!!.source()

            val outputStream = context.contentResolver.openOutputStream(outputUri)
            if (outputStream != null) {
                sink = Okio.sink(outputStream)
                source.readAll(sink)
            } else {
                throw NullPointerException("OutputStream for given output Uri is null")
            }
        } finally {
            BitmapLoadUtils.close(source)
            BitmapLoadUtils.close(sink)
            response?.body()?.let { BitmapLoadUtils.close(it) }
            client.dispatcher().cancelAll()

            // swap uris, because input image was downloaded to the output destination
            // (cropped image will override it later)
            mInputUri = outputUri
        }
    }

    override fun onPostExecute(@NonNull result: BitmapWorkerResult) {
        if (result.mBitmapWorkerException == null) {
            mBitmapLoadCallback.onBitmapLoaded(result.mBitmapResult!!, result.mExifInfo!!, mInputUri, mOutputUri)
        } else {
            mBitmapLoadCallback.onFailure(result.mBitmapWorkerException!!)
        }
    }
}

