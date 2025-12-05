package top.zibin.luban.io

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * @author：luck
 * @date：2021/8/26 3:21 下午
 * @describe：BufferedInputStreamWrap
 */
class BufferedInputStreamWrap : FilterInputStream {
    companion object {
        const val DEFAULT_MARK_READ_LIMIT = 8 * 1024 * 1024
    }

    /**
     * The buffer containing the current bytes read from the target InputStream.
     */
    @Volatile
    private var buf: ByteArray?

    /**
     * The total number of bytes inside the byte array [buf].
     */
    private var count: Int = 0

    /**
     * The current limit, which when passed, invalidates the current mark.
     */
    private var markLimit: Int = 0

    /**
     * The currently marked position. -1 indicates no mark has been put or the mark has been
     * invalidated.
     */
    private var markPos: Int = -1

    /**
     * The current position within the byte array [buf].
     */
    private var pos: Int = 0

    constructor(`in`: InputStream) : this(`in`, 64 * 1024)

    internal constructor(`in`: InputStream, bufferSize: Int) : super(`in`) {
        buf = ArrayPoolProvide.getInstance().get(bufferSize)
    }

    /**
     * Returns an estimated number of bytes that can be read or skipped without blocking for more
     * input. This method returns the number of bytes available in the buffer plus those available in
     * the source stream, but see [InputStream.available] for important caveats.
     *
     * @return the estimated number of bytes available
     * @throws IOException if this stream is closed or an error occurs
     */
    @Synchronized
    @Throws(IOException::class)
    override fun available(): Int {
        // in could be invalidated by close().
        val localIn = `in`
        val localBuf = buf
        if (localBuf == null || localIn == null) {
            return 0
        }
        return count - pos + localIn.available()
    }

    private fun streamClosed(): IOException {
        throw IOException("BufferedInputStream is closed")
    }

    /**
     * Reduces the mark limit to match the current buffer length to prevent the buffer from continuing
     * to increase in size.
     *
     * <p>Subsequent calls to [mark] will be obeyed and may cause the buffer size to
     * increase.
     */
    // Public API.
    @Synchronized
    fun fixMarkLimit() {
        buf?.let { markLimit = it.size }
    }

    @Synchronized
    fun release() {
        buf?.let {
            ArrayPoolProvide.getInstance().put(it)
            buf = null
        }
    }

    /**
     * Closes this stream. The source stream is closed and any resources associated with it are
     * released.
     *
     * @throws IOException if an error occurs while closing this stream.
     */
    @Throws(IOException::class)
    override fun close() {
        buf?.let {
            ArrayPoolProvide.getInstance().put(it)
            buf = null
        }
        val localIn = `in`
        `in` = null
        localIn?.close()
    }

    @Throws(IOException::class)
    private fun fillbuf(localIn: InputStream, localBuf: ByteArray): Int {
        if (markPos == -1 || pos - markPos >= markLimit) {
            // Mark position not put or exceeded readLimit
            val result = localIn.read(localBuf)
            if (result > 0) {
                markPos = -1
                pos = 0
                count = result
            }
            return result
        }
        // Added count == localBuf.length so that we do not immediately double the buffer size before
        // reading any data
        // when markLimit > localBuf.length. Instead, we will double the buffer size only after
        // reading the initial
        // localBuf worth of data without finding what we're looking for in the stream. This allows
        // us to put a
        // relatively small initial buffer size and a large markLimit for safety without causing an
        // allocation each time
        // read is called.
        if (markPos == 0 && markLimit > localBuf.size && count == localBuf.size) {
            // Increase buffer size to accommodate the readLimit
            var newLength = localBuf.size * 2
            if (newLength > markLimit) {
                newLength = markLimit
            }
            val newbuf = ArrayPoolProvide.getInstance().get(newLength)
            System.arraycopy(localBuf, 0, newbuf, 0, localBuf.size)
            val oldbuf = localBuf
            // Reassign buf, which will invalidate any local references
            // FIXME: what if buf was null?
            buf = newbuf
            ArrayPoolProvide.getInstance().put(oldbuf)
            return fillbuf(localIn, newbuf)
        } else if (markPos > 0) {
            System.arraycopy(localBuf, markPos, localBuf, 0, localBuf.size - markPos)
        }
        // Set the new position and mark position
        pos -= markPos
        count = 0
        markPos = 0
        val byteRead = localIn.read(localBuf, pos, localBuf.size - pos)
        count = if (byteRead <= 0) pos else pos + byteRead
        return byteRead
    }

