package top.zibin.luban.io

/**
 * @author：luck
 * @date：2021/8/26 3:19 下午
 * @describe：ArrayAdapterInterface
 */
interface ArrayAdapterInterface<T> {
    /**
     * TAG for logging.
     */
    fun getTag(): String

    /**
     * Return the length of the given array.
     */
    fun getArrayLength(array: T): Int

    /**
     * Allocate and return an array of the specified size.
     */
    fun newArray(length: Int): T

    /**
     * Return the size of an element in the array in bytes (e.g. for int return 4).
     */
    fun getElementSizeInBytes(): Int
}

