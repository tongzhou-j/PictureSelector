package com.luck.pictureselector

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luck.lib.camerax.CameraImageEngine
import com.luck.lib.camerax.SimpleCameraX
import com.luck.lib.camerax.listener.OnSimpleXPermissionDeniedListener
import com.luck.lib.camerax.listener.OnSimpleXPermissionDescriptionListener
import com.luck.lib.camerax.permissions.SimpleXPermissionUtil
import com.luck.picture.lib.PictureSelectorPreviewFragment
import com.luck.picture.lib.animators.AnimationType
import com.luck.picture.lib.basic.FragmentInjectManager
import com.luck.picture.lib.basic.IBridgePictureBehavior
import com.luck.picture.lib.basic.IBridgeViewLifecycle
import com.luck.picture.lib.basic.PictureCommonFragment.SelectorResult
import com.luck.picture.lib.basic.PictureSelectionCameraModel
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelectionSystemModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectLimitType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.dialog.RemindDialog
import com.luck.picture.lib.dialog.RemindDialog.OnDialogClickListener
import com.luck.picture.lib.engine.CompressEngine
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.engine.CropEngine
import com.luck.picture.lib.engine.CropFileEngine
import com.luck.picture.lib.engine.ExtendLoaderEngine
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.engine.UriToFileTransformEngine
import com.luck.picture.lib.engine.VideoPlayerEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnBitmapWatermarkEventListener
import com.luck.picture.lib.interfaces.OnCallbackListener
import com.luck.picture.lib.interfaces.OnCameraInterceptListener
import com.luck.picture.lib.interfaces.OnCustomLoadingListener
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.luck.picture.lib.interfaces.OnGridItemSelectAnimListener
import com.luck.picture.lib.interfaces.OnInjectActivityPreviewListener
import com.luck.picture.lib.interfaces.OnInjectLayoutResourceListener
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.interfaces.OnMediaEditInterceptListener
import com.luck.picture.lib.interfaces.OnPermissionDeniedListener
import com.luck.picture.lib.interfaces.OnPermissionDescriptionListener
import com.luck.picture.lib.interfaces.OnPreviewInterceptListener
import com.luck.picture.lib.interfaces.OnQueryAlbumListener
import com.luck.picture.lib.interfaces.OnQueryAllAlbumListener
import com.luck.picture.lib.interfaces.OnQueryDataResultListener
import com.luck.picture.lib.interfaces.OnQueryFilterListener
import com.luck.picture.lib.interfaces.OnRecordAudioInterceptListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.interfaces.OnSelectAnimListener
import com.luck.picture.lib.interfaces.OnSelectLimitTipsListener
import com.luck.picture.lib.interfaces.OnVideoThumbnailEventListener
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.loader.SandboxFileLoader
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.permissions.PermissionConfig
import com.luck.picture.lib.permissions.PermissionResultCallback
import com.luck.picture.lib.permissions.PermissionUtil
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.PictureWindowAnimationStyle
import com.luck.picture.lib.style.SelectMainStyle
import com.luck.picture.lib.style.TitleBarStyle
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.luck.picture.lib.utils.SandboxTransformUtils
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.StyleUtils
import com.luck.picture.lib.utils.ToastUtils
import com.luck.picture.lib.utils.ValueOf.toString
import com.luck.picture.lib.widget.MediumBoldTextView
import com.luck.pictureselector.CustomPreviewFragment.Companion.newInstance
import com.luck.pictureselector.GlideEngine.Companion.createGlideEngine
import com.luck.pictureselector.ImageLoaderUtils.assertValidRequest
import com.luck.pictureselector.ImageUtil.createWaterMaskRightTop
import com.luck.pictureselector.PicassoEngine.Companion.createPicassoEngine
import com.luck.pictureselector.adapter.GridImageAdapter
import com.luck.pictureselector.listener.DragListener
import com.luck.pictureselector.listener.OnItemLongClickListener
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine
import com.yalantis.ucrop.model.AspectRatio
import top.zibin.luban.CompressionPredicate
import top.zibin.luban.Luban.Companion.with
import top.zibin.luban.OnCompressListener
import top.zibin.luban.OnNewCompressListener
import top.zibin.luban.OnRenameListener
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections

/**
 * @author：luck
 * @data：2019/12/20 晚上 23:12
 * @描述: Demo
 */
