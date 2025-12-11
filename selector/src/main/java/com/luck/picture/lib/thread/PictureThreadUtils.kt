package com.luck.picture.lib.thread

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.IntRange
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * @author：luck
 * @date：2020/10/30 10:56 AM
 * @describe：ThreadPool
 */
object PictureThreadUtils {
    val mainHandler: Handler = Handler(Looper.getMainLooper())

    private val TYPE_PRIORITY_POOLS: MutableMap<Int?, MutableMap<Int?, ExecutorService?>?> =
        HashMap<Int?, MutableMap<Int?, ExecutorService?>?>()

    private val TASK_POOL_MAP: MutableMap<Task<*>?, ExecutorService?> =
        ConcurrentHashMap<Task<*>?, ExecutorService?>()

    private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
    private val TIMER = Timer()

    private val TYPE_SINGLE: Byte = -1
    private val TYPE_CACHED: Byte = -2
    private val TYPE_IO: Byte = -4
    private val TYPE_CPU: Byte = -8

    private var sDeliver: Executor? = null

    val isInUiThread: Boolean
        /**
         * Return whether the thread is the main thread.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() = Looper.myLooper() == Looper.getMainLooper()

    fun runOnUiThread(runnable: Runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run()
        } else {
            mainHandler.post(runnable)
        }
    }

    fun runOnUiThreadDelayed(runnable: Runnable, delayMillis: Long) {
        mainHandler.postDelayed(runnable, delayMillis)
    }

    /**
     * Return a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.
     *
     * @param size The size of thread in the pool.
     * @return a fixed thread pool
     */
    fun getFixedPool(@IntRange(from = 1) size: Int): ExecutorService {
        return PictureThreadUtils.getPoolByTypeAndPriority(size)
    }

    /**
     * Return a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.
     *
     * @param size     The size of thread in the pool.
     * @param priority The priority of thread in the poll.
     * @return a fixed thread pool
     */
    fun getFixedPool(
        @IntRange(from = 1) size: Int,
        @IntRange(from = 1, to = 10) priority: Int
    ): ExecutorService {
        return PictureThreadUtils.getPoolByTypeAndPriority(size, priority)
    }

    val singlePool: ExecutorService
        /**
         * Return a thread pool that uses a single worker thread operating
         * off an unbounded queue, and uses the provided ThreadFactory to
         * create a new thread when needed.
         *
         * @return a single thread pool
         */
        get() = PictureThreadUtils.getPoolByTypeAndPriority(PictureThreadUtils.TYPE_SINGLE.toInt())

    /**
     * Return a thread pool that uses a single worker thread operating
     * off an unbounded queue, and uses the provided ThreadFactory to
     * create a new thread when needed.
     *
     * @param priority The priority of thread in the poll.
     * @return a single thread pool
     */
    fun getSinglePool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService {
        return PictureThreadUtils.getPoolByTypeAndPriority(
            PictureThreadUtils.TYPE_SINGLE.toInt(),
            priority
        )
    }

    val cachedPool: ExecutorService
        /**
         * Return a thread pool that creates new threads as needed, but
         * will reuse previously constructed threads when they are
         * available.
         *
         * @return a cached thread pool
         */
        get() = PictureThreadUtils.getPoolByTypeAndPriority(PictureThreadUtils.TYPE_CACHED.toInt())

    /**
     * Return a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.
     *
     * @param priority The priority of thread in the poll.
     * @return a cached thread pool
     */
    fun getCachedPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService {
        return PictureThreadUtils.getPoolByTypeAndPriority(
            PictureThreadUtils.TYPE_CACHED.toInt(),
            priority
        )
    }

    val ioPool: ExecutorService
        /**
         * Return a thread pool that creates (2 * CPU_COUNT + 1) threads
         * operating off a queue which size is 128.
         *
         * @return a IO thread pool
         */
        get() = PictureThreadUtils.getPoolByTypeAndPriority(PictureThreadUtils.TYPE_IO.toInt())

    /**
     * Return a thread pool that creates (2 * CPU_COUNT + 1) threads
     * operating off a queue which size is 128.
     *
     * @param priority The priority of thread in the poll.
     * @return a IO thread pool
     */
    fun getIoPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService {
        return PictureThreadUtils.getPoolByTypeAndPriority(
            PictureThreadUtils.TYPE_IO.toInt(),
            priority
        )
    }

