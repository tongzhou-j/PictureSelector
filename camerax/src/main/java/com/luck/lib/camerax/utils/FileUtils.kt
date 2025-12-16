package com.luck.lib.camerax.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.Nullable
import androidx.core.content.FileProvider
import kotlin.jvm.JvmStatic
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * @author：luck
 * @date：2021/11/29 8:17 下午
 * @describe：FileUtils
 */
object FileUtils {
    const val POSTFIX = ".jpeg"
    const val POST_VIDEO = ".mp4"

    /**
     * @param context
     * @param chooseMode
     * @param format
     * @param outCameraDirectory
     * @return
     */
    @JvmStatic
    fun createCameraFile(
        context: Context,
        chooseMode: Int,
        fileName: String?,
        format: String?,
        outCameraDirectory: String?
    ): File {
        return createMediaFile(context, chooseMode, fileName, format, outCameraDirectory)
    }

    /**
     * 创建文件
     *
     * @param context
     * @param chooseMode
     * @param fileName
     * @param format
     * @param outCameraDirectory
     * @return
     */
    private fun createMediaFile(
        context: Context,
        chooseMode: Int,
        fileName: String?,
        format: String?,
        outCameraDirectory: String?
    ): File {
        return createOutFile(context, chooseMode, fileName, format, outCameraDirectory)
    }

    /**
     * 创建文件
     *
     * @param ctx                上下文
     * @param chooseMode         选择模式
     * @param fileName           文件名
     * @param format             文件格式
     * @param outCameraDirectory 输出目录
     * @return
     */
    private fun createOutFile(
        ctx: Context,
        chooseMode: Int,
        fileName: String?,
        format: String?,
        outCameraDirectory: String?
    ): File {
        val context = ctx.applicationContext
        val folderDir: File
        if (outCameraDirectory.isNullOrEmpty()) {
            // 外部没有自定义拍照存储路径使用默认
            val rootDir: File
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                folderDir = File(rootDir.absolutePath + File.separator + CameraUtils.CAMERA + File.separator)
            } else {
                rootDir = getRootDirFile(context, chooseMode) ?: context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                folderDir = File(rootDir.absolutePath + File.separator)
            }
            if (!rootDir.exists()) {
                rootDir.mkdirs()
            }
        } else {
            // 自定义存储路径
            folderDir = File(outCameraDirectory)
            folderDir.parentFile?.let { parent ->
                if (!parent.exists()) {
                    parent.mkdirs()
                }
            }
        }
        if (!folderDir.exists()) {
            folderDir.mkdirs()
        }

        val isOutFileNameEmpty = fileName.isNullOrEmpty()
        if (chooseMode == CameraUtils.TYPE_VIDEO) {
            val newFileVideoName = if (isOutFileNameEmpty) DateUtils.getCreateFileName("VID_") + POST_VIDEO else fileName
            return File(folderDir, newFileVideoName)
        }
        val suffix = format ?: POSTFIX
        val newFileImageName = if (isOutFileNameEmpty) DateUtils.getCreateFileName("IMG_") + suffix else fileName
        return File(folderDir, newFileImageName)
    }

    /**
     * 文件根目录
     *
     * @param context
     * @param type
     * @return
     */
    private fun getRootDirFile(context: Context, type: Int): File? {
        return if (type == CameraUtils.TYPE_VIDEO) {
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        } else {
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        }
    }

    /**
     * 创建一个临时路径，主要是解决华为手机放弃拍照后会弹出相册图片被删除的提示
     *
     * @param isVideo
     * @return
     */
    @JvmStatic
    fun createTempFile(context: Context, isVideo: Boolean): File {
        val externalFilesDir = context.getExternalFilesDir("")
        val tempCameraFile = File(externalFilesDir?.absolutePath ?: "", ".TemporaryCamera")
        if (!tempCameraFile.exists()) {
            tempCameraFile.mkdirs()
        }
        val fileName = System.currentTimeMillis().toString() + if (isVideo) CameraUtils.MP4 else CameraUtils.JPEG
        return File(tempCameraFile.absolutePath, fileName)
    }

    /**
     * 生成uri
     *
     * @param context
     * @param cameraFile
     * @return
     */
    @JvmStatic
    fun parUri(context: Context, cameraFile: File): Uri {
        val authority = context.packageName + ".luckProvider"
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            //通过FileProvider创建一个content类型的Uri
            FileProvider.getUriForFile(context, authority, cameraFile)
        } else {
            Uri.fromFile(cameraFile)
        }
    }

    /**
     * is content://
     *
     * @param url
     * @return
     */
    @JvmStatic
    fun isContent(url: String?): Boolean {
        return !url.isNullOrEmpty() && url.startsWith("content://")
    }

    /**
     * 文件复制
     *
     * @param context
     * @param originalPath
     * @param newPath
     * @return
     */
    @JvmStatic
    fun copyPath(context: Context, originalPath: String, newPath: String): Boolean {
        var fos: FileOutputStream? = null
        var stream: ByteArrayOutputStream? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(originalPath, options)
            options.inSampleSize = BitmapUtils.computeSize(options.outWidth, options.outHeight)
            options.inJustDecodeBounds = false

            val newBitmap = BitmapUtils.toHorizontalMirror(BitmapFactory.decodeFile(originalPath, options))
            stream = ByteArrayOutputStream()
            newBitmap.compress(
                if (newBitmap.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                90,
                stream
            )
            newBitmap.recycle()
            fos = FileOutputStream(newPath)
            fos.write(stream.toByteArray())
            fos.flush()
            deleteFile(context, originalPath)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            close(fos)
            close(stream)
        }
        return false
    }

    /**
     * 复制文件
     *
     * @param inputStream 文件输入流
     * @param outputStream 文件输出流
     * @return
     */
    @JvmStatic
    fun writeFileFromIS(inputStream: InputStream, outputStream: OutputStream): Boolean {
        var osBuffer: BufferedOutputStream? = null
        var isBuffer: BufferedInputStream? = null
        try {
            isBuffer = BufferedInputStream(inputStream)
            osBuffer = BufferedOutputStream(outputStream)
            val data = ByteArray(1024)
            var len = isBuffer.read(data)
            while (len != -1) {
                outputStream.write(data, 0, len)
                len = isBuffer.read(data)
            }
            outputStream.flush()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            close(isBuffer)
            close(osBuffer)
        }
    }

    /**
     * delete camera PATH
     *
     * @param context Context
     * @param path    path
     */
    @JvmStatic
    fun deleteFile(context: Context, path: String) {
        try {
            if (isContent(path)) {
                context.contentResolver.delete(Uri.parse(path), null, null)
            } else {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressWarnings("ConstantConditions")
    @JvmStatic
    fun close(@Nullable c: Closeable?) {
        // java.lang.IncompatibleClassChangeError: interface not implemented
        if (c is Closeable) {
            try {
                c.close()
            } catch (e: Exception) {
                // silence
            }
        }
    }
}

