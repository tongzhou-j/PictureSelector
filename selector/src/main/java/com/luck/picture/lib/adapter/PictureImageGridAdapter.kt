package com.luck.picture.lib.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.R
import com.luck.picture.lib.adapter.holder.BaseRecyclerMediaHolder
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia

/**
 * @author：luck
 * @date：2016-12-30 12:02
 * @describe：PictureImageGridAdapter
 */
class PictureImageGridAdapter(
    private val mContext: Context?,
    private val mConfig: SelectorConfig?
) : RecyclerView.Adapter<BaseRecyclerMediaHolder?>() {
    var isDisplayCamera: Boolean = false

    private var mData: java.util.ArrayList<LocalMedia>? = java.util.ArrayList<LocalMedia>()

    fun notifyItemPositionChanged(position: Int) {
        this.notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataAndDataSetChanged(result: java.util.ArrayList<LocalMedia>?) {
        if (result != null) {
            this.mData = result
            notifyDataSetChanged()
        }
    }

    val data: ArrayList<LocalMedia>
        get() = mData!!

    val isDataEmpty: Boolean
        get() = mData!!.size == 0

    override fun getItemViewType(position: Int): Int {
        if (isDisplayCamera && position == 0) {
            return ADAPTER_TYPE_CAMERA
        } else {
            val adapterPosition = if (isDisplayCamera) position - 1 else position
            val mimeType = mData!!.get(adapterPosition).mimeType
            if (PictureMimeType.isHasVideo(mimeType)) {
                return ADAPTER_TYPE_VIDEO
            } else if (PictureMimeType.isHasAudio(mimeType)) {
                return ADAPTER_TYPE_AUDIO
            }
            return ADAPTER_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseRecyclerMediaHolder {
        return BaseRecyclerMediaHolder.Companion.generate(
            parent,
            viewType,
            getItemResourceId(viewType),
            mConfig
        )
    }

    /**
     * getItemResourceId
     *
     * @param viewType
     * @return
     */
    private fun getItemResourceId(viewType: Int): Int {
        val layoutResourceId: Int
        when (viewType) {
            ADAPTER_TYPE_CAMERA -> return R.layout.ps_item_grid_camera
            ADAPTER_TYPE_VIDEO -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(
                    mContext,
                    InjectResourceSource.MAIN_ITEM_VIDEO_LAYOUT_RESOURCE,
                    mConfig
                )
                return if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_item_grid_video
            }

            ADAPTER_TYPE_AUDIO -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(
                    mContext,
                    InjectResourceSource.MAIN_ITEM_AUDIO_LAYOUT_RESOURCE,
                    mConfig
                )
                return if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_item_grid_audio
            }

            else -> {
                layoutResourceId = InjectResourceSource.getLayoutResource(
                    mContext,
                    InjectResourceSource.MAIN_ITEM_IMAGE_LAYOUT_RESOURCE,
                    mConfig
                )
                return if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_item_grid_image
            }
        }
    }

    override fun onBindViewHolder(holder: BaseRecyclerMediaHolder, position: Int) {
        if (getItemViewType(position) == ADAPTER_TYPE_CAMERA) {
            holder.itemView.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if (listener != null) {
                        listener!!.openCameraClick()
                    }
                }
            })
        } else {
            val adapterPosition = if (isDisplayCamera) position - 1 else position
            val media = mData!!.get(adapterPosition)
            holder.bindData(media, adapterPosition)
            holder.setOnItemClickListener(listener)
        }
    }


    override fun getItemCount(): Int {
        return if (isDisplayCamera) mData!!.size + 1 else mData!!.size
    }


    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    interface OnItemClickListener {
        /**
         * 拍照
         */
        fun openCameraClick()

        /**
         * 列表item点击事件
         *
         * @param selectedView 所产生点击事件的View
         * @param position     当前下标
         * @param media        当前LocalMedia对象
         */
        fun onItemClick(selectedView: View?, position: Int, media: LocalMedia?)

        /**
         * 列表item长按事件
         *
         * @param itemView
         * @param position
         */
        fun onItemLongClick(itemView: View?, position: Int)

        /**
         * 列表勾选点击事件
         *
         * @param selectedView 所产生点击事件的View
         * @param position     当前下标
         * @param media        当前LocalMedia对象
         */
        fun onSelected(selectedView: View?, position: Int, media: LocalMedia?): Int
    }

    companion object {
        /**
         * 拍照
         */
        const val ADAPTER_TYPE_CAMERA: Int = 1

        /**
         * 图片
         */
        const val ADAPTER_TYPE_IMAGE: Int = 2

        /**
         * 视频
         */
        const val ADAPTER_TYPE_VIDEO: Int = 3

        /**
         * 音频
         */
        const val ADAPTER_TYPE_AUDIO: Int = 4
    }
}
