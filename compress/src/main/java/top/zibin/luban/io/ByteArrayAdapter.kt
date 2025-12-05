package top.zibin.luban.io

/**
 * @author：luck
 * @date：2021/8/26 3:20 下午
 * @describe：ByteArrayAdapter
 */
object ByteArrayAdapter : ArrayAdapterInterface<ByteArray> {
    private const val TAG = "ByteArrayPool"

    override fun getTag(): String {
        return TAG
    }

    override fun getArrayLength(array: ByteArray): Int {
        return array.size
    }

    override fun newArray(length: Int): ByteArray {
        return ByteArray(length)
    }

    override fun getElementSizeInBytes(): Int {
        return 1
    }
}

