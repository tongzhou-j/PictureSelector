package com.luck.picture.lib

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.os.Bundle
import android.os.Vibrator
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.luck.picture.lib.adapter.PicturePreviewAdapter
import com.luck.picture.lib.adapter.holder.BasePreviewHolder.OnPreviewEventListener
import com.luck.picture.lib.adapter.holder.PreviewGalleryAdapter
import com.luck.picture.lib.adapter.holder.PreviewVideoHolder
import com.luck.picture.lib.basic.PictureCommonFragment
import com.luck.picture.lib.basic.PictureMediaScannerConnection
import com.luck.picture.lib.config.Crop
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.decoration.HorizontalItemDecoration
import com.luck.picture.lib.decoration.WrapContentLinearLayoutManager
import com.luck.picture.lib.dialog.PictureCommonDialog
import com.luck.picture.lib.dialog.PictureCommonDialog.OnDialogEventListener
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.MediaExtraInfo
import com.luck.picture.lib.interfaces.OnCallbackListener
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.loader.IBridgeMediaLoader
import com.luck.picture.lib.loader.LocalMediaLoader
import com.luck.picture.lib.loader.LocalMediaPageLoader
import com.luck.picture.lib.magical.BuildRecycleItemViewParams
import com.luck.picture.lib.magical.MagicalView
import com.luck.picture.lib.magical.OnMagicalViewCallback
import com.luck.picture.lib.manager.SelectedManager
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.DownloadFileUtils
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.StyleUtils
import com.luck.picture.lib.utils.ToastUtils
import com.luck.picture.lib.utils.ValueOf.toString
import com.luck.picture.lib.widget.BottomNavBar.OnBottomNavBarListener
import com.luck.picture.lib.widget.CompleteSelectView
import com.luck.picture.lib.widget.PreviewBottomNavBar
import com.luck.picture.lib.widget.PreviewTitleBar
import com.luck.picture.lib.widget.TitleBar
import com.luck.picture.lib.widget.TitleBar.OnTitleBarListener
import java.util.Collections

/**
 * @author：luck
 * @date：2021/11/18 10:13 下午
 * @describe：PictureSelectorPreviewFragment
 */
open class PictureSelectorPreviewFragment : PictureCommonFragment() {
    override fun onPermissionExplainEvent(
        isDisplayExplain: Boolean,
        permissionArray: Array<String?>?
    ) {
        super.onPermissionExplainEvent(isDisplayExplain, permissionArray)
    }

    protected var mData: ArrayList<LocalMedia>? = ArrayList<LocalMedia>()

    protected var magicalView: MagicalView? = null

    var viewPager2: ViewPager2? = null
        protected set

    var adapter: PicturePreviewAdapter? = null
        protected set

    protected var bottomNarBar: PreviewBottomNavBar? = null

    protected var titleBar: PreviewTitleBar? = null

    /**
     * if there more
     */
    protected var isHasMore: Boolean = true

    protected var curPosition: Int = 0

    protected var isInternalBottomPreview: Boolean = false

    protected var isSaveInstanceState: Boolean = false

    /**
     * 当前相册
     */
    protected var currentAlbum: String? = null

    /**
     * 是否显示了拍照入口
     */
    protected var isShowCamera: Boolean = false

    /**
     * 是否外部预览进来
     */
    protected var isExternalPreview: Boolean = false

    /**
     * 外部预览是否支持删除
     */
    protected var isDisplayDelete: Boolean = false

    protected var isAnimationStart: Boolean = false

    protected var totalNum: Int = 0

    protected var screenWidth: Int = 0
    protected var screenHeight: Int = 0

    protected var mBucketId: Long = -1

    protected var tvSelected: TextView? = null

    protected var tvSelectedWord: TextView? = null

    protected var selectClickArea: View? = null

    protected var completeSelectView: CompleteSelectView? = null

    protected var needScaleBig: Boolean = true

    protected var needScaleSmall: Boolean = false

    protected var mGalleryRecycle: RecyclerView? = null

    protected var mGalleryAdapter: PreviewGalleryAdapter? = null

    protected var mAnimViews: MutableList<View?> = ArrayList<View?>()

    private var isPause = false



    /**
     * 内部预览
     *
     * @param isBottomPreview 是否顶部预览进来的
     * @param currentAlbum    当前预览的目录
     * @param isShowCamera    是否有显示拍照图标
     * @param position        预览下标
     * @param totalNum        当前预览总数
     * @param page            当前页码
     * @param currentBucketId 当前相册目录id
     * @param data            预览数据源
     */
    fun setInternalPreviewData(
        isBottomPreview: Boolean, currentAlbumName: String?, isShowCamera: Boolean,
        position: Int, totalNum: Int, page: Int, currentBucketId: Long,
        data: ArrayList<LocalMedia>?
    ) {
        this.mPage = page
        this.mBucketId = currentBucketId
        this.mData = data
        this.totalNum = totalNum
        this.curPosition = position
        this.currentAlbum = currentAlbumName
        this.isShowCamera = isShowCamera
        this.isInternalBottomPreview = isBottomPreview
    }

    /**
     * 外部预览
     *
     * @param position        预览下标
     * @param totalNum        当前预览总数
     * @param data            预览数据源
     * @param isDisplayDelete 是否显示删除按钮
     */
    fun setExternalPreviewData(
        position: Int,
        totalNum: Int,
        data: ArrayList<LocalMedia>?,
        isDisplayDelete: Boolean
    ) {
        this.mData = data
        this.totalNum = totalNum
        this.curPosition = position
        this.isDisplayDelete = isDisplayDelete
        this.isExternalPreview = true
    }

    override val resourceId: Int
        get() {
            val layoutResourceId = InjectResourceSource.getLayoutResource(
                requireContext(),
                InjectResourceSource.PREVIEW_LAYOUT_RESOURCE,
                selectorConfig
            )
            return if (layoutResourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) {
                layoutResourceId
            } else {
                R.layout.ps_fragment_preview
            }
        }

    override fun onSelectedChange(isAddRemove: Boolean, currentMedia: LocalMedia?) {
        // 更新TitleBar和BottomNarBar选择态
        tvSelected!!.setSelected(selectorConfig?.selectedResult?.contains(currentMedia) ?: false)
        bottomNarBar!!.setSelectedChange()
        completeSelectView!!.setSelectedChange(true)
        notifySelectNumberStyle(currentMedia)
        notifyPreviewGalleryData(isAddRemove, currentMedia)
    }

    override fun onCheckOriginalChange() {
        bottomNarBar!!.setOriginalCheck()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reStartSavedInstance(savedInstanceState)
        isSaveInstanceState = savedInstanceState != null
        screenWidth = DensityUtil.getRealScreenWidth(requireContext())
        screenHeight = DensityUtil.getScreenHeight(requireContext())
        titleBar = view.findViewById<PreviewTitleBar>(R.id.title_bar)
        tvSelected = view.findViewById<TextView>(R.id.ps_tv_selected)
        tvSelectedWord = view.findViewById<TextView>(R.id.ps_tv_selected_word)
        selectClickArea = view.findViewById<View>(R.id.select_click_area)
        completeSelectView = view.findViewById<CompleteSelectView>(R.id.ps_complete_select)
        magicalView = view.findViewById<MagicalView>(R.id.magical)
        this.viewPager2 = ViewPager2(requireContext())
        bottomNarBar = view.findViewById<PreviewBottomNavBar>(R.id.bottom_nar_bar)
        magicalView!!.setMagicalContent(this.viewPager2)
        setMagicalViewBackgroundColor()
        setMagicalViewAction()
        addAminViews(
            titleBar,
            tvSelected,
            tvSelectedWord,
            selectClickArea,
            completeSelectView,
            bottomNarBar
        )
        onCreateLoader()
        initTitleBar()
        initViewPagerData(mData!!)
        if (isExternalPreview) {
            externalPreviewStyle()
        } else {
            initBottomNavBar()
            initPreviewSelectGallery(view as ViewGroup)
            initComplete()
        }
        iniMagicalView()
    }

