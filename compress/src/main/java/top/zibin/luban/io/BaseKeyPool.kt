package top.zibin.luban.io

import java.util.ArrayDeque
import java.util.Queue

/**
 * @author：luck
 * @date：2021/8/26 3:13 下午
 * @describe：BaseKeyPool
 */
abstract class BaseKeyPool<T : PoolAble> {
    companion object {
        private const val MAX_SIZE = 20
        
        fun <T> createQueue(size: Int): Queue<T> {
            return ArrayDeque(size)
        }
    }

    private val keyPool: Queue<T> = createQueue(MAX_SIZE)

    fun get(): T {
        val result = keyPool.poll()
        return result ?: create()
    }

    fun offer(key: T) {
        if (keyPool.size < MAX_SIZE) {
            keyPool.offer(key)
        }
    }

    abstract fun create(): T
}

