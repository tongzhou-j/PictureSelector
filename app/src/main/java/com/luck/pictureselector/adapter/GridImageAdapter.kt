package com.luck.pictureselector.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.R as PictureLibR
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.utils.DateUtils
import com.luck.pictureselector.R
import com.luck.pictureselector.listener.OnItemLongClickListener
import java.util.ArrayList

/**
 * @author：luck
 * @date：2016-7-27 23:02
 * @describe：GridImageAdapter
 */
class GridImageAdapter(context: Context, result: List<LocalMedia>) : RecyclerView.Adapter<GridImageAdapter.ViewHolder>() {
    companion object {
        const val TAG = "PictureSelector"
        const val TYPE_CAMERA = 1
        const val TYPE_PICTURE = 2
    }

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val list = ArrayList<LocalMedia>()
    private var selectMax = 9

    init {
        list.addAll(result)
    }

    /**
     * 删除
     */
    fun delete(position: Int) {
        try {
            if (position != RecyclerView.NO_POSITION && list.size > position) {
                list.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, list.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSelectMax(selectMax: Int) {
        this.selectMax = selectMax
    }

    fun getSelectMax(): Int {
        return selectMax
    }

    fun getData(): ArrayList<LocalMedia> {
        return list
    }

    fun remove(position: Int) {
        if (position < list.size) {
            list.removeAt(position)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mImg: ImageView = view.findViewById(R.id.fiv)
        var mIvDel: ImageView = view.findViewById(R.id.iv_del)
        var tvDuration: TextView = view.findViewById(R.id.tv_duration)
    }

    override fun getItemCount(): Int {
        return if (list.size < selectMax) {
            list.size + 1
        } else {
            list.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowAddItem(position)) {
            TYPE_CAMERA
        } else {
            TYPE_PICTURE
        }
    }

    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.gv_filter_image, viewGroup, false)
        return ViewHolder(view)
    }

    private fun isShowAddItem(position: Int): Boolean {
        val size = list.size
        return position == size
    }

    /**
     * 设置值
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        //少于MaxSize张，显示继续添加的图标
        if (getItemViewType(position) == TYPE_CAMERA) {
            viewHolder.mImg.setImageResource(R.drawable.ic_add_image)
            viewHolder.mImg.setOnClickListener {
                mItemClickListener?.openPicture()
            }
            viewHolder.mIvDel.visibility = View.INVISIBLE
        } else {
            viewHolder.mIvDel.visibility = View.VISIBLE
            viewHolder.mIvDel.setOnClickListener {
                val index = viewHolder.absoluteAdapterPosition
                if (index != RecyclerView.NO_POSITION && list.size > index) {
                    list.removeAt(index)
                    notifyItemRemoved(index)
                    notifyItemRangeChanged(index, list.size)
                }
            }
            val media = list[position]
            val chooseModel = media.chooseModel
            val path = media.availablePath
            val duration = media.duration
            viewHolder.tvDuration.visibility = if (PictureMimeType.isHasVideo(media.mimeType)) View.VISIBLE else View.GONE
            if (chooseModel == SelectMimeType.ofAudio()) {
                viewHolder.tvDuration.visibility = View.VISIBLE
                viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    PictureLibR.drawable.ps_ic_audio, 0, 0, 0
                )
            } else {
                viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    PictureLibR.drawable.ps_ic_video, 0, 0, 0
                )
            }
            viewHolder.tvDuration.text = DateUtils.formatDurationTime(duration)
            if (chooseModel == SelectMimeType.ofAudio()) {
                viewHolder.mImg.setImageResource(PictureLibR.drawable.ps_audio_placeholder)
            } else {
                Glide.with(viewHolder.itemView.context)
                    .load(if (PictureMimeType.isContent(path) && !media.isCut && !media.isCompressed) Uri.parse(path) else path)
                    .centerCrop()
                    .placeholder(R.color.app_color_f6)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(viewHolder.mImg)
            }
            //itemView 的点击事件
            mItemClickListener?.let {
                viewHolder.itemView.setOnClickListener { v ->
                    val adapterPosition = viewHolder.absoluteAdapterPosition
                    it.onItemClick(v, adapterPosition)
                }
            }

            mItemLongClickListener?.let {
                viewHolder.itemView.setOnLongClickListener { v ->
                    val adapterPosition = viewHolder.absoluteAdapterPosition
                    it.onItemLongClick(viewHolder, adapterPosition, v)
                    true
                }
            }
        }
    }

    private var mItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(l: OnItemClickListener?) {
        mItemClickListener = l
    }

    interface OnItemClickListener {
        /**
         * Item click event
         *
         * @param v
         * @param position
         */
        fun onItemClick(v: View, position: Int)

        /**
         * Open PictureSelector
         */
        fun openPicture()
    }

    private var mItemLongClickListener: OnItemLongClickListener? = null

    fun setItemLongClickListener(l: OnItemLongClickListener?) {
        mItemLongClickListener = l
    }
}

