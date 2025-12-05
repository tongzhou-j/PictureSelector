package top.zibin.luban

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import top.zibin.luban.io.ArrayPoolProvide
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList

@Suppress("unused")
class Luban private constructor(builder: Builder) : Handler.Callback {
    companion object {
        private const val TAG = "Luban"
        private const val DEFAULT_DISK_CACHE_DIR = "luban_disk_cache"
        private const val MSG_COMPRESS_SUCCESS = 0
        private const val MSG_COMPRESS_START = 1
        private const val MSG_COMPRESS_ERROR = 2
        private const val KEY_SOURCE = "source"

        @JvmStatic
        fun with(context: Context): Builder {
            return Builder(context)
        }
    }

    private var mTargetDir: String? = builder.mTargetDir
    private val focusAlpha: Boolean = builder.focusAlpha
    private val isUseIOBufferPool: Boolean = builder.isUseBufferPool
    private val mRenameListener: OnRenameListener? = builder.mRenameListener
    private val mStreamProviders: MutableList<InputStreamProvider> = builder.mStreamProviders
    private val mCompressListener: OnCompressListener? = builder.mCompressListener
    private val mNewCompressListener: OnNewCompressListener? = builder.mNewCompressListener
    private val mLeastCompressSize: Int = builder.mLeastCompressSize
    private val mCompressionPredicate: CompressionPredicate? = builder.mCompressionPredicate

    private val mHandler: Handler = Handler(Looper.getMainLooper(), this)

    /**
     * Returns a file with a cache image name in the private cache directory.
     *
     * @param context A context.
     */
    private fun getImageCacheFile(context: Context, suffix: String?): File {
        if (TextUtils.isEmpty(mTargetDir)) {
            mTargetDir = getImageCacheDir(context)?.absolutePath
        }

        val cacheBuilder = mTargetDir + "/" +
                System.currentTimeMillis() +
                (Math.random() * 1000).toInt() +
                (if (TextUtils.isEmpty(suffix)) ".jpg" else suffix)

        return File(cacheBuilder)
    }

    private fun getImageCustomFile(context: Context, filename: String): File {
        if (TextUtils.isEmpty(mTargetDir)) {
            mTargetDir = getImageCacheDir(context)?.absolutePath
        }

        val cacheBuilder = mTargetDir + "/" + filename

        return File(cacheBuilder)
    }

    /**
     * Returns a directory with a default name in the private cache directory of the application to
     * use to store retrieved audio.
     *
     * @param context A context.
     * @see getImageCacheDir
     */
    private fun getImageCacheDir(context: Context): File? {
        return getImageCacheDir(context, DEFAULT_DISK_CACHE_DIR)
    }

