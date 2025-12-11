package com.luck.picture.lib.config

import android.content.pm.ActivityInfo
import com.luck.picture.lib.basic.IBridgeLoaderFactory
import com.luck.picture.lib.basic.IBridgeViewLifecycle
import com.luck.picture.lib.basic.InterpolatorFactory
import com.luck.picture.lib.engine.CompressEngine
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.engine.CropEngine
import com.luck.picture.lib.engine.CropFileEngine
import com.luck.picture.lib.engine.ExtendLoaderEngine
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.engine.SandboxFileEngine
import com.luck.picture.lib.engine.UriToFileTransformEngine
import com.luck.picture.lib.engine.VideoPlayerEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.interfaces.OnBitmapWatermarkEventListener
import com.luck.picture.lib.interfaces.OnCameraInterceptListener
import com.luck.picture.lib.interfaces.OnCustomLoadingListener
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.luck.picture.lib.interfaces.OnGridItemSelectAnimListener
import com.luck.picture.lib.interfaces.OnInjectActivityPreviewListener
import com.luck.picture.lib.interfaces.OnInjectLayoutResourceListener
import com.luck.picture.lib.interfaces.OnMediaEditInterceptListener
import com.luck.picture.lib.interfaces.OnPermissionDeniedListener
import com.luck.picture.lib.interfaces.OnPermissionDescriptionListener
import com.luck.picture.lib.interfaces.OnPermissionsInterceptListener
import com.luck.picture.lib.interfaces.OnPreviewInterceptListener
import com.luck.picture.lib.interfaces.OnQueryFilterListener
import com.luck.picture.lib.interfaces.OnRecordAudioInterceptListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.interfaces.OnSelectAnimListener
import com.luck.picture.lib.interfaces.OnSelectFilterListener
import com.luck.picture.lib.interfaces.OnSelectLimitTipsListener
import com.luck.picture.lib.interfaces.OnVideoThumbnailEventListener
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.magical.BuildRecycleItemViewParams
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.thread.PictureThreadUtils
import com.luck.picture.lib.utils.FileDirMap
import com.luck.picture.lib.utils.SdkVersionUtils

/**
 * @author：luck
 * @date：2017-05-24 17:02
 * @describe：PictureSelector Config
 */
