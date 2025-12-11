package com.luck.picture.lib.adapter.holder

import android.content.Context
import android.graphics.ColorFilter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.R
import com.luck.picture.lib.adapter.PictureImageGridAdapter
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.manager.SelectedManager
import com.luck.picture.lib.utils.AnimUtils
import com.luck.picture.lib.utils.StyleUtils
import com.luck.picture.lib.utils.ValueOf.toString

/**
 * @author：luck
 * @date：2021/11/20 3:17 下午
 * @describe：BaseRecyclerMediaHolder
 */
open class BaseRecyclerMediaHolder : RecyclerView.ViewHolder {
    var ivPicture: ImageView? = null
    var tvCheck: TextView? = null
    var btnCheck: View? = null
    var mContext: Context? = null
    var selectorConfig: SelectorConfig? = null
    var isSelectNumberStyle: Boolean = false
    var isHandleMask: Boolean = false
    private var defaultColorFilter: ColorFilter? = null
    private var selectColorFilter: ColorFilter? = null
    private var maskWhiteColorFilter: ColorFilter? = null

    constructor(itemView: View) : super(itemView)

    constructor(itemView: View, config: SelectorConfig) : super(itemView) {
        this.selectorConfig = config
        this.mContext = itemView.context
        val context = mContext
        if (context != null) {
            defaultColorFilter = StyleUtils.getColorFilter(context, R.color.ps_color_20)
            selectColorFilter = StyleUtils.getColorFilter(context, R.color.ps_color_80)
            maskWhiteColorFilter = StyleUtils.getColorFilter(context, R.color.ps_color_half_white)
        }
        val selectMainStyle = selectorConfig!!.selectorStyle?.selectMainStyle
        isSelectNumberStyle = selectMainStyle?.isSelectNumberStyle ?: false
        ivPicture = itemView.findViewById<ImageView>(R.id.ivPicture)
        tvCheck = itemView.findViewById<TextView>(R.id.tvCheck)
        btnCheck = itemView.findViewById<View>(R.id.btnCheck)
        if (config.selectionMode == SelectModeConfig.SINGLE && config.isDirectReturnSingle) {
            tvCheck!!.visibility = View.GONE
            btnCheck!!.visibility = View.GONE
        } else {
            tvCheck!!.visibility = View.VISIBLE
            btnCheck!!.visibility = View.VISIBLE
        }

        isHandleMask = !config.isDirectReturnSingle
                && (config.selectionMode == SelectModeConfig.SINGLE || config.selectionMode == SelectModeConfig.MULTIPLE)

        val textSize = selectMainStyle?.adapterSelectTextSize ?: 0
        if (StyleUtils.checkSizeValidity(textSize)) {
            tvCheck!!.setTextSize(textSize.toFloat())
        }
        val textColor = selectMainStyle?.adapterSelectTextColor ?: 0
        if (StyleUtils.checkStyleValidity(textColor)) {
            tvCheck!!.setTextColor(textColor)
        }
        val adapterSelectBackground = selectMainStyle?.selectBackground ?: 0
        if (StyleUtils.checkStyleValidity(adapterSelectBackground)) {
            tvCheck!!.setBackgroundResource(adapterSelectBackground)
        }
        val selectStyleGravity = selectMainStyle?.adapterSelectStyleGravity
        if (StyleUtils.checkArrayValidity(selectStyleGravity)) {
            if (tvCheck!!.layoutParams is RelativeLayout.LayoutParams) {
                (tvCheck!!.layoutParams as RelativeLayout.LayoutParams).removeRule(
                    RelativeLayout.ALIGN_PARENT_END
                )
                for (i in selectStyleGravity!!) {
                    (tvCheck!!.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }
            if (btnCheck!!.layoutParams is RelativeLayout.LayoutParams) {
                (btnCheck!!.layoutParams as RelativeLayout.LayoutParams).removeRule(
                    RelativeLayout.ALIGN_PARENT_END
                )
                for (i in selectStyleGravity!!) {
                    (btnCheck!!.layoutParams as RelativeLayout.LayoutParams).addRule(i)
                }
            }

            val clickArea = selectMainStyle?.adapterSelectClickArea ?: 0
            if (StyleUtils.checkSizeValidity(clickArea)) {
                val clickAreaParams = btnCheck!!.layoutParams
                clickAreaParams.width = clickArea
                clickAreaParams.height = clickArea
            }
        }
    }

    /**
     * bind Data
     *
     * @param media
     * @param position
     */
    open fun bindData(media: LocalMedia, position: Int) {
        media.position = absoluteAdapterPosition

        selectedMedia(isSelected(media))

        if (isSelectNumberStyle) {
            notifySelectNumberStyle(media)
        }

        if (isHandleMask && selectorConfig!!.isMaxSelectEnabledMask) {
            dispatchHandleMask(media)
        }

        var path = media.path
        if (media.isEditorImage()) {
            path = media.cutPath
        }

        loadCover(path)

        tvCheck!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                btnCheck!!.performClick()
            }
        })

