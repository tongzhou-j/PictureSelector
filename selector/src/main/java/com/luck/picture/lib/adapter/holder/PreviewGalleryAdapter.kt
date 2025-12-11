package com.luck.picture.lib.adapter.holder

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.R
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.utils.StyleUtils

/**
 * @author：luck
 * @date：2019-11-30 20:50
 * @describe：preview gallery
 */
class PreviewGalleryAdapter(
    private val selectorConfig: SelectorConfig,
    private val isBottomPreview: Boolean
) : RecyclerView.Adapter<PreviewGalleryAdapter.ViewHolder?>() {
    val data: MutableList<LocalMedia>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResourceId = InjectResourceSource.getLayoutResource(
            parent.context,
            InjectResourceSource.PREVIEW_GALLERY_ITEM_LAYOUT_RESOURCE, selectorConfig
        )
        val itemView = LayoutInflater.from(parent.context)
            .inflate(
                if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE)
                    layoutResourceId
                else
                    R.layout.ps_preview_gallery_item, parent, false
            )
        return ViewHolder(itemView)
    }

    fun clear() {
        data.clear()
    }

    /**
     * 添加选中的至画廊效果里
     *
     * @param currentMedia
     */
    fun addGalleryData(currentMedia: LocalMedia) {
        val lastCheckPosition = this.lastCheckPosition
        if (lastCheckPosition != RecyclerView.NO_POSITION) {
            val lastSelectedMedia = data.get(lastCheckPosition)
            lastSelectedMedia.isChecked = false
            notifyItemChanged(lastCheckPosition)
        }
        if (isBottomPreview && data.contains(currentMedia)) {
            val currentPosition = getCurrentPosition(currentMedia)
            val media = data.get(currentPosition)
            media.isGalleryEnabledMask = false
            media.isChecked = true
            notifyItemChanged(currentPosition)
        } else {
            currentMedia.isChecked = true
            data.add(currentMedia)
            notifyItemChanged(data.size - 1)
        }
    }

    /**
     * 移除画廊中未选中的结果
     *
     * @param currentMedia
     */
    fun removeGalleryData(currentMedia: LocalMedia) {
        val currentPosition = getCurrentPosition(currentMedia)
        if (currentPosition != RecyclerView.NO_POSITION) {
            if (isBottomPreview) {
                val media = data.get(currentPosition)
                media.isGalleryEnabledMask = true
                notifyItemChanged(currentPosition)
            } else {
                data.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
            }
        }
    }

    /**
     * 当前LocalMedia是否选中
     *
     * @param currentMedia
     */
    fun isSelectMedia(currentMedia: LocalMedia) {
        val lastCheckPosition = this.lastCheckPosition
        if (lastCheckPosition != RecyclerView.NO_POSITION) {
            val lastSelectedMedia = data.get(lastCheckPosition)
            lastSelectedMedia.isChecked = false
            notifyItemChanged(lastCheckPosition)
        }

        val currentPosition = getCurrentPosition(currentMedia)
        if (currentPosition != RecyclerView.NO_POSITION) {
            val media = data.get(currentPosition)
            media.isChecked = true
            notifyItemChanged(currentPosition)
        }
    }

    val lastCheckPosition: Int
        /**
         * 获取画廊上一次选中的位置
         *
         * @return
         */
        get() {
            for (i in data.indices) {
                val media = data.get(i)
                if (media.isChecked) {
                    return i
                }
            }
            return RecyclerView.NO_POSITION
        }

    /**
     * 获取当前画廊LocalMedia的位置
     *
     * @param currentMedia
     * @return
     */
    private fun getCurrentPosition(currentMedia: LocalMedia): Int {
        for (i in data.indices) {
            val media = data.get(i)
            if (TextUtils.equals(media.path, currentMedia.path)
                || media.id == currentMedia.id
            ) {
                return i
            }
        }
        return RecyclerView.NO_POSITION
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val colorFilterRes = if (item.isGalleryEnabledMask)
            R.color.ps_color_half_white
        else
            R.color.ps_color_transparent
        val colorFilter = StyleUtils.getColorFilter(
            holder.itemView.context, colorFilterRes
        )
        if (item.isChecked && item.isGalleryEnabledMask) {
            holder.viewBorder.visibility = View.VISIBLE
        } else {
            holder.viewBorder.visibility = if (item.isChecked) View.VISIBLE else View.GONE
        }
        var path = item.path
        if (item.isEditorImage() && !TextUtils.isEmpty(item.cutPath)) {
            path = item.cutPath
            holder.ivEditor.visibility = View.VISIBLE
        } else {
            holder.ivEditor.visibility = View.GONE
        }
        holder.ivImage.colorFilter = colorFilter
        val imageEngine = selectorConfig.imageEngine
        if (imageEngine != null) {
            imageEngine.loadGridImage(
                holder.itemView.context,
                path,
                holder.ivImage
            )
        }
        holder.ivPlay.visibility = if (PictureMimeType.isHasVideo(item.mimeType)) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (listener != null) {
                    listener!!.onItemClick(holder.absoluteAdapterPosition, item, view)
                }
            }
        })
        holder.itemView.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                if (mItemLongClickListener != null) {
                    val adapterPosition = holder.absoluteAdapterPosition
                    mItemLongClickListener!!.onItemLongClick(holder, adapterPosition, v)
                }
                return true
            }
        })
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivImage: ImageView
        var ivPlay: ImageView
        var ivEditor: ImageView
        var viewBorder: View

        init {
            ivImage = itemView.findViewById<ImageView>(R.id.ivImage)
            ivPlay = itemView.findViewById<ImageView>(R.id.ivPlay)
            ivEditor = itemView.findViewById<ImageView>(R.id.ivEditor)
            viewBorder = itemView.findViewById<View>(R.id.viewBorder)
            val selectMainStyle = selectorConfig.selectorStyle?.selectMainStyle
            val editorRes = selectMainStyle?.adapterImageEditorResources ?: 0
            if (StyleUtils.checkStyleValidity(editorRes)) {
                ivEditor.setImageResource(editorRes)
            }
            val frameRes = selectMainStyle?.adapterPreviewGalleryFrameResource ?: 0
            if (StyleUtils.checkStyleValidity(frameRes)) {
                viewBorder.setBackgroundResource(frameRes)
            }

            val adapterPreviewGalleryItemSize = selectMainStyle?.adapterPreviewGalleryItemSize ?: 0
            if (StyleUtils.checkSizeValidity(adapterPreviewGalleryItemSize)) {
                val params = RelativeLayout.LayoutParams(
                    adapterPreviewGalleryItemSize,
                    adapterPreviewGalleryItemSize
                )
                itemView.layoutParams = params
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private var listener: OnItemClickListener? = null

    fun setItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, media: LocalMedia?, v: View?)
    }

    private var mItemLongClickListener: OnItemLongClickListener? = null

    init {
        this.data = ArrayList<LocalMedia>(selectorConfig.selectedResult.filterNotNull())
        for (i in this.data.indices) {
            val media = data[i]
            media.isGalleryEnabledMask = false
            media.isChecked = false
        }
    }

    fun setItemLongClickListener(listener: OnItemLongClickListener?) {
        this.mItemLongClickListener = listener
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(holder: RecyclerView.ViewHolder?, position: Int, v: View?)
    }
}