class SelectorConfig {
    var chooseMode: Int = 0
    var isOnlyCamera: Boolean = false
    var isDirectReturnSingle: Boolean = false
    var cameraImageFormat: String? = null
    var cameraVideoFormat: String? = null
    var cameraImageFormatForQ: String? = null
    var cameraVideoFormatForQ: String? = null
    var requestedOrientation: Int = 0
    var isCameraAroundState: Boolean = false
    var selectionMode: Int = 0
    var maxSelectNum: Int = 0
    var minSelectNum: Int = 0
    var maxVideoSelectNum: Int = 0
    var minVideoSelectNum: Int = 0
    var minAudioSelectNum: Int = 0
    var videoQuality: Int = 0
    var filterVideoMaxSecond: Int = 0
    var filterVideoMinSecond: Int = 0
    var selectMaxDurationSecond: Int = 0
    var selectMinDurationSecond: Int = 0
    var recordVideoMaxSecond: Int = 0
    var recordVideoMinSecond: Int = 0
    var imageSpanCount: Int = 0
    var filterMaxFileSize: Long = 0
    var filterMinFileSize: Long = 0
    var selectMaxFileSize: Long = 0
    var selectMinFileSize: Long = 0
    var language: Int = 0
    var defaultLanguage: Int = 0
    var isDisplayCamera: Boolean = false
    var isGif: Boolean = false
    var isWebp: Boolean = false
    var isBmp: Boolean = false
    var isHeic: Boolean = false
    var isEnablePreviewImage: Boolean = false
    var isEnablePreviewVideo: Boolean = false
    var isEnablePreviewAudio: Boolean = false
    var isPreviewFullScreenMode: Boolean = false
    var isPreviewZoomEffect: Boolean = false
    var isOpenClickSound: Boolean = false
    var isEmptyResultReturn: Boolean = false
    var isHidePreviewDownload: Boolean = false
    var isWithVideoImage: Boolean = false
    var queryOnlyImageList: MutableList<String?>? = null
    var queryOnlyVideoList: MutableList<String?>? = null
    var queryOnlyAudioList: MutableList<String?>? = null
    var skipCropList: MutableList<String?>? = null
    var isCheckOriginalImage: Boolean = false
    var outPutCameraImageFileName: String? = null
    var outPutCameraVideoFileName: String? = null
    var outPutAudioFileName: String? = null
    var outPutCameraDir: String? = null
    var outPutAudioDir: String? = null
    var sandboxDir: String? = null
    var originalPath: String? = null
    var cameraPath: String? = null
    var sortOrder: String? = null
    var defaultAlbumName: String? = null
    var pageSize: Int = 0
    var isPageStrategy: Boolean = false
    var isFilterInvalidFile: Boolean = false
    var isMaxSelectEnabledMask: Boolean = false
    var animationMode: Int = 0
    var isAutomaticTitleRecyclerTop: Boolean = false
    var isQuickCapture: Boolean = false
    var isCameraRotateImage: Boolean = false
    var isAutoRotating: Boolean = false
    var isSyncCover: Boolean = false
    var ofAllCameraType: Int = 0
    var isOnlySandboxDir: Boolean = false
    var isCameraForegroundService: Boolean = false
    var isResultListenerBack: Boolean = false
    var isInjectLayoutResource: Boolean = false
    var isActivityResultBack: Boolean = false
    var isCompressEngine: Boolean = false
    var isLoaderDataEngine: Boolean = false
    var isLoaderFactoryEngine: Boolean = false
    var isSandboxFileEngine: Boolean = false
    var isOriginalControl: Boolean = false
    var isDisplayTimeAxis: Boolean = false
    var isFastSlidingSelect: Boolean = false
    var isSelectZoomAnim: Boolean = false
    var isAutoVideoPlay: Boolean = false
    var isLoopAutoPlay: Boolean = false
    var isFilterSizeDuration: Boolean = false
    var isPageSyncAsCount: Boolean = false
    var isPauseResumePlay: Boolean = false
    var isSyncWidthAndHeight: Boolean = false
    var isOriginalSkipCompress: Boolean = false
    var isPreloadFirst: Boolean = false
    var isUseSystemVideoPlayer: Boolean = false
    var isNewKeyBackMode: Boolean = false
    var selectorStyle: PictureSelectorStyle? = null

