package com.luck.picture.lib.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.luck.picture.lib.R
import com.luck.picture.lib.adapter.PictureAlbumAdapter
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.decoration.WrapContentLinearLayoutManager
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnAlbumItemClickListener
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.SdkVersionUtils

/**
 * @author：luck
 * @date：2021/11/17 2:33 下午
 * @describe：AlbumListPopWindow
 */
class AlbumListPopWindow(
    private val mContext: Context?,
    private val selectorConfig: SelectorConfig
) : PopupWindow() {
    private var windMask: View? = null
    private var mRecyclerView: RecyclerView? = null
    private var isDismiss = false
    private var windowMaxHeight = 0
    private var mAdapter: PictureAlbumAdapter? = null

    private fun initViews() {
        windowMaxHeight = (DensityUtil.getScreenHeight(mContext!!) * 0.6).toInt()
        mRecyclerView = contentView?.findViewById<RecyclerView>(R.id.folder_list)
        windMask = contentView?.findViewById<View>(R.id.rootViewBg)
        mRecyclerView?.layoutManager = WrapContentLinearLayoutManager(mContext)
        mAdapter = PictureAlbumAdapter(selectorConfig)
        mRecyclerView?.adapter = mAdapter
        windMask?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                dismiss()
            }
        })
        contentView?.findViewById<View?>(R.id.rootView)
            ?.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if (SdkVersionUtils.isMinM) {
                        dismiss()
                    }
                }
            })
    }

    @SuppressLint("NotifyDataSetChanged")
    fun bindAlbumData(list: MutableList<LocalMediaFolder?>) {
        mAdapter?.bindAlbumData(list)
        mAdapter?.notifyDataSetChanged()
        val lp = mRecyclerView?.layoutParams
        if (lp != null) {
            lp.height =
                if (list.size > ALBUM_MAX_COUNT) windowMaxHeight else ViewGroup.LayoutParams.WRAP_CONTENT
            mRecyclerView?.layoutParams = lp
        }
    }

    val albumList: MutableList<LocalMediaFolder?>
        get() = mAdapter?.getAlbumList() ?: mutableListOf()

    fun getFolder(position: Int): LocalMediaFolder? {
        val albumList = mAdapter?.getAlbumList() ?: return null
        return if (albumList.size > 0 && position < albumList.size) {
            albumList[position]
        } else {
            null
        }
    }

    val firstAlbumImageCount: Int
        get() = if (this.folderCount > 0) getFolder(0)?.folderTotalNum ?: 0 else 0

    val folderCount: Int
        get() = mAdapter?.getAlbumList()?.size ?: 0

    /**
     * 专辑列表桥接类
     *
     * @param listener
     */
    fun setOnIBridgeAlbumWidget(listener: OnAlbumItemClickListener?) {
        mAdapter?.setOnIBridgeAlbumWidget(listener)
    }

    override fun showAsDropDown(anchor: View) {
        if (this.albumList.isEmpty()) {
            return
        }
        if (SdkVersionUtils.isN) {
            val location = IntArray(2)
            anchor.getLocationInWindow(location)
            showAtLocation(anchor, Gravity.NO_GRAVITY, 0, location[1] + anchor.height)
        } else {
            super.showAsDropDown(anchor)
        }
        isDismiss = false
        windowStatusListener?.onShowPopupWindow()
        windMask?.animate()?.alpha(1f)?.setDuration(250)?.setStartDelay(250)?.start()
        changeSelectedAlbumStyle()
    }

    /**
     * 设置选中状态
     */
    fun changeSelectedAlbumStyle() {
        val folders = mAdapter?.getAlbumList() ?: return
        for (i in folders.indices) {
            val folder = folders[i]
            folder?.isSelectTag = false
            mAdapter?.notifyItemChanged(i)
            for (j in 0 until selectorConfig.selectCount) {
                val media = selectorConfig.selectedResult[j]
                if (TextUtils.equals(folder?.getFolderName(), media?.parentFolderName)
                    || folder?.bucketId == PictureConfig.ALL.toLong()
                ) {
                    folder?.isSelectTag = true
                    mAdapter?.notifyItemChanged(i)
                    break
                }
            }
        }
    }

    override fun dismiss() {
        if (isDismiss) {
            return
        }
        windMask?.alpha = 0f
        windowStatusListener?.onDismissPopupWindow()
        isDismiss = true
        windMask?.post(object : Runnable {
            override fun run() {
                super@AlbumListPopWindow.dismiss()
                isDismiss = false
            }
        })
    }


    /**
     * AlbumListPopWindow 弹出与消失状态监听
     *
     * @param listener
     */
    fun setOnPopupWindowStatusListener(listener: OnPopupWindowStatusListener?) {
        this.windowStatusListener = listener
    }

    private var windowStatusListener: OnPopupWindowStatusListener? = null

    init {
        setContentView(LayoutInflater.from(mContext).inflate(R.layout.ps_window_folder, null))
        setWidth(RelativeLayout.LayoutParams.MATCH_PARENT)
        setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT)
        setAnimationStyle(R.style.PictureThemeWindowStyle)
        setFocusable(true)
        setOutsideTouchable(true)
        update()
        initViews()
    }

    interface OnPopupWindowStatusListener {
        fun onShowPopupWindow()

        fun onDismissPopupWindow()
    }

    companion object {
        private const val ALBUM_MAX_COUNT = 8
        fun buildPopWindow(context: Context?, config: SelectorConfig): AlbumListPopWindow {
            return AlbumListPopWindow(context, config)
        }
    }
}