    /**
     * Sets a mark position in this stream. The parameter [readlimit] indicates how many bytes
     * can be read before a mark is invalidated. Calling [reset] will reposition the stream
     * back to the marked position if [readlimit] has not been surpassed. The underlying buffer
     * may be increased in size to allow [readlimit] number of bytes to be supported.
     *
     * @param readLimit the number of bytes that can be read before the mark is invalidated.
     * @see reset
     */
    @Synchronized
    override fun mark(readLimit: Int) {
        // This is stupid, but BitmapFactory.decodeStream calls mark(1024)
        // which is too small for a substantial portion of images. This
        // change (using Math.max) ensures that we don't overwrite readLimit
        // with a smaller value
        markLimit = markLimit.coerceAtLeast(readLimit)
        markPos = pos
    }

    /**
     * Indicates whether [BufferedInputStreamWrap] supports the [mark] and [
     * reset] methods.
     *
     * @return [true] for BufferedInputStreams.
     * @see mark
     * @see reset
     */
    override fun markSupported(): Boolean {
        return true
    }

    /**
     * Reads a single byte from this stream and returns it as an integer in the range from 0 to 255.
     * Returns -1 if the end of the source string has been reached. If the internal buffer does not
     * contain any available bytes then it is filled from the source stream and the first byte is
     * returned.
     *
     * @return the byte read or -1 if the end of the source stream has been reached.
     * @throws IOException if this stream is closed or another IOException occurs.
     */
    @Synchronized
    @Throws(IOException::class)
    override fun read(): Int {
        // Use local refs since buf and in may be invalidated by an
        // unsynchronized close()
        var localBuf = buf
        val localIn = `in`
        if (localBuf == null || localIn == null) {
            throw streamClosed()
        }

        // Are there buffered bytes available?
        if (pos >= count && fillbuf(localIn, localBuf) == -1) {
            // no, fill buffer
            return -1
        }
        // localBuf may have been invalidated by fillbuf
        if (localBuf != buf) {
            localBuf = buf
            if (localBuf == null) {
                throw streamClosed()
            }
        }

        // Did filling the buffer fail with -1 (EOF)?
        return if (count - pos > 0) {
            localBuf[pos++].toInt() and 0xFF
        } else {
            -1
        }
    }

