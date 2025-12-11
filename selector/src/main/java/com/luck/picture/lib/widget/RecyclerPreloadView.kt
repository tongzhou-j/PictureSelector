package com.luck.picture.lib.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.interfaces.OnRecyclerViewPreloadMoreListener
import com.luck.picture.lib.interfaces.OnRecyclerViewScrollListener
import com.luck.picture.lib.interfaces.OnRecyclerViewScrollStateListener
import kotlin.math.abs

/**
 * @author：luck
 * @date：2020-04-14 18:43
 * @describe：RecyclerPreloadView
 */
class RecyclerPreloadView : RecyclerView {
    private var isInTheBottom = false
    /**
     * Whether to load more
     */
    /**
     * Whether to load more
     *
     * @param isEnabledLoadMore
     */
    var isEnabledLoadMore: Boolean = false

    /**
     * Gets the first visible position index
     *
     * @return
     */
    var firstVisiblePosition: Int = 0
        private set

    /**
     * Gets the last visible position index
     *
     * @return
     */
    var lastVisiblePosition: Int = 0

    /**
     * reachBottomRow = 1;(default
     * mean : when the lastVisibleRow is lastRow , call the onReachBottom();
     * reachBottomRow = 2;
     * mean : when the lastVisibleRow is Penultimate Row , call the onReachBottom();
     * And so on
     */
    private var reachBottomRow: Int = BOTTOM_DEFAULT

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )


    fun setReachBottomRow(reachBottomRow: Int) {
        var reachBottomRow = reachBottomRow
        if (reachBottomRow < 1) reachBottomRow = 1
        this.reachBottomRow = reachBottomRow
    }

    override fun onScrolled(dx: Int, dy: Int) {
        super.onScrolled(dx, dy)
        val layoutManager = layoutManager
        if (layoutManager == null) {
            throw RuntimeException("LayoutManager is null,Please check it!")
        }
        setLayoutManagerPosition(layoutManager)
        if (onRecyclerViewPreloadListener != null) {
            if (isEnabledLoadMore) {
                val adapter = adapter
                if (adapter == null) {
                    throw RuntimeException("Adapter is null,Please check it!")
                }
                var isReachBottom = false
                if (layoutManager is GridLayoutManager) {
                    val gridLayoutManager = layoutManager
                    val rowCount = adapter.itemCount / gridLayoutManager.spanCount
                    val lastVisibleRowPosition =
                        gridLayoutManager.findLastVisibleItemPosition() / gridLayoutManager.spanCount
                    isReachBottom = lastVisibleRowPosition >= rowCount - reachBottomRow
                }

                if (!isReachBottom) {
                    isInTheBottom = false
                } else if (!isInTheBottom) {
                    onRecyclerViewPreloadListener!!.onRecyclerViewPreloadMore()
                    if (dy > 0) {
                        isInTheBottom = true
                    }
                } else {
                    // 属于首次进入屏幕未滑动且内容未超过一屏，用于确保分页数设置过小导致内容不足二次上拉加载...
                    if (dy == 0) {
                        isInTheBottom = false
                    }
                }
            }
        }

        if (onRecyclerViewScrollListener != null) {
            onRecyclerViewScrollListener!!.onScrolled(dx, dy)
        }

        if (onRecyclerViewScrollStateListener != null) {
            if (abs(dy.toDouble()) < LIMIT) {
                onRecyclerViewScrollStateListener!!.onScrollSlow()
            } else {
                onRecyclerViewScrollStateListener!!.onScrollFast()
            }
        }
    }

    private fun setLayoutManagerPosition(layoutManager: LayoutManager?) {
        if (layoutManager is GridLayoutManager) {
            val gridLayoutManager = layoutManager
            this.firstVisiblePosition = gridLayoutManager.findFirstVisibleItemPosition()
            this.lastVisiblePosition = gridLayoutManager.findLastVisibleItemPosition()
        } else if (layoutManager is LinearLayoutManager) {
            val linearLayoutManager = layoutManager
            this.firstVisiblePosition = linearLayoutManager.findFirstVisibleItemPosition()
            this.lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition()
        }
    }


    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE || state == SCROLL_STATE_DRAGGING) {
            setLayoutManagerPosition(getLayoutManager())
        }

        if (onRecyclerViewScrollListener != null) {
            onRecyclerViewScrollListener!!.onScrollStateChanged(state)
        }

        if (state == SCROLL_STATE_IDLE) {
            if (onRecyclerViewScrollStateListener != null) {
                onRecyclerViewScrollStateListener!!.onScrollSlow()
            }
        }
    }


    private var onRecyclerViewPreloadListener: OnRecyclerViewPreloadMoreListener? = null

    fun setOnRecyclerViewPreloadListener(listener: OnRecyclerViewPreloadMoreListener?) {
        this.onRecyclerViewPreloadListener = listener
    }

    private var onRecyclerViewScrollStateListener: OnRecyclerViewScrollStateListener? = null

    fun setOnRecyclerViewScrollStateListener(listener: OnRecyclerViewScrollStateListener?) {
        this.onRecyclerViewScrollStateListener = listener
    }

    private var onRecyclerViewScrollListener: OnRecyclerViewScrollListener? = null

    fun setOnRecyclerViewScrollListener(listener: OnRecyclerViewScrollListener?) {
        this.onRecyclerViewScrollListener = listener
    }

    companion object {
        private val TAG: String = RecyclerPreloadView::class.java.simpleName
        private const val BOTTOM_DEFAULT = 1
        const val BOTTOM_PRELOAD: Int = 2
        private const val LIMIT = 150
    }
}
