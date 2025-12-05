package top.zibin.luban.io

/**
 * @author：luck
 * @date：2021/8/26 3:21 下午
 * @describe：IntegerArrayAdapter
 */
object IntegerArrayAdapter : ArrayAdapterInterface<IntArray> {
    private const val TAG = "IntegerArrayPool"

    override fun getTag(): String {
        return TAG
    }

    override fun getArrayLength(array: IntArray): Int {
        return array.size
    }

    override fun newArray(length: Int): IntArray {
        return IntArray(length)
    }

    override fun getElementSizeInBytes(): Int {
        return 4
    }
}

