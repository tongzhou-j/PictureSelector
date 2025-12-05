package top.zibin.luban.io

import android.annotation.SuppressLint
import android.util.Log
import java.util.HashMap
import java.util.NavigableMap
import java.util.TreeMap

/**
 * @author：luck
 * @date：2021/8/26 3:15 下午
 * @describe：LruArrayPool
 */
class LruArrayPool : ArrayPool {
    companion object {
        // 4MB.
        const val DEFAULT_SIZE = 4 * 1024 * 1024

        /**
         * The maximum number of times larger an int array may be to be than a requested size to eligible
         * to be returned from the pool.
         */
        private const val MAX_OVER_SIZE_MULTIPLE = 8
        /**
         * Used to calculate the maximum % of the total pool size a single byte array may consume.
         */
        private const val SINGLE_ARRAY_MAX_SIZE_DIVISOR = 2
    }

    private val groupedMap = GroupedLinkedMap<Key, Any>()
    private val keyPool = KeyPool()
    private val sortedSizes = HashMap<Class<*>, NavigableMap<Int, Int>>()
    private val adapters = HashMap<Class<*>, ArrayAdapterInterface<*>>()
    private val maxSize: Int
    private var currentSize: Int = 0

    constructor() {
        maxSize = DEFAULT_SIZE
    }

    /**
     * Constructor for a new pool.
     *
     * @param maxSize The maximum size in integers of the pool.
     */
    constructor(maxSize: Int) {
        this.maxSize = maxSize
    }

    @Deprecated("Use put")
    override fun <T> put(array: T, arrayClass: Class<T>) {
        put(array)
    }

    @Synchronized
    override fun <T> put(array: T) {
        @Suppress("UNCHECKED_CAST")
        val arrayClass = array!!::class.java as Class<T>

        val arrayAdapter = getAdapterFromType(arrayClass)
        val size = arrayAdapter.getArrayLength(array as T)
        val arrayBytes = size * arrayAdapter.getElementSizeInBytes()
        if (!isSmallEnoughForReuse(arrayBytes)) {
            return
        }
        val key = keyPool.get(size, arrayClass)

        groupedMap.put(key, array as Any)
        val sizes = getSizesForAdapter(arrayClass)
        val current = sizes[key.size]
        sizes[key.size] = (current ?: 0) + 1
        currentSize += arrayBytes
        evict()
    }

    @Synchronized
    override fun <T> get(size: Int, arrayClass: Class<T>): T {
        val possibleSize = getSizesForAdapter(arrayClass).ceilingKey(size)
        val key: Key
        key = if (mayFillRequest(size, possibleSize)) {
            keyPool.get(possibleSize!!, arrayClass)
        } else {
            keyPool.get(size, arrayClass)
        }
        return getForKey(key, arrayClass)
    }

    private fun <T> getForKey(key: Key, arrayClass: Class<T>): T {
        val arrayAdapter = getAdapterFromType(arrayClass)
        var result = getArrayForKey(key) as T?
        if (result != null) {
            currentSize -= arrayAdapter.getArrayLength(result) * arrayAdapter.getElementSizeInBytes()
            decrementArrayOfSize(arrayAdapter.getArrayLength(result), arrayClass)
        }

        if (result == null) {
            if (Log.isLoggable(arrayAdapter.getTag(), Log.VERBOSE)) {
                Log.v(arrayAdapter.getTag(), "Allocated " + key.size + " bytes")
            }
            result = arrayAdapter.newArray(key.size)
        }
        return result!!
    }

    // Our cast is safe because the Key is based on the type.
    @Suppress("UNCHECKED_CAST")
    private fun <T> getArrayForKey(key: Key): T? {
        return groupedMap.get(key) as? T
    }

    private fun isSmallEnoughForReuse(byteSize: Int): Boolean {
        return byteSize <= maxSize / SINGLE_ARRAY_MAX_SIZE_DIVISOR
    }