        btnCheck!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (listener == null) {
                    return
                }
                val resultCode = listener!!.onSelected(tvCheck, position, media)
                if (resultCode == SelectedManager.INVALID) {
                    return
                }
                if (resultCode == SelectedManager.ADD_SUCCESS) {
                    if (selectorConfig!!.isSelectZoomAnim) {
                        val animListener = selectorConfig!!.onItemSelectAnimListener
                        if (animListener != null) {
                            animListener.onSelectItemAnim(
                                ivPicture,
                                true
                            )
                        } else {
                            AnimUtils.selectZoom(ivPicture)
                        }
                    }
                } else if (resultCode == SelectedManager.REMOVE) {
                    if (selectorConfig!!.isSelectZoomAnim) {
                        val animListener = selectorConfig!!.onItemSelectAnimListener
                        if (animListener != null) {
                            animListener.onSelectItemAnim(
                                ivPicture,
                                false
                            )
                        }
                    }
                }
                selectedMedia(isSelected(media))
            }
        })

        itemView.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                if (listener != null) {
                    listener!!.onItemLongClick(v, position)
                }
                return false
            }
        })

        itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (listener == null) {
                    return
                }
                val isPreview =
                    PictureMimeType.isHasImage(media.mimeType) && selectorConfig!!.isEnablePreviewImage || selectorConfig!!.isDirectReturnSingle
                            || PictureMimeType.isHasVideo(media.mimeType) && (selectorConfig!!.isEnablePreviewVideo
                            || selectorConfig!!.selectionMode == SelectModeConfig.SINGLE) || PictureMimeType.isHasAudio(
                        media.mimeType
                    ) && (selectorConfig!!.isEnablePreviewAudio
                            || selectorConfig!!.selectionMode == SelectModeConfig.SINGLE)
                if (isPreview) {
                    if (media.isMaxSelectEnabledMask) {
                        return
                    }
                    listener!!.onItemClick(tvCheck, position, media)
                } else {
                    btnCheck!!.performClick()
                }
            }
        })
    }

    /**
     * 加载资源封面
     */
    protected open fun loadCover(path: String?) {
        val imageEngine = selectorConfig?.imageEngine
        if (imageEngine != null && ivPicture != null) {
            imageEngine.loadGridImage(ivPicture!!.context, path, ivPicture)
        }
    }


    /**
     * 处理到达选择条件后的蒙层效果
     */
    private fun dispatchHandleMask(media: LocalMedia) {
        var isEnabledMask = false
        val config = selectorConfig!!
        if (config.selectCount > 0 && !config.selectedResult.contains(media)) {
            if (config.isWithVideoImage) {
                if (config.selectionMode == SelectModeConfig.SINGLE) {
                    isEnabledMask = config.selectCount == Int.MAX_VALUE
                } else {
                    isEnabledMask =
                        config.selectCount == config.maxSelectNum
                }
            } else {
                if (PictureMimeType.isHasVideo(config.resultFirstMimeType)) {
                    val maxSelectNum: Int
                    if (config.selectionMode == SelectModeConfig.SINGLE) {
                        maxSelectNum = Int.MAX_VALUE
                    } else {
                        maxSelectNum = if (config.maxVideoSelectNum > 0)
                            config.maxVideoSelectNum
                        else
                            config.maxSelectNum
                    }
                    isEnabledMask = config.selectCount == maxSelectNum
                            || PictureMimeType.isHasImage(media.mimeType)
                } else {
                    val maxSelectNum: Int
                    if (config.selectionMode == SelectModeConfig.SINGLE) {
                        maxSelectNum = Int.MAX_VALUE
                    } else {
                        maxSelectNum = config.maxSelectNum
                    }
                    isEnabledMask = config.selectCount == maxSelectNum
                            || PictureMimeType.isHasVideo(media.mimeType)
                }
            }
        }
        if (isEnabledMask) {
            ivPicture!!.setColorFilter(maskWhiteColorFilter)
            media.isMaxSelectEnabledMask = true
        } else {
            media.isMaxSelectEnabledMask = false
        }
    }

    /**
     * 设置选中缩放动画
     *
     * @param isChecked
     */
    private fun selectedMedia(isChecked: Boolean) {
        if (tvCheck!!.isSelected != isChecked) {
            tvCheck!!.setSelected(isChecked)
        }
        if (selectorConfig!!.isDirectReturnSingle) {
            ivPicture!!.setColorFilter(defaultColorFilter)
        } else {
            ivPicture!!.setColorFilter(if (isChecked) selectColorFilter else defaultColorFilter)
        }
    }

    /**
     * 检查LocalMedia是否被选中
     *
     * @param currentMedia
     * @return
     */
    private fun isSelected(currentMedia: LocalMedia): Boolean {
        val selectedResult = selectorConfig!!.selectedResult
        val isSelected = selectedResult.contains(currentMedia)
        if (isSelected) {
            val compare = currentMedia.compareLocalMedia
            if (compare != null && compare.isEditorImage()) {
                currentMedia.cutPath = compare.cutPath
                currentMedia.setCut(!TextUtils.isEmpty(compare.cutPath))
                currentMedia.setEditorImage(compare.isEditorImage())
            }
        }
        return isSelected
    }

    /**
     * 对选择数量进行编号排序
     */
    private fun notifySelectNumberStyle(currentMedia: LocalMedia) {
        tvCheck!!.text = ""
        for (i in 0 until selectorConfig!!.selectCount) {
            val media = selectorConfig!!.selectedResult[i]
            if (media != null) {
                if (TextUtils.equals(media.path, currentMedia.path)
                    || media.id == currentMedia.id
                ) {
                    currentMedia.num = media.num
                    media.position = currentMedia.position
                    tvCheck!!.text = toString(currentMedia.num)
                }
            }
        }
    }

    private var listener: PictureImageGridAdapter.OnItemClickListener? = null

    fun setOnItemClickListener(listener: PictureImageGridAdapter.OnItemClickListener?) {
        this.listener = listener
    }

    companion object {
        fun generate(
            parent: ViewGroup,
            viewType: Int,
            resource: Int,
            config: SelectorConfig?
        ): BaseRecyclerMediaHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(resource, parent, false)
            val nonNullConfig = config ?: throw IllegalArgumentException("SelectorConfig cannot be null")
            when (viewType) {
                PictureImageGridAdapter.Companion.ADAPTER_TYPE_CAMERA -> return CameraViewHolder(
                    itemView
                )

                PictureImageGridAdapter.Companion.ADAPTER_TYPE_VIDEO -> return VideoViewHolder(
                    itemView,
                    nonNullConfig
                )

                PictureImageGridAdapter.Companion.ADAPTER_TYPE_AUDIO -> return AudioViewHolder(
                    itemView,
                    nonNullConfig
                )

                else -> return ImageViewHolder(itemView, nonNullConfig)
            }
        }
    }
}
