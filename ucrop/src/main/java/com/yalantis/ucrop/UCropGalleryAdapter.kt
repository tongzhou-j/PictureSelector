package com.yalantis.ucrop

import android.graphics.ColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.recyclerview.widget.RecyclerView
import com.yalantis.ucrop.R

/**
 * @author：luck
 * @date：2016-12-31 22:22
 * @describe：UCropGalleryAdapter
 */
class UCropGalleryAdapter(private val list: List<String>) : RecyclerView.Adapter<UCropGalleryAdapter.ViewHolder>() {
    private var currentSelectPosition: Int = 0

    fun setCurrentSelectPosition(currentSelectPosition: Int) {
        this.currentSelectPosition = currentSelectPosition
    }

    fun getCurrentSelectPosition(): Int {
        return currentSelectPosition
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ucrop_gallery_adapter_item,
            parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = list[position]
        UCropDevelopConfig.imageEngine?.loadImage(holder.itemView.context, path, holder.mIvPhoto)
        
        val colorFilter: ColorFilter? = if (currentSelectPosition == position) {
            holder.mViewCurrentSelect.visibility = View.VISIBLE
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                ContextCompat.getColor(holder.itemView.context, R.color.ucrop_color_80),
                BlendModeCompat.SRC_ATOP)
        } else {
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                ContextCompat.getColor(holder.itemView.context, R.color.ucrop_color_20),
                BlendModeCompat.SRC_ATOP).also {
                holder.mViewCurrentSelect.visibility = View.GONE
            }
        }
        holder.mIvPhoto.colorFilter = colorFilter
        holder.itemView.setOnClickListener { v ->
            listener?.onItemClick(holder.absoluteAdapterPosition, v)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val mIvPhoto: ImageView = view.findViewById(R.id.iv_photo)
        val mViewCurrentSelect: View = view.findViewById(R.id.view_current_select)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View)
    }
}