class MainActivity : AppCompatActivity(), IBridgePictureBehavior, View.OnClickListener,
    RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {
    private var mAdapter: GridImageAdapter? = null
    private var maxSelectNum = 9
    private var maxSelectVideoNum = 1
    private var tv_select_num: TextView? = null
    private var tv_select_video_num: TextView? = null
    private var tv_original_tips: TextView? = null
    private var tvDeleteText: TextView? = null
    private var rgb_crop: RadioGroup? = null
    private var llSelectVideoSize: LinearLayout? = null
    private var aspect_ratio_x = -1
    private var aspect_ratio_y = -1
    private var cb_voice: CheckBox? = null
    private var cb_choose_mode: CheckBox? = null
    private var cb_isCamera: CheckBox? = null
    private var cb_isGif: CheckBox? = null
    private var cb_preview_img: CheckBox? = null
    private var cb_preview_video: CheckBox? = null
    private var cb_crop: CheckBox? = null
    private var cb_compress: CheckBox? = null
    private var cb_mode: CheckBox? = null
    private var cb_hide: CheckBox? = null
    private var cb_crop_circular: CheckBox? = null
    private var cb_styleCrop: CheckBox? = null
    private var cb_showCropGrid: CheckBox? = null
    private var cb_showCropFrame: CheckBox? = null
    private var cb_preview_audio: CheckBox? = null
    private var cb_original: CheckBox? = null
    private var cb_single_back: CheckBox? = null
    private var cb_custom_camera: CheckBox? = null
    private var cbPage: CheckBox? = null
    private var cbEnabledMask: CheckBox? = null
    private var cbEditor: CheckBox? = null
    private var cb_custom_sandbox: CheckBox? = null
    private var cb_only_dir: CheckBox? = null
    private var cb_preview_full: CheckBox? = null
    private var cb_preview_scale: CheckBox? = null
    private var cb_inject_layout: CheckBox? = null
    private var cb_time_axis: CheckBox? = null
    private var cb_WithImageVideo: CheckBox? = null
    private var cb_system_album: CheckBox? = null
    private var cb_fast_select: CheckBox? = null
    private var cb_skip_not_gif: CheckBox? = null
    private var cb_not_gif: CheckBox? = null
    private var cb_attach_camera_mode: CheckBox? = null
    private var cb_attach_system_mode: CheckBox? = null
    private var cb_camera_zoom: CheckBox? = null
    private var cb_camera_focus: CheckBox? = null
    private var cb_query_sort_order: CheckBox? = null
    private var cb_watermark: CheckBox? = null
    private var cb_custom_preview: CheckBox? = null
    private var cb_permission_desc: CheckBox? = null
    private var cb_video_thumbnails: CheckBox? = null
    private var cb_auto_video: CheckBox? = null
    private var cb_selected_anim: CheckBox? = null
    private var cb_video_resume: CheckBox? = null
    private var cb_custom_loading: CheckBox? = null
    private var chooseMode = SelectMimeType.ofAll()
    private var isHasLiftDelete = false
    private var needScaleBig = true
    private var needScaleSmall = false
    private var isUseSystemPlayer = false
    private var language = LanguageConfig.UNKNOWN_LANGUAGE
    private var x = 0
    private var y = 0
    private var animationMode = AnimationType.DEFAULT_ANIMATION
    private var selectorStyle: PictureSelectorStyle? = null
    private val mData: MutableList<LocalMedia> = ArrayList()
    private var launcherResult: ActivityResultLauncher<Intent>? = null
    private var resultMode: Int = LAUNCHER_RESULT
    private var imageEngine: ImageEngine? = null
    private var videoPlayerEngine: VideoPlayerEngine<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selectorStyle = PictureSelectorStyle()
        val minus = findViewById<ImageView>(R.id.minus)
        val plus = findViewById<ImageView>(R.id.plus)
        tv_select_num = findViewById<TextView>(R.id.tv_select_num)

        val videoMinus = findViewById<ImageView>(R.id.video_minus)
        val videoPlus = findViewById<ImageView>(R.id.video_plus)
        tv_select_video_num = findViewById<TextView>(R.id.tv_select_video_num)
        llSelectVideoSize = findViewById<LinearLayout>(R.id.ll_select_video_size)
        tvDeleteText = findViewById<TextView>(R.id.tv_delete_text)
        tv_original_tips = findViewById<TextView>(R.id.tv_original_tips)
        rgb_crop = findViewById<RadioGroup>(R.id.rgb_crop)
        cb_video_thumbnails = findViewById<CheckBox>(R.id.cb_video_thumbnails)
        val rgb_video_player = findViewById<RadioGroup>(R.id.rgb_video_player)
        val rgb_result = findViewById<RadioGroup>(R.id.rgb_result)
        val rgb_style = findViewById<RadioGroup>(R.id.rgb_style)
        val rgb_animation = findViewById<RadioGroup>(R.id.rgb_animation)
        val rgb_list_anim = findViewById<RadioGroup>(R.id.rgb_list_anim)
        val rgb_photo_mode = findViewById<RadioGroup>(R.id.rgb_photo_mode)
        val rgb_language = findViewById<RadioGroup>(R.id.rgb_language)
        val rgb_engine = findViewById<RadioGroup>(R.id.rgb_engine)
        cb_voice = findViewById<CheckBox>(R.id.cb_voice)
        cb_choose_mode = findViewById<CheckBox>(R.id.cb_choose_mode)
        cb_video_resume = findViewById<CheckBox>(R.id.cb_video_resume)
        cb_isCamera = findViewById<CheckBox>(R.id.cb_isCamera)
        cb_isGif = findViewById<CheckBox>(R.id.cb_isGif)
        cb_watermark = findViewById<CheckBox>(R.id.cb_watermark)
        cb_WithImageVideo = findViewById<CheckBox>(R.id.cbWithImageVideo)
        cb_system_album = findViewById<CheckBox>(R.id.cb_system_album)
        cb_fast_select = findViewById<CheckBox>(R.id.cb_fast_select)
        cb_preview_full = findViewById<CheckBox>(R.id.cb_preview_full)
        cb_preview_scale = findViewById<CheckBox>(R.id.cb_preview_scale)
        cb_inject_layout = findViewById<CheckBox>(R.id.cb_inject_layout)
        cb_preview_img = findViewById<CheckBox>(R.id.cb_preview_img)
        cb_camera_zoom = findViewById<CheckBox>(R.id.cb_camera_zoom)
        cb_camera_focus = findViewById<CheckBox>(R.id.cb_camera_focus)
        cb_query_sort_order = findViewById<CheckBox>(R.id.cb_query_sort_order)
        cb_custom_preview = findViewById<CheckBox>(R.id.cb_custom_preview)
        cb_permission_desc = findViewById<CheckBox>(R.id.cb_permission_desc)
        cb_preview_video = findViewById<CheckBox>(R.id.cb_preview_video)
        cb_auto_video = findViewById<CheckBox>(R.id.cb_auto_video)
        cb_selected_anim = findViewById<CheckBox>(R.id.cb_selected_anim)
        cb_time_axis = findViewById<CheckBox>(R.id.cb_time_axis)
        cb_custom_loading = findViewById<CheckBox>(R.id.cb_custom_loading)
        cb_crop = findViewById<CheckBox>(R.id.cb_crop)
        cbPage = findViewById<CheckBox>(R.id.cbPage)
        cbEditor = findViewById<CheckBox>(R.id.cb_editor)
        cbEnabledMask = findViewById<CheckBox>(R.id.cbEnabledMask)
        cb_styleCrop = findViewById<CheckBox>(R.id.cb_styleCrop)
        cb_compress = findViewById<CheckBox>(R.id.cb_compress)
        cb_mode = findViewById<CheckBox>(R.id.cb_mode)
        cb_custom_sandbox = findViewById<CheckBox>(R.id.cb_custom_sandbox)
        cb_only_dir = findViewById<CheckBox>(R.id.cb_only_dir)
        cb_showCropGrid = findViewById<CheckBox>(R.id.cb_showCropGrid)
        cb_showCropFrame = findViewById<CheckBox>(R.id.cb_showCropFrame)
        cb_preview_audio = findViewById<CheckBox>(R.id.cb_preview_audio)
        cb_original = findViewById<CheckBox>(R.id.cb_original)
        cb_single_back = findViewById<CheckBox>(R.id.cb_single_back)
        cb_custom_camera = findViewById<CheckBox>(R.id.cb_custom_camera)
        cb_hide = findViewById<CheckBox>(R.id.cb_hide)
        cb_not_gif = findViewById<CheckBox>(R.id.cb_not_gif)
        cb_skip_not_gif = findViewById<CheckBox>(R.id.cb_skip_not_gif)
        cb_crop_circular = findViewById<CheckBox>(R.id.cb_crop_circular)
        cb_attach_camera_mode = findViewById<CheckBox>(R.id.cb_attach_camera_mode)
        cb_attach_system_mode = findViewById<CheckBox>(R.id.cb_attach_system_mode)
        cb_mode!!.setOnCheckedChangeListener(this)
        rgb_crop!!.setOnCheckedChangeListener(this)
        cb_custom_camera!!.setOnCheckedChangeListener(this)
        rgb_result.setOnCheckedChangeListener(this)
        rgb_style.setOnCheckedChangeListener(this)
        rgb_animation.setOnCheckedChangeListener(this)
        rgb_list_anim.setOnCheckedChangeListener(this)
        rgb_photo_mode.setOnCheckedChangeListener(this)
        rgb_language.setOnCheckedChangeListener(this)
        rgb_video_player.setOnCheckedChangeListener(this)
        rgb_engine.setOnCheckedChangeListener(this)
        val mRecyclerView = findViewById<RecyclerView>(R.id.recycler)
        val left_back = findViewById<ImageView>(R.id.left_back)
        left_back.setOnClickListener(this)
        minus.setOnClickListener(this)
        plus.setOnClickListener(this)
        videoMinus.setOnClickListener(this)
        videoPlus.setOnClickListener(this)
        cb_crop!!.setOnCheckedChangeListener(this)
        cb_only_dir!!.setOnCheckedChangeListener(this)
        cb_custom_sandbox!!.setOnCheckedChangeListener(this)
        cb_crop_circular!!.setOnCheckedChangeListener(this)
        cb_attach_camera_mode!!.setOnCheckedChangeListener(this)
        cb_attach_system_mode!!.setOnCheckedChangeListener(this)
        cb_system_album!!.setOnCheckedChangeListener(this)
        cb_compress!!.setOnCheckedChangeListener(this)
        cb_not_gif!!.setOnCheckedChangeListener(this)
        cb_skip_not_gif!!.setOnCheckedChangeListener(this)
        tv_select_num!!.setText(toString(maxSelectNum))
        tv_select_video_num!!.setText(toString(maxSelectVideoNum))
        // 注册需要写在onCreate或Fragment onAttach里，否则会报java.lang.IllegalStateException异常
        launcherResult = createActivityResultLauncher()

        //        List<LocalMedia> list = new ArrayList<>();
//        list.add(LocalMedia.generateHttpAsLocalMedia("https://gossv.cfp.cn/videos/mts_videos/medium/temp/VCG42483198574.mp4"));
//        list.add(LocalMedia.generateHttpAsLocalMedia("https://wx1.sinaimg.cn/mw2000/0073ozWdly1h0afogn4vij30u05keb29.jpg"));
//        list.add(LocalMedia.generateHttpAsLocalMedia("https://wx3.sinaimg.cn/mw2000/0073ozWdly1h0afohdkygj30u05791kx.jpg"));
//        list.add(LocalMedia.generateHttpAsLocalMedia("https://wx2.sinaimg.cn/mw2000/0073ozWdly1h0afoi70m2j30u05fq1kx.jpg"));
//        list.add(LocalMedia.generateHttpAsLocalMedia("https://wx2.sinaimg.cn/mw2000/0073ozWdly1h0afoipj8xj30kw3kmwru.jpg"));
//        list.add(LocalMedia.generateHttpAsLocalMedia("https://wx4.sinaimg.cn/mw2000/0073ozWdly1h0afoj5q8ij30u04gqkb1.jpg"));
//        list.add(LocalMedia.generateHttpAsLocalMedia("https://ww1.sinaimg.cn/bmiddle/bcd10523ly1g96mg4sfhag20c806wu0x.gif"));
//        mData.addAll(list);
        val manager = FullyGridLayoutManager(
            this,
            4, GridLayoutManager.VERTICAL, false
        )
        mRecyclerView.setLayoutManager(manager)
        val itemAnimator = mRecyclerView.getItemAnimator()
        if (itemAnimator != null) {
            (itemAnimator as SimpleItemAnimator).setSupportsChangeAnimations(false)
        }
        mRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                4,
                DensityUtil.dip2px(this, 8f), false
            )
        )
        mAdapter = GridImageAdapter(this@MainActivity, mData)
        mAdapter!!.setSelectMax(maxSelectNum + maxSelectVideoNum)
        mRecyclerView.setAdapter(mAdapter)
        if (savedInstanceState != null && savedInstanceState.getParcelableArrayList<Parcelable?>("selectorList") != null) {
            mData.clear()
            mData.addAll(savedInstanceState.getParcelableArrayList<LocalMedia?>("selectorList")!!)
        }
        val systemHigh = " (仅支持部分api)"
        val systemTips = "使用系统图库" + systemHigh
        val startIndex = systemTips.indexOf(systemHigh)
        val endOf = startIndex + systemHigh.length
        val builder = SpannableStringBuilder(systemTips)
        builder.setSpan(
            AbsoluteSizeSpan(DensityUtil.dip2px(this@MainActivity, 12f)),
            startIndex,
            endOf,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        builder.setSpan(
            ForegroundColorSpan(-0x340000),
            startIndex,
            endOf,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        cb_system_album!!.setText(builder)

        val cameraHigh = " (默认fragment)"
        val cameraTips = "使用Activity承载Camera相机" + cameraHigh
        val startIndex2 = cameraTips.indexOf(cameraHigh)
        val endOf2 = startIndex2 + cameraHigh.length
        val builder2 = SpannableStringBuilder(cameraTips)
        builder2.setSpan(
            AbsoluteSizeSpan(DensityUtil.dip2px(this@MainActivity, 12f)),
            startIndex2,
            endOf2,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        builder2.setSpan(
            ForegroundColorSpan(-0x340000),
            startIndex2,
            endOf2,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        cb_attach_camera_mode!!.setText(builder2)


        val systemAlbumHigh = " (默认fragment)"
        val systemAlbumTips = "使用Activity承载系统相册" + systemAlbumHigh
        val startIndex3 = systemAlbumTips.indexOf(systemAlbumHigh)
        val endOf3 = startIndex3 + systemAlbumHigh.length
        val builder3 = SpannableStringBuilder(systemAlbumTips)
        builder3.setSpan(
            AbsoluteSizeSpan(DensityUtil.dip2px(this@MainActivity, 12f)),
            startIndex3,
            endOf3,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        builder3.setSpan(
            ForegroundColorSpan(-0x340000),
            startIndex3,
            endOf3,
            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
        )
        cb_attach_system_mode!!.setText(builder3)

        cb_original!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            tv_original_tips!!.setVisibility(
                if (isChecked) View.VISIBLE else View.GONE
            )
        })
        cb_choose_mode!!.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
            cb_single_back!!.setVisibility(if (isChecked) View.GONE else View.VISIBLE)
            cb_single_back!!.setChecked(!isChecked && cb_single_back!!.isChecked())
        })

        imageEngine = createGlideEngine()

        mAdapter!!.setOnItemClickListener(object : GridImageAdapter.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                // 预览图片、视频、音频
                PictureSelector.create(this@MainActivity)
                    .openPreview()
                    .setImageEngine(imageEngine)
                    .setVideoPlayerEngine(videoPlayerEngine)
                    .setSelectorUIStyle(selectorStyle)
                    .setLanguage(language)
                    .isAutoVideoPlay(cb_auto_video!!.isChecked())
                    .isLoopAutoVideoPlay(cb_auto_video!!.isChecked())
                    .isPreviewFullScreenMode(cb_preview_full!!.isChecked())
                    .isVideoPauseResumePlay(cb_video_resume!!.isChecked())
                    .isUseSystemVideoPlayer(isUseSystemPlayer)
                    .setCustomLoadingListener(getCustomLoadingListener())
                    .isPreviewZoomEffect(
                        chooseMode != SelectMimeType.ofAudio() && cb_preview_scale!!.isChecked(),
                        mRecyclerView
                    )
                    .setAttachViewLifecycle(object : IBridgeViewLifecycle {
                        override fun onViewCreated(
                            fragment: Fragment?,
                            view: View?,
                            savedInstanceState: Bundle?
                        ) {
//                                PictureSelectorPreviewFragment previewFragment = (PictureSelectorPreviewFragment) fragment;
//                                MediumBoldTextView tvShare = view.findViewById(R.id.tv_share);
//                                tvShare.setVisibility(View.VISIBLE)
//                                previewFragment.addAminViews(tvShare);
//                                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) tvShare.getLayoutParams();
//                                layoutParams.topMargin = cb_preview_full.isChecked() ? DensityUtil.getStatusBarHeight(getContext()) : 0;
//                                tvShare.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        PicturePreviewAdapter previewAdapter = previewFragment.getAdapter();
//                                        ViewPager2 viewPager2 = previewFragment.getViewPager2();
//                                        LocalMedia media = previewAdapter.getItem(viewPager2.getCurrentItem());
//                                        ToastUtils.showToast(fragment.getContext(), "自定义分享事件:" + viewPager2.getCurrentItem());
//                                    }
//                                });
                        }

                        override fun onDestroy(fragment: Fragment?) {
//                                if (cb_preview_full.isChecked()) {
//                                    // 如果是全屏预览模式且是startFragmentPreview预览，回到自己的界面时需要恢复一下自己的沉浸式状态
//                                    // 以下提供2种解决方案:
//                                    // 1.通过ImmersiveManager.immersiveAboveAPI23重新设置一下沉浸式
//                                    int statusBarColor = ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_grey);
//                                    int navigationBarColor = ContextCompat.getColor(getContext(), com.luck.picture.lib.R.color.ps_color_grey);
//                                    ImmersiveManager.immersiveAboveAPI23(MainActivity.this,
//                                            true, true,
//                                            statusBarColor, navigationBarColor, false);
//                                    // 2.让自己的titleBar的高度加上一个状态栏高度且内容PaddingTop下沉一个状态栏的高度
//                                }
                        }
                    })
                    .setInjectLayoutResourceListener(object : OnInjectLayoutResourceListener {
                        override fun getLayoutResourceId(
                            context: Context?,
                            resourceSource: Int
                        ): Int {
                            return if (resourceSource == InjectResourceSource.PREVIEW_LAYOUT_RESOURCE)
                                R.layout.ps_custom_fragment_preview
                            else
                                InjectResourceSource.DEFAULT_LAYOUT_RESOURCE
                        }
                    })
                    .setExternalPreviewEventListener(MyExternalPreviewEventListener())
                    .setInjectActivityPreviewFragment(object : OnInjectActivityPreviewListener {
                        override fun onInjectPreviewFragment(): PictureSelectorPreviewFragment? {
                            return if (cb_custom_preview!!.isChecked()) newInstance() else null
                        }
                    })
                    .startActivityPreview(position, true, mAdapter!!.getData())
            }

            override fun openPicture() {
                val mode = cb_mode!!.isChecked()
                if (mode) {
                    // 进入系统相册
                    if (cb_system_album!!.isChecked()) {
                        val systemGalleryMode: PictureSelectionSystemModel = PictureSelector.create(
                            this@MainActivity
                        )
                            .openSystemGallery(chooseMode)
                            .setSelectionMode(if (cb_choose_mode!!.isChecked()) SelectModeConfig.MULTIPLE else SelectModeConfig.SINGLE)
                            .setCompressEngine(getCompressFileEngine())
                            .setCropEngine(getCropFileEngine())
                            .setSkipCropMimeType(*getNotSupportCrop() ?: emptyArray())
                            .setSelectLimitTipsListener(MeOnSelectLimitTipsListener())
                            .setAddBitmapWatermarkListener(getAddBitmapWatermarkListener())
                            .setVideoThumbnailListener(getVideoThumbnailEventListener())
                            .setCustomLoadingListener(getCustomLoadingListener())
                            .isOriginalControl(cb_original!!.isChecked())
                            .setPermissionDescriptionListener(getPermissionDescriptionListener())
                            .setSandboxFileEngine(MeSandboxFileEngine())
                        forSystemResult(systemGalleryMode)
                    } else {
                        // 进入相册
                        val selectionModel: PictureSelectionModel =
                            PictureSelector.create(this@MainActivity)
                                .openGallery(chooseMode)
                                .setSelectorUIStyle(selectorStyle)
                                .setImageEngine(imageEngine)
                                .setVideoPlayerEngine(videoPlayerEngine)
                                .setCropEngine(getCropFileEngine())
                                .setCompressEngine(getCompressFileEngine())
                                .setSandboxFileEngine(MeSandboxFileEngine())
                                .setCameraInterceptListener(getCustomCameraEvent())
                                .setRecordAudioInterceptListener(MeOnRecordAudioInterceptListener())
                                .setSelectLimitTipsListener(MeOnSelectLimitTipsListener())
                                .setEditMediaInterceptListener(getCustomEditMediaEvent())
                                .setPermissionDescriptionListener(getPermissionDescriptionListener())
                                .setPreviewInterceptListener(getPreviewInterceptListener())
                                .setPermissionDeniedListener(getPermissionDeniedListener())
                                .setAddBitmapWatermarkListener(getAddBitmapWatermarkListener())
                                .setVideoThumbnailListener(getVideoThumbnailEventListener())
                                .isAutoVideoPlay(cb_auto_video!!.isChecked())
                                .isLoopAutoVideoPlay(cb_auto_video!!.isChecked())
                                .isUseSystemVideoPlayer(isUseSystemPlayer)
                                .isPageSyncAlbumCount(true)
                                .setCustomLoadingListener(getCustomLoadingListener())
                                .setQueryFilterListener(object : OnQueryFilterListener {
                                    override fun onFilter(media: LocalMedia?): Boolean {
                                        return false
                                    }
                                }) //.setExtendLoaderEngine(getExtendLoaderEngine())
                                .setInjectLayoutResourceListener(getInjectLayoutResource())
                                .setSelectionMode(if (cb_choose_mode!!.isChecked()) SelectModeConfig.MULTIPLE else SelectModeConfig.SINGLE)
                                .setLanguage(language)
                                .setQuerySortOrder(if (cb_query_sort_order!!.isChecked()) MediaStore.MediaColumns.DATE_MODIFIED + " ASC" else "")
                                .setOutputCameraDir(
                                    if (chooseMode == SelectMimeType.ofAudio())
                                        getSandboxAudioOutputPath()
                                    else
                                        getSandboxCameraOutputPath()
                                )
                                .setOutputAudioDir(
                                    if (chooseMode == SelectMimeType.ofAudio())
                                        getSandboxAudioOutputPath()
                                    else
                                        getSandboxCameraOutputPath()
                                )
                                .setQuerySandboxDir(
                                    if (chooseMode == SelectMimeType.ofAudio())
                                        getSandboxAudioOutputPath()
                                    else
                                        getSandboxCameraOutputPath()
                                )
                                .isDisplayTimeAxis(cb_time_axis!!.isChecked())
                                .isOnlyObtainSandboxDir(cb_only_dir!!.isChecked())
                                .isPageStrategy(cbPage!!.isChecked())
                                .isOriginalControl(cb_original!!.isChecked())
                                .isDisplayCamera(cb_isCamera!!.isChecked())
                                .isOpenClickSound(cb_voice!!.isChecked())
                                .setSkipCropMimeType(*getNotSupportCrop() ?: emptyArray())
                                .isFastSlidingSelect(cb_fast_select!!.isChecked()) //.setOutputCameraImageFileName("luck.jpeg")
                                //.setOutputCameraVideoFileName("luck.mp4")
                                .isWithSelectVideoImage(cb_WithImageVideo!!.isChecked())
                                .isPreviewFullScreenMode(cb_preview_full!!.isChecked())
                                .isVideoPauseResumePlay(cb_video_resume!!.isChecked())
                                .isPreviewZoomEffect(cb_preview_scale!!.isChecked())
                                .isPreviewImage(cb_preview_img!!.isChecked())
                                .isPreviewVideo(cb_preview_video!!.isChecked())
                                .isPreviewAudio(cb_preview_audio!!.isChecked())
                                .setGridItemSelectAnimListener(if (cb_selected_anim!!.isChecked()) object :
                                    OnGridItemSelectAnimListener {
                                    override fun onSelectItemAnim(
                                        view: View?,
                                        isSelected: Boolean
                                    ) {
                                        val set = AnimatorSet()
                                        set.playTogether(
                                            ObjectAnimator.ofFloat(
                                                view,
                                                "scaleX",
                                                if (isSelected) 1f else 1.12f,
                                                if (isSelected) 1.12f else 1.0f
                                            ),
                                            ObjectAnimator.ofFloat(
                                                view,
                                                "scaleY",
                                                if (isSelected) 1f else 1.12f,
                                                if (isSelected) 1.12f else 1.0f
                                            )
                                        )
                                        set.setDuration(350)
                                        set.start()
                                    }
                                } else null)
                                .setSelectAnimListener(if (cb_selected_anim!!.isChecked()) object :
                                    OnSelectAnimListener {
                                    override fun onSelectAnim(view: View): Long {
                                        val animation = AnimationUtils.loadAnimation(
                                            this@MainActivity,
                                            com.luck.picture.lib.R.anim.ps_anim_modal_in
                                        )
                                        view.startAnimation(animation)
                                        return animation.getDuration()
                                    }
                                } else null) //.setQueryOnlyMimeType(PictureMimeType.ofGIF())
                                .isMaxSelectEnabledMask(cbEnabledMask!!.isChecked())
                                .isDirectReturnSingle(cb_single_back!!.isChecked())
                                .setMaxSelectNum(maxSelectNum)
                                .setMaxVideoSelectNum(maxSelectVideoNum)
                                .setRecyclerAnimationMode(animationMode)
                                .isGif(cb_isGif!!.isChecked())
                                .setSelectedData(mAdapter!!.getData())
                        forSelectResult(selectionModel)
                    }
                } else {
                    // 单独拍照
                    val cameraModel: PictureSelectionCameraModel =
                        PictureSelector.create(this@MainActivity)
                            .openCamera(chooseMode)
                            .setCameraInterceptListener(getCustomCameraEvent())
                            .setRecordAudioInterceptListener(MeOnRecordAudioInterceptListener())
                            .setCropEngine(getCropFileEngine())
                            .setCompressEngine(getCompressFileEngine())
                            .setSelectLimitTipsListener(MeOnSelectLimitTipsListener())
                            .setAddBitmapWatermarkListener(getAddBitmapWatermarkListener())
                            .setVideoThumbnailListener(getVideoThumbnailEventListener())
                            .setCustomLoadingListener(getCustomLoadingListener())
                            .setLanguage(language)
                            .setSandboxFileEngine(MeSandboxFileEngine())
                            .isOriginalControl(cb_original!!.isChecked())
                            .setPermissionDescriptionListener(getPermissionDescriptionListener())
                            .setOutputAudioDir(getSandboxAudioOutputPath())
                            .setSelectedData(mAdapter!!.getData())
                    forOnlyCameraResult(cameraModel)
                }
            }
        })

        mAdapter!!.setItemLongClickListener(object : OnItemLongClickListener {
            override fun onItemLongClick(holder: RecyclerView.ViewHolder, position: Int, v: View) {
                val itemViewType = holder.getItemViewType()
                if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                    mItemTouchHelper.startDrag(holder)
                }
            }
        })
        // 绑定拖拽事件
        mItemTouchHelper.attachToRecyclerView(mRecyclerView)
        // 清除缓存