    private fun initDefaultValue() {
        chooseMode = SelectMimeType.ofImage()
        isOnlyCamera = false
        selectionMode = SelectModeConfig.MULTIPLE
        selectorStyle = PictureSelectorStyle()
        maxSelectNum = 9
        minSelectNum = 0
        maxVideoSelectNum = 1
        minVideoSelectNum = 0
        minAudioSelectNum = 0
        videoQuality = VideoQuality.VIDEO_QUALITY_HIGH
        language = LanguageConfig.UNKNOWN_LANGUAGE
        defaultLanguage = LanguageConfig.SYSTEM_LANGUAGE
        filterVideoMaxSecond = 0
        filterVideoMinSecond = 0
        selectMaxDurationSecond = 0
        selectMinDurationSecond = 0
        filterMaxFileSize = 0
        filterMinFileSize = 0
        selectMaxFileSize = 0
        selectMinFileSize = 0
        recordVideoMaxSecond = 60
        recordVideoMinSecond = 0
        imageSpanCount = PictureConfig.DEFAULT_SPAN_COUNT
        isCameraAroundState = false
        isWithVideoImage = false
        isDisplayCamera = true
        isGif = false
        isWebp = true
        isBmp = true
        isHeic = true
        isCheckOriginalImage = false
        isDirectReturnSingle = false
        isEnablePreviewImage = true
        isEnablePreviewVideo = true
        isEnablePreviewAudio = true
        isHidePreviewDownload = false
        isOpenClickSound = false
        isEmptyResultReturn = false
        cameraImageFormat = PictureMimeType.JPEG
        cameraVideoFormat = PictureMimeType.MP4
        cameraImageFormatForQ = PictureMimeType.MIME_TYPE_IMAGE
        cameraVideoFormatForQ = PictureMimeType.MIME_TYPE_VIDEO
        outPutCameraImageFileName = ""
        outPutCameraVideoFileName = ""
        outPutAudioFileName = ""
        queryOnlyImageList = ArrayList<String?>()
        queryOnlyVideoList = ArrayList<String?>()
        queryOnlyAudioList = ArrayList<String?>()
        outPutCameraDir = ""
        outPutAudioDir = ""
        sandboxDir = ""
        originalPath = ""
        cameraPath = ""
        pageSize = PictureConfig.MAX_PAGE_SIZE
        isPageStrategy = true
        isFilterInvalidFile = false
        isMaxSelectEnabledMask = false
        animationMode = -1
        isAutomaticTitleRecyclerTop = true
        isQuickCapture = true
        isCameraRotateImage = true
        isAutoRotating = true
        isSyncCover = !SdkVersionUtils.isQ
        ofAllCameraType = SelectMimeType.ofAll()
        isOnlySandboxDir = false
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        isCameraForegroundService = false
        isResultListenerBack = true
        isActivityResultBack = false
        isCompressEngine = false
        isLoaderDataEngine = false
        isLoaderFactoryEngine = false
        isSandboxFileEngine = false
        isPreviewFullScreenMode = true
        isPreviewZoomEffect = chooseMode != SelectMimeType.ofAudio()
        isOriginalControl = false
        isInjectLayoutResource = false
        isDisplayTimeAxis = true
        isFastSlidingSelect = false
        skipCropList = ArrayList<String?>()
        sortOrder = ""
        isSelectZoomAnim = true
        defaultAlbumName = ""
        isAutoVideoPlay = false
        isLoopAutoPlay = false
        isFilterSizeDuration = true
        isPageSyncAsCount = false
        isPauseResumePlay = false
        isSyncWidthAndHeight = true
        isOriginalSkipCompress = false
        isPreloadFirst = true
        isNewKeyBackMode = true
        isUseSystemVideoPlayer = false
    }

    /**
     * Callback listening
     */
    var imageEngine: ImageEngine? = null
    var compressEngine: CompressEngine? = null
    var compressFileEngine: CompressFileEngine? = null
    var cropEngine: CropEngine? = null
    var cropFileEngine: CropFileEngine? = null
    var sandboxFileEngine: SandboxFileEngine? = null
    var uriToFileTransformEngine: UriToFileTransformEngine? = null
    var loaderDataEngine: ExtendLoaderEngine? = null
    var videoPlayerEngine: VideoPlayerEngine<*>? = null
    var viewLifecycle: IBridgeViewLifecycle? = null
    var loaderFactory: IBridgeLoaderFactory? = null
    var interpolatorFactory: InterpolatorFactory? = null
    var onCameraInterceptListener: OnCameraInterceptListener? = null
    var onSelectLimitTipsListener: OnSelectLimitTipsListener? = null
    var onResultCallListener: OnResultCallbackListener<LocalMedia?>? = null
    var onExternalPreviewEventListener: OnExternalPreviewEventListener? = null
    var onInjectActivityPreviewListener: OnInjectActivityPreviewListener? = null
    var onEditMediaEventListener: OnMediaEditInterceptListener? = null
    var onPermissionsEventListener: OnPermissionsInterceptListener? = null
    var onLayoutResourceListener: OnInjectLayoutResourceListener? = null
    var onPreviewInterceptListener: OnPreviewInterceptListener? = null
    var onSelectFilterListener: OnSelectFilterListener? = null
    var onPermissionDescriptionListener: OnPermissionDescriptionListener? = null
    var onPermissionDeniedListener: OnPermissionDeniedListener? = null
    var onRecordAudioListener: OnRecordAudioInterceptListener? = null
    var onQueryFilterListener: OnQueryFilterListener? = null
    var onBitmapWatermarkListener: OnBitmapWatermarkEventListener? = null
    var onVideoThumbnailEventListener: OnVideoThumbnailEventListener? = null
    var onItemSelectAnimListener: OnGridItemSelectAnimListener? = null
    var onSelectAnimListener: OnSelectAnimListener? = null
    var onCustomLoadingListener: OnCustomLoadingListener? = null

