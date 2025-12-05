package top.zibin.luban

import top.zibin.luban.io.ArrayPoolProvide
import java.io.IOException
import java.io.InputStream

/**
 * Automatically close the previous InputStream when opening a new InputStream,
 * and finally need to manually call [close] to release the resource.
 */
abstract class InputStreamAdapter : InputStreamProvider {
    @Throws(IOException::class)
    override fun open(): InputStream {
        return openInternal()
    }

    @Throws(IOException::class)
    abstract fun openInternal(): InputStream

    override fun close() {
        ArrayPoolProvide.getInstance().clearMemory()
    }
}

