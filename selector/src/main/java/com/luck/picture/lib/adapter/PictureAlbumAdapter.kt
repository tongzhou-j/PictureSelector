package com.luck.picture.lib.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.R
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnAlbumItemClickListener
import kotlin.collections.ArrayList
import kotlin.collections.MutableList

/**
 * @author：luck
 * @date：2016-12-11 17:02
 * @describe：PictureAlbumDirectoryAdapter
 */
class PictureAlbumAdapter(private val selectorConfig: SelectorConfig) :
    RecyclerView.Adapter<PictureAlbumAdapter.ViewHolder?>() {
    private var albumList: MutableList<LocalMediaFolder>? = null

    fun bindAlbumData(albumList: MutableList<LocalMediaFolder?>) {
        this.albumList = albumList.filterNotNull().toMutableList()
    }

    fun getAlbumList(): MutableList<LocalMediaFolder?> {
        return albumList?.map { it as LocalMediaFolder? }?.toMutableList() ?: ArrayList<LocalMediaFolder?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResourceId = InjectResourceSource.getLayoutResource(
            parent.context,
            InjectResourceSource.ALBUM_ITEM_LAYOUT_RESOURCE,
            selectorConfig
        )
        val itemView = LayoutInflater.from(parent.context)
            .inflate(
                if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) layoutResourceId else R.layout.ps_album_folder_item,
                parent,
                false
            )
        return ViewHolder(itemView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val folder = albumList!!.get(position)
        val name = folder.getFolderName()
        val imageNum = folder.folderTotalNum
        val imagePath = folder.firstImagePath
        holder.tvSelectTag.visibility = if (folder.isSelectTag) View.VISIBLE else View.INVISIBLE
        val currentLocalMediaFolder = selectorConfig.currentLocalMediaFolder
        holder.itemView.isSelected = currentLocalMediaFolder != null
                && folder.bucketId == currentLocalMediaFolder.bucketId
        
        val firstMimeType = folder.firstMimeType
        if (PictureMimeType.isHasAudio(firstMimeType)) {
            holder.ivFirstImage.setImageResource(R.drawable.ps_audio_placeholder)
        } else {
            val imageEngine = selectorConfig.imageEngine
            if (imageEngine != null) {
                imageEngine.loadAlbumCover(
                    holder.itemView.context,
                    imagePath, holder.ivFirstImage
                )
            }
        }
        val context = holder.itemView.context
        holder.tvFolderName.text = context.getString(R.string.ps_camera_roll_num, name, imageNum)
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (onAlbumItemClickListener == null) {
                    return
                }
                onAlbumItemClickListener!!.onItemClick(position, folder)
            }
        })
    }

    override fun getItemCount(): Int {
        return albumList!!.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivFirstImage: ImageView
        var tvFolderName: TextView
        var tvSelectTag: TextView

        init {
            ivFirstImage = itemView.findViewById<ImageView>(R.id.first_image)
            tvFolderName = itemView.findViewById<TextView>(R.id.tv_folder_name)
            tvSelectTag = itemView.findViewById<TextView>(R.id.tv_select_tag)
            val selectorStyle = selectorConfig.selectorStyle
            val albumWindowStyle = selectorStyle?.albumWindowStyle
            val itemBackground = albumWindowStyle?.albumAdapterItemBackground ?: 0
            if (itemBackground != 0) {
                itemView.setBackgroundResource(itemBackground)
            }
            val itemSelectStyle = albumWindowStyle?.albumAdapterItemSelectStyle ?: 0
            if (itemSelectStyle != 0) {
                tvSelectTag.setBackgroundResource(itemSelectStyle)
            }
            val titleColor = albumWindowStyle?.albumAdapterItemTitleColor ?: 0
            if (titleColor != 0) {
                tvFolderName.setTextColor(titleColor)
            }
            val titleSize = albumWindowStyle?.albumAdapterItemTitleSize ?: 0
            if (titleSize > 0) {
                tvFolderName.setTextSize(titleSize.toFloat())
            }
        }
    }

    private var onAlbumItemClickListener: OnAlbumItemClickListener? = null

    /**
     * 专辑列表桥接类
     *
     * @param listener
     */
    fun setOnIBridgeAlbumWidget(listener: OnAlbumItemClickListener?) {
        this.onAlbumItemClickListener = listener
    }
}
