package com.luck.pictureselector

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener
import com.luck.picture.lib.interfaces.OnQueryDataSourceListener
import com.luck.picture.lib.loader.IBridgeMediaLoader
import com.luck.picture.lib.R as PictureLibR
import com.luck.pictureselector.R
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.widget.RecyclerPreloadView
import java.util.ArrayList

/**
 * @author：luck
 * @date：2022/2/17 6:24 下午
 * @describe：OnlyQueryDataActivity
 */
class OnlyQueryDataActivity : AppCompatActivity() {
    private val mData = ArrayList<LocalMedia>()

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_only_query_data)
        val mRecycler = findViewById<RecyclerPreloadView>(R.id.recycler)
        mRecycler.addItemDecoration(GridSpacingItemDecoration(4,
            DensityUtil.dip2px(this, 1f), false))
        mRecycler.layoutManager = GridLayoutManager(this, 4)
        val itemAnimator = mRecycler.itemAnimator
        if (itemAnimator != null) {
            (itemAnimator as SimpleItemAnimator).setSupportsChangeAnimations(false)
            mRecycler.itemAnimator = null
        }

        val adapter = GridAdapter(mData)
        mRecycler.adapter = adapter
        PictureSelector.create(this)
            .dataSource(SelectMimeType.ofAll())
            .setQuerySortOrder(MediaStore.MediaColumns.DATE_MODIFIED + " DESC")
            .obtainMediaData(object : OnQueryDataSourceListener<LocalMedia> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onComplete(result: List<LocalMedia>) {
                    mData.addAll(result)
                    adapter.notifyDataSetChanged()
                }
            })

        findViewById<View>(R.id.tv_build_loader).setOnClickListener { v ->
            val loader = PictureSelector.create(v.context)
                .dataSource(SelectMimeType.ofImage()).buildMediaLoader()
            loader.loadAllAlbum(object : OnQueryAllAlbumListener<LocalMediaFolder> {
                override fun onComplete(result: List<LocalMediaFolder>) {
                }
            })
        }
    }

    class GridAdapter(private val list: List<LocalMedia>) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {
        @NonNull
        override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.gv_filter_image, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(@NonNull viewHolder: ViewHolder, position: Int) {
            viewHolder.mIvDel.visibility = View.GONE
            val media = list[position]
            val chooseModel = media.chooseModel
            val path = media.path
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
                    .load(if (PictureMimeType.isContent(path)) Uri.parse(path) else path)
                    .centerCrop()
                    .placeholder(PictureLibR.drawable.ps_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(viewHolder.mImg)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var mImg: ImageView = view.findViewById(R.id.fiv)
            var mIvDel: ImageView = view.findViewById(R.id.iv_del)
            var tvDuration: TextView = view.findViewById(R.id.tv_duration)
        }
    }
}

