package com.luck.picture.lib.widget

import com.luck.picture.lib.widget.SlideSelectTouchListener.OnAdvancedSlideSelectListener

/**
 * @author：luck
 * @date：2022/1/23 10:40 上午
 * @describe：SlideSelectionProcessor
 */
class SlideSelectionHandler
/**
 * @param mSelectionHandler the handler that takes care to handle the selection events
 */(private val mSelectionHandler: ISelectionHandler) : OnAdvancedSlideSelectListener {
    private var mStartFinishedListener: ISelectionStartFinishedListener? = null
    private var mOriginalSelection: HashSet<Int?>? = null


    /**
     * @param startFinishedListener a listener that get's notified when the drag selection is started or finished
     * @return this
     */
    fun withStartFinishedListener(startFinishedListener: ISelectionStartFinishedListener?): SlideSelectionHandler {
        mStartFinishedListener = startFinishedListener
        return this
    }


    override fun onSelectionStarted(start: Int) {
        mOriginalSelection = HashSet<Int?>()
        val selected = mSelectionHandler.selection
        if (selected != null) mOriginalSelection!!.addAll(selected)
        val isFirstSelected = mOriginalSelection!!.contains(start)
        mSelectionHandler.changeSelection(start, start, !mOriginalSelection!!.contains(start), true)
        if (mStartFinishedListener != null) {
            mStartFinishedListener!!.onSelectionStarted(start, isFirstSelected)
        }
    }

    override fun onSelectionFinished(end: Int) {
        mOriginalSelection = null
        if (mStartFinishedListener != null) mStartFinishedListener!!.onSelectionFinished(end)
    }

    override fun onSelectChange(start: Int, end: Int, isSelected: Boolean) {
        for (i in start..end) {
            checkedChangeSelection(i, i, isSelected != mOriginalSelection!!.contains(i))
        }
    }

    private fun checkedChangeSelection(start: Int, end: Int, newSelectionState: Boolean) {
        mSelectionHandler.changeSelection(start, end, newSelectionState, false)
    }

    interface ISelectionHandler {
        /**
         * @return the currently selected items => can be ignored
         */
        val selection: MutableSet<Int?>?

        /**
         * update your adapter and select select/unselect the passed index range, you be get a single for all modes but [Mode.Simple] and [Mode.FirstItemDependent]
         *
         * @param start             the first item of the range who's selection state changed
         * @param end               the last item of the range who's selection state changed
         * @param isSelected        true, if the range should be selected, false otherwise
         * @param calledFromOnStart true, if it was called from the [SlideSelectionHandler.onSelectionStarted] event
         */
        fun changeSelection(start: Int, end: Int, isSelected: Boolean, calledFromOnStart: Boolean)
    }

    interface ISelectionStartFinishedListener {
        /**
         * @param start                  the item on which the drag selection was started at
         * @param originalSelectionState the original selection state
         */
        fun onSelectionStarted(start: Int, originalSelectionState: Boolean)

        /**
         * @param end the item on which the drag selection was finished at
         */
        fun onSelectionFinished(end: Int)
    }
}
