package top.zibin.luban.io

import android.content.ContentResolver
import android.net.Uri
import java.io.Closeable
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.HashSet
import java.util.concurrent.ConcurrentHashMap

/**
 * @author：luck
 * @date：2021/8/26 4:07 下午
 * @describe：ArrayPoolProvide
 */
class ArrayPoolProvide private constructor() {
    /**
     * Uri对应的BufferedInputStreamWrap缓存Key
     */
    private val keyCache = HashSet<String>()

    /**
     * Uri对应的BufferedInputStreamWrap缓存数据
     */
    private val bufferedLruCache = ConcurrentHashMap<String, BufferedInputStreamWrap>()

    /**
     * byte[]数组的缓存队列
     */
    private val arrayPool = LruArrayPool(LruArrayPool.DEFAULT_SIZE)

    /**
     * 获取相应的byte数组
     *
     * @param bufferSize
     */
    fun get(bufferSize: Int): ByteArray {
        return arrayPool.get(bufferSize, ByteArray::class.java)
    }

    /**
     * 缓存相应的byte数组
     *
     * @param buffer
     */
    fun put(buffer: ByteArray) {
        arrayPool.put(buffer)
    }

    /**
     * ContentResolver openInputStream
     *
     * @param resolver ContentResolver
     * @param uri      data
     * @return
     */
    fun openInputStream(resolver: ContentResolver, uri: Uri): InputStream {
        var bufferedInputStreamWrap: BufferedInputStreamWrap?
        bufferedInputStreamWrap = try {
            val cached = bufferedLruCache[uri.toString()]
            if (cached != null) {
                cached.reset()
                cached
            } else {
                wrapInputStream(resolver, uri)
            }
        } catch (e: Exception) {
            try {
                return resolver.openInputStream(uri) ?: throw IOException("Failed to open input stream")
            } catch (exception: Exception) {
                exception.printStackTrace()
                wrapInputStream(resolver, uri)
            }
        }
        return bufferedInputStreamWrap ?: throw IOException("Failed to create input stream")
    }

    /**
     * open real path FileInputStream
     *
     * @param path data
     * @return
     */
    fun openInputStream(path: String): InputStream {
        val bufferedInputStreamWrap = try {
            val cached = bufferedLruCache[path]
            if (cached != null) {
                cached.reset()
                cached
            } else {
                wrapInputStream(path)
            }
        } catch (e: Exception) {
            wrapInputStream(path)
        }
        return bufferedInputStreamWrap ?: throw IOException("Failed to create input stream")
    }

    /**
     * BufferedInputStreamWrap
     *
     * @param resolver ContentResolver
     * @param uri      data
     */
    private fun wrapInputStream(resolver: ContentResolver, uri: Uri): BufferedInputStreamWrap? {
        return try {
            val inputStream = resolver.openInputStream(uri) ?: return null
            val bufferedInputStreamWrap = BufferedInputStreamWrap(inputStream)
            val available = bufferedInputStreamWrap.available()
            bufferedInputStreamWrap.mark(
                if (available > 0) available else BufferedInputStreamWrap.DEFAULT_MARK_READ_LIMIT
            )
            bufferedLruCache[uri.toString()] = bufferedInputStreamWrap
            keyCache.add(uri.toString())
            bufferedInputStreamWrap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * BufferedInputStreamWrap
     *
     * @param path data
     */
    private fun wrapInputStream(path: String): BufferedInputStreamWrap? {
        return try {
            val bufferedInputStreamWrap = BufferedInputStreamWrap(FileInputStream(path))
            val available = bufferedInputStreamWrap.available()
            bufferedInputStreamWrap.mark(
                if (available > 0) available else BufferedInputStreamWrap.DEFAULT_MARK_READ_LIMIT
            )
            bufferedLruCache[path] = bufferedInputStreamWrap
            keyCache.add(path)
            bufferedInputStreamWrap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 清空内存占用
     */
    fun clearMemory() {
        for (key in keyCache) {
            val inputStreamWrap = bufferedLruCache[key]
            close(inputStreamWrap)
            bufferedLruCache.remove(key)
        }
        keyCache.clear()
        arrayPool.clearMemory()
    }

    companion object {
        @Volatile
        private var mInstance: ArrayPoolProvide? = null

        @JvmStatic
        fun getInstance(): ArrayPoolProvide {
            if (mInstance == null) {
                synchronized(ArrayPoolProvide::class.java) {
                    if (mInstance == null) {
                        mInstance = ArrayPoolProvide()
                    }
                }
            }
            return mInstance!!
        }

        @Suppress("ConstantConditions")
        @JvmStatic
        fun close(c: Closeable?) {
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
}