    private fun mayFillRequest(requestedSize: Int, actualSize: Int?): Boolean {
        return actualSize != null
            && (isNoMoreThanHalfFull() || actualSize <= MAX_OVER_SIZE_MULTIPLE * requestedSize)
    }

    private fun isNoMoreThanHalfFull(): Boolean {
        return currentSize == 0 || maxSize / currentSize >= 2
    }

    @Synchronized
    override fun clearMemory() {
        evictToSize(0)
    }

    private fun evict() {
        evictToSize(maxSize)
    }

    @SuppressLint("RestrictedApi")
    private fun evictToSize(size: Int) {
        while (currentSize > size) {
            val evicted = groupedMap.removeLast() ?: break
            @Suppress("UNCHECKED_CAST")
            val arrayAdapter = getAdapterFromType(evicted.javaClass) as ArrayAdapterInterface<Any>
            val arrayLength = arrayAdapter.getArrayLength(evicted)
            currentSize -= arrayLength * arrayAdapter.getElementSizeInBytes()
            decrementArrayOfSize(arrayLength, evicted.javaClass)
            if (Log.isLoggable(arrayAdapter.getTag(), Log.VERBOSE)) {
                Log.v(arrayAdapter.getTag(), "evicted: $arrayLength")
            }
        }
    }

    private fun decrementArrayOfSize(size: Int, arrayClass: Class<*>) {
        val sizes = getSizesForAdapter(arrayClass)
        val current = sizes[size]
            ?: throw NullPointerException(
                "Tried to decrement empty size, size: $size, this: $this"
            )
        if (current == 1) {
            sizes.remove(size)
        } else {
            sizes[size] = current - 1
        }
    }

    private fun getSizesForAdapter(arrayClass: Class<*>): NavigableMap<Int, Int> {
        var sizes = sortedSizes[arrayClass]
        if (sizes == null) {
            sizes = TreeMap()
            sortedSizes[arrayClass] = sizes
        }
        return sizes
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getAdapterFromObject(`object`: Any): ArrayAdapterInterface<T> {
        return getAdapterFromType(`object`.javaClass) as ArrayAdapterInterface<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getAdapterFromType(arrayPoolClass: Class<T>): ArrayAdapterInterface<T> {
        var adapter = adapters[arrayPoolClass]
        if (adapter == null) {
            adapter = when {
                arrayPoolClass == IntArray::class.java -> IntegerArrayAdapter
                arrayPoolClass == ByteArray::class.java -> ByteArrayAdapter
                else -> throw IllegalArgumentException(
                    "No array pool found for: " + arrayPoolClass.simpleName
                )
            }
            adapters[arrayPoolClass] = adapter
        }
        return adapter as ArrayAdapterInterface<T>
    }

    // VisibleForTesting
    fun getCurrentSize(): Int {
        var currentSize = 0
        for (type in sortedSizes.keys) {
            for (size in sortedSizes[type]!!.keys) {
                val adapter = getAdapterFromType(type)
                currentSize += size * sortedSizes[type]!![size]!! * adapter.getElementSizeInBytes()
            }
        }
        return currentSize
    }

    private class KeyPool : BaseKeyPool<Key>() {
        fun get(size: Int, arrayClass: Class<*>): Key {
            val result = get()
            result.init(size, arrayClass)
            return result
        }

        override fun create(): Key {
            return Key(this)
        }
    }

    private class Key(private val pool: KeyPool) : PoolAble {
        var size: Int = 0
        private var arrayClass: Class<*>? = null

        fun init(length: Int, arrayClass: Class<*>) {
            this.size = length
            this.arrayClass = arrayClass
        }

        override fun equals(other: Any?): Boolean {
            if (other is Key) {
                return size == other.size && arrayClass == other.arrayClass
            }
            return false
        }

        override fun toString(): String {
            return "Key{size=$size array=$arrayClass}"
        }

        override fun offer() {
            pool.offer(this)
        }

        override fun hashCode(): Int {
            var result = size
            result = 31 * result + (arrayClass?.hashCode() ?: 0)
            return result
        }
    }
}