//        clearCache();
    }

    private fun getNotSupportCrop(): Array<String?>? {
        if (cb_skip_not_gif!!.isChecked()) {
            return arrayOf<String?>(
                PictureMimeType.ofGIF(),
                PictureMimeType.ofWEBP()
            )
        }
        return null
    }

    private val mItemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val itemViewType = viewHolder.getItemViewType()
            if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                viewHolder.itemView.setAlpha(0.7f)
            }
            return makeMovementFlags(
                (ItemTouchHelper.DOWN or ItemTouchHelper.UP
                        or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT), 0
            )
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            try {
                //得到item原来的position
                val fromPosition = viewHolder.getAbsoluteAdapterPosition()
                //得到目标position
                val toPosition = target.getAbsoluteAdapterPosition()
                val itemViewType = target.getItemViewType()
                if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                    if (fromPosition < toPosition) {
                        for (i in fromPosition..<toPosition) {
                            Collections.swap(mAdapter!!.getData(), i, i + 1)
                        }
                    } else {
                        for (i in fromPosition downTo toPosition + 1) {
                            Collections.swap(mAdapter!!.getData(), i, i - 1)
                        }
                    }
                    mAdapter!!.notifyItemMoved(fromPosition, toPosition)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dx: Float,
            dy: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemViewType = viewHolder.getItemViewType()
            if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                if (needScaleBig) {
                    needScaleBig = false
                    val animatorSet = AnimatorSet()
                    animatorSet.playTogether(
                        ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.0f, 1.1f),
                        ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.0f, 1.1f)
                    )
                    animatorSet.setDuration(50)
                    animatorSet.setInterpolator(LinearInterpolator())
                    animatorSet.start()
                    animatorSet.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            needScaleSmall = true
                        }
                    })
                }
                val targetDy = tvDeleteText!!.getTop() - viewHolder.itemView.getBottom()
                if (dy >= targetDy) {
                    //拖到删除处
                    mDragListener.deleteState(true)
                    if (isHasLiftDelete) {
                        //在删除处放手，则删除item
                        viewHolder.itemView.setVisibility(View.INVISIBLE)
                        mAdapter!!.delete(viewHolder.getAbsoluteAdapterPosition())
                        resetState()
                        return
                    }
                } else {
                    //没有到删除处
                    if (View.INVISIBLE == viewHolder.itemView.getVisibility()) {
                        //如果viewHolder不可见，则表示用户放手，重置删除区域状态
                        mDragListener.dragState(false)
                    }
                    mDragListener.deleteState(false)
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dx,
                    dy,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            val itemViewType =
                if (viewHolder != null) viewHolder.getItemViewType() else GridImageAdapter.TYPE_CAMERA
            if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                if (ItemTouchHelper.ACTION_STATE_DRAG == actionState) {
                    mDragListener.dragState(true)
                }
                super.onSelectedChanged(viewHolder, actionState)
            }
        }

        override fun getAnimationDuration(
            recyclerView: RecyclerView,
            animationType: Int,
            animateDx: Float,
            animateDy: Float
        ): Long {
            isHasLiftDelete = true
            return super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            val itemViewType = viewHolder.getItemViewType()
            if (itemViewType != GridImageAdapter.TYPE_CAMERA) {
                viewHolder.itemView.setAlpha(1.0f)
                if (needScaleSmall) {
                    needScaleSmall = false
                    val animatorSet = AnimatorSet()
                    animatorSet.playTogether(
                        ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.1f, 1.0f),
                        ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.1f, 1.0f)
                    )
                    animatorSet.setInterpolator(LinearInterpolator())
                    animatorSet.setDuration(50)
                    animatorSet.start()
                    animatorSet.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            needScaleBig = true
                        }
                    })
                }
                super.clearView(recyclerView, viewHolder)
                mAdapter!!.notifyItemChanged(viewHolder.getAbsoluteAdapterPosition())
                resetState()
            }
        }
    })

    private val mDragListener: DragListener = object : DragListener {
        override fun deleteState(isDelete: Boolean) {
            if (isDelete) {
                if (!TextUtils.equals(
                        getString(R.string.app_let_go_drag_delete),
                        tvDeleteText!!.getText()
                    )
                ) {
                    tvDeleteText!!.setText(getString(R.string.app_let_go_drag_delete))
                    tvDeleteText!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        R.drawable.ic_dump_delete,
                        0,
                        0
                    )
                }
            } else {
                if (!TextUtils.equals(
                        getString(R.string.app_drag_delete),
                        tvDeleteText!!.getText()
                    )
                ) {
                    tvDeleteText!!.setText(getString(R.string.app_drag_delete))
                    tvDeleteText!!.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,
                        R.drawable.ic_normal_delete,
                        0,
                        0
                    )
                }
            }
        }

        override fun dragState(isStart: Boolean) {
            if (isStart) {
                if (tvDeleteText!!.getAlpha() == 0f) {
                    val alphaAnimator = ObjectAnimator.ofFloat(tvDeleteText, "alpha", 0f, 1f)
                    alphaAnimator.setInterpolator(LinearInterpolator())
                    alphaAnimator.setDuration(120)
                    alphaAnimator.start()
                }
            } else {
                if (tvDeleteText!!.getAlpha() == 1f) {
                    val alphaAnimator = ObjectAnimator.ofFloat(tvDeleteText, "alpha", 1f, 0f)
                    alphaAnimator.setInterpolator(LinearInterpolator())
                    alphaAnimator.setDuration(120)
                    alphaAnimator.start()
                }
            }
        }
    }

    private fun forSystemResult(model: PictureSelectionSystemModel) {
        if (cb_attach_system_mode!!.isChecked()) {
            when (resultMode) {
                ACTIVITY_RESULT -> model.forSystemResultActivity(PictureConfig.REQUEST_CAMERA)
                CALLBACK_RESULT -> model.forSystemResultActivity(MeOnResultCallbackListener())
                else -> model.forSystemResultActivity(launcherResult)
            }
        } else {
            if (resultMode == CALLBACK_RESULT) {
                model.forSystemResult(MeOnResultCallbackListener())
            } else {
                model.forSystemResult()
            }
        }
    }

    private fun forSelectResult(model: PictureSelectionModel) {
        when (resultMode) {
            ACTIVITY_RESULT -> model.forResult(PictureConfig.CHOOSE_REQUEST)
            CALLBACK_RESULT -> model.forResult(MeOnResultCallbackListener())
            else -> model.forResult(launcherResult)
        }
    }

    private fun forOnlyCameraResult(model: PictureSelectionCameraModel) {
        if (cb_attach_camera_mode!!.isChecked()) {
            when (resultMode) {
                ACTIVITY_RESULT -> model.forResultActivity(PictureConfig.REQUEST_CAMERA)
                CALLBACK_RESULT -> model.forResultActivity(MeOnResultCallbackListener())
                else -> model.forResultActivity(launcherResult)
            }
        } else {
            if (resultMode == CALLBACK_RESULT) {
                model.forResult(MeOnResultCallbackListener())
            } else {
                model.forResult()
            }
        }
    }

    /**
     * 重置
     */
    private fun resetState() {
        isHasLiftDelete = false
        mDragListener.deleteState(false)
        mDragListener.dragState(false)
    }

    /**
     * 外部预览监听事件
     */
    private inner class MyExternalPreviewEventListener : OnExternalPreviewEventListener {
        override fun onPreviewDelete(position: Int) {
            mAdapter!!.remove(position)
            mAdapter!!.notifyItemRemoved(position)
        }

        override fun onLongPressDownload(context: Context?, media: LocalMedia?): Boolean {
            return false
        }
    }

    /**
     * 选择结果
     */
    private inner class MeOnResultCallbackListener : OnResultCallbackListener<LocalMedia?> {
        override fun onResult(result: ArrayList<LocalMedia?>) {
            val filteredResult = ArrayList<LocalMedia>()
            for (media in result) {
                if (media != null) {
                    filteredResult.add(media)
                }
            }
            analyticalSelectResults(filteredResult)
        }

        override fun onCancel() {
            Log.i(TAG, "PictureSelector Cancel")
        }
    }

    private fun getCompressFileEngine(): ImageFileCompressEngine? {
        return if (cb_compress!!.isChecked()) ImageFileCompressEngine() else null
    }

    @get:Deprecated("")
    private val compressEngine: ImageCompressEngine?
        /**
         * 压缩引擎
         *
         * @return
         */
        get() = if (cb_compress!!.isChecked()) ImageCompressEngine() else null

    private fun getCropFileEngine(): ImageFileCropEngine? {
        return if (cb_crop!!.isChecked()) ImageFileCropEngine() else null
    }

    private val cropEngine: ImageCropEngine?
        /**
         * 裁剪引擎
         *
         * @return
         */
        get() = if (cb_crop!!.isChecked()) ImageCropEngine() else null

    private fun getCustomCameraEvent(): OnCameraInterceptListener? {
        return if (cb_custom_camera!!.isChecked()) MeOnCameraInterceptListener() else null
    }


    private val extendLoaderEngine: ExtendLoaderEngine
        /**
         * 自定义数据加载器
         *
         * @return
         */
        get() = MeExtendLoaderEngine()


    private fun getInjectLayoutResource(): OnInjectLayoutResourceListener? {
        return if (cb_inject_layout!!.isChecked()) MeOnInjectLayoutResourceListener() else null
    }


    private fun getVideoThumbnailEventListener(): OnVideoThumbnailEventListener? {
        return if (cb_video_thumbnails!!.isChecked()) MeOnVideoThumbnailEventListener(getVideoThumbnailDir()) else null
    }

    /**
     * 处理视频缩略图
     */
    private class MeOnVideoThumbnailEventListener(private val targetPath: String?) :
        OnVideoThumbnailEventListener {
        override fun onVideoThumbnail(
            context: Context,
            videoPath: String?,
            call: OnKeyValueResultCallbackListener?
        ) {
            Glide.with(context).asBitmap().sizeMultiplier(0.6f).load(videoPath)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        val stream = ByteArrayOutputStream()
                        resource.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                        var fos: FileOutputStream? = null
                        var result: String? = null
                        try {
                            val targetFile = File(
                                targetPath,
                                "thumbnails_" + System.currentTimeMillis() + ".jpg"
                            )
                            fos = FileOutputStream(targetFile)
                            fos.write(stream.toByteArray())
                            fos.flush()
                            result = targetFile.getAbsolutePath()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            PictureFileUtils.close(fos)
                            PictureFileUtils.close(stream)
                        }
                        if (call != null) {
                            call.onCallback(videoPath, result)
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        if (call != null) {
                            call.onCallback(videoPath, "")
                        }
                    }
                })
        }
    }

    private fun getCustomLoadingListener(): OnCustomLoadingListener? {
        if (cb_custom_loading!!.isChecked()) {
            return object : OnCustomLoadingListener {
                override fun create(context: Context): Dialog {
                    return CustomLoadingDialog(context)
                }
            }
        }
        return null
    }

    private fun getAddBitmapWatermarkListener(): OnBitmapWatermarkEventListener? {
        return if (cb_watermark!!.isChecked()) MeBitmapWatermarkEventListener(getSandboxMarkDir()) else null
    }

    /**
     * 给图片添加水印
     */
    private class MeBitmapWatermarkEventListener(private val targetPath: String?) :
        OnBitmapWatermarkEventListener {
        override fun onAddBitmapWatermark(
            context: Context,
            srcPath: String?,
            mimeType: String?,
            call: OnKeyValueResultCallbackListener?
        ) {
            if (PictureMimeType.isHasHttp(srcPath) || PictureMimeType.isHasVideo(mimeType)) {
                // 网络图片和视频忽略，有需求的可自行扩展
                call!!.onCallback(srcPath, "")
            } else {
                // 暂时只以图片为例
                Glide.with(context).asBitmap().sizeMultiplier(0.6f).load(srcPath)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            val stream = ByteArrayOutputStream()
                            val watermark = BitmapFactory.decodeResource(
                                context.getResources(),
                                R.drawable.ic_mark_win
                            )
                            val watermarkBitmap =
                                createWaterMaskRightTop(context, resource, watermark, 15, 15)
                            watermarkBitmap!!.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                            watermarkBitmap.recycle()
                            var fos: FileOutputStream? = null
                            var result: String? = null
                            try {
                                val targetFile =
                                    File(targetPath, DateUtils.getCreateFileName("Mark_") + ".jpg")
                                fos = FileOutputStream(targetFile)
                                fos.write(stream.toByteArray())
                                fos.flush()
                                result = targetFile.getAbsolutePath()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            } finally {
                                PictureFileUtils.close(fos)
                                PictureFileUtils.close(stream)
                            }
                            if (call != null) {
                                call.onCallback(srcPath, result)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            if (call != null) {
                                call.onCallback(srcPath, "")
                            }
                        }
                    })
            }
        }
    }


    private fun getPermissionDeniedListener(): OnPermissionDeniedListener? {
        return if (cb_permission_desc!!.isChecked()) MeOnPermissionDeniedListener() else null
    }


    /**
     * 权限拒绝后回调
     */
    private class MeOnPermissionDeniedListener : OnPermissionDeniedListener {
        override fun onDenied(
            fragment: Fragment, permissionArray: Array<String?>,
            requestCode: Int, call: OnCallbackListener<Boolean?>?
        ) {
            val tips: String?
            if (TextUtils.equals(permissionArray[0], PermissionConfig.CAMERA[0])) {
                tips = "缺少相机权限\n可能会导致不能使用摄像头功能"
            } else if (TextUtils.equals(permissionArray[0], Manifest.permission.RECORD_AUDIO)) {
                tips = "缺少录音权限\n访问您设备上的音频、媒体内容和文件"
            } else {
                tips = "缺少存储权限\n访问您设备上的照片、媒体内容和文件"
            }
            val dialog = RemindDialog.buildDialog(fragment.getContext(), tips)
            dialog.setButtonText("去设置")
            dialog.setButtonTextColor(-0x828201)
            dialog.setContentTextColor(-0xcccccd)
            dialog.setOnDialogClickListener(object : OnDialogClickListener {
                override fun onClick(view: View?) {
                    PermissionUtil.goIntentSetting(fragment, requestCode)
                    dialog.dismiss()
                }
            })
            dialog.show()
        }
    }

    private fun getSimpleXPermissionDeniedListener(): OnSimpleXPermissionDeniedListener? {
        return if (cb_permission_desc!!.isChecked()) MeOnSimpleXPermissionDeniedListener() else null
    }

    /**
     * SimpleCameraX添加权限说明
     */
    private class MeOnSimpleXPermissionDeniedListener : OnSimpleXPermissionDeniedListener {
        override fun onDenied(context: Context?, permission: String?, requestCode: Int) {
            val tips: String?
            if (TextUtils.equals(permission, Manifest.permission.RECORD_AUDIO)) {
                tips = "缺少麦克风权限\n可能会导致录视频无法采集声音"
            } else {
                tips = "缺少相机权限\n可能会导致不能使用摄像头功能"
            }
            val dialog = RemindDialog.buildDialog(context, tips)
            dialog.setButtonText("去设置")
            dialog.setButtonTextColor(-0x828201)
            dialog.setContentTextColor(-0xcccccd)
            dialog.setOnDialogClickListener(object : OnDialogClickListener {
                override fun onClick(view: View?) {
                    SimpleXPermissionUtil.goIntentSetting(context as Activity?, requestCode)
                    dialog.dismiss()
                }
            })
            dialog.show()
        }
    }

    private fun getSimpleXPermissionDescriptionListener(): OnSimpleXPermissionDescriptionListener? {
        return if (cb_permission_desc!!.isChecked()) MeOnSimpleXPermissionDescriptionListener() else null
    }

    /**
     * SimpleCameraX添加权限说明
     */
    private class MeOnSimpleXPermissionDescriptionListener :
        OnSimpleXPermissionDescriptionListener {
        override fun onPermissionDescription(
            context: Context?,
            viewGroup: ViewGroup,
            permission: String?
        ) {
            addPermissionDescription(true, viewGroup, arrayOf<String?>(permission))
        }

        override fun onDismiss(viewGroup: ViewGroup) {
            removePermissionDescription(viewGroup)
        }
    }


    private fun getPermissionDescriptionListener(): OnPermissionDescriptionListener? {
        return if (cb_permission_desc!!.isChecked()) MeOnPermissionDescriptionListener() else null
    }

    /**
     * 添加权限说明
     */
    private class MeOnPermissionDescriptionListener : OnPermissionDescriptionListener {
        override fun onPermissionDescription(fragment: Fragment, permissionArray: Array<String?>) {
            val rootView = fragment.requireView()
            if (rootView is ViewGroup) {
                addPermissionDescription(false, rootView, permissionArray)
            }
        }

        override fun onDismiss(fragment: Fragment) {
            removePermissionDescription(fragment.requireView() as ViewGroup)
        }
    }

    private fun getPreviewInterceptListener(): OnPreviewInterceptListener? {
        return if (cb_custom_preview!!.isChecked()) MeOnPreviewInterceptListener() else null
    }

    /**
     * 自定义预览
     *
     * @return
     */
    private class MeOnPreviewInterceptListener : OnPreviewInterceptListener {
        override fun onPreview(
            context: Context?,
            position: Int,
            totalNum: Int,
            page: Int,
            currentBucketId: Long,
            currentAlbumName: String?,
            isShowCamera: Boolean,
            data: ArrayList<LocalMedia?>?,
            isBottomPreview: Boolean
        ) {
            val previewFragment = newInstance()
            previewFragment.setInternalPreviewData(
                isBottomPreview, currentAlbumName, isShowCamera,
                position, totalNum, page, currentBucketId, data
            )
            FragmentInjectManager.injectFragment(
                context as FragmentActivity?,
                previewFragment.getFragmentTag(),
                previewFragment
            )
        }
    }

    /**
     * 拦截自定义提示
     */
    private class MeOnSelectLimitTipsListener : OnSelectLimitTipsListener {
        override fun onSelectLimitTips(
            context: Context?,
            media: LocalMedia?,
            config: SelectorConfig,
            limitType: Int
        ): Boolean {
            if (limitType == SelectLimitType.SELECT_MIN_SELECT_LIMIT) {
                ToastUtils.showToast(context, "图片最少不能低于" + config.minSelectNum + "张")
                return true
            } else if (limitType == SelectLimitType.SELECT_MIN_VIDEO_SELECT_LIMIT) {
                ToastUtils.showToast(context, "视频最少不能低于" + config.minVideoSelectNum + "个")
                return true
            } else if (limitType == SelectLimitType.SELECT_MIN_AUDIO_SELECT_LIMIT) {
                ToastUtils.showToast(context, "音频最少不能低于" + config.minAudioSelectNum + "个")
                return true
            }
            return false
        }
    }

    /**
     * 注入自定义布局UI，前提是布局View id 和 根目录Layout必须一致
     */
    private class MeOnInjectLayoutResourceListener : OnInjectLayoutResourceListener {
        override fun getLayoutResourceId(context: Context?, resourceSource: Int): Int {
            when (resourceSource) {
                InjectResourceSource.MAIN_SELECTOR_LAYOUT_RESOURCE -> return R.layout.ps_custom_fragment_selector
                InjectResourceSource.PREVIEW_LAYOUT_RESOURCE -> return R.layout.ps_custom_fragment_preview
                InjectResourceSource.MAIN_ITEM_IMAGE_LAYOUT_RESOURCE -> return R.layout.ps_custom_item_grid_image
                InjectResourceSource.MAIN_ITEM_VIDEO_LAYOUT_RESOURCE -> return R.layout.ps_custom_item_grid_video
                InjectResourceSource.MAIN_ITEM_AUDIO_LAYOUT_RESOURCE -> return R.layout.ps_custom_item_grid_audio
                InjectResourceSource.ALBUM_ITEM_LAYOUT_RESOURCE -> return R.layout.ps_custom_album_folder_item
                InjectResourceSource.PREVIEW_ITEM_IMAGE_LAYOUT_RESOURCE -> return R.layout.ps_custom_preview_image
                InjectResourceSource.PREVIEW_ITEM_VIDEO_LAYOUT_RESOURCE -> return R.layout.ps_custom_preview_video
                InjectResourceSource.PREVIEW_GALLERY_ITEM_LAYOUT_RESOURCE -> return R.layout.ps_custom_preview_gallery_item
                else -> return 0
            }
        }
    }

    /**
     * 自定义数据加载器
     */
    private inner class MeExtendLoaderEngine : ExtendLoaderEngine {
        override fun loadAllAlbumData(
            context: Context?,
            query: OnQueryAllAlbumListener<LocalMediaFolder?>
        ) {
            val folder = SandboxFileLoader
                .loadInAppSandboxFolderFile(context, this@MainActivity.getSandboxPath())
            val folders: MutableList<LocalMediaFolder?> = ArrayList<LocalMediaFolder?>()
            folders.add(folder)
            query.onComplete(folders)
        }

        override fun loadOnlyInAppDirAllMediaData(
            context: Context?,
            query: OnQueryAlbumListener<LocalMediaFolder?>
        ) {
            val folder = SandboxFileLoader
                .loadInAppSandboxFolderFile(context, this@MainActivity.getSandboxPath())
            query.onComplete(folder)
        }

        override fun loadFirstPageMediaData(
            context: Context?,
            bucketId: Long,
            page: Int,
            pageSize: Int,
            query: OnQueryDataResultListener<LocalMedia?>
        ) {
            val folder = SandboxFileLoader
                .loadInAppSandboxFolderFile(context, this@MainActivity.getSandboxPath())
            query.onComplete(folder.getData(), false)
        }

        override fun loadMoreMediaData(
            context: Context?,
            bucketId: Long,
            page: Int,
            limit: Int,
            pageSize: Int,
            query: OnQueryDataResultListener<LocalMedia?>?
        ) {
        }
    }

    private fun getCustomEditMediaEvent(): OnMediaEditInterceptListener? {
        return if (cbEditor!!.isChecked()) MeOnMediaEditInterceptListener(
            getSandboxPath(),
            buildOptions()
        ) else null
    }


    /**
     * 自定义编辑
     */
    private class MeOnMediaEditInterceptListener(
        private val outputCropPath: String?,
        private val options: UCrop.Options
    ) : OnMediaEditInterceptListener {
        override fun onStartMediaEdit(
            fragment: Fragment,
            currentLocalMedia: LocalMedia,
            requestCode: Int
        ) {
            val currentEditPath = currentLocalMedia.getAvailablePath()
            val inputUri = if (PictureMimeType.isContent(currentEditPath))
                Uri.parse(currentEditPath)
            else
                Uri.fromFile(File(currentEditPath))
            val destinationUri = Uri.fromFile(
                File(outputCropPath, DateUtils.getCreateFileName("CROP_") + ".jpeg")
            )
            val uCrop = UCrop.of<Any?>(inputUri, destinationUri)
            options.setHideBottomControls(false)
            uCrop.withOptions(options)
            uCrop.setImageEngine(object : UCropImageEngine {
                override fun loadImage(context: Context, url: String?, imageView: ImageView) {
                    if (!assertValidRequest(context)) {
                        return
                    }
                    Glide.with(context).load(url).override(180, 180).into(imageView)
                }

                override fun loadImage(
                    context: Context,
                    url: Uri?,
                    maxWidth: Int,
                    maxHeight: Int,
                    call: UCropImageEngine.OnCallbackListener<Bitmap?>?
                ) {
                    Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight)
                        .into(object : CustomTarget<Bitmap?>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap?>?
                            ) {
                                if (call != null) {
                                    call.onCall(resource)
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                if (call != null) {
                                    call.onCall(null)
                                }
                            }
                        })
                }
            })
            uCrop.startEdit(fragment.requireActivity(), fragment, requestCode)
        }
    }

    /**
     * 录音回调事件
     */
    private class MeOnRecordAudioInterceptListener : OnRecordAudioInterceptListener {
        override fun onRecordAudio(fragment: Fragment, requestCode: Int) {
            val recordAudio = arrayOf<String?>(Manifest.permission.RECORD_AUDIO)
            if (PermissionChecker.isCheckSelfPermission(fragment.getContext(), recordAudio)) {
                startRecordSoundAction(fragment, requestCode)
            } else {
                addPermissionDescription(false, fragment.requireView() as ViewGroup, recordAudio)
                PermissionChecker.getInstance().requestPermissions(
                    fragment,
                    arrayOf<String>(Manifest.permission.RECORD_AUDIO),
                    object : PermissionResultCallback {
                        override fun onGranted() {
                            removePermissionDescription(fragment.requireView() as ViewGroup)
                            startRecordSoundAction(fragment, requestCode)
                        }

                        override fun onDenied() {
                            removePermissionDescription(fragment.requireView() as ViewGroup)
                        }
                    })
            }
        }
    }

    /**
     * 自定义拍照
     */
    private inner class MeOnCameraInterceptListener : OnCameraInterceptListener {
        override fun openCamera(fragment: Fragment, cameraMode: Int, requestCode: Int) {
            val camera = SimpleCameraX.of()
            camera.isAutoRotation(true)
            camera.setCameraMode(cameraMode)
            camera.setVideoFrameRate(25)
            camera.setVideoBitRate(3 * 1024 * 1024)
            camera.isDisplayRecordChangeTime(true)
            camera.isManualFocusCameraPreview(cb_camera_focus!!.isChecked())
            camera.isZoomCameraPreview(cb_camera_zoom!!.isChecked())
            camera.setOutputPathDir(this@MainActivity.getSandboxCameraOutputPath())
            camera.setPermissionDeniedListener(this@MainActivity.getSimpleXPermissionDeniedListener())
            camera.setPermissionDescriptionListener(this@MainActivity.getSimpleXPermissionDescriptionListener())
            camera.setImageEngine(object : CameraImageEngine {
                override fun loadImage(context: Context, url: String?, imageView: ImageView) {
                    Glide.with(context).load(url).into(imageView)
                }
            })
            camera.start(fragment.requireActivity(), fragment, requestCode)
        }
    }

    /**
     * 自定义沙盒文件处理
     */
    private class MeSandboxFileEngine : UriToFileTransformEngine {
        override fun onUriToFileAsyncTransform(
            context: Context?,
            srcPath: String?,
            mineType: String?,
            call: OnKeyValueResultCallbackListener?
        ) {
            if (call != null) {
                call.onCallback(
                    srcPath,
                    SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType)
                )
            }
        }
    }

    /**
     * 自定义裁剪
     */
    private inner class ImageFileCropEngine : CropFileEngine {
        override fun onStartCrop(
            fragment: Fragment,
            srcUri: Uri,
            destinationUri: Uri,
            dataSource: ArrayList<String?>?,
            requestCode: Int
        ) {
            val options = buildOptions()
            val uCrop = UCrop.of(srcUri, destinationUri, dataSource)
            uCrop.withOptions(options)
            uCrop.setImageEngine(object : UCropImageEngine {
                override fun loadImage(context: Context, url: String?, imageView: ImageView) {
                    if (!assertValidRequest(context)) {
                        return
                    }
                    Glide.with(context).load(url).override(180, 180).into(imageView)
                }

                override fun loadImage(
                    context: Context,
                    url: Uri?,
                    maxWidth: Int,
                    maxHeight: Int,
                    call: UCropImageEngine.OnCallbackListener<Bitmap?>?
                ) {
                    Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight)
                        .into(object : CustomTarget<Bitmap?>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap?>?
                            ) {
                                if (call != null) {
                                    call.onCall(resource)
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                if (call != null) {
                                    call.onCall(null)
                                }
                            }
                        })
                }
            })
            uCrop.start(fragment.requireActivity(), fragment, requestCode)
        }
    }

    /**
     * 自定义裁剪
     */
    private inner class ImageCropEngine : CropEngine {
        override fun onStartCrop(
            fragment: Fragment, currentLocalMedia: LocalMedia,
            dataSource: ArrayList<LocalMedia>, requestCode: Int
        ) {
            val currentCropPath = currentLocalMedia.getAvailablePath()
            val inputUri: Uri
            if (PictureMimeType.isContent(currentCropPath) || PictureMimeType.isHasHttp(
                    currentCropPath
                )
            ) {
                inputUri = Uri.parse(currentCropPath)
            } else {
                inputUri = Uri.fromFile(File(currentCropPath))
            }
            val fileName = DateUtils.getCreateFileName("CROP_") + ".jpg"
            val destinationUri = Uri.fromFile(File(this@MainActivity.getSandboxPath(), fileName))
            val options = buildOptions()
            val dataCropSource = ArrayList<String?>()
            for (i in dataSource.indices) {
                val media = dataSource.get(i)
                dataCropSource.add(media.getAvailablePath())
            }
            val uCrop = UCrop.of(inputUri, destinationUri, dataCropSource)
            //options.setMultipleCropAspectRatio(buildAspectRatios(dataSource.size()));
            uCrop.withOptions(options)
            uCrop.setImageEngine(object : UCropImageEngine {
                override fun loadImage(context: Context, url: String?, imageView: ImageView) {
                    if (!assertValidRequest(context)) {
                        return
                    }
                    Glide.with(context).load(url).override(180, 180).into(imageView)
                }

                override fun loadImage(
                    context: Context?,
                    url: Uri?,
                    maxWidth: Int,
                    maxHeight: Int,
                    call: UCropImageEngine.OnCallbackListener<Bitmap?>?
                ) {
                }
            })
            uCrop.start(fragment.requireActivity(), fragment, requestCode)
        }
    }


    /**
     * 多图裁剪时每张对应的裁剪比例
     *
     * @param dataSourceCount
     * @return
     */
    private fun buildAspectRatios(dataSourceCount: Int): Array<AspectRatio?> {
        val aspectRatios = arrayOfNulls<AspectRatio>(dataSourceCount)
        for (i in 0..<dataSourceCount) {
            if (i == 0) {
                aspectRatios[i] = AspectRatio("16:9", 16f, 9f)
            } else if (i == 1) {
                aspectRatios[i] = AspectRatio("3:2", 3f, 2f)
            } else {
                aspectRatios[i] = AspectRatio("原始比例", 0f, 0f)
            }
        }
        return aspectRatios
    }

    /**
     * 配制UCrop，可根据需求自我扩展
     *
     * @return
     */
    private fun buildOptions(): UCrop.Options {
        val options = UCrop.Options()
        options.setHideBottomControls(!cb_hide!!.isChecked())
        options.setFreeStyleCropEnabled(cb_styleCrop!!.isChecked())
        options.setShowCropFrame(cb_showCropFrame!!.isChecked())
        options.setShowCropGrid(cb_showCropGrid!!.isChecked())
        options.setCircleDimmedLayer(cb_crop_circular!!.isChecked())
        options.withAspectRatio(aspect_ratio_x.toFloat(), aspect_ratio_y.toFloat())
        options.setCropOutputPathDir(getSandboxPath())
        options.isCropDragSmoothToCenter(false)
        options.setSkipCropMimeType(*getNotSupportCrop() ?: emptyArray())
        options.isForbidCropGifWebp(cb_not_gif!!.isChecked())
        options.isForbidSkipMultipleCrop(true)
        options.setMaxScaleMultiplier(100f)
        if (selectorStyle != null && selectorStyle!!.getSelectMainStyle()
                .getStatusBarColor() != 0
        ) {
            val mainStyle = selectorStyle!!.getSelectMainStyle()
            val isDarkStatusBarBlack = mainStyle.isDarkStatusBarBlack()
            val statusBarColor = mainStyle.getStatusBarColor()
            options.isDarkStatusBarBlack(isDarkStatusBarBlack)
            if (StyleUtils.checkStyleValidity(statusBarColor)) {
                options.setStatusBarColor(statusBarColor)
                options.setToolbarColor(statusBarColor)
            } else {
                options.setStatusBarColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        com.luck.picture.lib.R.color.ps_color_grey
                    )
                )
                options.setToolbarColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        com.luck.picture.lib.R.color.ps_color_grey
                    )
                )
            }
            val titleBarStyle = selectorStyle!!.getTitleBarStyle()
            if (StyleUtils.checkStyleValidity(titleBarStyle.getTitleTextColor())) {
                options.setToolbarWidgetColor(titleBarStyle.getTitleTextColor())
            } else {
                options.setToolbarWidgetColor(
                    ContextCompat.getColor(
                        this.context,
                        com.luck.picture.lib.R.color.ps_color_white
                    )
                )
            }
        } else {
            options.setStatusBarColor(
                ContextCompat.getColor(
                    this.context,
                    com.luck.picture.lib.R.color.ps_color_grey
                )
            )
            options.setToolbarColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    com.luck.picture.lib.R.color.ps_color_grey
                )
            )
            options.setToolbarWidgetColor(
                ContextCompat.getColor(
                    this@MainActivity,
                    com.luck.picture.lib.R.color.ps_color_white
                )
            )
        }
        return options
    }

    /**
     * 自定义压缩
     */
    private class ImageFileCompressEngine : CompressFileEngine {
        override fun onStartCompress(
            context: Context,
            source: ArrayList<Uri?>,
            call: OnKeyValueResultCallbackListener?
        ) {
            with(context).load<Uri?>(source).ignoreBy(100)
                .setRenameListener(object : OnRenameListener {
                    override fun rename(filePath: String): String {
                        val indexOf = filePath.lastIndexOf(".")
                        val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
                        return DateUtils.getCreateFileName("CMP_") + postfix
                    }
                }).filter(object : CompressionPredicate {
                    override fun apply(path: String): Boolean {
                        if (PictureMimeType.isUrlHasImage(path) && !PictureMimeType.isHasHttp(path)) {
                            return true
                        }
                        return !PictureMimeType.isUrlHasGif(path)
                    }
                }).setCompressListener(object : OnNewCompressListener {
                    override fun onStart() {
                    }

                    override fun onSuccess(source: String, compressFile: File) {
                        call?.onCallback(source, compressFile.getAbsolutePath())
                    }

                    override fun onError(source: String, e: Throwable) {
                        call?.onCallback(source, null)
                    }
                }).launch()
        }
    }


    /**
     * 自定义压缩
     */
    @Deprecated("")
    private class ImageCompressEngine : CompressEngine {
        override fun onStartCompress(
            context: Context, list: ArrayList<LocalMedia>,
            listener: OnCallbackListener<ArrayList<LocalMedia>>
        ) {
            // 自定义压缩
            val compress: MutableList<Uri?> = ArrayList<Uri?>()
            for (i in list.indices) {
                val media = list.get(i)
                val availablePath = media.getAvailablePath()
                val uri = if (PictureMimeType.isContent(availablePath) || PictureMimeType.isHasHttp(
                        availablePath
                    )
                )
                    Uri.parse(availablePath)
                else
                    Uri.fromFile(File(availablePath))
                compress.add(uri)
            }
            if (compress.isEmpty()) {
                listener.onCall(list)
                return
            }
            with(context)
                .load<Uri?>(compress)
                .ignoreBy(100)
                .filter(object : CompressionPredicate {
                    override fun apply(path: String): Boolean {
                        return PictureMimeType.isUrlHasImage(path) && !PictureMimeType.isHasHttp(
                            path
                        )
                    }
                })
                .setRenameListener(object : OnRenameListener {
                    override fun rename(filePath: String): String {
                        val indexOf = filePath.lastIndexOf(".")
                        val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
                        return DateUtils.getCreateFileName("CMP_") + postfix
                    }
                })
                .setCompressListener(object : OnCompressListener {
                    override fun onStart() {
                    }

                    override fun onSuccess(index: Int, compressFile: File) {
                        val media = list.get(index)
                        if (compressFile.exists() && !TextUtils.isEmpty(compressFile.getAbsolutePath())) {
                            media.setCompressed(true)
                            media.setCompressPath(compressFile.getAbsolutePath())
                            media.setSandboxPath(if (SdkVersionUtils.isQ()) media.getCompressPath() else null)
                        }
                        if (index == list.size - 1) {
                            listener.onCall(list)
                        }
                    }

                    override fun onError(index: Int, e: Throwable) {
                        if (index != -1) {
                            val media = list.get(index)
                            media.setCompressed(false)
                            media.setCompressPath(null)
                            media.setSandboxPath(null)
                            if (index == list.size - 1) {
                                listener.onCall(list)
                            }
                        }
                    }
                }).launch()
        }
    }

    private fun getSandboxCameraOutputPath(): String {
        if (cb_custom_sandbox!!.isChecked()) {
            val externalFilesDir = getExternalFilesDir("")
            val customFile =
                File(externalFilesDir!!.getAbsolutePath(), "Sandbox")
            if (!customFile.exists()) {
                customFile.mkdirs()
            }
            return customFile.getAbsolutePath() + File.separator
        } else {
            return ""
        }
    }

    private fun getSandboxAudioOutputPath(): String {
        if (cb_custom_sandbox!!.isChecked()) {
            val externalFilesDir = getExternalFilesDir("")
            val customFile =
                File(externalFilesDir!!.getAbsolutePath(), "Sound")
            if (!customFile.exists()) {
                customFile.mkdirs()
            }
            return customFile.getAbsolutePath() + File.separator
        } else {
            return ""
        }
    }

    private fun getSandboxPath(): String {
        val externalFilesDir = getExternalFilesDir("")
        val customFile =
            File(externalFilesDir!!.getAbsolutePath(), "Sandbox")
        if (!customFile.exists()) {
            customFile.mkdirs()
        }
        return customFile.getAbsolutePath() + File.separator
    }

    private fun getSandboxMarkDir(): String {
        val externalFilesDir = getExternalFilesDir("")
        val customFile = File(externalFilesDir!!.getAbsolutePath(), "Mark")
        if (!customFile.exists()) {
            customFile.mkdirs()
        }
        return customFile.getAbsolutePath() + File.separator
    }

    private fun getVideoThumbnailDir(): String {
        val externalFilesDir = getExternalFilesDir("")
        val customFile =
            File(externalFilesDir!!.getAbsolutePath(), "Thumbnail")
        if (!customFile.exists()) {
            customFile.mkdirs()
        }
        return customFile.getAbsolutePath() + File.separator
    }

    override fun onClick(v: View) {
        if (v.getId() == R.id.left_back) {
            finish()
        } else if (v.getId() == R.id.minus) {
            if (maxSelectNum > 1) {
                maxSelectNum--
            }
            tv_select_num!!.setText(maxSelectNum.toString())
            mAdapter!!.setSelectMax(maxSelectNum + maxSelectVideoNum)
        } else if (v.getId() == R.id.plus) {
            maxSelectNum++
            tv_select_num!!.setText(maxSelectNum.toString())
            mAdapter!!.setSelectMax(maxSelectNum + maxSelectVideoNum)
        } else if (v.getId() == R.id.video_minus) {
            if (maxSelectVideoNum > 1) {
                maxSelectVideoNum--
            }
            tv_select_video_num!!.setText(maxSelectVideoNum.toString())
            mAdapter!!.setSelectMax(maxSelectVideoNum + maxSelectNum)
        } else if (v.getId() == R.id.video_plus) {
            maxSelectVideoNum++
            tv_select_video_num!!.setText(maxSelectVideoNum.toString())
            mAdapter!!.setSelectMax(maxSelectVideoNum + maxSelectNum)
        }
    }

    override fun onCheckedChanged(group: RadioGroup?, @IdRes checkedId: Int) {
        if (checkedId == R.id.rb_all) {
            chooseMode = SelectMimeType.ofAll()
            cb_preview_img!!.setChecked(true)
            cb_preview_video!!.setChecked(true)
            cb_isGif!!.setChecked(false)
            cb_preview_video!!.setChecked(true)
            cb_preview_img!!.setChecked(true)
            cb_preview_video!!.setVisibility(View.VISIBLE)
            cb_preview_img!!.setVisibility(View.VISIBLE)
            llSelectVideoSize!!.setVisibility(View.VISIBLE)
            cb_compress!!.setVisibility(View.VISIBLE)
            cb_crop!!.setVisibility(View.VISIBLE)
            cb_isGif!!.setVisibility(View.VISIBLE)
            cb_preview_audio!!.setVisibility(View.GONE)
        } else if (checkedId == R.id.rb_image) {
            llSelectVideoSize!!.setVisibility(View.GONE)
            chooseMode = SelectMimeType.ofImage()
            cb_preview_img!!.setChecked(true)
            cb_preview_video!!.setChecked(false)
            cb_isGif!!.setChecked(false)
            cb_preview_video!!.setChecked(false)
            cb_preview_video!!.setVisibility(View.GONE)
            cb_preview_img!!.setChecked(true)
            cb_preview_audio!!.setVisibility(View.GONE)
            cb_preview_img!!.setVisibility(View.VISIBLE)
            cb_compress!!.setVisibility(View.VISIBLE)
            cb_crop!!.setVisibility(View.VISIBLE)
            cb_isGif!!.setVisibility(View.VISIBLE)
        } else if (checkedId == R.id.rb_video) {
            llSelectVideoSize!!.setVisibility(View.GONE)
            chooseMode = SelectMimeType.ofVideo()
            cb_preview_img!!.setChecked(false)
            cb_preview_video!!.setChecked(true)
            cb_isGif!!.setChecked(false)
            cb_isGif!!.setVisibility(View.GONE)
            cb_preview_video!!.setChecked(true)
            cb_preview_video!!.setVisibility(View.VISIBLE)
            cb_preview_img!!.setVisibility(View.GONE)
            cb_preview_img!!.setChecked(false)
            cb_compress!!.setVisibility(View.GONE)
            cb_preview_audio!!.setVisibility(View.GONE)
            cb_crop!!.setVisibility(View.GONE)
        } else if (checkedId == R.id.rb_audio) {
            chooseMode = SelectMimeType.ofAudio()
            cb_preview_audio!!.setVisibility(View.VISIBLE)
        } else if (checkedId == R.id.rb_glide) {
            imageEngine = createGlideEngine()
        } else if (checkedId == R.id.rb_picasso) {
            imageEngine = createPicassoEngine()
        } else if (checkedId == R.id.rb_coil) {
            imageEngine = CoilEngine()
        } else if (checkedId == R.id.rb_media_player) {
            videoPlayerEngine = null
            isUseSystemPlayer = false
        } else if (checkedId == R.id.rb_exo_player) {
            videoPlayerEngine = ExoPlayerEngine()
            isUseSystemPlayer = false
        } else if (checkedId == R.id.rb_ijk_player) {
            videoPlayerEngine = IjkPlayerEngine()
            isUseSystemPlayer = false
        } else if (checkedId == R.id.rb_system_player) {
            isUseSystemPlayer = true
        } else if (checkedId == R.id.rb_system) {
            language = LanguageConfig.SYSTEM_LANGUAGE
        } else if (checkedId == R.id.rb_jpan) {
            language = LanguageConfig.JAPAN
        } else if (checkedId == R.id.rb_tw) {
            language = LanguageConfig.TRADITIONAL_CHINESE
        } else if (checkedId == R.id.rb_us) {
            language = LanguageConfig.ENGLISH
        } else if (checkedId == R.id.rb_ka) {
            language = LanguageConfig.KOREA
        } else if (checkedId == R.id.rb_de) {
            language = LanguageConfig.GERMANY
        } else if (checkedId == R.id.rb_fr) {
            language = LanguageConfig.FRANCE
        } else if (checkedId == R.id.rb_spanish) {
            language = LanguageConfig.SPANISH
        } else if (checkedId == R.id.rb_portugal) {
            language = LanguageConfig.PORTUGAL
        } else if (checkedId == R.id.rb_ar) {
            language = LanguageConfig.AR
        } else if (checkedId == R.id.rb_ru) {
            language = LanguageConfig.RU
        } else if (checkedId == R.id.rb_cs) {
            language = LanguageConfig.CS
        } else if (checkedId == R.id.rb_kk) {
            language = LanguageConfig.KK
        } else if (checkedId == R.id.rb_crop_default) {
            aspect_ratio_x = -1
            aspect_ratio_y = -1
        } else if (checkedId == R.id.rb_crop_1to1) {
            aspect_ratio_x = 1
            aspect_ratio_y = 1
        } else if (checkedId == R.id.rb_crop_3to4) {
            aspect_ratio_x = 3
            aspect_ratio_y = 4
        } else if (checkedId == R.id.rb_crop_3to2) {
            aspect_ratio_x = 3
            aspect_ratio_y = 2
        } else if (checkedId == R.id.rb_crop_16to9) {
            aspect_ratio_x = 16
            aspect_ratio_y = 9
        } else if (checkedId == R.id.rb_launcher_result) {
            resultMode = 0
        } else if (checkedId == R.id.rb_activity_result) {
            resultMode = 1
        } else if (checkedId == R.id.rb_callback_result) {
            resultMode = 2
        } else if (checkedId == R.id.rb_photo_default_animation) {
            val defaultAnimationStyle = PictureWindowAnimationStyle()
            defaultAnimationStyle.setActivityEnterAnimation(com.luck.picture.lib.R.anim.ps_anim_enter)
            defaultAnimationStyle.setActivityExitAnimation(com.luck.picture.lib.R.anim.ps_anim_exit)
            selectorStyle!!.setWindowAnimationStyle(defaultAnimationStyle)
        } else if (checkedId == R.id.rb_photo_up_animation) {
            val animationStyle = PictureWindowAnimationStyle()
            animationStyle.setActivityEnterAnimation(com.luck.picture.lib.R.anim.ps_anim_up_in)
            animationStyle.setActivityExitAnimation(com.luck.picture.lib.R.anim.ps_anim_down_out)
            selectorStyle!!.setWindowAnimationStyle(animationStyle)
        } else if (checkedId == R.id.rb_default_style) {
            selectorStyle = PictureSelectorStyle()
        } else if (checkedId == R.id.rb_white_style) {
            val whiteTitleBarStyle = TitleBarStyle()
            whiteTitleBarStyle.setTitleBackgroundColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_white
                )
            )
            whiteTitleBarStyle.setTitleDrawableRightResource(R.drawable.ic_orange_arrow_down)
            whiteTitleBarStyle.setTitleLeftBackResource(com.luck.picture.lib.R.drawable.ps_ic_black_back)
            whiteTitleBarStyle.setTitleTextColor(
                ContextCompat.getColor(
                    this.context,
                    com.luck.picture.lib.R.color.ps_color_black
                )
            )
            whiteTitleBarStyle.setTitleCancelTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_53575e
                )
            )
            whiteTitleBarStyle.setDisplayTitleBarLine(true)

            val whiteBottomNavBarStyle = BottomNavBarStyle()
            whiteBottomNavBarStyle.setBottomNarBarBackgroundColor(Color.parseColor("#EEEEEE"))
            whiteBottomNavBarStyle.setBottomPreviewSelectTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_53575e
                )
            )

            whiteBottomNavBarStyle.setBottomPreviewNormalTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_9b
                )
            )
            whiteBottomNavBarStyle.setBottomPreviewSelectTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_fa632d
                )
            )
            whiteBottomNavBarStyle.setCompleteCountTips(false)
            whiteBottomNavBarStyle.setBottomEditorTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_53575e
                )
            )
            whiteBottomNavBarStyle.setBottomOriginalTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_53575e
                )
            )

            val selectMainStyle = SelectMainStyle()
            selectMainStyle.setStatusBarColor(
                ContextCompat.getColor(
                    this.context,
                    com.luck.picture.lib.R.color.ps_color_white
                )
            )
            selectMainStyle.setDarkStatusBarBlack(true)
            selectMainStyle.setSelectNormalTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_9b
                )
            )
            selectMainStyle.setSelectTextColor(
                ContextCompat.getColor(
                    this.context,
                    com.luck.picture.lib.R.color.ps_color_fa632d
                )
            )
            selectMainStyle.setPreviewSelectBackground(R.drawable.ps_demo_white_preview_selector)
            selectMainStyle.setSelectBackground(com.luck.picture.lib.R.drawable.ps_checkbox_selector)
            selectMainStyle.setSelectText(com.luck.picture.lib.R.string.ps_done_front_num)
            selectMainStyle.setMainListBackgroundColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_white
                )
            )

            selectorStyle!!.setTitleBarStyle(whiteTitleBarStyle)
            selectorStyle!!.setBottomBarStyle(whiteBottomNavBarStyle)
            selectorStyle!!.setSelectMainStyle(selectMainStyle)
        } else if (checkedId == R.id.rb_num_style) {
            val blueTitleBarStyle = TitleBarStyle()
            blueTitleBarStyle.setTitleBackgroundColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_blue
                )
            )

            val numberBlueBottomNavBarStyle = BottomNavBarStyle()
            numberBlueBottomNavBarStyle.setBottomPreviewNormalTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_9b
                )
            )
            numberBlueBottomNavBarStyle.setBottomPreviewSelectTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_blue
                )
            )
            numberBlueBottomNavBarStyle.setBottomNarBarBackgroundColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_white
                )
            )
            numberBlueBottomNavBarStyle.setBottomSelectNumResources(R.drawable.ps_demo_blue_num_selected)
            numberBlueBottomNavBarStyle.setBottomEditorTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_53575e
                )
            )
            numberBlueBottomNavBarStyle.setBottomOriginalTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_53575e
                )
            )


            val numberBlueSelectMainStyle = SelectMainStyle()
            numberBlueSelectMainStyle.setStatusBarColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_blue
                )
            )
            numberBlueSelectMainStyle.setSelectNumberStyle(true)
            numberBlueSelectMainStyle.setPreviewSelectNumberStyle(true)
            numberBlueSelectMainStyle.setSelectBackground(R.drawable.ps_demo_blue_num_selector)
            numberBlueSelectMainStyle.setMainListBackgroundColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_white
                )
            )
            numberBlueSelectMainStyle.setPreviewSelectBackground(R.drawable.ps_demo_preview_blue_num_selector)

            numberBlueSelectMainStyle.setSelectNormalTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_9b
                )
            )
            numberBlueSelectMainStyle.setSelectTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_blue
                )
            )
            numberBlueSelectMainStyle.setSelectText(com.luck.picture.lib.R.string.ps_completed)

            selectorStyle!!.setTitleBarStyle(blueTitleBarStyle)
            selectorStyle!!.setBottomBarStyle(numberBlueBottomNavBarStyle)
            selectorStyle!!.setSelectMainStyle(numberBlueSelectMainStyle)
        } else if (checkedId == R.id.rb_we_chat_style) {
            // 主体风格
            val numberSelectMainStyle = SelectMainStyle()
            numberSelectMainStyle.setSelectNumberStyle(true)
            numberSelectMainStyle.setPreviewSelectNumberStyle(false)
            numberSelectMainStyle.setPreviewDisplaySelectGallery(true)
            numberSelectMainStyle.setSelectBackground(com.luck.picture.lib.R.drawable.ps_default_num_selector)
            numberSelectMainStyle.setPreviewSelectBackground(com.luck.picture.lib.R.drawable.ps_preview_checkbox_selector)
            numberSelectMainStyle.setSelectNormalBackgroundResources(com.luck.picture.lib.R.drawable.ps_select_complete_normal_bg)
            numberSelectMainStyle.setSelectNormalTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_53575e
                )
            )
            numberSelectMainStyle.setSelectNormalText(com.luck.picture.lib.R.string.ps_send)
            numberSelectMainStyle.setAdapterPreviewGalleryBackgroundResource(com.luck.picture.lib.R.drawable.ps_preview_gallery_bg)
            numberSelectMainStyle.setAdapterPreviewGalleryItemSize(
                DensityUtil.dip2px(
                    this.context, 52f
                )
            )
            numberSelectMainStyle.setPreviewSelectText(com.luck.picture.lib.R.string.ps_select)
            numberSelectMainStyle.setPreviewSelectTextSize(14)
            numberSelectMainStyle.setPreviewSelectTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_white
                )
            )
            numberSelectMainStyle.setPreviewSelectMarginRight(
                DensityUtil.dip2px(
                    this.context, 6f
                )
            )
            numberSelectMainStyle.setSelectBackgroundResources(com.luck.picture.lib.R.drawable.ps_select_complete_bg)
            numberSelectMainStyle.setSelectText(com.luck.picture.lib.R.string.ps_send_num)
            numberSelectMainStyle.setSelectTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_white
                )
            )
            numberSelectMainStyle.setMainListBackgroundColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_black
                )
            )
            numberSelectMainStyle.setCompleteSelectRelativeTop(true)
            numberSelectMainStyle.setPreviewSelectRelativeBottom(true)
            numberSelectMainStyle.setAdapterItemIncludeEdge(false)

            // 头部TitleBar 风格
            val numberTitleBarStyle = TitleBarStyle()
            numberTitleBarStyle.setHideCancelButton(true)
            numberTitleBarStyle.setAlbumTitleRelativeLeft(true)
            if (cb_only_dir!!.isChecked()) {
                numberTitleBarStyle.setTitleAlbumBackgroundResource(R.drawable.ps_demo_only_album_bg)
            } else {
                numberTitleBarStyle.setTitleAlbumBackgroundResource(com.luck.picture.lib.R.drawable.ps_album_bg)
            }
            numberTitleBarStyle.setTitleDrawableRightResource(com.luck.picture.lib.R.drawable.ps_ic_grey_arrow)
            numberTitleBarStyle.setPreviewTitleLeftBackResource(com.luck.picture.lib.R.drawable.ps_ic_normal_back)

            // 底部NavBar 风格
            val numberBottomNavBarStyle = BottomNavBarStyle()
            numberBottomNavBarStyle.setBottomPreviewNarBarBackgroundColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_half_grey
                )
            )
            numberBottomNavBarStyle.setBottomPreviewNormalText(com.luck.picture.lib.R.string.ps_preview)
            numberBottomNavBarStyle.setBottomPreviewNormalTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_9b
                )
            )
            numberBottomNavBarStyle.setBottomPreviewNormalTextSize(16)
            numberBottomNavBarStyle.setCompleteCountTips(false)
            numberBottomNavBarStyle.setBottomPreviewSelectText(com.luck.picture.lib.R.string.ps_preview_num)
            numberBottomNavBarStyle.setBottomPreviewSelectTextColor(
                ContextCompat.getColor(
                    this.context, com.luck.picture.lib.R.color.ps_color_white
                )
            )


            selectorStyle!!.setTitleBarStyle(numberTitleBarStyle)
            selectorStyle!!.setBottomBarStyle(numberBottomNavBarStyle)
            selectorStyle!!.setSelectMainStyle(numberSelectMainStyle)
        } else if (checkedId == R.id.rb_default) {
            animationMode = AnimationType.DEFAULT_ANIMATION
        } else if (checkedId == R.id.rb_alpha) {
            animationMode = AnimationType.ALPHA_IN_ANIMATION
        } else if (checkedId == R.id.rb_slide_in) {
            animationMode = AnimationType.SLIDE_IN_BOTTOM_ANIMATION
        }
    }


    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (buttonView.getId() == R.id.cb_crop) {
            rgb_crop!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            cb_hide!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            cb_crop_circular!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            cb_styleCrop!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            cb_showCropFrame!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            cb_showCropGrid!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            cb_skip_not_gif!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            cb_not_gif!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
        } else if (buttonView.getId() == R.id.cb_custom_sandbox) {
            cb_only_dir!!.setChecked(isChecked)
        } else if (buttonView.getId() == R.id.cb_only_dir) {
            cb_custom_sandbox!!.setChecked(isChecked)
        } else if (buttonView.getId() == R.id.cb_skip_not_gif) {
            cb_not_gif!!.setChecked(false)
            cb_skip_not_gif!!.setChecked(isChecked)
        } else if (buttonView.getId() == R.id.cb_not_gif) {
            cb_skip_not_gif!!.setChecked(false)
            cb_not_gif!!.setChecked(isChecked)
        } else if (buttonView.getId() == R.id.cb_mode) {
            cb_attach_camera_mode!!.setVisibility(if (isChecked) View.GONE else View.VISIBLE)
        } else if (buttonView.getId() == R.id.cb_system_album) {
            cb_attach_system_mode!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
        } else if (buttonView.getId() == R.id.cb_custom_camera) {
            cb_camera_zoom!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            cb_camera_focus!!.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
            if (isChecked) {
            } else {
                cb_camera_zoom!!.setChecked(false)
                cb_camera_focus!!.setChecked(false)
            }
        } else if (buttonView.getId() == R.id.cb_crop_circular) {
            if (isChecked) {
                x = aspect_ratio_x
                y = aspect_ratio_y
                aspect_ratio_x = 1
                aspect_ratio_y = 1
            } else {
                aspect_ratio_x = x
                aspect_ratio_y = y
            }
            rgb_crop!!.setVisibility(if (isChecked) View.GONE else View.VISIBLE)
            if (isChecked) {
                cb_showCropFrame!!.setChecked(false)
                cb_showCropGrid!!.setChecked(false)
            } else {
                cb_showCropFrame!!.setChecked(true)
                cb_showCropGrid!!.setChecked(true)
            }
        }
    }


    override fun onSelectFinish(result: SelectorResult?) {
        if (result == null) {
            return
        }
        if (result.mResultCode == RESULT_OK) {
            val selectorResult = PictureSelector.obtainSelectorList(result.mResultData)
            analyticalSelectResults(selectorResult)
        } else if (result.mResultCode == RESULT_CANCELED) {
            Log.i(TAG, "onSelectFinish PictureSelector Cancel")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureConfig.CHOOSE_REQUEST || requestCode == PictureConfig.REQUEST_CAMERA) {
                val result = PictureSelector.obtainSelectorList(data)
                analyticalSelectResults(result)
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.i(TAG, "onActivityResult PictureSelector Cancel")
        }
    }

    /**
     * 创建一个ActivityResultLauncher
     *
     * @return
     */
    private fun createActivityResultLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult> {
                override fun onActivityResult(result: ActivityResult) {
                    val resultCode = result.getResultCode()
                    if (resultCode == RESULT_OK) {
                        val selectList = PictureSelector.obtainSelectorList(result.getData())
                        analyticalSelectResults(selectList)
                    } else if (resultCode == RESULT_CANCELED) {
                        Log.i(TAG, "onActivityResult PictureSelector Cancel")
                    }
                }
            })
    }


    /**
     * 处理选择结果
     *
     * @param result
     */
    private fun analyticalSelectResults(result: ArrayList<LocalMedia>) {
        for (media in result) {
            if (media.getWidth() == 0 || media.getHeight() == 0) {
                if (PictureMimeType.isHasImage(media.getMimeType())) {
                    val imageExtraInfo = MediaUtils.getImageSize(this@MainActivity, media.getPath())
                    media.setWidth(imageExtraInfo.getWidth())
                    media.setHeight(imageExtraInfo.getHeight())
                } else if (PictureMimeType.isHasVideo(media.getMimeType())) {
                    val videoExtraInfo = MediaUtils.getVideoSize(this@MainActivity, media.getPath())
                    media.setWidth(videoExtraInfo.getWidth())
                    media.setHeight(videoExtraInfo.getHeight())
                }
            }
            Log.i(TAG, "文件名: " + media.getFileName())
            Log.i(TAG, "是否压缩:" + media.isCompressed())
            Log.i(TAG, "压缩:" + media.getCompressPath())
            Log.i(TAG, "初始路径:" + media.getPath())
            Log.i(TAG, "绝对路径:" + media.getRealPath())
            Log.i(TAG, "是否裁剪:" + media.isCut())
            Log.i(TAG, "裁剪路径:" + media.getCutPath())
            Log.i(TAG, "是否开启原图:" + media.isOriginal())
            Log.i(TAG, "原图路径:" + media.getOriginalPath())
            Log.i(TAG, "沙盒路径:" + media.getSandboxPath())
            Log.i(TAG, "水印路径:" + media.getWatermarkPath())
            Log.i(TAG, "视频缩略图:" + media.getVideoThumbnailPath())
            Log.i(TAG, "原始宽高: " + media.getWidth() + "x" + media.getHeight())
            Log.i(TAG, "裁剪宽高: " + media.getCropImageWidth() + "x" + media.getCropImageHeight())
            Log.i(TAG, "文件大小: " + PictureFileUtils.formatAccurateUnitFileSize(media.getSize()))
            Log.i(TAG, "文件时长: " + media.getDuration())
        }
        runOnUiThread(object : Runnable {
            override fun run() {
                val isMaxSize = result.size == mAdapter!!.getSelectMax()
                val oldSize = mAdapter!!.getData().size
                mAdapter!!.notifyItemRangeRemoved(0, if (isMaxSize) oldSize + 1 else oldSize)
                mAdapter!!.getData().clear()

                mAdapter!!.getData().addAll(result)
                mAdapter!!.notifyItemRangeInserted(0, result.size)
            }
        })
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mAdapter != null && mAdapter!!.getData() != null && mAdapter!!.getData().size > 0) {
            outState.putParcelableArrayList(
                "selectorList",
                mAdapter!!.getData()
            )
        }
    }

    val context: Context
        get() = this

    companion object {
        private const val TAG = "PictureSelectorTag"
        private const val TAG_EXPLAIN_VIEW = "TAG_EXPLAIN_VIEW"
        private const val ACTIVITY_RESULT = 1
        private const val CALLBACK_RESULT = 2
        private const val LAUNCHER_RESULT = 3

        /**
         * 添加权限说明
         *
         * @param viewGroup
         * @param permissionArray
         */
        private fun addPermissionDescription(
            isHasSimpleXCamera: Boolean,
            viewGroup: ViewGroup,
            permissionArray: Array<String?>
        ) {
            val dp10 = DensityUtil.dip2px(viewGroup.getContext(), 10f)
            val dp15 = DensityUtil.dip2px(viewGroup.getContext(), 15f)
            val view = MediumBoldTextView(viewGroup.getContext())
            view.setTag(TAG_EXPLAIN_VIEW)
            view.setTextSize(14f)
            view.setTextColor(Color.parseColor("#333333"))
            view.setPadding(dp10, dp15, dp10, dp15)

            val title: String?
            val explain: String?

            if (TextUtils.equals(permissionArray[0], PermissionConfig.CAMERA[0])) {
                title = "相机权限使用说明"
                explain = "相机权限使用说明\n用户app用于拍照/录视频"
            } else if (TextUtils.equals(permissionArray[0], Manifest.permission.RECORD_AUDIO)) {
                if (isHasSimpleXCamera) {
                    title = "麦克风权限使用说明"
                    explain = "麦克风权限使用说明\n用户app用于录视频时采集声音"
                } else {
                    title = "录音权限使用说明"
                    explain = "录音权限使用说明\n用户app用于采集声音"
                }
            } else {
                title = "存储权限使用说明"
                explain =
                    "存储权限使用说明\n用户app写入/下载/保存/读取/修改/删除图片、视频、文件等信息"
            }
            val startIndex = 0
            val endOf = startIndex + title.length
            val builder = SpannableStringBuilder(explain)
            builder.setSpan(
                AbsoluteSizeSpan(DensityUtil.dip2px(viewGroup.getContext(), 16f)),
                startIndex,
                endOf,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            builder.setSpan(
                ForegroundColorSpan(-0xcccccd),
                startIndex,
                endOf,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE
            )
            view.setText(builder)
            view.setBackground(
                ContextCompat.getDrawable(
                    viewGroup.getContext(),
                    R.drawable.ps_demo_permission_desc_bg
                )
            )

            if (isHasSimpleXCamera) {
                val layoutParams =
                    RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                layoutParams.topMargin = DensityUtil.getStatusBarHeight(viewGroup.getContext())
                layoutParams.leftMargin = dp10
                layoutParams.rightMargin = dp10
                viewGroup.addView(view, layoutParams)
            } else {
                val layoutParams =
                    ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                layoutParams.topToBottom = R.id.title_bar
                layoutParams.leftToLeft = ConstraintSet.PARENT_ID
                layoutParams.leftMargin = dp10
                layoutParams.rightMargin = dp10
                viewGroup.addView(view, layoutParams)
            }
        }

        /**
         * 移除权限说明
         *
         * @param viewGroup
         */
        private fun removePermissionDescription(viewGroup: ViewGroup) {
            val tagExplainView = viewGroup.findViewWithTag<View?>(TAG_EXPLAIN_VIEW)
            viewGroup.removeView(tagExplainView)
        }


        /**
         * 启动录音意图
         *
         * @param fragment
         * @param requestCode
         */
        private fun startRecordSoundAction(fragment: Fragment, requestCode: Int) {
            val recordAudioIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
            if (recordAudioIntent.resolveActivity(
                    fragment.requireActivity().getPackageManager()
                ) != null
            ) {
                fragment.startActivityForResult(recordAudioIntent, requestCode)
            } else {
                ToastUtils.showToast(
                    fragment.getContext(),
                    "The system is missing a recording component"
                )
            }
        }
    }
}
