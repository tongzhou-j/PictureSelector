package com.luck.picture.lib

import android.annotation.SuppressLint
import android.app.Service
import android.os.Bundle
import android.os.SystemClock
import android.os.Vibrator
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.luck.picture.lib.PictureOnlyCameraFragment.Companion.newInstance
import com.luck.picture.lib.adapter.PictureImageGridAdapter
import com.luck.picture.lib.animators.AlphaInAnimationAdapter
import com.luck.picture.lib.animators.AnimationType
import com.luck.picture.lib.animators.SlideInBottomAnimationAdapter
import com.luck.picture.lib.basic.FragmentInjectManager
import com.luck.picture.lib.basic.IPictureSelectorEvent
import com.luck.picture.lib.basic.PictureCommonFragment
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.config.PermissionEvent
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.dialog.AlbumListPopWindow
import com.luck.picture.lib.dialog.AlbumListPopWindow.OnPopupWindowStatusListener
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnAlbumItemClickListener
import com.luck.picture.lib.interfaces.OnQueryAlbumListener
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.interfaces.OnRecyclerViewPreloadMoreListener
import com.luck.picture.lib.interfaces.OnRecyclerViewScrollListener
import com.luck.picture.lib.interfaces.OnRecyclerViewScrollStateListener
import com.luck.picture.lib.interfaces.OnRequestPermissionListener
import com.luck.picture.lib.loader.IBridgeMediaLoader
import com.luck.picture.lib.loader.LocalMediaLoader
import com.luck.picture.lib.loader.LocalMediaPageLoader
import com.luck.picture.lib.magical.BuildRecycleItemViewParams
import com.luck.picture.lib.manager.SelectedManager
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.permissions.PermissionConfig
import com.luck.picture.lib.permissions.PermissionResultCallback
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.luck.picture.lib.utils.AnimUtils
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.DoubleUtils
import com.luck.picture.lib.utils.StyleUtils
import com.luck.picture.lib.utils.ToastUtils
import com.luck.picture.lib.utils.ValueOf.toLong
import com.luck.picture.lib.widget.BottomNavBar
import com.luck.picture.lib.widget.BottomNavBar.OnBottomNavBarListener
import com.luck.picture.lib.widget.CompleteSelectView
import com.luck.picture.lib.widget.RecyclerPreloadView
import com.luck.picture.lib.widget.SlideSelectTouchListener
import com.luck.picture.lib.widget.SlideSelectionHandler
import com.luck.picture.lib.widget.SlideSelectionHandler.ISelectionHandler
import com.luck.picture.lib.widget.TitleBar
import com.luck.picture.lib.widget.TitleBar.OnTitleBarListener
import java.io.File

/**
 * @author：luck
 * @date：2021/11/17 10:24 上午
 * @describe：PictureSelectorFragment
 */