    val cpuPool: ExecutorService
        /**
         * Return a thread pool that creates (CPU_COUNT + 1) threads
         * operating off a queue which size is 128 and the maximum
         * number of threads equals (2 * CPU_COUNT + 1).
         *
         * @return a cpu thread pool for
         */
        get() = PictureThreadUtils.getPoolByTypeAndPriority(PictureThreadUtils.TYPE_CPU.toInt())

    /**
     * Return a thread pool that creates (CPU_COUNT + 1) threads
     * operating off a queue which size is 128 and the maximum
     * number of threads equals (2 * CPU_COUNT + 1).
     *
     * @param priority The priority of thread in the poll.
     * @return a cpu thread pool for
     */
    fun getCpuPool(@IntRange(from = 1, to = 10) priority: Int): ExecutorService {
        return PictureThreadUtils.getPoolByTypeAndPriority(
            PictureThreadUtils.TYPE_CPU.toInt(),
            priority
        )
    }

    /**
     * Executes the given task in a fixed thread pool.
     *
     * @param size The size of thread in the fixed thread pool.
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByFixed(@IntRange(from = 1) size: Int, task: Task<T?>) {
        PictureThreadUtils.execute<T?>(PictureThreadUtils.getPoolByTypeAndPriority(size), task)
    }

    /**
     * Executes the given task in a fixed thread pool.
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param task     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByFixed(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(size, priority),
            task
        )
    }

    /**
     * Executes the given task in a fixed thread pool after the given delay.
     *
     * @param size  The size of thread in the fixed thread pool.
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByFixedWithDelay(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(size),
            task,
            delay,
            unit
        )
    }

    /**
     * Executes the given task in a fixed thread pool after the given delay.
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param task     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByFixedWithDelay(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                size,
                priority
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size   The size of thread in the fixed thread pool.
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByFixedAtFixRate(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(size),
            task,
            0,
            period,
            unit
        )
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size     The size of thread in the fixed thread pool.
     * @param task     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByFixedAtFixRate(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                size,
                priority
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size         The size of thread in the fixed thread pool.
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByFixedAtFixRate(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(size),
            task,
            initialDelay,
            period,
            unit
        )
    }

    /**
     * Executes the given task in a fixed thread pool at fix rate.
     *
     * @param size         The size of thread in the fixed thread pool.
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByFixedAtFixRate(
        @IntRange(from = 1) size: Int,
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                size,
                priority
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a single thread pool.
     *
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeBySingle(task: Task<T?>) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_SINGLE.toInt()
            ), task
        )
    }

    /**
     * Executes the given task in a single thread pool.
     *
     * @param task     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeBySingle(
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_SINGLE.toInt(),
                priority
            ), task
        )
    }

    /**
     * Executes the given task in a single thread pool after the given delay.
     *
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeBySingleWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_SINGLE.toInt()
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in a single thread pool after the given delay.
     *
     * @param task     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeBySingleWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_SINGLE.toInt(),
                priority
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeBySingleAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_SINGLE.toInt()
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param task     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeBySingleAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_SINGLE.toInt(),
                priority
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeBySingleAtFixRate(
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_SINGLE.toInt()
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a single thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeBySingleAtFixRate(
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_SINGLE.toInt(),
                priority
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a cached thread pool.
     *
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByCached(task: Task<T?>) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CACHED.toInt()
            ), task
        )
    }

    /**
     * Executes the given task in a cached thread pool.
     *
     * @param task     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCached(
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CACHED.toInt(),
                priority
            ), task
        )
    }

    /**
     * Executes the given task in a cached thread pool after the given delay.
     *
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByCachedWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CACHED.toInt()
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in a cached thread pool after the given delay.
     *
     * @param task     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCachedWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CACHED.toInt(),
                priority
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByCachedAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CACHED.toInt()
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param task     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCachedAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CACHED.toInt(),
                priority
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCachedAtFixRate(
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CACHED.toInt()
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a cached thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCachedAtFixRate(
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CACHED.toInt(),
                priority
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in an IO thread pool.
     *
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByIo(task: Task<T?>) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_IO.toInt()
            ), task
        )
    }

    /**
     * Executes the given task in an IO thread pool.
     *
     * @param task     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByIo(
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_IO.toInt(),
                priority
            ), task
        )
    }

    /**
     * Executes the given task in an IO thread pool after the given delay.
     *
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByIoWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_IO.toInt()
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in an IO thread pool after the given delay.
     *
     * @param task     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByIoWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_IO.toInt(),
                priority
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByIoAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_IO.toInt()
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param task     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByIoAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_IO.toInt(),
                priority
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByIoAtFixRate(
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_IO.toInt()
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in an IO thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByIoAtFixRate(
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_IO.toInt(),
                priority
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a cpu thread pool.
     *
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByCpu(task: Task<T?>) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CPU.toInt()
            ), task
        )
    }

    /**
     * Executes the given task in a cpu thread pool.
     *
     * @param task     The task to execute.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCpu(
        task: Task<T?>,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.execute<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CPU.toInt(),
                priority
            ), task
        )
    }

    /**
     * Executes the given task in a cpu thread pool after the given delay.
     *
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByCpuWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CPU.toInt()
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in a cpu thread pool after the given delay.
     *
     * @param task     The task to execute.
     * @param delay    The time from now to delay execution.
     * @param unit     The time unit of the delay parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCpuWithDelay(
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeWithDelay<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CPU.toInt(),
                priority
            ), task, delay, unit
        )
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByCpuAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CPU.toInt()
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param task     The task to execute.
     * @param period   The period between successive executions.
     * @param unit     The time unit of the period parameter.
     * @param priority The priority of thread in the poll.
     * @param <T>      The type of the task's result.
    </T> */
    fun <T> executeByCpuAtFixRate(
        task: Task<T?>,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CPU.toInt(),
                priority
            ), task, 0, period, unit
        )
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCpuAtFixRate(
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CPU.toInt()
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a cpu thread pool at fix rate.
     *
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param priority     The priority of thread in the poll.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCpuAtFixRate(
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit,
        @IntRange(from = 1, to = 10) priority: Int
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(
            PictureThreadUtils.getPoolByTypeAndPriority(
                PictureThreadUtils.TYPE_CPU.toInt(),
                priority
            ), task, initialDelay, period, unit
        )
    }

    /**
     * Executes the given task in a custom thread pool.
     *
     * @param pool The custom thread pool.
     * @param task The task to execute.
     * @param <T>  The type of the task's result.
    </T> */
    fun <T> executeByCustom(pool: ExecutorService, task: Task<T?>) {
        PictureThreadUtils.execute<T?>(pool, task)
    }

    /**
     * Executes the given task in a custom thread pool after the given delay.
     *
     * @param pool  The custom thread pool.
     * @param task  The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit  The time unit of the delay parameter.
     * @param <T>   The type of the task's result.
    </T> */
    fun <T> executeByCustomWithDelay(
        pool: ExecutorService,
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeWithDelay<T?>(pool, task, delay, unit)
    }

    /**
     * Executes the given task in a custom thread pool at fix rate.
     *
     * @param pool   The custom thread pool.
     * @param task   The task to execute.
     * @param period The period between successive executions.
     * @param unit   The time unit of the period parameter.
     * @param <T>    The type of the task's result.
    </T> */
    fun <T> executeByCustomAtFixRate(
        pool: ExecutorService,
        task: Task<T?>,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(pool, task, 0, period, unit)
    }

    /**
     * Executes the given task in a custom thread pool at fix rate.
     *
     * @param pool         The custom thread pool.
     * @param task         The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period       The period between successive executions.
     * @param unit         The time unit of the initialDelay and period parameters.
     * @param <T>          The type of the task's result.
    </T> */
    fun <T> executeByCustomAtFixRate(
        pool: ExecutorService,
        task: Task<T?>,
        initialDelay: Long,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.executeAtFixedRate<T?>(pool, task, initialDelay, period, unit)
    }

    /**
     * Cancel the given task.
     *
     * @param task The task to cancel.
     */
    fun cancel(task: Task<*>?) {
        if (task == null) return
        task.cancel()
    }

    /**
     * Cancel the given tasks.
     *
     * @param tasks The tasks to cancel.
     */
    fun cancel(vararg tasks: Task<*>?) {
        if (tasks == null || tasks.size == 0) return
        for (task in tasks) {
            if (task == null) continue
            task.cancel()
        }
    }

    /**
     * Cancel the given tasks.
     *
     * @param tasks The tasks to cancel.
     */
    fun cancel(tasks: MutableList<Task<*>?>?) {
        if (tasks == null || tasks.size == 0) return
        for (task in tasks) {
            if (task == null) continue
            task.cancel()
        }
    }

    /**
     * Cancel the tasks in pool.
     *
     * @param executorService The pool.
     */
        fun cancel(executorService: ExecutorService?) {
        if (executorService is ThreadPoolExecutor4Util) {
            for (taskTaskInfoEntry in TASK_POOL_MAP.entries) {
                if (taskTaskInfoEntry.value === executorService) {
                    cancel(taskTaskInfoEntry.key)
                }
            }
        } else {
            Log.e("ThreadUtils", "The executorService is not ThreadUtils's pool.")
        }
    }

    /**
     * Set the deliver.
     *
     * @param deliver The deliver.
     */
    fun setDeliver(deliver: Executor?) {
        PictureThreadUtils.sDeliver = deliver
    }

    private fun <T> execute(pool: ExecutorService, task: Task<T?>) {
        PictureThreadUtils.execute<T?>(pool, task, 0, 0, TimeUnit.MILLISECONDS)
    }

    private fun <T> executeWithDelay(
        pool: ExecutorService,
        task: Task<T?>,
        delay: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.execute<T?>(pool, task, delay, 0, unit)
    }

    private fun <T> executeAtFixedRate(
        pool: ExecutorService,
        task: Task<T?>,
        delay: Long,
        period: Long,
        unit: TimeUnit
    ) {
        PictureThreadUtils.execute<T?>(pool, task, delay, period, unit)
    }

    private fun <T> execute(
        pool: ExecutorService, task: Task<T?>,
        delay: Long, period: Long, unit: TimeUnit
    ) {
        synchronized(TASK_POOL_MAP) {
            if (TASK_POOL_MAP[task] != null) {
                Log.e("ThreadUtils", "Task can only be executed once.")
                return
            }
            TASK_POOL_MAP[task] = pool
        }
        if (period == 0L) {
            if (delay == 0L) {
                pool.execute(task)
            } else {
                val timerTask: TimerTask = object : TimerTask() {
                    override fun run() {
                        pool.execute(task)
                    }
                }
                PictureThreadUtils.TIMER.schedule(timerTask, unit.toMillis(delay))
            }
        } else {
            task.setScheduleInternal(true)
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    pool.execute(task)
                }
            }
            TIMER.scheduleAtFixedRate(
                timerTask,
                unit.toMillis(delay),
                unit.toMillis(period)
            )
        }
    }

    private fun getPoolByTypeAndPriority(type: Int): ExecutorService {
        return PictureThreadUtils.getPoolByTypeAndPriority(type, Thread.NORM_PRIORITY)
    }

    private fun getPoolByTypeAndPriority(type: Int, priority: Int): ExecutorService {
        synchronized(TYPE_PRIORITY_POOLS) {
            var pool: ExecutorService?
            var priorityPools = TYPE_PRIORITY_POOLS[type]
            if (priorityPools == null) {
                priorityPools = ConcurrentHashMap<Int?, ExecutorService?>()
                pool = ThreadPoolExecutor4Util.createPool(type, priority)
                priorityPools[priority] = pool
                TYPE_PRIORITY_POOLS[type] = priorityPools
            } else {
                pool = priorityPools[priority]
                if (pool == null) {
                    pool = ThreadPoolExecutor4Util.createPool(type, priority)
                    priorityPools[priority] = pool
                }
            }
            return pool!!
        }
    }

    private val globalDeliver: Executor
        get() {
            val deliver = sDeliver
            if (deliver == null) {
                val newDeliver = object : Executor {
                    override fun execute(command: Runnable) {
                        runOnUiThread(command)
                    }
                }
                sDeliver = newDeliver
                return newDeliver
            }
            return deliver
        }

    internal class ThreadPoolExecutor4Util private constructor(
        corePoolSize: Int, maximumPoolSize: Int,
        keepAliveTime: Long, unit: TimeUnit?,
        workQueue: LinkedBlockingQueue4Util,
        threadFactory: ThreadFactory?
    ) : ThreadPoolExecutor(
        corePoolSize, maximumPoolSize,
        keepAliveTime, unit,
        workQueue,
        threadFactory
    ) {
        private val mSubmittedCount = AtomicInteger(0)

        private val mWorkQueue: LinkedBlockingQueue4Util

        init {
            workQueue.mPool = this
            mWorkQueue = workQueue
        }

        private val submittedCount: Int
            get() = mSubmittedCount.get()

        override fun afterExecute(r: Runnable?, t: Throwable?) {
            mSubmittedCount.decrementAndGet()
            super.afterExecute(r, t)
        }

        override fun execute(command: Runnable) {
            if (this.isShutdown()) return
            mSubmittedCount.incrementAndGet()
            try {
                super.execute(command)
            } catch (ignore: RejectedExecutionException) {
                Log.e("ThreadUtils", "This will not happen!")
                mWorkQueue.offer(command)
            } catch (t: Throwable) {
                mSubmittedCount.decrementAndGet()
            }
        }

        companion object {
            internal fun createPool(type: Int, priority: Int): ExecutorService {
                when (type) {
                    TYPE_SINGLE.toInt() -> return ThreadPoolExecutor4Util(
                        1, 1,
                        0L, TimeUnit.MILLISECONDS,
                        LinkedBlockingQueue4Util(),
                        UtilsThreadFactory("single", priority)
                    )

                    TYPE_CACHED.toInt() -> return ThreadPoolExecutor4Util(
                        0, 128,
                        60L, TimeUnit.SECONDS,
                        LinkedBlockingQueue4Util(true),
                        UtilsThreadFactory("cached", priority)
                    )

                    TYPE_IO.toInt() -> return ThreadPoolExecutor4Util(
                        2 * CPU_COUNT + 1, 2 * CPU_COUNT + 1,
                        30, TimeUnit.SECONDS,
                        LinkedBlockingQueue4Util(),
                        UtilsThreadFactory("io", priority)
                    )

                    TYPE_CPU.toInt() -> return ThreadPoolExecutor4Util(
                        CPU_COUNT + 1, 2 * CPU_COUNT + 1,
                        30, TimeUnit.SECONDS,
                        LinkedBlockingQueue4Util(true),
                        UtilsThreadFactory("cpu", priority)
                    )

                    else -> return ThreadPoolExecutor4Util(
                        type, type,
                        0L, TimeUnit.MILLISECONDS,
                        LinkedBlockingQueue4Util(),
                        UtilsThreadFactory("fixed(" + type + ")", priority)
                    )
                }
            }
        }
    }

    private class LinkedBlockingQueue4Util : LinkedBlockingQueue<Runnable?> {
        @Volatile
        internal var mPool: ThreadPoolExecutor4Util? = null

        private var mCapacity = Int.MAX_VALUE

        internal constructor() : super()

        internal constructor(isAddSubThreadFirstThenAddQueue: Boolean) : super() {
            if (isAddSubThreadFirstThenAddQueue) {
                mCapacity = 0
            }
        }

        internal constructor(capacity: Int) : super() {
            mCapacity = capacity
        }

        override fun offer(runnable: Runnable?): Boolean {
            if (mCapacity <= size && mPool != null && mPool!!.poolSize < mPool!!.maximumPoolSize) {
                // create a non-core thread
                return false
            }
            return super.offer(runnable)
        }
    }

    internal class UtilsThreadFactory @JvmOverloads constructor(
        prefix: String?,
        private val priority: Int,
        private val isDaemon: Boolean = false
    ) : ThreadFactory {
        private val namePrefix: String
        private val threadNumber = AtomicLong(0)

        init {
            namePrefix = prefix + "-pool-" +
                    POOL_NUMBER.getAndIncrement() +
                    "-thread-"
        }

        override fun newThread(r: Runnable): Thread {
            val t: Thread = object : Thread(r, namePrefix + threadNumber.getAndIncrement()) {
                override fun run() {
                    try {
                        super.run()
                    } catch (t: Throwable) {
                        Log.e("ThreadUtils", "Request threw uncaught throwable", t)
                    }
                }
            }
            t.isDaemon = isDaemon
            t.uncaughtExceptionHandler = object : Thread.UncaughtExceptionHandler {
                override fun uncaughtException(t: Thread?, e: Throwable?) {
                    println(e)
                }
            }
            t.priority = priority
            return t
        }

        companion object {
            private val POOL_NUMBER = AtomicInteger(1)
            private val serialVersionUID = -9209200509960368598L
        }
    }

    abstract class SimpleTask<T> : Task<T?>() {
        override fun onCancel() {
            Log.e("ThreadUtils", "onCancel: " + Thread.currentThread())
        }

        override fun onFail(t: Throwable?) {
            Log.e("ThreadUtils", "onFail: ", t)
        }
    }

    abstract class Task<T> : Runnable {
        private val state = AtomicInteger(NEW)

        @Volatile
        private var isSchedule = false

        @Volatile
        private var runner: Thread? = null

        private var mTimer: Timer? = null
        private var mTimeoutMillis: Long = 0
        private var mTimeoutListener: OnTimeoutListener? = null

        private var deliver: Executor? = null

        @Throws(Throwable::class)
        abstract fun doInBackground(): T?

        abstract fun onSuccess(result: T?)

        abstract fun onCancel()

        abstract fun onFail(t: Throwable?)

        override fun run() {
            if (isSchedule) {
                if (runner == null) {
                    if (!state.compareAndSet(NEW, RUNNING)) return
                    runner = Thread.currentThread()
                    if (mTimeoutListener != null) {
                        Log.w("ThreadUtils", "Scheduled task doesn't support timeout.")
                    }
                } else {
                    if (state.get() != RUNNING) return
                }
            } else {
                if (!state.compareAndSet(NEW, RUNNING)) return
                runner = Thread.currentThread()
                if (mTimeoutListener != null) {
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            if (!isDone && mTimeoutListener != null) {
                                timeout()
                                mTimeoutListener!!.onTimeout()
                                onDone()
                            }
                        }
                    }, mTimeoutMillis)
                }
            }
            try {
                val result = doInBackground()
                if (isSchedule) {
                    if (state.get() != RUNNING) return
                    getDeliver()!!.execute(object : Runnable {
                        override fun run() {
                            onSuccess(result)
                        }
                    })
                } else {
                    if (!state.compareAndSet(
                            RUNNING,
                            COMPLETING
                        )
                    ) return
                    getDeliver()!!.execute(object : Runnable {
                        override fun run() {
                            onSuccess(result)
                            onDone()
                        }
                    })
                }
            } catch (ignore: InterruptedException) {
                state.compareAndSet(CANCELLED, INTERRUPTED)
            } catch (throwable: Throwable) {
                if (!state.compareAndSet(RUNNING, EXCEPTIONAL)) return
                getDeliver()!!.execute(object : Runnable {
                    override fun run() {
                        onFail(throwable)
                        onDone()
                    }
                })
            }
        }

        @JvmOverloads
        fun cancel(mayInterruptIfRunning: Boolean = true) {
            synchronized(state) {
                if (state.get() > RUNNING) return
                state.set(CANCELLED)
            }
            if (mayInterruptIfRunning) {
                if (runner != null) {
                    runner!!.interrupt()
                }
            }

            getDeliver()!!.execute(object : Runnable {
                override fun run() {
                    onCancel()
                    onDone()
                }
            })
        }

        private fun timeout() {
            synchronized(state) {
                if (state.get() > RUNNING) return
                state.set(TIMEOUT)
            }
            if (runner != null) {
                runner!!.interrupt()
            }
        }


        val isCanceled: Boolean
            get() = state.get() >= CANCELLED

        val isDone: Boolean
            get() = state.get() > RUNNING

        fun setDeliver(deliver: Executor?): Task<T> {
            this.deliver = deliver
            return this
        }

        /**
         * Scheduled task doesn't support timeout.
         */
        fun setTimeout(timeoutMillis: Long, listener: OnTimeoutListener?): Task<T> {
            mTimeoutMillis = timeoutMillis
            mTimeoutListener = listener
            return this
        }

        internal fun setScheduleInternal(isSchedule: Boolean) {
            this.isSchedule = isSchedule
        }

        private fun getDeliver(): Executor? {
            if (deliver == null) {
                return PictureThreadUtils.globalDeliver
            }
            return deliver
        }

        @CallSuper
        protected fun onDone() {
            TASK_POOL_MAP.remove(this)
            if (mTimer != null) {
                mTimer!!.cancel()
                mTimer = null
                mTimeoutListener = null
            }
        }

        interface OnTimeoutListener {
            fun onTimeout()
        }

        companion object {
            private const val NEW = 0
            private const val RUNNING = 1
            private const val EXCEPTIONAL = 2
            private const val COMPLETING = 3
            private const val CANCELLED = 4
            private const val INTERRUPTED = 5
            private const val TIMEOUT = 6
        }
    }

    class SyncValue<T> {
        private val mLatch = CountDownLatch(1)
        private val mFlag = AtomicBoolean(false)
        private var mValue: T? = null

        var value: T?
            get() {
                if (!mFlag.get()) {
                    try {
                        mLatch.await()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                return mValue
            }
            set(value) {
                if (mFlag.compareAndSet(false, true)) {
                    mValue = value
                    mLatch.countDown()
                }
            }

        fun getValue(timeout: Long, unit: TimeUnit?, defaultValue: T?): T? {
            if (!mFlag.get()) {
                try {
                    mLatch.await(timeout, unit)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    return defaultValue
                }
            }
            return mValue
        }
    }
}
