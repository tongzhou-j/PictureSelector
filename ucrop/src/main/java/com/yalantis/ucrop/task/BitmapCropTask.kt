package com.yalantis.ucrop.task

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.exifinterface.media.ExifInterface
import com.yalantis.ucrop.callback.BitmapCropCallback
import com.yalantis.ucrop.model.CropParameters
import com.yalantis.ucrop.model.ExifInfo
import com.yalantis.ucrop.model.ImageState
import com.yalantis.ucrop.util.BitmapLoadUtils
import com.yalantis.ucrop.util.FileUtils
import com.yalantis.ucrop.util.ImageHeaderParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * Crops part of image that fills the crop bounds.
 * <p/>
 * First image is downscaled if max size was set and if resulting image is larger that max size.
 * Then image is rotated accordingly.
 * Finally new Bitmap object is created and saved to file.
 */
class BitmapCropTask(
    @NonNull context: Context,
    @Nullable viewBitmap: Bitmap?,
    @NonNull imageState: ImageState,
    @NonNull cropParameters: CropParameters,
    @Nullable cropCallback: BitmapCropCallback?
) : AsyncTask<Void?, Void?, Throwable?>() {
    companion object {
        private const val MIN_CROPPED_HEIGHT = 1
        private const val TAG = "BitmapCropTask"
        private const val CONTENT_SCHEME = "content"
    }

    private val mContext: WeakReference<Context> = WeakReference(context)

    private var mViewBitmap: Bitmap? = viewBitmap

    private val mCropRect: RectF = imageState.cropRect
    private val mCurrentImageRect: RectF = imageState.currentImageRect

    private var mCurrentScale: Float = imageState.currentScale
    private val mCurrentAngle: Float = imageState.currentAngle
    private val mMaxResultImageSizeX: Int = cropParameters.maxResultImageSizeX
    private val mMaxResultImageSizeY: Int = cropParameters.maxResultImageSizeY

    private val mCompressFormat: Bitmap.CompressFormat = cropParameters.compressFormat
    private val mCompressQuality: Int = cropParameters.compressQuality
    private val mImageInputPath: String = cropParameters.imageInputPath
    private val mImageOutputPath: String = cropParameters.imageOutputPath
    private val mImageInputUri: Uri? = cropParameters.contentImageInputUri
    private val mImageOutputUri: Uri? = cropParameters.contentImageOutputUri
    private val mExifInfo: ExifInfo = cropParameters.exifInfo
    private val mCropCallback: BitmapCropCallback? = cropCallback

    private var mCroppedImageWidth: Int = 0
    private var mCroppedImageHeight: Int = 0
    private var cropOffsetX: Int = 0
    private var cropOffsetY: Int = 0

    @Nullable
    override fun doInBackground(vararg params: Void?): Throwable? {
        when {
            mViewBitmap == null -> return NullPointerException("ViewBitmap is null")
            mViewBitmap!!.isRecycled -> return NullPointerException("ViewBitmap is recycled")
            mCurrentImageRect.isEmpty() -> return NullPointerException("CurrentImageRect is empty")
        }

        if (mImageOutputUri == null) {
            return NullPointerException("ImageOutputUri is null")
        }

        return try {
            crop()
            mViewBitmap = null
            null
        } catch (throwable: Throwable) {
            throwable
        }
    }

    @Throws(IOException::class)
    private fun crop(): Boolean {
        val context = mContext.get() ?: return false

        // Downsize if needed
        if (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0) {
            val cropWidth = mCropRect.width() / mCurrentScale
            val cropHeight = mCropRect.height() / mCurrentScale

            if (cropWidth > mMaxResultImageSizeX || cropHeight > mMaxResultImageSizeY) {
                val scaleX = mMaxResultImageSizeX / cropWidth
                val scaleY = mMaxResultImageSizeY / cropHeight
                val resizeScale = min(scaleX, scaleY)

                val resizedBitmap = Bitmap.createScaledBitmap(mViewBitmap!!,
                    round(mViewBitmap!!.width * resizeScale).toInt(),
                    round(mViewBitmap!!.height * resizeScale).toInt(), false)
                if (mViewBitmap !== resizedBitmap) {
                    mViewBitmap!!.recycle()
                }
                mViewBitmap = resizedBitmap

                mCurrentScale /= resizeScale
            }
        }

        // Rotate if needed
        if (mCurrentAngle != 0f) {
            val tempMatrix = Matrix()
            tempMatrix.setRotate(mCurrentAngle, mViewBitmap!!.width / 2f, mViewBitmap!!.height / 2f)

            val rotatedBitmap = Bitmap.createBitmap(mViewBitmap!!, 0, 0, mViewBitmap!!.width, mViewBitmap!!.height,
                tempMatrix, true)
            if (mViewBitmap !== rotatedBitmap) {
                mViewBitmap!!.recycle()
            }
            mViewBitmap = rotatedBitmap
        }

        cropOffsetX = round((mCropRect.left - mCurrentImageRect.left) / mCurrentScale).toInt()
        cropOffsetY = round((mCropRect.top - mCurrentImageRect.top) / mCurrentScale).toInt()
        mCroppedImageWidth = round(mCropRect.width() / mCurrentScale).toInt()
        mCroppedImageHeight = round(mCropRect.height() / mCurrentScale).toInt()

        val shouldCrop = shouldCrop(mCroppedImageWidth, mCroppedImageHeight)
        Log.i(TAG, "Should crop: $shouldCrop")
        return if (shouldCrop) {
            checkValidityCropBounds()
            saveImage(Bitmap.createBitmap(mViewBitmap!!, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight))
            if (mCompressFormat == Bitmap.CompressFormat.JPEG) {
                copyExifForOutputFile(context)
            }
            true
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && FileUtils.isContent(mImageInputPath)) {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(mImageInputPath))
                if (inputStream != null) {
                    FileUtils.writeFileFromIS(inputStream, FileOutputStream(mImageOutputPath))
                }
            } else {
                FileUtils.copyFile(mImageInputPath, mImageOutputPath)
            }
            false
        }
    }

    /**
     * Check the validity of the crop bounds
     */
    private fun checkValidityCropBounds() {
        if (cropOffsetX < 0) {
            cropOffsetX = 0
            mCroppedImageWidth = mViewBitmap!!.width
        }
        if (cropOffsetY < 0) {
            cropOffsetY = 0
            mCroppedImageHeight = mViewBitmap!!.height
        }
    }

    @Throws(IOException::class)
    private fun copyExifForOutputFile(context: Context) {
        val hasImageInputUriContentSchema = BitmapLoadUtils.hasContentScheme(mImageInputUri)
        val hasImageOutputUriContentSchema = BitmapLoadUtils.hasContentScheme(mImageOutputUri)
        /*
         * ImageHeaderParser.copyExif with output uri as a parameter
         * uses ExifInterface constructor with FileDescriptor param for overriding output file exif info,
         * which doesn't support ExitInterface.saveAttributes call for SDK lower than 21.
         *
         * See documentation for ImageHeaderParser.copyExif and ExifInterface.saveAttributes implementation.
         */
        when {
            hasImageInputUriContentSchema && hasImageOutputUriContentSchema -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ImageHeaderParser.copyExif(context, mCroppedImageWidth, mCroppedImageHeight, mImageInputUri!!, mImageOutputUri!!)
                } else {
                    Log.e(TAG, "It is not possible to write exif info into file represented by \"content\" Uri if Android < LOLLIPOP")
                }
            }
            hasImageInputUriContentSchema -> {
                ImageHeaderParser.copyExif(context, mCroppedImageWidth, mCroppedImageHeight, mImageInputUri!!, mImageOutputPath)
            }
            hasImageOutputUriContentSchema -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val originalExif = ExifInterface(mImageInputPath)
                    ImageHeaderParser.copyExif(context, originalExif, mCroppedImageWidth, mCroppedImageHeight, mImageOutputUri!!)
                } else {
                    Log.e(TAG, "It is not possible to write exif info into file represented by \"content\" Uri if Android < LOLLIPOP")
                }
            }
            else -> {
                val originalExif = ExifInterface(mImageInputPath)
                ImageHeaderParser.copyExif(originalExif, mCroppedImageWidth, mCroppedImageHeight, mImageOutputPath)
            }
        }
    }

    private fun saveImage(@NonNull croppedBitmap: Bitmap) {
        val context = mContext.get() ?: return

        var outputStream: OutputStream? = null
        var outStream: ByteArrayOutputStream? = null
        try {
            outputStream = context.contentResolver.openOutputStream(mImageOutputUri!!)
            outStream = ByteArrayOutputStream()
            croppedBitmap.compress(mCompressFormat, mCompressQuality, outStream)
            outputStream!!.write(outStream.toByteArray())
            croppedBitmap.recycle()
        } catch (exc: IOException) {
            Log.e(TAG, exc.localizedMessage)
        } finally {
            BitmapLoadUtils.close(outputStream)
            BitmapLoadUtils.close(outStream)
        }
    }

    /**
     * Check whether an image should be cropped at all or just file can be copied to the destination path.
     * For each 1000 pixels there is one pixel of error due to matrix calculations etc.
     *
     * @param width  - crop area width
     * @param height - crop area height
     * @return - true if image must be cropped, false - if original image fits requirements
     */
    private fun shouldCrop(width: Int, height: Int): Boolean {
        var pixelError = 1
        pixelError += round(max(width, height) / 1000f).toInt()
        return (mMaxResultImageSizeX > 0 && mMaxResultImageSizeY > 0)
                || abs(mCropRect.left - mCurrentImageRect.left) > pixelError
                || abs(mCropRect.top - mCurrentImageRect.top) > pixelError
                || abs(mCropRect.bottom - mCurrentImageRect.bottom) > pixelError
                || abs(mCropRect.right - mCurrentImageRect.right) > pixelError
                || mCurrentAngle != 0f
    }

    override fun onPostExecute(@Nullable t: Throwable?) {
        mCropCallback?.let { callback ->
            if (t == null) {
                val uri = if (BitmapLoadUtils.hasContentScheme(mImageOutputUri)) {
                    mImageOutputUri!!
                } else {
                    Uri.fromFile(File(mImageOutputPath))
                }
                callback.onBitmapCropped(uri, cropOffsetX, cropOffsetY, mCroppedImageWidth, mCroppedImageHeight)
            } else {
                callback.onCropFailure(t)
            }
        }
    }
}