class PictureSelectorFragment : PictureCommonFragment(), OnRecyclerViewPreloadMoreListener,
    IPictureSelectorEvent {
    override fun onPermissionExplainEvent(
        isDisplayExplain: Boolean,
        permissionArray: Array<String?>?
    ) {
        super.onPermissionExplainEvent(isDisplayExplain, permissionArray)
    }

    private var mRecycler: RecyclerPreloadView? = null
    private var tvDataEmpty: TextView? = null
    private var titleBar: TitleBar? = null
    private var bottomNarBar: BottomNavBar? = null
    private var completeSelectView: CompleteSelectView? = null
    private var tvCurrentDataTime: TextView? = null
    private var intervalClickTime: Long = 0
    private var allFolderSize = 0
    private var currentPosition = -1

    /**
     * Use camera to callback
     */
    private var isCameraCallback = false

    /**
     * memory recycling
     */
    private var isMemoryRecycling = false
    private var isDisplayCamera = false

    private var mAdapter: PictureImageGridAdapter? = null

    private var albumListPopWindow: AlbumListPopWindow? = null

    private var mDragSelectTouchListener: SlideSelectTouchListener? = null

    override fun handlePermissionDenied(permissionArray: Array<String?>?) {
        super.handlePermissionDenied(permissionArray ?: arrayOf())
    }

    override val resourceId: Int
        get() {
            val layoutResourceId = InjectResourceSource.getLayoutResource(
                context!!,
                InjectResourceSource.MAIN_SELECTOR_LAYOUT_RESOURCE,
                selectorConfig
            )
            return if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) {
                layoutResourceId
            } else {
                R.layout.ps_fragment_selector
            }
        }

    override fun confirmSelect(currentMedia: LocalMedia?, isSelected: Boolean): Int {
        return super.confirmSelect(currentMedia, isSelected)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSelectedChange(isAddRemove: Boolean, currentMedia: LocalMedia?) {
        if (currentMedia == null) return
        bottomNarBar!!.setSelectedChange()
        completeSelectView!!.setSelectedChange(false)
        // 刷新列表数据
        if (checkNotifyStrategy(isAddRemove)) {
            mAdapter!!.notifyItemPositionChanged(currentMedia.position)
            mRecycler!!.postDelayed(object : Runnable {
                override fun run() {
                    mAdapter!!.notifyDataSetChanged()
                }
            }, PictureSelectorFragment.Companion.SELECT_ANIM_DURATION.toLong())
        } else {
            mAdapter!!.notifyItemPositionChanged(currentMedia.position)
        }
        if (!isAddRemove) {
            sendChangeSubSelectPositionEvent(true)
        }
    }

    override fun onFixedSelectedChange(oldLocalMedia: LocalMedia?) {
        if (oldLocalMedia == null) return
        mAdapter!!.notifyItemPositionChanged(oldLocalMedia.position)
    }

    override fun sendChangeSubSelectPositionEvent(adapterChange: Boolean) {
        val config = selectorConfig ?: return
        if (config.selectorStyle?.selectMainStyle?.isSelectNumberStyle == true) {
            for (index in 0..<config.selectCount) {
                val media = config.selectedResult.get(index)
                media?.num = index + 1
                if (adapterChange && media != null) {
                    mAdapter!!.notifyItemPositionChanged(media.position)
                }
            }
        }
    }

    override fun onCheckOriginalChange() {
        bottomNarBar!!.setOriginalCheck()
    }

    /**
     * 刷新列表策略
     *
     * @param isAddRemove
     * @return
     */
    private fun checkNotifyStrategy(isAddRemove: Boolean): Boolean {
        val config = selectorConfig ?: return false
        var isNotifyAll = false
        if (config.isMaxSelectEnabledMask) {
            if (config.isWithVideoImage) {
                if (config.selectionMode == SelectModeConfig.SINGLE) {
                    // ignore
                } else {
                    isNotifyAll = config.selectCount == config.maxSelectNum
                            || (!isAddRemove && config.selectCount == config.maxSelectNum - 1)
                }
            } else {
                if (config.selectCount == 0 || (isAddRemove && config.selectCount == 1)) {
                    // 首次添加或单选，选择数量变为0了，都notifyDataSetChanged
                    isNotifyAll = true
                } else {
                    if (PictureMimeType.isHasVideo(config.resultFirstMimeType)) {
                        val maxSelectNum = if (config.maxVideoSelectNum > 0)
                            config.maxVideoSelectNum
                        else
                            config.maxSelectNum
                        isNotifyAll = config.selectCount == maxSelectNum
                                || (!isAddRemove && config.selectCount == maxSelectNum - 1)
                    } else {
                        isNotifyAll = config.selectCount == config.maxSelectNum
                                || (!isAddRemove && config.selectCount == config.maxSelectNum - 1)
                    }
                }
            }
        }
        return isNotifyAll
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PictureConfig.EXTRA_ALL_FOLDER_SIZE, allFolderSize)
        outState.putInt(PictureConfig.EXTRA_CURRENT_PAGE, mPage)
        if (mRecycler != null) {
            outState.putInt(
                PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION,
                mRecycler!!.lastVisiblePosition
            )
        }
        if (mAdapter != null) {
            outState.putBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA, mAdapter!!.isDisplayCamera)
            val dataList = mAdapter!!.data.filterNotNull().map { it as LocalMedia? }.toMutableList() as ArrayList<LocalMedia?>
            selectorConfig?.addDataSource(dataList)
        }
        if (albumListPopWindow != null) {
            selectorConfig?.addAlbumDataSource(albumListPopWindow!!.albumList)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reStartSavedInstance(savedInstanceState)
        isMemoryRecycling = savedInstanceState != null
        tvDataEmpty = view.findViewById<TextView>(R.id.tv_data_empty)
        completeSelectView = view.findViewById<CompleteSelectView>(R.id.ps_complete_select)
        titleBar = view.findViewById<TitleBar>(R.id.title_bar)
        bottomNarBar = view.findViewById<BottomNavBar>(R.id.bottom_nar_bar)
        tvCurrentDataTime = view.findViewById<TextView>(R.id.tv_current_data_time)
        onCreateLoader()
        initAlbumListPopWindow()
        initTitleBar()
        initComplete()
        initRecycler(view)
        initBottomNavBar()
        if (isMemoryRecycling) {
            recoverSaveInstanceData()
        } else {
            requestLoadData()
        }
    }


    override fun onFragmentResume() {
        setRootViewKeyListener(requireView())
    }

    override fun reStartSavedInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            allFolderSize = savedInstanceState.getInt(PictureConfig.EXTRA_ALL_FOLDER_SIZE)
            mPage = savedInstanceState.getInt(PictureConfig.EXTRA_CURRENT_PAGE, mPage)
            currentPosition = savedInstanceState.getInt(
                PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION,
                currentPosition
            )
            isDisplayCamera = savedInstanceState.getBoolean(
                PictureConfig.EXTRA_DISPLAY_CAMERA,
                selectorConfig?.isDisplayCamera ?: false
            )
        } else {
            isDisplayCamera = selectorConfig?.isDisplayCamera ?: false
        }
    }


    /**
     * 完成按钮
     */
    private fun initComplete() {
        val config = selectorConfig ?: return
        val ctx = context ?: return
        if (config.selectionMode == SelectModeConfig.SINGLE && config.isDirectReturnSingle) {
            config.selectorStyle?.titleBarStyle?.isHideCancelButton = false
            titleBar!!.titleCancelView.setVisibility(View.VISIBLE)
            completeSelectView!!.setVisibility(View.GONE)
        } else {
            completeSelectView!!.setCompleteSelectViewStyle()
            completeSelectView!!.setSelectedChange(false)
            val selectMainStyle = config.selectorStyle?.selectMainStyle
            if (selectMainStyle?.isCompleteSelectRelativeTop == true) {
                if (completeSelectView!!.getLayoutParams() is ConstraintLayout.LayoutParams) {
                    (completeSelectView!!.getLayoutParams() as ConstraintLayout.LayoutParams).topToTop =
                        R.id.title_bar
                    (completeSelectView!!.getLayoutParams() as ConstraintLayout.LayoutParams).bottomToBottom =
                        R.id.title_bar
                    if (config.isPreviewFullScreenMode) {
                        (completeSelectView!!
                            .getLayoutParams() as ConstraintLayout.LayoutParams).topMargin =
                            DensityUtil.getStatusBarHeight(ctx)
                    }
                } else if (completeSelectView!!.getLayoutParams() is RelativeLayout.LayoutParams) {
                    if (config.isPreviewFullScreenMode) {
                        (completeSelectView!!
                            .getLayoutParams() as RelativeLayout.LayoutParams).topMargin =
                            DensityUtil.getStatusBarHeight(ctx)
                    }
                }
            }
            completeSelectView!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    if (config.isEmptyResultReturn && config.selectCount == 0) {
                        this@PictureSelectorFragment.onExitPictureSelector()
                    } else {
                        this@PictureSelectorFragment.dispatchTransformResult()
                    }
                }
            })
        }
    }


    override fun onCreateLoader() {
        val config = selectorConfig ?: return
        val ctx = requireContext()
        val loaderFactory = config.loaderFactory
        if (loaderFactory != null) {
            mLoader = loaderFactory.onCreateLoader()
            if (mLoader == null) {
                throw NullPointerException("No available " + IBridgeMediaLoader::class.java + " loader found")
            }
        } else {
            mLoader = if (config.isPageStrategy)
                LocalMediaPageLoader(ctx, config)
            else
                LocalMediaLoader(ctx, config)
        }
    }

    private fun initTitleBar() {
        val config = selectorConfig ?: return
        if (config.selectorStyle?.titleBarStyle?.isHideTitleBar == true) {
            titleBar!!.setVisibility(View.GONE)
        }
        titleBar!!.setTitleBarStyle()
        titleBar!!.setOnTitleBarListener(object : OnTitleBarListener() {
            override fun onTitleDoubleClick() {
                if (config.isAutomaticTitleRecyclerTop) {
                    val intervalTime = 500
                    if (SystemClock.uptimeMillis() - intervalClickTime < intervalTime && mAdapter!!.getItemCount() > 0) {
                        mRecycler!!.scrollToPosition(0)
                    } else {
                        intervalClickTime = SystemClock.uptimeMillis()
                    }
                }
            }

            override fun onBackPressed() {
                if (albumListPopWindow!!.isShowing()) {
                    albumListPopWindow!!.dismiss()
                } else {
                    onKeyBackFragmentFinish()
                }
            }

            override fun onShowAlbumPopWindow(anchor: View?) {
                anchor?.let { albumListPopWindow!!.showAsDropDown(it) }
            }
        })
    }

    /**
     * initAlbumListPopWindow
     */
    private fun initAlbumListPopWindow() {
        val config = selectorConfig ?: return
        albumListPopWindow =
            AlbumListPopWindow.Companion.buildPopWindow(requireContext(), config)
        albumListPopWindow!!.setOnPopupWindowStatusListener(object : OnPopupWindowStatusListener {
            override fun onShowPopupWindow() {
                if (!config.isOnlySandboxDir) {
                    AnimUtils.rotateArrow(titleBar!!.imageArrow, true)
                }
            }

            override fun onDismissPopupWindow() {
                if (!config.isOnlySandboxDir) {
                    AnimUtils.rotateArrow(titleBar!!.imageArrow, false)
                }
            }
        })
        addAlbumPopWindowAction()
    }

    private fun recoverSaveInstanceData() {
        val config = selectorConfig ?: return
        mAdapter!!.isDisplayCamera = isDisplayCamera
        enterAnimationDuration = 0L
        if (config.isOnlySandboxDir) {
            handleInAppDirAllMedia(config.currentLocalMediaFolder)
        } else {
            handleRecoverAlbumData(config.albumDataSource?.filterNotNull()?.toMutableList() ?: mutableListOf())
        }
    }


    private fun handleRecoverAlbumData(albumData: MutableList<LocalMediaFolder>) {
        val config = selectorConfig ?: return
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        if (albumData.size > 0) {
            val firstFolder: LocalMediaFolder
            if (config.currentLocalMediaFolder != null) {
                firstFolder = config.currentLocalMediaFolder!!
            } else {
                firstFolder = albumData[0] ?: return
                config.currentLocalMediaFolder = firstFolder
            }
            titleBar!!.setTitle(firstFolder.getFolderName())
            val albumDataList = albumData.map { it as LocalMediaFolder? }.toMutableList() as MutableList<LocalMediaFolder?>
            albumListPopWindow!!.bindAlbumData(albumDataList)
            if (config.isPageStrategy) {
                handleFirstPageMedia(ArrayList<LocalMedia?>(config.dataSource), true)
            } else {
                setAdapterData(firstFolder.getData())
            }
        } else {
            showDataNull()
        }
    }


    private fun requestLoadData() {
        val config = selectorConfig ?: return
        mAdapter!!.isDisplayCamera = isDisplayCamera
        val ctx = context ?: return
        if (PermissionChecker.isCheckReadStorage(
                config.chooseMode,
                ctx
            )
        ) {
            beginLoadData()
        } else {
            val readPermissionArray =
                PermissionConfig.getReadPermissionArray(ctx, config.chooseMode)
            onPermissionExplainEvent(true, readPermissionArray)
            if (config.onPermissionsEventListener != null) {
                onApplyPermissionsEvent(PermissionEvent.EVENT_SOURCE_DATA, readPermissionArray)
            } else {
                PermissionChecker.instance.requestPermissions(
                    this,
                    readPermissionArray,
                    object : PermissionResultCallback {
                        override fun onGranted() {
                            beginLoadData()
                        }

                        override fun onDenied() {
                            handlePermissionDenied(readPermissionArray)
                        }
                    })
            }
        }
    }

    override fun onApplyPermissionsEvent(event: Int, permissionArray: Array<String?>?) {
        val config = selectorConfig ?: return
        if (event != PermissionEvent.EVENT_SOURCE_DATA) {
            super.onApplyPermissionsEvent(event, permissionArray)
        } else {
            config.onPermissionsEventListener?.requestPermission(
                this,
                permissionArray,
                object : OnRequestPermissionListener {
                    override fun onCall(permissionArray: Array<String?>?, isResult: Boolean) {
                        if (isResult) {
                            beginLoadData()
                        } else {
                            handlePermissionDenied(permissionArray)
                        }
                    }
                })
        }
    }

    /**
     * 开始获取数据
     */
    private fun beginLoadData() {
        val config = selectorConfig ?: return
        onPermissionExplainEvent(false, null)
        if (config.isOnlySandboxDir) {
            loadOnlyInAppDirectoryAllMediaData()
        } else {
            loadAllAlbumData()
        }
    }

    override fun handlePermissionSettingResult(permissions: Array<String?>?) {
        if (permissions == null) {
            return
        }
        onPermissionExplainEvent(false, null)
        val isHasCamera =
            permissions.size > 0 && TextUtils.equals(permissions[0], PermissionConfig.CAMERA[0])
        val isHasPermissions: Boolean
        val config = selectorConfig ?: return
        val ctx = requireContext()
        val permissionsListener = config.onPermissionsEventListener
        if (permissionsListener != null) {
            isHasPermissions =
                permissionsListener.hasPermissions(this, permissions)
        } else {
            val nonNullPermissions = permissions.filterNotNull().toTypedArray()
            isHasPermissions =
                PermissionChecker.isCheckSelfPermission(ctx, nonNullPermissions)
        }
        if (isHasPermissions) {
            if (isHasCamera) {
                openSelectedCamera()
            } else {
                beginLoadData()
            }
        } else {
            if (isHasCamera) {
                ToastUtils.showToast(ctx, getString(R.string.ps_camera))
            } else {
                ToastUtils.showToast(ctx, getString(R.string.ps_jurisdiction))
                onKeyBackFragmentFinish()
            }
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = arrayOf<String?>()
    }

    /**
     * 给AlbumListPopWindow添加事件
     */
    private fun addAlbumPopWindowAction() {
        albumListPopWindow!!.setOnIBridgeAlbumWidget(object : OnAlbumItemClickListener {
            override fun onItemClick(position: Int, curFolder: LocalMediaFolder) {
                val config = selectorConfig ?: return
                isDisplayCamera =
                    config.isDisplayCamera && curFolder.bucketId == PictureConfig.ALL.toLong()
                mAdapter!!.isDisplayCamera = isDisplayCamera
                titleBar!!.setTitle(curFolder.getFolderName())
                val lastFolder = config.currentLocalMediaFolder
                val lastBucketId = lastFolder?.bucketId ?: PictureConfig.ALL.toLong()
                if (config.isPageStrategy) {
                    if (curFolder.bucketId != lastBucketId) {
                        // 1、记录一下上一次相册数据加载到哪了，到时候切回来的时候要续上
                        lastFolder?.setData(mAdapter!!.data.filterNotNull().map { it as LocalMedia? }.toMutableList() as ArrayList<LocalMedia?>)
                        lastFolder?.currentDataPage = mPage
                        lastFolder?.isHasMore = mRecycler!!.isEnabledLoadMore

                        // 2、判断当前相册是否请求过，如果请求过则不从MediaStore去拉取了
                        if (curFolder.getData().size > 0 && !curFolder.isHasMore) {
                            setAdapterData(curFolder.getData())
                            mPage = curFolder.currentDataPage
                            mRecycler!!.isEnabledLoadMore = curFolder.isHasMore
                            mRecycler!!.smoothScrollToPosition(0)
                        } else {
                            // 3、从MediaStore拉取数据
                            mPage = 1
                            val ctx = requireContext()
                            val loaderEngine = config.loaderDataEngine
                            if (loaderEngine != null) {
                                loaderEngine.loadFirstPageMediaData(
                                    ctx,
                                    curFolder.bucketId, mPage, config.pageSize,
                                    object : OnQueryDataResultListener<LocalMedia?>() {
                                        override fun onComplete(
                                            result: ArrayList<LocalMedia?>?,
                                            isHasMore: Boolean
                                        ) {
                                            handleSwitchAlbum(result ?: arrayListOf(), isHasMore)
                                        }
                                    })
                            } else {
                                mLoader?.loadPageMediaData(
                                    curFolder.bucketId, mPage, config.pageSize,
                                    object : OnQueryDataResultListener<LocalMedia?>() {
                                        override fun onComplete(
                                            result: ArrayList<LocalMedia?>?,
                                            isHasMore: Boolean
                                        ) {
                                            handleSwitchAlbum(result ?: arrayListOf(), isHasMore)
                                        }
                                    })
                            }
                        }
                    }
                } else {
                    // 非分页模式直接导入该相册下的所有资源
                    if (curFolder.bucketId != lastBucketId) {
                        setAdapterData(curFolder.getData())
                        mRecycler!!.smoothScrollToPosition(0)
                    }
                }
                config.currentLocalMediaFolder = curFolder
                albumListPopWindow!!.dismiss()
                if (mDragSelectTouchListener != null && config.isFastSlidingSelect) {
                    mDragSelectTouchListener!!.setRecyclerViewHeaderCount(if (mAdapter!!.isDisplayCamera) 1 else 0)
                }
            }
        })
    }

    private fun handleSwitchAlbum(result: ArrayList<LocalMedia?>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        mRecycler!!.isEnabledLoadMore = isHasMore
        if (result.size == 0) {
            // 如果从MediaStore拉取都没有数据了，adapter里的可能是缓存所以也清除
            mAdapter!!.data.clear()
        }
        setAdapterData(result)
        mRecycler!!.onScrolled(0, 0)
        mRecycler!!.smoothScrollToPosition(0)
    }


    private fun initBottomNavBar() {
        bottomNarBar!!.setBottomNavBarStyle()
        bottomNarBar!!.setOnBottomNavBarListener(object : OnBottomNavBarListener() {
            override fun onPreview() {
                onStartPreview(0, true)
            }

            override fun onCheckOriginalChange() {
                sendSelectedOriginalChangeEvent()
            }
        })
        bottomNarBar!!.setSelectedChange()
    }


    override fun loadAllAlbumData() {
        val engine = selectorConfig?.loaderDataEngine
        if (engine != null) {
            engine.loadAllAlbumData(
                context!!,
                object : OnQueryAllAlbumListener<LocalMediaFolder?> {
                    override fun onComplete(result: MutableList<LocalMediaFolder?>?) {
                        handleAllAlbumData(false, result?.filterNotNull()?.toMutableList() ?: mutableListOf())
                    }
                })
        } else {
            val isPreload = preloadPageFirstData()
            mLoader?.loadAllAlbum(object : OnQueryAllAlbumListener<LocalMediaFolder?> {
                override fun onComplete(result: MutableList<LocalMediaFolder?>?) {
                    handleAllAlbumData(isPreload, result?.filterNotNull()?.toMutableList() ?: mutableListOf())
                }
            })
        }
    }

    private fun preloadPageFirstData(): Boolean {
        var isPreload = false
        val config = selectorConfig ?: return false
        if (config.isPageStrategy && config.isPreloadFirst) {
            val firstFolder = LocalMediaFolder()
            firstFolder.bucketId = PictureConfig.ALL.toLong()
            if (TextUtils.isEmpty(config.defaultAlbumName)) {
                titleBar!!.setTitle(
                    if (config.chooseMode == SelectMimeType.ofAudio()) requireContext().getString(
                        R.string.ps_all_audio
                    ) else requireContext().getString(R.string.ps_camera_roll)
                )
            } else {
                titleBar!!.setTitle(config.defaultAlbumName)
            }
            firstFolder.setFolderName(titleBar!!.titleText)
            config.currentLocalMediaFolder = firstFolder
            loadFirstPageMediaData(firstFolder.bucketId)
            isPreload = true
        }
        return isPreload
    }

    private fun handleAllAlbumData(isPreload: Boolean, result: MutableList<LocalMediaFolder>) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        if (result.size > 0) {
            val firstFolder: LocalMediaFolder
            val config = selectorConfig ?: return
            if (isPreload) {
                firstFolder = result[0] ?: return
                config.currentLocalMediaFolder = firstFolder
            } else {
                if (config.currentLocalMediaFolder != null) {
                    firstFolder = config.currentLocalMediaFolder!!
                } else {
                    firstFolder = result[0] ?: return
                    config.currentLocalMediaFolder = firstFolder
                }
            }
            titleBar!!.setTitle(firstFolder.getFolderName())
            val resultList = result.map { it as LocalMediaFolder? }.toMutableList() as MutableList<LocalMediaFolder?>
            albumListPopWindow!!.bindAlbumData(resultList)
            if (config.isPageStrategy) {
                if (config.isPreloadFirst) {
                    mRecycler!!.isEnabledLoadMore = true
                } else {
                    loadFirstPageMediaData(firstFolder.bucketId)
                }
            } else {
                setAdapterData(firstFolder.getData())
            }
        } else {
            showDataNull()
        }
    }

    override fun loadFirstPageMediaData(firstBucketId: Long) {
        mPage = 1
        mRecycler!!.isEnabledLoadMore = true
        val engine = selectorConfig?.loaderDataEngine
        if (engine != null) {
            engine.loadFirstPageMediaData(
                context!!,
                firstBucketId,
                mPage,
                mPage * (selectorConfig?.pageSize ?: 0),
                object : OnQueryDataResultListener<LocalMedia?>() {
                    override fun onComplete(result: ArrayList<LocalMedia?>?, isHasMore: Boolean) {
                        handleFirstPageMedia(result ?: arrayListOf(), isHasMore)
                    }
                })
        } else {
            mLoader?.loadPageMediaData(
                firstBucketId, mPage, mPage * (selectorConfig?.pageSize ?: 0),
                object : OnQueryDataResultListener<LocalMedia?>() {
                    override fun onComplete(result: ArrayList<LocalMedia?>?, isHasMore: Boolean) {
                        handleFirstPageMedia(result ?: arrayListOf(), isHasMore)
                    }
                })
        }
    }

    private fun handleFirstPageMedia(result: ArrayList<LocalMedia?>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        mRecycler!!.isEnabledLoadMore = isHasMore
        if (mRecycler!!.isEnabledLoadMore && result.size == 0) {
            // 如果isHasMore为true但result.size() = 0;
            // 那么有可能是开启了某些条件过滤，实际上是还有更多资源的再强制请求
            onRecyclerViewPreloadMore()
        } else {
            setAdapterData(result)
        }
    }

    override fun loadOnlyInAppDirectoryAllMediaData() {
        val engine = selectorConfig?.loaderDataEngine
        if (engine != null) {
            engine.loadOnlyInAppDirAllMediaData(
                context!!,
                object : OnQueryAlbumListener<LocalMediaFolder?> {
                    override fun onComplete(folder: LocalMediaFolder?) {
                        handleInAppDirAllMedia(folder)
                    }
                })
        } else {
            mLoader?.loadOnlyInAppDirAllMedia(object : OnQueryAlbumListener<LocalMediaFolder?> {
                override fun onComplete(folder: LocalMediaFolder?) {
                    handleInAppDirAllMedia(folder)
                }
            })
        }
    }

    private fun handleInAppDirAllMedia(folder: LocalMediaFolder?) {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            val config = selectorConfig ?: return
            val sandboxDir = config.sandboxDir
            val isNonNull = folder != null
            val folderName = if (isNonNull) folder!!.getFolderName() else File(sandboxDir).getName()
            titleBar!!.setTitle(folderName)
            if (isNonNull) {
                config.currentLocalMediaFolder = folder
                setAdapterData(folder!!.getData())
            } else {
                showDataNull()
            }
        }
    }

    /**
     * 内存不足时，恢复RecyclerView定位位置
     */
    private fun recoveryRecyclerPosition() {
        if (currentPosition > 0) {
            mRecycler!!.post(object : Runnable {
                override fun run() {
                    mRecycler!!.scrollToPosition(currentPosition)
                    mRecycler!!.lastVisiblePosition = currentPosition
                }
            })
        }
    }

    private fun initRecycler(view: View) {
        val config = selectorConfig ?: return
        val ctx = requireContext()
        mRecycler = view.findViewById<RecyclerPreloadView?>(R.id.recycler)
        val selectorStyle = config.selectorStyle
        val selectMainStyle = selectorStyle?.selectMainStyle
        val listBackgroundColor = selectMainStyle?.mainListBackgroundColor ?: 0
        if (StyleUtils.checkStyleValidity(listBackgroundColor)) {
            mRecycler!!.setBackgroundColor(listBackgroundColor)
        } else {
            mRecycler!!.setBackgroundColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.ps_color_black
                )
            )
        }
        val imageSpanCount =
            if (config.imageSpanCount <= 0) PictureConfig.DEFAULT_SPAN_COUNT else config.imageSpanCount
        if (mRecycler!!.getItemDecorationCount() == 0) {
            val spacingSize = selectMainStyle?.adapterItemSpacingSize ?: 0
            if (StyleUtils.checkSizeValidity(spacingSize)) {
                mRecycler!!.addItemDecoration(
                    GridSpacingItemDecoration(
                        imageSpanCount,
                        spacingSize,
                        selectMainStyle?.isAdapterItemIncludeEdge ?: false
                    )
                )
            } else {
                mRecycler!!.addItemDecoration(
                    GridSpacingItemDecoration(
                        imageSpanCount,
                        DensityUtil.dip2px(ctx, 1f),
                        selectMainStyle?.isAdapterItemIncludeEdge ?: false
                    )
                )
            }
        }
        mRecycler!!.setLayoutManager(GridLayoutManager(ctx, imageSpanCount))
        val itemAnimator = mRecycler!!.getItemAnimator()
        if (itemAnimator != null) {
            (itemAnimator as SimpleItemAnimator).setSupportsChangeAnimations(false)
            mRecycler!!.setItemAnimator(null)
        }
        if (config.isPageStrategy) {
            mRecycler!!.setReachBottomRow(RecyclerPreloadView.BOTTOM_PRELOAD)
            mRecycler!!.setOnRecyclerViewPreloadListener(this)
        } else {
            mRecycler!!.setHasFixedSize(true)
        }
        mAdapter = PictureImageGridAdapter(ctx, config)
        mAdapter!!.isDisplayCamera = isDisplayCamera
        when (config.animationMode) {
            AnimationType.ALPHA_IN_ANIMATION -> mRecycler!!.adapter =
                AlphaInAnimationAdapter(
                    mAdapter!! as RecyclerView.Adapter<RecyclerView.ViewHolder>
                )

            AnimationType.SLIDE_IN_BOTTOM_ANIMATION -> mRecycler!!.adapter =
                SlideInBottomAnimationAdapter(mAdapter!! as RecyclerView.Adapter<RecyclerView.ViewHolder>)

            else -> mRecycler!!.adapter = mAdapter
        }

        addRecyclerAction()
    }


    private fun addRecyclerAction() {
        mAdapter!!.setOnItemClickListener(object : PictureImageGridAdapter.OnItemClickListener {
            override fun openCameraClick() {
                if (DoubleUtils.isFastDoubleClick) {
                    return
                }
                openSelectedCamera()
            }

            override fun onSelected(selectedView: View?, position: Int, media: LocalMedia?): Int {
                val selectResultCode = confirmSelect(media, selectedView?.isSelected ?: false)
                if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                    val animListener = selectorConfig?.onSelectAnimListener
                    if (animListener != null && selectedView != null) {
                        val duration =
                            animListener.onSelectAnim(selectedView)
                        if (duration > 0) {
                            PictureSelectorFragment.Companion.SELECT_ANIM_DURATION =
                                duration.toInt()
                        }
                    } else if (selectedView != null) {
                        val animation =
                            AnimationUtils.loadAnimation(context!!, R.anim.ps_anim_modal_in)
                        PictureSelectorFragment.Companion.SELECT_ANIM_DURATION =
                            animation.duration.toInt()
                        selectedView.startAnimation(animation)
                    }
                }
                return selectResultCode
            }

            override fun onItemClick(selectedView: View?, position: Int, media: LocalMedia?) {
                if (selectorConfig?.selectionMode == SelectModeConfig.SINGLE && selectorConfig?.isDirectReturnSingle == true) {
                    selectorConfig?.selectedResult?.clear()
                    val selectResultCode = confirmSelect(media, false)
                    if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                        this@PictureSelectorFragment.dispatchTransformResult()
                    }
                } else {
                    if (DoubleUtils.isFastDoubleClick) {
                        return
                    }
                    onStartPreview(position, false)
                }
            }

            override fun onItemLongClick(itemView: View?, position: Int) {
                if (mDragSelectTouchListener != null && selectorConfig?.isFastSlidingSelect == true) {
                    val vibrator =
                        activity!!.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(50)
                    mDragSelectTouchListener!!.startSlideSelection(position)
                }
            }
        })

        mRecycler!!.setOnRecyclerViewScrollStateListener(object :
            OnRecyclerViewScrollStateListener {
            override fun onScrollFast() {
                val imageEngine = selectorConfig?.imageEngine
                if (imageEngine != null) {
                    imageEngine.pauseRequests(context!!)
                }
            }

            override fun onScrollSlow() {
                val imageEngine = selectorConfig?.imageEngine
                if (imageEngine != null) {
                    imageEngine.resumeRequests(context!!)
                }
            }
        })
        mRecycler!!.setOnRecyclerViewScrollListener(object : OnRecyclerViewScrollListener {
            override fun onScrolled(dx: Int, dy: Int) {
                setCurrentMediaCreateTimeText()
            }

            override fun onScrollStateChanged(state: Int) {
                if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
                    showCurrentMediaCreateTimeUI()
                } else if (state == RecyclerView.SCROLL_STATE_IDLE) {
                    hideCurrentMediaCreateTimeUI()
                }
            }
        })

        if (selectorConfig?.isFastSlidingSelect == true) {
            val selectedPosition = HashSet<Int?>()
            val slideSelectionHandler = SlideSelectionHandler(object : ISelectionHandler {
                override val selection: MutableSet<Int?>?
                    get() {
                        for (i in 0 until (selectorConfig?.selectCount ?: 0)) {
                            val media = selectorConfig?.selectedResult?.get(i)
                            media?.position?.let { selectedPosition.add(it) }
                        }
                        return selectedPosition
                    }

                override fun changeSelection(
                    start: Int,
                    end: Int,
                    isSelected: Boolean,
                    calledFromOnStart: Boolean
                ) {
                    val adapterData = mAdapter!!.data
                    if (adapterData.size == 0 || start > adapterData.size) {
                        return
                    }
                    val media = adapterData.get(start)
                    val selectResultCode =
                        confirmSelect(media, selectorConfig?.selectedResult?.contains(media) ?: false)
                    mDragSelectTouchListener!!.setActive(selectResultCode != SelectedManager.INVALID)
                }
            })
            mDragSelectTouchListener = SlideSelectTouchListener()
                .setRecyclerViewHeaderCount(if (mAdapter!!.isDisplayCamera) 1 else 0)
                .withSelectListener(slideSelectionHandler)
            mRecycler!!.addOnItemTouchListener(mDragSelectTouchListener!!)
        }
    }

    /**
     * 显示当前资源时间轴
     */
    private fun setCurrentMediaCreateTimeText() {
        if (selectorConfig?.isDisplayTimeAxis == true) {
            val position = mRecycler!!.firstVisiblePosition
            if (position != RecyclerView.NO_POSITION) {
                val data = mAdapter!!.data
                if (data.size > position && data[position]?.dateAddedTime ?: 0 > 0) {
                    tvCurrentDataTime!!.text =
                        DateUtils.getDataFormat(
                            context!!,
                            data[position]?.dateAddedTime ?: 0
                        )
                }
            }
        }
    }

    /**
     * 显示当前资源时间轴
     */
    private fun showCurrentMediaCreateTimeUI() {
        if (selectorConfig?.isDisplayTimeAxis == true && mAdapter!!.data.size > 0) {
            if (tvCurrentDataTime!!.alpha == 0f) {
                tvCurrentDataTime!!.animate().setDuration(150).alphaBy(1.0f).start()
            }
        }
    }

    /**
     * 隐藏当前资源时间轴
     */
    private fun hideCurrentMediaCreateTimeUI() {
        if (selectorConfig?.isDisplayTimeAxis == true && mAdapter!!.data.size > 0) {
            tvCurrentDataTime!!.animate().setDuration(250).alpha(0.0f).start()
        }
    }

    /**
     * 预览图片
     *
     * @param position        预览图片下标
     * @param isBottomPreview true 底部预览模式 false列表预览模式
     */
    private fun onStartPreview(position: Int, isBottomPreview: Boolean) {
        if (ActivityCompatHelper.checkFragmentNonExits(
                activity,
                PictureSelectorPreviewFragment.Companion.TAG
            )
        ) {
            val data: ArrayList<LocalMedia?>?
            val totalNum: Int
            var currentBucketId: Long = 0
            if (isBottomPreview) {
                data = ArrayList<LocalMedia?>(selectorConfig?.selectedResult?.filterNotNull() ?: arrayListOf())
                totalNum = data.size
            } else {
                data = ArrayList<LocalMedia?>(mAdapter!!.data)
                val currentLocalMediaFolder = selectorConfig?.currentLocalMediaFolder
                if (currentLocalMediaFolder != null) {
                    totalNum = currentLocalMediaFolder.folderTotalNum
                    currentBucketId = currentLocalMediaFolder.bucketId
                } else {
                    totalNum = data.size
                    currentBucketId = if (data.size > 0) data[0]?.bucketId ?: PictureConfig.ALL.toLong() else PictureConfig.ALL.toLong()
                }
            }
            if (!isBottomPreview && selectorConfig?.isPreviewZoomEffect == true) {
                BuildRecycleItemViewParams.generateViewParams(
                    mRecycler!!,
                    if (selectorConfig?.isPreviewFullScreenMode == true) 0 else DensityUtil.getStatusBarHeight(
                        context!!
                    )
                )
            }
            val previewListener = selectorConfig?.onPreviewInterceptListener
            if (previewListener != null) {
                previewListener
                    .onPreview(
                        context!!,
                        position,
                        totalNum,
                        mPage,
                        currentBucketId,
                        titleBar!!.titleText,
                        mAdapter!!.isDisplayCamera,
                        data.filterNotNull().map { it as LocalMedia? }.toMutableList() as ArrayList<LocalMedia?>,
                        isBottomPreview
                    )
            } else {
                if (ActivityCompatHelper.checkFragmentNonExits(
                        activity,
                        PictureSelectorPreviewFragment.Companion.TAG
                    )
                ) {
                    val previewFragment: PictureSelectorPreviewFragment =
                        PictureSelectorPreviewFragment.Companion.newInstance()
                    val previewData = data.filterNotNull().map { it as LocalMedia }.toMutableList() as ArrayList<LocalMedia>
                    previewFragment.setInternalPreviewData(
                        isBottomPreview, titleBar!!.titleText, mAdapter!!.isDisplayCamera,
                        position, totalNum, mPage, currentBucketId, previewData
                    )
                    FragmentInjectManager.injectFragment(
                        activity!!,
                        PictureSelectorPreviewFragment.Companion.TAG,
                        previewFragment
                    )
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setAdapterData(result: ArrayList<LocalMedia?>?) {
        // 这个地方有个时间差，主要是解决进场动画和查询数据同时进行导致动画有点卡顿问题，
        // 主要是针对添加PictureSelectorFragment方式下
        val enterAnimationDuration = this.enterAnimationDuration
        if (enterAnimationDuration > 0) {
            requireView().postDelayed(object : Runnable {
                override fun run() {
                    setAdapterDataComplete(result)
                }
            }, enterAnimationDuration)
        } else {
            setAdapterDataComplete(result)
        }
    }

    private fun setAdapterDataComplete(result: ArrayList<LocalMedia?>?) {
        enterAnimationDuration = 0L
        sendChangeSubSelectPositionEvent(false)
        val nonNullResult = result?.filterNotNull()?.toMutableList() as? ArrayList<LocalMedia>
        mAdapter!!.setDataAndDataSetChanged(nonNullResult)
        selectorConfig?.dataSource?.clear()
        selectorConfig?.albumDataSource?.clear()
        recoveryRecyclerPosition()
        if (mAdapter!!.isDataEmpty) {
            showDataNull()
        } else {
            hideDataNull()
        }
    }

    override fun onRecyclerViewPreloadMore() {
        if (isMemoryRecycling) {
            // 这里延迟是拍照导致的页面被回收，Fragment的重创会快于相机的onActivityResult的
            requireView().postDelayed(object : Runnable {
                override fun run() {
                    loadMoreMediaData()
                }
            }, 350)
        } else {
            loadMoreMediaData()
        }
    }

    /**
     * 加载更多
     */
    override fun loadMoreMediaData() {
        if (mRecycler!!.isEnabledLoadMore) {
            mPage++
            val localMediaFolder = selectorConfig?.currentLocalMediaFolder
            val bucketId = localMediaFolder?.bucketId ?: 0
            val engine = selectorConfig?.loaderDataEngine
            if (engine != null) {
                engine.loadMoreMediaData(
                    context!!,
                    bucketId,
                    mPage,
                    selectorConfig?.pageSize ?: 0,
                    selectorConfig?.pageSize ?: 0,
                    object : OnQueryDataResultListener<LocalMedia?>() {
                        override fun onComplete(
                            result: ArrayList<LocalMedia?>?,
                            isHasMore: Boolean
                        ) {
                            handleMoreMediaData(result ?: arrayListOf(), isHasMore)
                        }
                    })
            } else {
                mLoader?.loadPageMediaData(
                    bucketId, mPage, selectorConfig?.pageSize ?: 0,
                    object : OnQueryDataResultListener<LocalMedia?>() {
                        override fun onComplete(
                            result: ArrayList<LocalMedia?>?,
                            isHasMore: Boolean
                        ) {
                            handleMoreMediaData(result ?: arrayListOf(), isHasMore)
                        }
                    })
            }
        }
    }

    private fun handleMoreMediaData(result: MutableList<LocalMedia?>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        mRecycler!!.isEnabledLoadMore = isHasMore
        if (mRecycler!!.isEnabledLoadMore) {
            removePageCameraRepeatData(result)
            if (result.size > 0) {
                val positionStart = mAdapter!!.data.size
                mAdapter!!.data.addAll(result.filterNotNull())
                mAdapter!!.notifyItemRangeChanged(positionStart, mAdapter!!.getItemCount())
                hideDataNull()
            } else {
                // 如果没数据这里在强制调用一下上拉加载更多，防止是因为某些条件过滤导致的假为0的情况
                onRecyclerViewPreloadMore()
            }
            if (result.size < PictureConfig.MIN_PAGE_SIZE) {
                // 当数据量过少时强制触发一下上拉加载更多，防止没有自动触发加载更多
                mRecycler!!.onScrolled(mRecycler!!.getScrollX(), mRecycler!!.getScrollY())
            }
        }
    }

    private fun removePageCameraRepeatData(result: MutableList<LocalMedia?>) {
        try {
            if (selectorConfig?.isPageStrategy == true && isCameraCallback) {
                synchronized(PictureSelectorFragment.Companion.LOCK) {
                    val iterator = result.iterator()
                    while (iterator.hasNext()) {
                        if (mAdapter!!.data.contains(iterator.next())) {
                            iterator.remove()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isCameraCallback = false
        }
    }


    override fun dispatchCameraMediaResult(media: LocalMedia?) {
        if (media == null) return
        val exitsTotalNum = albumListPopWindow!!.firstAlbumImageCount
        if (!isAddSameImp(exitsTotalNum)) {
            mAdapter!!.data.add(0, media)
            isCameraCallback = true
        }
        if (selectorConfig?.selectionMode == SelectModeConfig.SINGLE && selectorConfig?.isDirectReturnSingle == true) {
            selectorConfig?.selectedResult?.clear()
            val selectResultCode = confirmSelect(media, false)
            if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                this@PictureSelectorFragment.dispatchTransformResult()
            }
        } else {
            confirmSelect(media, false)
        }
        mAdapter!!.notifyItemInserted(if (selectorConfig?.isDisplayCamera == true) 1 else 0)
        mAdapter!!.notifyItemRangeChanged(
            if (selectorConfig?.isDisplayCamera == true) 1 else 0,
            mAdapter!!.data.size
        )
        if (selectorConfig?.isOnlySandboxDir == true) {
            var currentLocalMediaFolder = selectorConfig?.currentLocalMediaFolder
            if (currentLocalMediaFolder == null) {
                currentLocalMediaFolder = LocalMediaFolder()
            }
            currentLocalMediaFolder.bucketId = media.parentFolderName.hashCode().toLong()
            currentLocalMediaFolder.setFolderName(media.parentFolderName)
            currentLocalMediaFolder.firstMimeType = media.mimeType
            currentLocalMediaFolder.firstImagePath = media.path
            currentLocalMediaFolder.folderTotalNum = mAdapter!!.data.size
            currentLocalMediaFolder.currentDataPage = mPage
            currentLocalMediaFolder.isHasMore = false
            currentLocalMediaFolder.setData(mAdapter!!.data.filterNotNull().toMutableList() as ArrayList<LocalMedia?>)
            mRecycler!!.isEnabledLoadMore = false
            selectorConfig?.currentLocalMediaFolder = currentLocalMediaFolder
        } else {
            mergeFolder(media)
        }
        allFolderSize = 0
        if (mAdapter!!.data.size > 0 || selectorConfig?.isDirectReturnSingle == true) {
            hideDataNull()
        } else {
            showDataNull()
        }
    }

    /**
     * 拍照出来的合并到相应的专辑目录中去
     *
     * @param media
     */
    private fun mergeFolder(media: LocalMedia) {
        val allFolder: LocalMediaFolder
        val albumList = albumListPopWindow!!.albumList
        if (albumListPopWindow!!.folderCount == 0) {
            // 1、没有相册时需要手动创建相机胶卷
            allFolder = LocalMediaFolder()
            val folderName: String?
            if (TextUtils.isEmpty(selectorConfig?.defaultAlbumName)) {
                folderName =
                    if (selectorConfig?.chooseMode == SelectMimeType.ofAudio()) getString(R.string.ps_all_audio) else getString(
                        R.string.ps_camera_roll
                    )
            } else {
                folderName = selectorConfig?.defaultAlbumName
            }
            allFolder.setFolderName(folderName)
            allFolder.firstImagePath = ""
            allFolder.bucketId = PictureConfig.ALL.toLong()
            albumList.add(0, allFolder)
        } else {
            // 2、有相册就找到对应的相册把数据加进去
            allFolder = albumListPopWindow!!.getFolder(0) ?: return
        }
        if (allFolder != null) {
                allFolder.firstImagePath = media.path
                allFolder.firstMimeType = media.mimeType
                allFolder.setData(mAdapter!!.data.filterNotNull().map { it as LocalMedia? }.toMutableList() as ArrayList<LocalMedia?>)
                allFolder.bucketId = PictureConfig.ALL.toLong()
                allFolder.folderTotalNum = if (isAddSameImp(allFolder.folderTotalNum)) allFolder.folderTotalNum else allFolder.folderTotalNum + 1
                val currentLocalMediaFolder = selectorConfig?.currentLocalMediaFolder
                if (currentLocalMediaFolder == null || currentLocalMediaFolder.folderTotalNum == 0) {
                    selectorConfig?.currentLocalMediaFolder = allFolder
                }
            }
        // 先查找Camera目录，没有找到则创建一个Camera目录
        var cameraFolder: LocalMediaFolder? = null
        for (i in albumList.indices) {
            val exitsFolder = albumList[i]
            if (TextUtils.equals(exitsFolder?.getFolderName(), media.parentFolderName)) {
                cameraFolder = exitsFolder
                break
            }
        }
        if (cameraFolder == null) {
            // 还没有这个目录，创建一个
            cameraFolder = LocalMediaFolder()
            albumList.add(cameraFolder)
        }
        cameraFolder.setFolderName(media.parentFolderName)
        if (cameraFolder.bucketId == -1L || cameraFolder.bucketId == 0L) {
            cameraFolder.bucketId = media.bucketId
        }
        // 分页模式下，切换到Camera目录下时，会直接从MediaStore拉取
        if (selectorConfig?.isPageStrategy == true) {
            cameraFolder.isHasMore = true
        } else {
            // 非分页模式数据都是存在目录的data下，所以直接添加进去就行
            if (!isAddSameImp(allFolder.folderTotalNum) || !TextUtils.isEmpty(selectorConfig?.outPutCameraDir) || !TextUtils.isEmpty(
                    selectorConfig?.outPutAudioDir
                )
            ) {
                val folderData = cameraFolder.getData()
                folderData.add(0, media)
                cameraFolder.setData(folderData)
            }
        }
        if (cameraFolder != null) {
            cameraFolder.folderTotalNum = 
                if (isAddSameImp(allFolder?.folderTotalNum ?: 0))
                    cameraFolder.folderTotalNum
                else
                    cameraFolder.folderTotalNum + 1
            cameraFolder.firstImagePath = selectorConfig?.cameraPath
            cameraFolder.firstMimeType = media.mimeType
        }
        albumListPopWindow!!.bindAlbumData(albumList)
    }

    /**
     * 数量是否一致
     */
    private fun isAddSameImp(totalNum: Int): Boolean {
        if (totalNum == 0) {
            return false
        }
        return allFolderSize > 0 && allFolderSize < totalNum
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (mDragSelectTouchListener != null) {
            mDragSelectTouchListener!!.stopAutoScroll()
        }
    }

    /**
     * 显示数据为空提示
     */
    private fun showDataNull() {
        if (selectorConfig?.currentLocalMediaFolder == null
            || selectorConfig?.currentLocalMediaFolder?.bucketId == PictureConfig.ALL.toLong()
        ) {
            if (tvDataEmpty!!.getVisibility() == View.GONE) {
                tvDataEmpty!!.setVisibility(View.VISIBLE)
            }
            tvDataEmpty!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                R.drawable.ps_ic_no_data,
                0,
                0
            )
            val tips =
                if (selectorConfig?.chooseMode == SelectMimeType.ofAudio()) getString(R.string.ps_audio_empty) else getString(
                    R.string.ps_empty
                )
            tvDataEmpty!!.setText(tips)
        }
    }

    /**
     * 隐藏数据为空提示
     */
    private fun hideDataNull() {
        if (tvDataEmpty!!.getVisibility() == View.VISIBLE) {
            tvDataEmpty!!.setVisibility(View.GONE)
        }
    }

    companion object {
        val TAG: String = PictureSelectorFragment::class.java.getSimpleName()
        private val LOCK = Any()

        /**
         * 这个时间对应的是R.anim.ps_anim_modal_in里面的
         */
        private var SELECT_ANIM_DURATION = 135
        fun newInstance(): PictureSelectorFragment {
            val fragment = PictureSelectorFragment()
            fragment.setArguments(Bundle())
            return fragment
        }
    }
}