    /**
     * Reads at most [byteCount] bytes from this stream and stores them in byte array [
     * buffer] starting at offset [offset]. Returns the number of bytes actually read or -1 if
     * no bytes were read and the end of the stream was encountered. If all the buffered bytes have
     * been used, a mark has not been put and the requested number of bytes is larger than the
     * receiver's buffer size, this implementation bypasses the buffer and simply places the results
     * directly into [buffer].
     *
     * @param buffer the byte array in which to store the bytes read.
     * @return the number of bytes actually read or -1 if end of stream.
     * @throws IndexOutOfBoundsException if [offset] < 0 or [byteCount] < 0, or if [
     * offset] + [byteCount] is greater than the size of [buffer].
     * @throws IOException               if the stream is already closed or another IOException occurs.
     */
    @Synchronized
    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, byteCount: Int): Int {
        // Use local ref since buf may be invalidated by an unsynchronized close()
        var localBuf = buf
        if (localBuf == null) {
            throw streamClosed()
        }
        // Arrays.checkOffsetAndCount(buffer.length, offset, byteCount);
        if (byteCount == 0) {
            return 0
        }
        val localIn = `in`
        if (localIn == null) {
            throw streamClosed()
        }

        val required: Int
        if (pos < count) {
            // There are bytes available in the buffer.
            val copylength = (count - pos).coerceAtMost(byteCount)
            System.arraycopy(localBuf, pos, buffer, offset, copylength)
            pos += copylength
            if (copylength == byteCount || localIn.available() == 0) {
                return copylength
            }
            val newOffset = offset + copylength
            required = byteCount - copylength
            return read(buffer, newOffset, required) + copylength
        } else {
            required = byteCount
        }

        var currentOffset = offset
        var remaining = required
        while (true) {
            val read: Int
            // If we're not marked and the required size is greater than the buffer,
            // simply read the bytes directly bypassing the buffer.
            if (markPos == -1 && remaining >= localBuf!!.size) {
                read = localIn.read(buffer, currentOffset, remaining)
                if (read == -1) {
                    return if (remaining == byteCount) -1 else byteCount - remaining
                }
            } else {
                if (fillbuf(localIn, localBuf!!) == -1) {
                    return if (remaining == byteCount) -1 else byteCount - remaining
                }
                // localBuf may have been invalidated by fillbuf
                if (localBuf != buf) {
                    localBuf = buf
                    if (localBuf == null) {
                        throw streamClosed()
                    }
                }

                read = (count - pos).coerceAtMost(remaining)
                System.arraycopy(localBuf!!, pos, buffer, currentOffset, read)
                pos += read
            }
            remaining -= read
            if (remaining == 0) {
                return byteCount
            }
            if (localIn.available() == 0) {
                return byteCount - remaining
            }
            currentOffset += read
        }
    }

    /**
     * Resets this stream to the last marked location.
     *
     * @throws IOException if this stream is closed, no mark has been put or the mark is no longer
     * valid because more than [readlimit] bytes have been read since setting the mark.
     * @see mark
     */
    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
        val localBuf = buf
        if (localBuf == null) {
            throw IOException("Stream is closed")
        }
        if (-1 == markPos) {
            throw InvalidMarkException(
                "Mark has been invalidated, pos: $pos markLimit: $markLimit"
            )
        }
        pos = markPos
    }

    /**
     * Skips [byteCount] bytes in this stream. Subsequent calls to [read] will not return
     * these bytes unless [reset] is used.
     *
     * @param byteCount the number of bytes to skip. This method does nothing and returns 0 if [
     * byteCount] is less than zero.
     * @return the number of bytes actually skipped.
     * @throws IOException if this stream is closed or another IOException occurs.
     */
    @Synchronized
    @Throws(IOException::class)
    override fun skip(byteCount: Long): Long {
        if (byteCount < 1) {
            return 0
        }
        // Use local refs since buf and in may be invalidated by an unsynchronized close()
        var localBuf = buf
        if (localBuf == null) {
            throw streamClosed()
        }
        val localIn = `in`
        if (localIn == null) {
            throw streamClosed()
        }

        if (count - pos >= byteCount) {
            pos = (pos + byteCount).toInt()
            return byteCount
        }
        // See https://errorprone.info/bugpattern/IntLongMath.
        var read = (count - pos).toLong()
        pos = count

        if (markPos != -1 && byteCount <= markLimit) {
            if (fillbuf(localIn, localBuf) == -1) {
                return read
            }
            if (localBuf != buf) {
                localBuf = buf
            }
            if (count - pos >= byteCount - read) {
                // See https://errorprone.info/bugpattern/NarrowingCompoundAssignment.
                pos = (pos + byteCount - read).toInt()
                return byteCount
            }
            // Couldn't get all the bytes, skip what we read.
            read += count - pos
            pos = count
            return read
        }
        return read + localIn.skip(byteCount - read)
    }

    /**
     * An exception thrown when a mark can no longer be obeyed because the underlying buffer size is
     * smaller than the amount of data read after the mark position.
     */
    internal class InvalidMarkException(detailMessage: String) : IOException(detailMessage)
}