    /**
     * addAminViews
     *
     * @param views
     */
    fun addAminViews(vararg views: View?) {
        Collections.addAll<View?>(mAnimViews, *views)
    }

    private fun setMagicalViewBackgroundColor() {
        val mainStyle = selectorConfig?.selectorStyle?.selectMainStyle
        val previewBgColor = mainStyle?.previewBackgroundColor ?: 0
        if (StyleUtils.checkStyleValidity(previewBgColor)) {
            magicalView!!.setBackgroundColor(previewBgColor)
        } else {
            if (selectorConfig?.chooseMode == SelectMimeType.ofAudio()
                || (mData != null && mData!!.size > 0 && PictureMimeType.isHasAudio(
                    mData!!.get(0).mimeType
                ))
            ) {
                magicalView!!.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.ps_color_white
                    )
                )
            } else {
                magicalView!!.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.ps_color_black
                    )
                )
            }
        }
    }

    override fun reStartSavedInstance(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mPage = savedInstanceState.getInt(PictureConfig.EXTRA_CURRENT_PAGE, 1)
            mBucketId = savedInstanceState.getLong(PictureConfig.EXTRA_CURRENT_BUCKET_ID, -1)
            curPosition =
                savedInstanceState.getInt(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION, curPosition)
            isShowCamera =
                savedInstanceState.getBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA, isShowCamera)
            totalNum =
                savedInstanceState.getInt(PictureConfig.EXTRA_PREVIEW_CURRENT_ALBUM_TOTAL, totalNum)
            isExternalPreview = savedInstanceState.getBoolean(
                PictureConfig.EXTRA_EXTERNAL_PREVIEW,
                isExternalPreview
            )
            isDisplayDelete = savedInstanceState.getBoolean(
                PictureConfig.EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE,
                isDisplayDelete
            )
            isInternalBottomPreview = savedInstanceState.getBoolean(
                PictureConfig.EXTRA_BOTTOM_PREVIEW,
                isInternalBottomPreview
            )
            currentAlbum = savedInstanceState.getString(PictureConfig.EXTRA_CURRENT_ALBUM_NAME, "")
            if (mData!!.size == 0) {
                mData!!.addAll(selectorConfig?.selectedPreviewResult?.filterNotNull() ?: arrayListOf())
            }
        }
    }

    override fun onKeyBackFragmentFinish() {
        onKeyDownBackToMin()
    }

    /**
     * 设置MagicalView
     */
    private fun iniMagicalView() {
        if (this.isHasMagicalEffect) {
            val alpha = if (isSaveInstanceState) 1.0f else 0.0f
            magicalView!!.setBackgroundAlpha(alpha)
            for (i in mAnimViews.indices) {
                if (mAnimViews.get(i) is TitleBar) {
                    continue
                }
                mAnimViews.get(i)!!.setAlpha(alpha)
            }
        } else {
            magicalView!!.setBackgroundAlpha(1.0f)
        }
    }

    private val isHasMagicalEffect: Boolean
        get() = !isInternalBottomPreview && (selectorConfig?.isPreviewZoomEffect ?: false)

    /**
     * 设置MagicalView监听器
     */
    protected fun setMagicalViewAction() {
        if (this.isHasMagicalEffect) {
            magicalView!!.setOnMojitoViewCallback(object : OnMagicalViewCallback {
                override fun onBeginBackMinAnim() {
                    onMojitoBeginBackMinAnim()
                }

                override fun onBeginMagicalAnimComplete(
                    mojitoView: MagicalView?,
                    showImmediately: Boolean
                ) {
                    onMojitoBeginAnimComplete(mojitoView, showImmediately)
                }

                override fun onBackgroundAlpha(alpha: Float) {
                    onMojitoBackgroundAlpha(alpha)
                }

                override fun onMagicalViewFinish() {
                    onMojitoMagicalViewFinish()
                }

                override fun onBeginBackMinMagicalFinish(isResetSize: Boolean) {
                    onMojitoBeginBackMinFinish(isResetSize)
                }
            })
        }
    }

    /**
     * 开始准备执行缩放动画
     */
    protected fun onMojitoBeginBackMinAnim() {
        val currentHolder = adapter!!.getCurrentHolder(viewPager2!!.currentItem)
        if (currentHolder == null) {
            return
        }
        if (currentHolder.coverImageView.getVisibility() == View.GONE) {
            currentHolder.coverImageView.setVisibility(View.VISIBLE)
        }
        if (currentHolder is PreviewVideoHolder) {
            val videoHolder = currentHolder
            if (videoHolder.ivPlayButton.getVisibility() == View.VISIBLE) {
                videoHolder.ivPlayButton.setVisibility(View.GONE)
            }
        }
    }

    /**
     * 关闭缩放动画执行完成后关闭页面
     */
    protected fun onMojitoMagicalViewFinish() {
        if (isExternalPreview && isNormalDefaultEnter && this.isHasMagicalEffect) {
            onExitPictureSelector()
        } else {
            this@PictureSelectorPreviewFragment.onBackCurrentFragment()
        }
    }

    /**
     * 缩放动画执行时透明度跟随变化
     *
     * @param alpha
     */
    protected fun onMojitoBackgroundAlpha(alpha: Float) {
        for (i in mAnimViews.indices) {
            if (mAnimViews.get(i) is TitleBar) {
                continue
            }
            mAnimViews.get(i)!!.setAlpha(alpha)
        }
    }

    /**
     * 关闭缩放动画执行完成
     *
     * @param isResetSize
     */
    protected fun onMojitoBeginBackMinFinish(isResetSize: Boolean) {
        val itemViewParams =
            BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) curPosition + 1 else curPosition)
        if (itemViewParams == null) {
            return
        }
        val currentHolder = adapter!!.getCurrentHolder(viewPager2!!.currentItem)
        if (currentHolder == null) {
            return
        }
        currentHolder.coverImageView.layoutParams.width = itemViewParams.width
        currentHolder.coverImageView.layoutParams.height = itemViewParams.height
        currentHolder.coverImageView.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    /**
     * 缩放动画执行完成
     *
     * @param mojitoView
     * @param showImmediately
     */
    protected fun onMojitoBeginAnimComplete(mojitoView: MagicalView?, showImmediately: Boolean) {
        val currentHolder = adapter!!.getCurrentHolder(viewPager2!!.currentItem)
        if (currentHolder == null) {
            return
        }
        val media = mData!!.get(viewPager2!!.currentItem)
        val realWidth: Int
        val realHeight: Int
        if (media.isCut() && media.cropImageWidth > 0 && media.cropImageHeight > 0) {
            realWidth = media.cropImageWidth
            realHeight = media.cropImageHeight
        } else {
            realWidth = media.width
            realHeight = media.height
        }
        if (MediaUtils.isLongImage(realWidth, realHeight)) {
            currentHolder.coverImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        } else {
            currentHolder.coverImageView.scaleType = ImageView.ScaleType.FIT_CENTER
        }
        if (currentHolder is PreviewVideoHolder) {
            val videoHolder = currentHolder
            if (selectorConfig?.isAutoVideoPlay == true) {
                startAutoVideoPlay(viewPager2!!.currentItem)
            } else {
                if (videoHolder.ivPlayButton.getVisibility() == View.GONE) {
                    if (!this.isPlaying) {
                        videoHolder.ivPlayButton.setVisibility(View.VISIBLE)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(PictureConfig.EXTRA_CURRENT_PAGE, mPage)
        outState.putLong(PictureConfig.EXTRA_CURRENT_BUCKET_ID, mBucketId)
        outState.putInt(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION, curPosition)
        outState.putInt(PictureConfig.EXTRA_PREVIEW_CURRENT_ALBUM_TOTAL, totalNum)
        outState.putBoolean(PictureConfig.EXTRA_EXTERNAL_PREVIEW, isExternalPreview)
        outState.putBoolean(PictureConfig.EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE, isDisplayDelete)
        outState.putBoolean(PictureConfig.EXTRA_DISPLAY_CAMERA, isShowCamera)
        outState.putBoolean(PictureConfig.EXTRA_BOTTOM_PREVIEW, isInternalBottomPreview)
        outState.putString(PictureConfig.EXTRA_CURRENT_ALBUM_NAME, currentAlbum)
        val previewData = mData?.map { it as LocalMedia? }?.toMutableList() as? ArrayList<LocalMedia?>
        selectorConfig?.addSelectedPreviewResult(previewData)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (this.isHasMagicalEffect) {
            // config.isPreviewZoomEffect模式下使用缩放动画
            return null
        }
        val windowAnimationStyle = selectorConfig?.selectorStyle?.windowAnimationStyle
        if (windowAnimationStyle?.activityPreviewEnterAnimation != 0 && windowAnimationStyle?.activityPreviewExitAnimation != 0) {
            val loadAnimation = AnimationUtils.loadAnimation(
                activity,
                if (enter) windowAnimationStyle?.activityPreviewEnterAnimation ?: 0 else windowAnimationStyle?.activityPreviewExitAnimation ?: 0
            )
            if (enter) {
                onEnterFragment()
            } else {
                onExitFragment()
            }
            return loadAnimation
        } else {
            return super.onCreateAnimation(transit, enter, nextAnim)
        }
    }

    override fun sendChangeSubSelectPositionEvent(adapterChange: Boolean) {
        val style = selectorConfig?.selectorStyle?.selectMainStyle
        val config = selectorConfig
        if (style?.isPreviewSelectNumberStyle == true) {
            if (style.isSelectNumberStyle == true) {
                val selectCount = config?.selectCount ?: 0
                for (index in 0 until selectCount) {
                    val media = config?.selectedResult?.get(index)
                    media?.num = index + 1
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (this.isHasMagicalEffect && mData!!.size > curPosition) {
            val media = mData!!.get(curPosition)
            if (PictureMimeType.isHasVideo(media.mimeType)) {
                getVideoRealSizeFromMedia(media, false, object : OnCallbackListener<IntArray?> {
                    override fun onCall(size: IntArray?) {
                        if (size != null) {
                            changeViewParams(size)
                        }
                    }
                })
            } else {
                getImageRealSizeFromMedia(media, false, object : OnCallbackListener<IntArray?> {
                    override fun onCall(size: IntArray?) {
                        if (size != null) {
                            changeViewParams(size)
                        }
                    }
                })
            }
        }
    }

    private fun changeViewParams(size: IntArray) {
        val viewParams =
            BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) curPosition + 1 else curPosition)
        if (viewParams == null || size[0] == 0 || size[1] == 0) {
            magicalView!!.setViewParams(0, 0, 0, 0, size[0], size[1])
            magicalView!!.resetStartNormal(size[0], size[1], false)
        } else {
            magicalView!!.setViewParams(
                viewParams.left,
                viewParams.top,
                viewParams.width,
                viewParams.height,
                size[0],
                size[1]
            )
            magicalView!!.resetStart()
        }
    }

    override fun onCreateLoader() {
        if (isExternalPreview) {
            return
        }
        val loaderFactory = selectorConfig?.loaderFactory
        if (loaderFactory != null) {
            mLoader = loaderFactory.onCreateLoader()
            if (mLoader == null) {
                throw NullPointerException("No available " + IBridgeMediaLoader::class.java + " loader found")
            }
        } else {
            val config = selectorConfig
            val ctx = requireContext()
            if (config != null) {
                mLoader = if (config.isPageStrategy)
                    LocalMediaPageLoader(ctx, config)
                else
                    LocalMediaLoader(ctx, config)
            }
        }
    }

    /**
     * 加载更多
     */
    private fun loadMoreData() {
        mPage++
        val loaderEngine = selectorConfig?.loaderDataEngine
        val config = selectorConfig
        val ctx = requireContext()
        if (loaderEngine != null && config != null) {
            loaderEngine.loadMoreMediaData(
                ctx,
                mBucketId,
                mPage,
                config.pageSize,
                config.pageSize,
                object : OnQueryDataResultListener<LocalMedia?>() {
                    override fun onComplete(result: ArrayList<LocalMedia?>?, isHasMore: Boolean) {
                        handleMoreData(result ?: arrayListOf(), isHasMore)
                    }
                })
        } else {
            val config = selectorConfig
            mLoader?.loadPageMediaData(
                mBucketId,
                mPage,
                config?.pageSize ?: 0,
                object : OnQueryDataResultListener<LocalMedia?>() {
                    override fun onComplete(result: ArrayList<LocalMedia?>?, isHasMore: Boolean) {
                        handleMoreData(result ?: arrayListOf(), isHasMore)
                    }
                })
        }
    }

    private fun handleMoreData(result: MutableList<LocalMedia?>, isHasMore: Boolean) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        this@PictureSelectorPreviewFragment.isHasMore = isHasMore
        if (isHasMore) {
            if (result.size > 0) {
                val oldStartPosition = mData!!.size
                mData!!.addAll(result.filterNotNull())
                val itemCount = mData!!.size
                adapter!!.notifyItemRangeChanged(oldStartPosition, itemCount)
            } else {
                loadMoreData()
            }
        }
    }


    private fun initComplete() {
        val selectMainStyle = selectorConfig?.selectorStyle?.selectMainStyle

        val previewSelectBg = selectMainStyle?.previewSelectBackground ?: 0
        if (StyleUtils.checkStyleValidity(previewSelectBg)) {
            tvSelected!!.setBackgroundResource(previewSelectBg)
        } else {
            val selectBg = selectMainStyle?.selectBackground ?: 0
            if (StyleUtils.checkStyleValidity(selectBg)) {
                tvSelected!!.setBackgroundResource(selectBg)
            }
        }
        val previewSelectTextResId = selectMainStyle?.previewSelectTextResId ?: 0
        if (StyleUtils.checkStyleValidity(previewSelectTextResId)) {
            tvSelectedWord!!.setText(getString(previewSelectTextResId))
        } else {
            val previewSelectText = selectMainStyle?.previewSelectText
            if (StyleUtils.checkTextValidity(previewSelectText)) {
                tvSelectedWord!!.setText(previewSelectText)
            } else {
                tvSelectedWord!!.setText("")
            }
        }
        val previewSelectTextSize = selectMainStyle?.previewSelectTextSize ?: 0
        if (StyleUtils.checkSizeValidity(previewSelectTextSize)) {
            tvSelectedWord!!.setTextSize(previewSelectTextSize.toFloat())
        }

        val previewSelectTextColor = selectMainStyle?.previewSelectTextColor ?: 0
        if (StyleUtils.checkStyleValidity(previewSelectTextColor)) {
            tvSelectedWord!!.setTextColor(previewSelectTextColor)
        }

        val previewSelectMarginRight = selectMainStyle?.previewSelectMarginRight ?: 0
        if (StyleUtils.checkSizeValidity(previewSelectMarginRight)) {
            if (tvSelected!!.layoutParams is ConstraintLayout.LayoutParams) {
                if (tvSelected!!.layoutParams is ConstraintLayout.LayoutParams) {
                    val layoutParams =
                        tvSelected!!.layoutParams as ConstraintLayout.LayoutParams
                    layoutParams.rightMargin = selectMainStyle?.previewSelectMarginRight ?: 0
                }
            } else if (tvSelected!!.layoutParams is RelativeLayout.LayoutParams) {
                val layoutParams = tvSelected!!.layoutParams as RelativeLayout.LayoutParams
                layoutParams.rightMargin = selectMainStyle?.previewSelectMarginRight ?: 0
            }
        }
        completeSelectView!!.setCompleteSelectViewStyle()
        completeSelectView!!.setSelectedChange(true)
        if (selectMainStyle?.isCompleteSelectRelativeTop == true) {
            if (completeSelectView!!.layoutParams is ConstraintLayout.LayoutParams) {
                (completeSelectView!!
                    .layoutParams as ConstraintLayout.LayoutParams).topToTop = R.id.title_bar
                (completeSelectView!!
                    .layoutParams as ConstraintLayout.LayoutParams).bottomToBottom =
                    R.id.title_bar
                if (selectorConfig?.isPreviewFullScreenMode == true) {
                    (completeSelectView!!
                        .getLayoutParams() as ConstraintLayout.LayoutParams).topMargin =
                        DensityUtil.getStatusBarHeight(requireContext())
                }
            } else if (completeSelectView!!.getLayoutParams() is RelativeLayout.LayoutParams) {
                if (selectorConfig?.isPreviewFullScreenMode == true) {
                    (completeSelectView!!
                        .getLayoutParams() as RelativeLayout.LayoutParams).topMargin =
                        DensityUtil.getStatusBarHeight(requireContext())
                }
            }
        }

        if (selectMainStyle?.isPreviewSelectRelativeBottom == true) {
            if (tvSelected!!.getLayoutParams() is ConstraintLayout.LayoutParams) {
                (tvSelected!!
                    .getLayoutParams() as ConstraintLayout.LayoutParams).topToTop =
                    R.id.bottom_nar_bar
                (tvSelected!!
                    .getLayoutParams() as ConstraintLayout.LayoutParams).bottomToBottom =
                    R.id.bottom_nar_bar

                (tvSelectedWord!!
                    .getLayoutParams() as ConstraintLayout.LayoutParams).topToTop =
                    R.id.bottom_nar_bar
                (tvSelectedWord!!
                    .getLayoutParams() as ConstraintLayout.LayoutParams).bottomToBottom =
                    R.id.bottom_nar_bar

                (selectClickArea!!
                    .getLayoutParams() as ConstraintLayout.LayoutParams).topToTop =
                    R.id.bottom_nar_bar
                (selectClickArea!!
                    .getLayoutParams() as ConstraintLayout.LayoutParams).bottomToBottom =
                    R.id.bottom_nar_bar
            }
        } else {
            if (selectorConfig?.isPreviewFullScreenMode == true) {
                if (tvSelectedWord!!.layoutParams is ConstraintLayout.LayoutParams) {
                    (tvSelectedWord!!
                        .layoutParams as ConstraintLayout.LayoutParams).topMargin =
                        DensityUtil.getStatusBarHeight(requireContext())
                } else if (tvSelectedWord!!.layoutParams is RelativeLayout.LayoutParams) {
                    (tvSelectedWord!!
                        .layoutParams as RelativeLayout.LayoutParams).topMargin =
                        DensityUtil.getStatusBarHeight(requireContext())
                }
            }
        }
        completeSelectView!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val isComplete: Boolean
                val config = selectorConfig
                val style = selectMainStyle
                if (style?.isCompleteSelectRelativeTop == true && (config?.selectCount ?: 0) == 0) {
                    isComplete = (confirmSelect(mData!!.get(viewPager2!!.currentItem), false)
                            == SelectedManager.ADD_SUCCESS)
                } else {
                    isComplete = (config?.selectCount ?: 0) > 0
                }
                if (config?.isEmptyResultReturn == true && (config.selectCount ?: 0) == 0) {
                    onExitPictureSelector()
                } else {
                    if (isComplete) {
                        this@PictureSelectorPreviewFragment.dispatchTransformResult()
                    }
                }
            }
        })
    }


    private fun initTitleBar() {
        if (selectorConfig?.selectorStyle?.titleBarStyle?.isHideTitleBar == true) {
            titleBar!!.setVisibility(View.GONE)
        }
        titleBar!!.setTitleBarStyle()
        titleBar!!.setOnTitleBarListener(object : OnTitleBarListener() {
            override fun onBackPressed() {
                if (isExternalPreview) {
                    if (selectorConfig?.isPreviewZoomEffect == true) {
                        magicalView!!.backToMin()
                    } else {
                        handleExternalPreviewBack()
                    }
                } else {
                    if (!isInternalBottomPreview && selectorConfig?.isPreviewZoomEffect == true) {
                        magicalView!!.backToMin()
                    } else {
                        this@PictureSelectorPreviewFragment.onBackCurrentFragment()
                    }
                }
            }
        })
        titleBar!!.setTitle((curPosition + 1).toString() + "/" + totalNum)
        titleBar!!.imageDelete?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                deletePreview()
            }
        })

        selectClickArea!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (isExternalPreview) {
                    deletePreview()
                } else {
                    val currentMedia = mData!!.get(viewPager2!!.getCurrentItem())
                    val selectResultCode = confirmSelect(currentMedia, tvSelected!!.isSelected)
                    if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                        val animListener = selectorConfig?.onSelectAnimListener
                        if (animListener != null) {
                            animListener.onSelectAnim(tvSelected)
                        } else {
                            tvSelected!!.startAnimation(
                                AnimationUtils.loadAnimation(
                                    context,
                                    R.anim.ps_anim_modal_in
                                )
                            )
                        }
                    }
                }
            }
        })
        tvSelected!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                selectClickArea!!.performClick()
            }
        })
    }

    protected fun initPreviewSelectGallery(group: ViewGroup) {
        val selectMainStyle = selectorConfig?.selectorStyle?.selectMainStyle
        if (selectMainStyle?.isPreviewDisplaySelectGallery == true) {
            mGalleryRecycle = RecyclerView(requireContext())
            val galleryBgRes = selectMainStyle?.adapterPreviewGalleryBackgroundResource ?: 0
            if (StyleUtils.checkStyleValidity(galleryBgRes)) {
                mGalleryRecycle!!.setBackgroundResource(galleryBgRes)
            } else {
                mGalleryRecycle!!.setBackgroundResource(R.drawable.ps_preview_gallery_bg)
            }
            group.addView(mGalleryRecycle)

            val layoutParams = mGalleryRecycle!!.layoutParams
            if (layoutParams is ConstraintLayout.LayoutParams) {
                val params = layoutParams
                params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                params.bottomToTop = R.id.bottom_nar_bar
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            }
            val layoutManager: WrapContentLinearLayoutManager =
                object : WrapContentLinearLayoutManager(requireContext()) {
                    override fun smoothScrollToPosition(
                        recyclerView: RecyclerView,
                        state: RecyclerView.State?,
                        position: Int
                    ) {
                        super.smoothScrollToPosition(recyclerView, state, position)
                        val smoothScroller: LinearSmoothScroller =
                            object : LinearSmoothScroller(recyclerView.context) {
                                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                                    return 300f / displayMetrics.densityDpi
                                }
                            }
                        smoothScroller.setTargetPosition(position)
                        startSmoothScroll(smoothScroller)
                    }
                }
            val itemAnimator = mGalleryRecycle!!.itemAnimator
            if (itemAnimator != null) {
                (itemAnimator as SimpleItemAnimator).setSupportsChangeAnimations(false)
            }
            if (mGalleryRecycle!!.itemDecorationCount == 0) {
                mGalleryRecycle!!.addItemDecoration(
                    HorizontalItemDecoration(
                        Int.MAX_VALUE,
                        DensityUtil.dip2px(requireContext(), 6f)
                    )
                )
            }
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL
            mGalleryRecycle!!.layoutManager = layoutManager
            if (selectorConfig?.selectCount ?: 0 > 0) {
                mGalleryRecycle!!.layoutAnimation = AnimationUtils
                    .loadLayoutAnimation(requireContext(), R.anim.ps_anim_layout_fall_enter)
            }
            val config = selectorConfig ?: return
            mGalleryAdapter = PreviewGalleryAdapter(config, isInternalBottomPreview)
            notifyGallerySelectMedia(mData!![curPosition])
            mGalleryRecycle!!.adapter = mGalleryAdapter
            mGalleryAdapter!!.setItemClickListener(object :
                PreviewGalleryAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, media: LocalMedia?, v: View?) {
                    if (position == RecyclerView.NO_POSITION) {
                        return
                    }
                    val albumName = if (TextUtils.isEmpty(selectorConfig?.defaultAlbumName)) getString(R.string.ps_camera_roll) else selectorConfig?.defaultAlbumName
                    if (isInternalBottomPreview || TextUtils.equals(currentAlbum, albumName)
                        || TextUtils.equals(media?.parentFolderName, currentAlbum)
                    ) {
                        val newPosition =
                            if (isInternalBottomPreview) position else if (isShowCamera) (media?.position ?: 0) - 1 else media?.position ?: 0
                        if (newPosition == viewPager2!!.currentItem && media?.isChecked == true) {
                            return
                        }
                        val item = adapter!!.getItem(newPosition)
                        if (item != null && (!TextUtils.equals(
                                media?.path,
                                item.path
                            ) || media?.id != item.id)) {
                            return
                        }
                        if (viewPager2!!.adapter != null) {
                            // 这里清空一下重新设置，发现频繁调用setCurrentItem会出现页面闪现之前图片
                            viewPager2!!.adapter = null
                            viewPager2!!.adapter = adapter
                        }
                        viewPager2!!.setCurrentItem(newPosition, false)
                        notifyGallerySelectMedia(media)
                        viewPager2!!.post(object : Runnable {
                            override fun run() {
                                if (selectorConfig?.isPreviewZoomEffect == true) {
                                    adapter!!.setVideoPlayButtonUI(newPosition)
                                }
                            }
                        })
                    }
                }
            })
            if ((selectorConfig?.selectCount ?: 0) > 0) {
                mGalleryRecycle!!.setVisibility(View.VISIBLE)
            } else {
                mGalleryRecycle!!.setVisibility(View.INVISIBLE)
            }
            addAminViews(mGalleryRecycle)
            val mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
                override fun isLongPressDragEnabled(): Boolean {
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    viewHolder.itemView.setAlpha(0.7f)
                    return ItemTouchHelper.Callback.makeMovementFlags(
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
                        0
                    )
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    try {
                        //得到item原来的position
                        val fromPosition = viewHolder.absoluteAdapterPosition
                        //得到目标position
                        val toPosition = target.absoluteAdapterPosition
                        if (fromPosition < toPosition) {
                            for (i in fromPosition..<toPosition) {
                                Collections.swap(mGalleryAdapter!!.data, i, i + 1)
                                selectorConfig?.selectedResult?.let { Collections.swap(it, i, i + 1) }
                                if (isInternalBottomPreview) {
                                    Collections.swap(mData, i, i + 1)
                                }
                            }
                        } else {
                            for (i in fromPosition downTo toPosition + 1) {
                                Collections.swap(mGalleryAdapter!!.data, i, i - 1)
                                selectorConfig?.selectedResult?.let { Collections.swap(it, i, i - 1) }
                                if (isInternalBottomPreview) {
                                    Collections.swap(mData, i, i - 1)
                                }
                            }
                        }
                        mGalleryAdapter!!.notifyItemMoved(fromPosition, toPosition)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return true
                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    if (needScaleBig) {
                        needScaleBig = false
                        val animatorSet = AnimatorSet()
                        animatorSet.playTogether(
                            ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.0f, 1.1f),
                            ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.0f, 1.1f)
                        )
                        animatorSet.duration = 50
                        animatorSet.interpolator = LinearInterpolator()
                        animatorSet.start()
                        animatorSet.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                needScaleSmall = true
                            }
                        })
                    }
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }

                override fun onSelectedChanged(
                    viewHolder: RecyclerView.ViewHolder?,
                    actionState: Int
                ) {
                    super.onSelectedChanged(viewHolder, actionState)
                }

                override fun getAnimationDuration(
                    recyclerView: RecyclerView,
                    animationType: Int,
                    animateDx: Float,
                    animateDy: Float
                ): Long {
                    return super.getAnimationDuration(
                        recyclerView,
                        animationType,
                        animateDx,
                        animateDy
                    )
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    viewHolder.itemView.setAlpha(1.0f)
                    if (needScaleSmall) {
                        needScaleSmall = false
                        val animatorSet = AnimatorSet()
                        animatorSet.playTogether(
                            ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.1f, 1.0f),
                            ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.1f, 1.0f)
                        )
                        animatorSet.interpolator = LinearInterpolator()
                        animatorSet.duration = 50
                        animatorSet.start()
                        animatorSet.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                needScaleBig = true
                            }
                        })
                    }
                    super.clearView(recyclerView, viewHolder)
                    mGalleryAdapter!!.notifyItemChanged(viewHolder.getAbsoluteAdapterPosition())
                    if (isInternalBottomPreview) {
                        val position = mGalleryAdapter!!.lastCheckPosition
                        if (viewPager2!!.getCurrentItem() != position && position != RecyclerView.NO_POSITION) {
                            if (viewPager2!!.adapter != null) {
                                viewPager2!!.setAdapter(null)
                                viewPager2!!.setAdapter(adapter)
                            }
                            viewPager2!!.setCurrentItem(position, false)
                        }
                    }
                    if (selectorConfig?.selectorStyle?.selectMainStyle?.isSelectNumberStyle == true) {
                        if (!ActivityCompatHelper.isDestroy(activity)) {
                            val fragments =
                                activity!!.getSupportFragmentManager().getFragments()
                            for (i in fragments.indices) {
                                val fragment = fragments[i]
                                if (fragment is PictureCommonFragment) {
                                    fragment.sendChangeSubSelectPositionEvent(true)
                                }
                            }
                        }
                    }
                }
            })
            mItemTouchHelper.attachToRecyclerView(mGalleryRecycle)
            mGalleryAdapter!!.setItemLongClickListener(object :
                PreviewGalleryAdapter.OnItemLongClickListener {
                override fun onItemLongClick(
                    holder: RecyclerView.ViewHolder?,
                    position: Int,
                    v: View?
                ) {
                    if (holder != null) {
                        val vibrator =
                            activity!!.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(50)
                        if (mGalleryAdapter!!.itemCount != selectorConfig?.maxSelectNum) {
                            mItemTouchHelper.startDrag(holder)
                            return
                        }
                        if (holder.getLayoutPosition() != mGalleryAdapter!!.getItemCount() - 1) {
                            mItemTouchHelper.startDrag(holder)
                        }
                    }
                }
            })
        }
    }

    /**
     * 刷新画廊数据选中状态
     *
     * @param currentMedia
     */
    private fun notifyGallerySelectMedia(currentMedia: LocalMedia?) {
        if (mGalleryAdapter != null && selectorConfig?.selectorStyle
                ?.selectMainStyle?.isPreviewDisplaySelectGallery == true
        ) {
            currentMedia?.let { mGalleryAdapter!!.isSelectMedia(it) }
        }
    }

    /**
     * 刷新画廊数据
     */
    private fun notifyPreviewGalleryData(isAddRemove: Boolean, currentMedia: LocalMedia?) {
        if (mGalleryAdapter != null && selectorConfig?.selectorStyle
                ?.selectMainStyle?.isPreviewDisplaySelectGallery == true
        ) {
            if (mGalleryRecycle!!.visibility == View.INVISIBLE) {
                mGalleryRecycle!!.visibility = View.VISIBLE
            }
            if (isAddRemove) {
                if (selectorConfig?.selectionMode == SelectModeConfig.SINGLE) {
                    mGalleryAdapter!!.clear()
                }
                currentMedia?.let { mGalleryAdapter!!.addGalleryData(it) }
                mGalleryRecycle!!.smoothScrollToPosition(mGalleryAdapter!!.itemCount - 1)
            } else {
                currentMedia?.let { mGalleryAdapter!!.removeGalleryData(it) }
                if (selectorConfig?.selectCount == 0) {
                    mGalleryRecycle!!.visibility = View.INVISIBLE
                }
            }
        }
    }

    /**
     * 调用了startPreview预览逻辑
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun deletePreview() {
        if (isDisplayDelete) {
            val listener = selectorConfig?.onExternalPreviewEventListener
            if (listener != null) {
                listener.onPreviewDelete(viewPager2!!.currentItem)
                val currentItem = viewPager2!!.currentItem
                mData!!.removeAt(currentItem)
                if (mData!!.size == 0) {
                    handleExternalPreviewBack()
                    return
                }
                titleBar!!.setTitle(
                    getString(
                        R.string.ps_preview_image_num,
                        curPosition + 1, mData!!.size
                    )
                )
                totalNum = mData!!.size
                curPosition = currentItem
                if (viewPager2!!.adapter != null) {
                    viewPager2!!.adapter = null
                    viewPager2!!.adapter = this.adapter
                }
                viewPager2!!.setCurrentItem(curPosition, false)
            }
        }
    }

    /**
     * 处理外部预览返回处理
     */
    private fun handleExternalPreviewBack() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            if (selectorConfig?.isPreviewFullScreenMode == true) {
                hideFullScreenStatusBar()
            }
            onExitPictureSelector()
        }
    }

    override fun onExitFragment() {
        if (selectorConfig?.isPreviewFullScreenMode == true) {
            hideFullScreenStatusBar()
        }
    }

    private fun initBottomNavBar() {
        bottomNarBar!!.setBottomNavBarStyle()
        bottomNarBar!!.setSelectedChange()
        bottomNarBar!!.setOnBottomNavBarListener(object : OnBottomNavBarListener() {
            override fun onEditImage() {
                val editListener = selectorConfig?.onEditMediaEventListener
                if (editListener != null) {
                    val media = mData!!.get(viewPager2!!.currentItem)
                    editListener
                        .onStartMediaEdit(
                            this@PictureSelectorPreviewFragment, media,
                            Crop.REQUEST_EDIT_CROP
                        )
                }
            }

            override fun onCheckOriginalChange() {
                sendSelectedOriginalChangeEvent()
            }

            override fun onFirstCheckOriginalSelectedChange() {
                val currentItem = viewPager2!!.currentItem
                if (mData!!.size > currentItem) {
                    val media = mData!!.get(currentItem)
                    confirmSelect(media, false)
                }
            }
        })
    }

    /**
     * 外部预览的样式
     */
    private fun externalPreviewStyle() {
        titleBar!!.imageDelete?.visibility = if (isDisplayDelete) View.VISIBLE else View.GONE
        tvSelected?.visibility = View.GONE
        bottomNarBar?.visibility = View.GONE
        completeSelectView!!.setVisibility(View.GONE)
    }

    protected open fun createAdapter(): PicturePreviewAdapter {
        return PicturePreviewAdapter(selectorConfig)
    }

    private fun initViewPagerData(data: ArrayList<LocalMedia>) {
        this.adapter = createAdapter()
        adapter!!.setData(data.filterNotNull().toMutableList())
        adapter!!.setOnPreviewEventListener(MyOnPreviewEventListener())
        viewPager2!!.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager2!!.adapter = this.adapter
        selectorConfig?.selectedPreviewResult?.clear()
        if (data.size == 0 || curPosition >= data.size || curPosition < 0) {
            onKeyBackFragmentFinish()
            return
        }
        val media = data.get(curPosition)
        bottomNarBar!!.isDisplayEditor(
            PictureMimeType.isHasVideo(media.mimeType)
                    || PictureMimeType.isHasAudio(media.mimeType)
        )
        tvSelected!!.isSelected = selectorConfig?.selectedResult?.contains(data.get(viewPager2!!.currentItem)) ?: false
        viewPager2!!.registerOnPageChangeCallback(pageChangeCallback)
        viewPager2!!.setPageTransformer(
            MarginPageTransformer(
                DensityUtil.dip2px(
                    requireContext(),
                    3f
                )
            )
        )
        viewPager2!!.setCurrentItem(curPosition, false)
        sendChangeSubSelectPositionEvent(false)
        notifySelectNumberStyle(data.get(curPosition))
        startZoomEffect(media)
    }

    /**
     * 启动预览缩放特效
     */
    protected fun startZoomEffect(media: LocalMedia) {
        if (isSaveInstanceState || isInternalBottomPreview) {
            return
        }
        if (selectorConfig?.isPreviewZoomEffect == true) {
            viewPager2!!.post(object : Runnable {
                override fun run() {
                    adapter!!.setCoverScaleType(curPosition)
                }
            })
            if (PictureMimeType.isHasVideo(media.mimeType)) {
                getVideoRealSizeFromMedia(
                    media,
                    !PictureMimeType.isHasHttp(media.availablePath),
                    object : OnCallbackListener<IntArray?> {
                        override fun onCall(size: IntArray?) {
                            if (size != null) {
                                start(size)
                            }
                        }
                    })
            } else {
                getImageRealSizeFromMedia(
                    media,
                    !PictureMimeType.isHasHttp(media.availablePath),
                    object : OnCallbackListener<IntArray?> {
                        override fun onCall(size: IntArray?) {
                            if (size != null) {
                                start(size)
                            }
                        }
                    })
            }
        }
    }

    /**
     * start magical
     *
     * @param size
     */
    private fun start(size: IntArray) {
        magicalView!!.changeRealScreenHeight(size[0], size[1], false)
        val viewParams =
            BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) curPosition + 1 else curPosition)
        if (viewParams == null || (size[0] == 0 && size[1] == 0)) {
            viewPager2!!.post(object : Runnable {
                override fun run() {
                    magicalView!!.startNormal(size[0], size[1], false)
                }
            })
            magicalView!!.setBackgroundAlpha(1.0f)
            for (i in mAnimViews.indices) {
                mAnimViews.get(i)!!.setAlpha(1.0f)
            }
        } else {
            magicalView!!.setViewParams(
                viewParams.left,
                viewParams.top,
                viewParams.width,
                viewParams.height,
                size[0],
                size[1]
            )
            magicalView!!.start(false)
        }
        ObjectAnimator.ofFloat(this.viewPager2, "alpha", 0.0f, 1.0f).apply {
            duration = 50
            start()
        }
    }

    /**
     * ViewPageAdapter回调事件处理
     */
    private inner class MyOnPreviewEventListener : OnPreviewEventListener {
        override fun onBackPressed() {
            if (selectorConfig?.isPreviewFullScreenMode == true) {
                previewFullScreenMode()
            } else {
                if (isExternalPreview) {
                    if (selectorConfig?.isPreviewZoomEffect == true) {
                        magicalView!!.backToMin()
                    } else {
                        handleExternalPreviewBack()
                    }
                } else {
                    if (!isInternalBottomPreview && selectorConfig?.isPreviewZoomEffect == true) {
                        magicalView!!.backToMin()
                    } else {
                        this@PictureSelectorPreviewFragment.onBackCurrentFragment()
                    }
                }
            }
        }

        override fun onPreviewVideoTitle(videoName: String?) {
            if (TextUtils.isEmpty(videoName)) {
                titleBar!!.setTitle((curPosition + 1).toString() + "/" + totalNum)
            } else {
                titleBar!!.setTitle(videoName)
            }
        }

        override fun onLongPressDownload(media: LocalMedia?) {
            if (selectorConfig?.isHidePreviewDownload == true) {
                return
            }
            if (isExternalPreview) {
                onExternalLongPressDownload(media)
            }
        }
    }

    /**
     * 回到初始位置
     */
    private fun onKeyDownBackToMin() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            if (isExternalPreview) {
                if (selectorConfig?.isPreviewZoomEffect == true) {
                    magicalView!!.backToMin()
                } else {
                    onExitPictureSelector()
                }
            } else if (isInternalBottomPreview) {
                this@PictureSelectorPreviewFragment.onBackCurrentFragment()
            } else if (selectorConfig?.isPreviewZoomEffect == true) {
                magicalView!!.backToMin()
            } else {
                this@PictureSelectorPreviewFragment.onBackCurrentFragment()
            }
        }
    }

    /**
     * 预览全屏模式
     */
    private fun previewFullScreenMode() {
        if (isAnimationStart) {
            return
        }
        val isAnimInit = titleBar!!.getTranslationY() == 0.0f
        val set = AnimatorSet()
        val titleBarForm = (if (isAnimInit) 0 else -titleBar!!.height).toFloat()
        val titleBarTo = (if (isAnimInit) -titleBar!!.height else 0).toFloat()
        val alphaForm = if (isAnimInit) 1.0f else 0.0f
        val alphaTo = if (isAnimInit) 0.0f else 1.0f
        for (i in mAnimViews.indices) {
            val view = mAnimViews[i]
            set.playTogether(ObjectAnimator.ofFloat(view, "alpha", alphaForm, alphaTo))
            if (view is TitleBar) {
                set.playTogether(
                    ObjectAnimator.ofFloat(
                        view,
                        "translationY",
                        titleBarForm,
                        titleBarTo
                    )
                )
            }
        }
        set.duration = 350
        set.start()
        isAnimationStart = true
        set.addListener(object : AnimatorListenerAdapter() {
            @SuppressLint("WrongConstant")
            override fun onAnimationEnd(animation: Animator) {
                isAnimationStart = false
                if (SdkVersionUtils.isP && isAdded()) {
                    val window = requireActivity().window
                    val lp = window.attributes
                    if (isAnimInit) {
                        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
                        lp.layoutInDisplayCutoutMode =
                            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        window.attributes = lp
                        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    } else {
                        lp.flags = lp.flags and (WindowManager.LayoutParams.FLAG_FULLSCREEN.inv())
                        window.attributes = lp
                        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
                    }
                }
            }
        })

        if (isAnimInit) {
            showFullScreenStatusBar()
        } else {
            hideFullScreenStatusBar()
        }
    }

    /**
     * 全屏模式
     */
    private fun showFullScreenStatusBar() {
        for (i in mAnimViews.indices) {
            mAnimViews[i]?.isEnabled = false
        }
        bottomNarBar?.editor?.isEnabled = false
    }

    /**
     * 隐藏全屏模式
     */
    private fun hideFullScreenStatusBar() {
        for (i in mAnimViews.indices) {
            mAnimViews[i]?.isEnabled = true
        }
        bottomNarBar!!.editor?.isEnabled = true
    }

    /**
     * 外部预览长按下载
     *
     * @param media
     */
    private fun onExternalLongPressDownload(media: LocalMedia?) {
        val listener = selectorConfig?.onExternalPreviewEventListener
        if (listener != null && media != null) {
            val ctx = requireContext()
            if (!listener.onLongPressDownload(
                    ctx,
                    media
                )
            ) {
                val content: String?
                if (PictureMimeType.isHasAudio(media?.mimeType) == true
                    || PictureMimeType.isUrlHasAudio(media?.availablePath ?: "") == true
                ) {
                    content = getString(R.string.ps_prompt_audio_content)
                } else if (PictureMimeType.isHasVideo(media.mimeType) == true
                    || PictureMimeType.isUrlHasVideo(media?.availablePath ?: "") == true
                ) {
                    content = getString(R.string.ps_prompt_video_content)
                } else {
                    content = getString(R.string.ps_prompt_image_content)
                }
                val dialog: PictureCommonDialog = PictureCommonDialog.Companion.showDialog(
                    requireContext(),
                    getString(R.string.ps_prompt),
                    content ?: ""
                )
                dialog.setOnDialogEventListener(object : OnDialogEventListener {
                    override fun onConfirm() {
                        val path = media.availablePath
                        if (PictureMimeType.isHasHttp(path)) {
                            showLoading()
                        }
                        DownloadFileUtils.saveLocalFile(
                            requireContext(),
                            path ?: "",
                            media?.mimeType ?: "",
                            object : OnCallbackListener<String?> {
                                override fun onCall(realPath: String?) {
                                    dismissLoading()
                                    if (TextUtils.isEmpty(realPath)) {
                                        val errorMsg: String?
                                        if (PictureMimeType.isHasAudio(media?.mimeType) == true) {
                                            errorMsg = getString(R.string.ps_save_audio_error)
                                        } else if (PictureMimeType.isHasVideo(media?.mimeType) == true) {
                                            errorMsg = getString(R.string.ps_save_video_error)
                                        } else {
                                            errorMsg = getString(R.string.ps_save_image_error)
                                        }
                                        ToastUtils.showToast(requireContext(), errorMsg ?: "")
                                    } else {
                                        PictureMediaScannerConnection(requireActivity(), realPath ?: "")
                                        ToastUtils.showToast(
                                            requireContext(),
                                            getString(R.string.ps_save_success) + "\n" + (realPath ?: "")
                                        )
                                    }
                                }
                            })
                    }
                })
            }
        }
    }

    private val pageChangeCallback: OnPageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            if (mData!!.size > position) {
                val currentMedia =
                    if (positionOffsetPixels < screenWidth / 2) mData!!.get(position) else mData!!.get(
                        position + 1
                    )
                tvSelected!!.setSelected(isSelected(currentMedia))
                notifyGallerySelectMedia(currentMedia)
                notifySelectNumberStyle(currentMedia)
            }
        }

        override fun onPageSelected(position: Int) {
            curPosition = position
            titleBar!!.setTitle((curPosition + 1).toString() + "/" + totalNum)
            if (mData!!.size > position) {
                val currentMedia = mData!![position]
                notifySelectNumberStyle(currentMedia)
                if (isHasMagicalEffect) {
                    changeMagicalViewParams(position)
                }
                if (selectorConfig?.isPreviewZoomEffect == true) {
                    if (isInternalBottomPreview && selectorConfig?.isAutoVideoPlay == true) {
                        startAutoVideoPlay(position)
                    } else {
                        adapter!!.setVideoPlayButtonUI(position)
                    }
                } else {
                    if (selectorConfig?.isAutoVideoPlay == true) {
                        startAutoVideoPlay(position)
                    }
                }
                notifyGallerySelectMedia(currentMedia)
                bottomNarBar!!.isDisplayEditor(
                    PictureMimeType.isHasVideo(currentMedia?.mimeType) == true
                            || PictureMimeType.isHasAudio(currentMedia?.mimeType) == true
                )
                if (!isExternalPreview && !isInternalBottomPreview && selectorConfig?.isOnlySandboxDir != true) {
                    if (selectorConfig?.isPageStrategy == true) {
                        if (isHasMore) {
                            if (position == (adapter!!.itemCount - 1) - PictureConfig.MIN_PAGE_SIZE
                                || position == adapter!!.itemCount - 1
                            ) {
                                loadMoreData()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 自动播放视频
     *
     * @param position
     */
    private fun startAutoVideoPlay(position: Int) {
        viewPager2!!.post(object : Runnable {
            override fun run() {
                adapter!!.startAutoVideoPlay(position)
            }
        })
    }

    /**
     * 更新MagicalView ViewParams 参数
     *
     * @param position
     */
    private fun changeMagicalViewParams(position: Int) {
        val media = mData!!.get(position)
        if (PictureMimeType.isHasVideo(media.mimeType)) {
            getVideoRealSizeFromMedia(media, false, object : OnCallbackListener<IntArray?> {
                override fun onCall(size: IntArray?) {
                    if (size != null) {
                        setMagicalViewParams(size[0], size[1], position)
                    }
                }
            })
        } else {
            getImageRealSizeFromMedia(media, false, object : OnCallbackListener<IntArray?> {
                override fun onCall(size: IntArray?) {
                    if (size != null) {
                        setMagicalViewParams(size[0], size[1], position)
                    }
                }
            })
        }
    }

    /**
     * setMagicalViewParams
     *
     * @param imageWidth
     * @param imageHeight
     * @param position
     */
    private fun setMagicalViewParams(imageWidth: Int, imageHeight: Int, position: Int) {
        magicalView!!.changeRealScreenHeight(imageWidth, imageHeight, true)
        val viewParams =
            BuildRecycleItemViewParams.getItemViewParams(if (isShowCamera) position + 1 else position)
        if (viewParams == null || imageWidth == 0 || imageHeight == 0) {
            magicalView!!.setViewParams(0, 0, 0, 0, imageWidth, imageHeight)
        } else {
            magicalView!!.setViewParams(
                viewParams.left,
                viewParams.top,
                viewParams.width,
                viewParams.height,
                imageWidth,
                imageHeight
            )
        }
    }

    /**
     * 获取图片Media的真实大小
     *
     * @param media
     * @param resize
     */
    private fun getImageRealSizeFromMedia(
        media: LocalMedia,
        resize: Boolean,
        call: OnCallbackListener<IntArray?>?
    ) {
        var realWidth: Int
        var realHeight: Int
        var isReturnNow = true
        if (MediaUtils.isLongImage(media.width, media.height)) {
            realWidth = screenWidth
            realHeight = screenHeight
        } else {
            realWidth = media.width
            realHeight = media.height
            if (resize) {
                if ((realWidth <= 0 || realHeight <= 0) || (realWidth > realHeight)) {
                    val config = selectorConfig
                    if (config?.isSyncWidthAndHeight == true) {
                        isReturnNow = false
                        // 先不展现内容，异步获取可能耗时会导致界面先出现图片而后在放大出现
                        viewPager2!!.alpha = 0f
                        MediaUtils.getImageSize(
                            requireContext(),
                            media.availablePath,
                            object : OnCallbackListener<MediaExtraInfo?> {
                                override fun onCall(extraInfo: MediaExtraInfo?) {
                                    if (extraInfo?.width ?: 0 > 0) {
                                        media.width = extraInfo?.width ?: 0
                                    }
                                    if (extraInfo?.height ?: 0 > 0) {
                                        media.height = extraInfo?.height ?: 0
                                    }
                                    if (call != null) {
                                        call.onCall(intArrayOf(media.width, media.height))
                                    }
                                }
                            })
                    }
                }
            }
        }
        if (media.isCut() && media.cropImageWidth > 0 && media.cropImageHeight > 0) {
            realWidth = media.cropImageWidth
            realHeight = media.cropImageHeight
        }
        if (isReturnNow) {
            call!!.onCall(intArrayOf(realWidth, realHeight))
        }
    }


    /**
     * 获取视频Media的真实大小
     *
     * @param media
     * @param resize
     */
    private fun getVideoRealSizeFromMedia(
        media: LocalMedia,
        resize: Boolean,
        call: OnCallbackListener<IntArray?>?
    ) {
        var isReturnNow = true
        if (resize) {
            if ((media.width <= 0 || media.height <= 0) || (media.width > media.height)) {
                if (selectorConfig?.isSyncWidthAndHeight == true) {
                    isReturnNow = false
                    // 先不展现内容，异步获取可能耗时会导致界面先出现图片而后在放大出现
                    viewPager2!!.alpha = 0f
                    MediaUtils.getVideoSize(
                        requireContext(),
                        media.availablePath,
                        object : OnCallbackListener<MediaExtraInfo?> {
                            override fun onCall(extraInfo: MediaExtraInfo?) {
                                if (extraInfo?.width ?: 0 > 0) {
                                    media.width = extraInfo?.width ?: 0
                                }
                                if (extraInfo?.height ?: 0 > 0) {
                                    media.height = extraInfo?.height ?: 0
                                }
                                if (call != null) {
                                    call.onCall(intArrayOf(media.width, media.height))
                                }
                            }
                        })
                }
            }
        }
        if (isReturnNow) {
            call!!.onCall(intArrayOf(media.width, media.height))
        }
    }

    /**
     * 对选择数量进行编号排序
     */
    fun notifySelectNumberStyle(currentMedia: LocalMedia?) {
        val selectMainStyle = selectorConfig?.selectorStyle?.selectMainStyle
        if (selectMainStyle?.isPreviewSelectNumberStyle == true) {
            if (selectMainStyle.isSelectNumberStyle) {
                tvSelected!!.setText("")
                val selectCount = selectorConfig?.selectCount ?: 0
                for (i in 0 until selectCount) {
                    val media = selectorConfig?.selectedResult?.get(i)
                    if (media != null && currentMedia != null && (TextUtils.equals(media.path, currentMedia.path)
                        || media.id == currentMedia.id)) {
                        currentMedia.num = media.num
                        media.position = currentMedia.position
                        tvSelected!!.setText(toString(currentMedia.num))
                    }
                }
            }
        }
    }

    /**
     * 当前图片是否选中
     *
     * @param media
     * @return
     */
    protected fun isSelected(media: LocalMedia?): Boolean {
        return selectorConfig?.selectedResult?.contains(media) ?: false
    }

    override fun onEditMedia(data: Intent?) {
        if (mData!!.size > viewPager2!!.currentItem && data != null) {
            val currentMedia = mData!![viewPager2!!.currentItem]
            val output = Crop.getOutput(data)
            currentMedia?.cutPath = if (output != null) output.path else ""
            currentMedia?.cropImageWidth = Crop.getOutputImageWidth(data)
            currentMedia?.cropImageHeight = Crop.getOutputImageHeight(data)
            currentMedia?.cropOffsetX = Crop.getOutputImageOffsetX(data)
            currentMedia?.cropOffsetY = Crop.getOutputImageOffsetY(data)
            currentMedia?.cropResultAspectRatio = Crop.getOutputCropAspectRatio(data)
            currentMedia?.setCut(!TextUtils.isEmpty(currentMedia?.cutPath))
            currentMedia?.customData = Crop.getOutputCustomExtraData(data)
            currentMedia?.setEditorImage(currentMedia?.isCut() ?: false)
            currentMedia.sandboxPath = currentMedia.cutPath
            if (selectorConfig?.selectedResult?.contains(currentMedia) == true) {
                val exitsMedia = currentMedia.compareLocalMedia
                if (exitsMedia != null) {
                    exitsMedia.cutPath = currentMedia.cutPath
                    exitsMedia.setCut(currentMedia.isCut())
                    exitsMedia.setEditorImage(currentMedia.isEditorImage())
                    exitsMedia.customData = currentMedia.customData
                    exitsMedia.sandboxPath = currentMedia.cutPath
                    exitsMedia.cropImageWidth = Crop.getOutputImageWidth(data)
                    exitsMedia.cropImageHeight = Crop.getOutputImageHeight(data)
                    exitsMedia.cropOffsetX = Crop.getOutputImageOffsetX(data)
                    exitsMedia.cropOffsetY = Crop.getOutputImageOffsetY(data)
                    exitsMedia.cropResultAspectRatio = Crop.getOutputCropAspectRatio(data)
                }
                sendFixedSelectedChangeEvent(currentMedia)
            } else {
                confirmSelect(currentMedia, false)
            }
            adapter!!.notifyItemChanged(viewPager2!!.currentItem)
            notifyGallerySelectMedia(currentMedia)
        }
    }

    override fun onExitPictureSelector() {
        if (adapter != null) {
            adapter!!.destroy()
        }
        super.onExitPictureSelector()
    }

    override fun onResume() {
        super.onResume()
        if (isPause) {
            resumePausePlay()
            isPause = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (this.isPlaying) {
            resumePausePlay()
            isPause = true
        }
    }

    private fun resumePausePlay() {
        if (this.adapter != null) {
            val holder = adapter!!.getCurrentHolder(viewPager2!!.getCurrentItem())
            if (holder != null) {
                holder.resumePausePlay()
            }
        }
    }

    private val isPlaying: Boolean
        get() = this.adapter != null && adapter!!.isPlaying(viewPager2!!.getCurrentItem())

    override fun onDestroy() {
        if (this.adapter != null) {
            adapter!!.destroy()
        }
        if (this.viewPager2 != null) {
            viewPager2!!.unregisterOnPageChangeCallback(pageChangeCallback)
        }
        super.onDestroy()
    }


    companion object {
        val TAG: String = PictureSelectorPreviewFragment::class.java.simpleName

        fun newInstance(): PictureSelectorPreviewFragment {
            val fragment = PictureSelectorPreviewFragment()
            fragment.arguments = Bundle()
            return fragment
        }
    }
}