    /**
     * Returns a directory with the given name in the private cache directory of the application to
     * use to store retrieved media and thumbnails.
     *
     * @param context   A context.
     * @param cacheName The name of the subdirectory in which to store the cache.
     * @see getImageCacheDir
     */
    private fun getImageCacheDir(context: Context, cacheName: String): File? {
        val cacheDir = context.externalCacheDir
        if (cacheDir != null) {
            val result = File(cacheDir, cacheName)
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory)) {
                // File wasn't able to create a directory, or the result exists but not a directory
                return null
            }
            return result
        }
        if (Log.isLoggable(TAG, Log.ERROR)) {
            Log.e(TAG, "default disk cache dir is null")
        }
        return null
    }

    /**
     * start asynchronous compress thread
     */
    private fun launch(context: Context) {
        if (mStreamProviders.isEmpty()) {
            mCompressListener?.onError(-1, NullPointerException("image file cannot be null"))
            mNewCompressListener?.onError("", NullPointerException("image file cannot be null"))
            return
        }

        val iterator = mStreamProviders.iterator()

        while (iterator.hasNext()) {
            val path = iterator.next()

            AsyncTask.SERIAL_EXECUTOR.execute {
                try {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_COMPRESS_START))
                    val result = compress(context, path)
                    val message = mHandler.obtainMessage(MSG_COMPRESS_SUCCESS)
                    message.arg1 = path.getIndex()
                    message.obj = result
                    val bundle = Bundle()
                    bundle.putString(KEY_SOURCE, path.getPath())
                    message.data = bundle
                    mHandler.sendMessage(message)
                } catch (e: Exception) {
                    val message = mHandler.obtainMessage(MSG_COMPRESS_ERROR)
                    message.arg1 = path.getIndex()
                    val bundle = Bundle()
                    bundle.putString(KEY_SOURCE, path.getPath())
                    message.data = bundle
                    mHandler.sendMessage(message)
                }
            }

            iterator.remove()
        }
    }

    /**
     * start compress and return the file
     */
    @Throws(IOException::class)
    private fun get(input: InputStreamProvider, context: Context): File {
        return try {
            Engine(input, getImageCacheFile(context, Checker.SINGLE.extSuffix(input)), focusAlpha).compress()
        } finally {
            input.close()
        }
    }

    @Throws(IOException::class)
    private fun get(context: Context): List<File> {
        val results = ArrayList<File>()
        val iterator = mStreamProviders.iterator()

        while (iterator.hasNext()) {
            results.add(compress(context, iterator.next()))
            iterator.remove()
        }

        return results
    }

    @Throws(IOException::class)
    private fun compress(context: Context, path: InputStreamProvider): File {
        return try {
            compressReal(context, path)
        } finally {
            path.close()
        }
    }

    @Throws(IOException::class)
    private fun compressReal(context: Context, path: InputStreamProvider): File {
        var outFile = getImageCacheFile(context, Checker.SINGLE.extSuffix(path))
        val source = if (Checker.SINGLE.isContent(path.getPath())) {
            LubanUtils.getPath(context, Uri.parse(path.getPath()))
        } else {
            path.getPath()
        }
        if (mRenameListener != null) {
            val filename = mRenameListener.rename(source)
            outFile = getImageCustomFile(context, filename)
        }

        return if (mCompressionPredicate != null) {
            if (mCompressionPredicate.apply(source)
                && Checker.SINGLE.needCompress(mLeastCompressSize, source)
            ) {
                Engine(path, outFile, focusAlpha).compress()
            } else {
                // Ignore compression
                File(source)
            }
        } else {
            if (Checker.SINGLE.needCompress(mLeastCompressSize, source)) {
                Engine(path, outFile, focusAlpha).compress()
            } else {
                // Ignore compression
                File(source)
            }
        }
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_COMPRESS_START -> {
                mCompressListener?.onStart()
                mNewCompressListener?.onStart()
            }
            MSG_COMPRESS_SUCCESS -> {
                mCompressListener?.onSuccess(msg.arg1, msg.obj as File)
                mNewCompressListener?.onSuccess(msg.data.getString(KEY_SOURCE) ?: "", msg.obj as File)
            }
            MSG_COMPRESS_ERROR -> {
                mCompressListener?.onError(msg.arg1, msg.obj as? Throwable ?: Exception())
                mNewCompressListener?.onError(msg.data.getString(KEY_SOURCE) ?: "", msg.obj as? Throwable ?: Exception())
            }
        }
        return false
    }

    class Builder(private val context: Context) {
        var mTargetDir: String? = null
        var focusAlpha: Boolean = false
        var isUseBufferPool: Boolean = true
        var mLeastCompressSize: Int = 100
        var mRenameListener: OnRenameListener? = null
        var mCompressListener: OnCompressListener? = null
        var mNewCompressListener: OnNewCompressListener? = null
        var mCompressionPredicate: CompressionPredicate? = null
        val mStreamProviders: MutableList<InputStreamProvider> = ArrayList()

        private fun build(): Luban {
            return Luban(this)
        }

        fun load(inputStreamProvider: InputStreamProvider): Builder {
            mStreamProviders.add(inputStreamProvider)
            return this
        }

        fun <T> load(list: List<T>): Builder {
            var index = -1
            for (src in list) {
                index++
                when (src) {
                    is String -> load(src, index)
                    is File -> load(src, index)
                    is Uri -> load(src, index)
                    else -> throw IllegalArgumentException("Incoming data type exception, it must be String, File, Uri or Bitmap")
                }
            }
            return this
        }

        fun load(file: File): Builder {
            load(file, 0)
            return this
        }

        private fun load(file: File, index: Int): Builder {
            mStreamProviders.add(object : InputStreamAdapter() {
                override fun openInternal(): InputStream {
                    return ArrayPoolProvide.getInstance().openInputStream(file.absolutePath)
                }

                override fun getIndex(): Int {
                    return index
                }

                override fun getPath(): String {
                    return file.absolutePath
                }
            })
            return this
        }

        fun load(string: String): Builder {
            load(string, 0)
            return this
        }

        private fun load(string: String, index: Int): Builder {
            mStreamProviders.add(object : InputStreamAdapter() {
                override fun openInternal(): InputStream {
                    return ArrayPoolProvide.getInstance().openInputStream(string)
                }

                override fun getIndex(): Int {
                    return index
                }

                override fun getPath(): String {
                    return string
                }
            })
            return this
        }

        fun load(uri: Uri): Builder {
            load(uri, 0)
            return this
        }

        private fun load(uri: Uri, index: Int): Builder {
            mStreamProviders.add(object : InputStreamAdapter() {
                @Throws(IOException::class)
                override fun openInternal(): InputStream {
                    return if (isUseBufferPool) {
                        ArrayPoolProvide.getInstance().openInputStream(context.contentResolver, uri)
                    } else {
                        context.contentResolver.openInputStream(uri) ?: throw IOException("Failed to open input stream")
                    }
                }

                override fun getIndex(): Int {
                    return index
                }

                override fun getPath(): String {
                    return if (Checker.SINGLE.isContent(uri.toString())) {
                        uri.toString()
                    } else {
                        uri.path ?: ""
                    }
                }
            })
            return this
        }

        @Deprecated("No longer used")
        fun putGear(gear: Int): Builder {
            return this
        }

        fun setRenameListener(listener: OnRenameListener?): Builder {
            this.mRenameListener = listener
            return this
        }

        fun setCompressListener(listener: OnCompressListener?): Builder {
            this.mCompressListener = listener
            return this
        }

        fun setCompressListener(listener: OnNewCompressListener?): Builder {
            this.mNewCompressListener = listener
            return this
        }

        fun setTargetDir(targetDir: String?): Builder {
            this.mTargetDir = targetDir
            return this
        }

        /**
         * Do I need to keep the image's alpha channel
         *
         * @param focusAlpha <p> true - to keep alpha channel, the compress speed will be slow. </p>
         *                   <p> false - don't keep alpha channel, it might have a black background.</p>
         */
        @Deprecated("No longer used")
        fun setFocusAlpha(focusAlpha: Boolean): Builder {
            this.focusAlpha = focusAlpha
            return this
        }

        /**
         * getContentResolver().openInputStream(); open using buffer pool mode
         *
         * @param isUseBufferPool
         * @return
         */
        fun isUseIOBufferPool(isUseBufferPool: Boolean): Builder {
            this.isUseBufferPool = isUseBufferPool
            return this
        }

        /**
         * do not compress when the origin image file size less than one value
         *
         * @param size the value of file size, unit KB, default 100K
         */
        fun ignoreBy(size: Int): Builder {
            this.mLeastCompressSize = size
            return this
        }

        /**
         * do compress image when return value was true, otherwise, do not compress the image file
         *
         * @param compressionPredicate A predicate callback that returns true or false for the given input path should be compressed.
         */
        fun filter(compressionPredicate: CompressionPredicate?): Builder {
            this.mCompressionPredicate = compressionPredicate
            return this
        }

        /**
         * begin compress image with asynchronous
         */
        fun launch() {
            build().launch(context)
        }

        @Throws(IOException::class)
        fun get(path: String): File {
            return get(path, 0)
        }

        @Throws(IOException::class)
        fun get(path: String, index: Int): File {
            return build().get(object : InputStreamAdapter() {
                override fun openInternal(): InputStream {
                    return ArrayPoolProvide.getInstance().openInputStream(path)
                }

                override fun getIndex(): Int {
                    return index
                }

                override fun getPath(): String {
                    return path
                }
            }, context)
        }

        /**
         * begin compress image with synchronize
         *
         * @return the thumb image file list
         */
        @Throws(IOException::class)
        fun get(): List<File> {
            return build().get(context)
        }
    }
}