    /**
     * selected current album folder
     */
    var currentLocalMediaFolder: LocalMediaFolder? = null

    /**
     * selected result
     */
    @get:Synchronized
    val selectedResult: ArrayList<LocalMedia?> = ArrayList<LocalMedia?>()

    val selectCount: Int
        get() = selectedResult.size

    fun addSelectResult(media: LocalMedia?) {
        selectedResult.add(media)
    }

    fun addAllSelectResult(result: ArrayList<LocalMedia?>) {
        selectedResult.addAll(result)
    }

    val resultFirstMimeType: String?
        get() = if (selectedResult.size > 0) selectedResult.get(0)!!.mimeType else ""

    /**
     * selected preview result
     */
    val selectedPreviewResult: ArrayList<LocalMedia?> = ArrayList<LocalMedia?>()

    fun addSelectedPreviewResult(list: ArrayList<LocalMedia?>?) {
        if (list != null) {
            selectedPreviewResult.clear()
            selectedPreviewResult.addAll(list)
        }
    }

    /**
     * all album data source
     */
    val albumDataSource: ArrayList<LocalMediaFolder?> = ArrayList<LocalMediaFolder?>()

    fun addAlbumDataSource(list: MutableList<LocalMediaFolder?>?) {
        if (list != null) {
            albumDataSource.clear()
            albumDataSource.addAll(list)
        }
    }

    /**
     * all data source
     */
    val dataSource: ArrayList<LocalMedia?> = ArrayList<LocalMedia?>()

    init {
        initDefaultValue()
    }

    fun addDataSource(list: ArrayList<LocalMedia?>?) {
        if (list != null) {
            dataSource.clear()
            dataSource.addAll(list)
        }
    }

    /**
     * 释放监听器
     */
    fun destroy() {
        imageEngine = null
        compressEngine = null
        compressFileEngine = null
        cropEngine = null
        cropFileEngine = null
        sandboxFileEngine = null
        uriToFileTransformEngine = null
        loaderDataEngine = null
        onResultCallListener = null
        onCameraInterceptListener = null
        onExternalPreviewEventListener = null
        onInjectActivityPreviewListener = null
        onEditMediaEventListener = null
        onPermissionsEventListener = null
        onLayoutResourceListener = null
        onPreviewInterceptListener = null
        onSelectLimitTipsListener = null
        onSelectFilterListener = null
        onPermissionDescriptionListener = null
        onPermissionDeniedListener = null
        onRecordAudioListener = null
        onQueryFilterListener = null
        onBitmapWatermarkListener = null
        onVideoThumbnailEventListener = null
        viewLifecycle = null
        loaderFactory = null
        interpolatorFactory = null
        onItemSelectAnimListener = null
        onSelectAnimListener = null
        videoPlayerEngine = null
        onCustomLoadingListener = null
        currentLocalMediaFolder = null
        dataSource.clear()
        selectedResult.clear()
        albumDataSource.clear()
        selectedPreviewResult.clear()
        PictureThreadUtils.cancel(PictureThreadUtils.getIoPool(5))
        BuildRecycleItemViewParams.clear()
        FileDirMap.clear()
        LocalMedia.Companion.destroyPool()
    }
}
