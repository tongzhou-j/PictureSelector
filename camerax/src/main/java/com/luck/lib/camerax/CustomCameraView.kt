package com.luck.lib.camerax

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Display
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.FocusMeteringResult
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPoint
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.VideoCapture
import androidx.camera.core.ZoomState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.google.common.util.concurrent.ListenableFuture
import com.luck.lib.camerax.listener.CameraListener
import com.luck.lib.camerax.listener.CameraXOrientationEventListener
import com.luck.lib.camerax.listener.CameraXPreviewViewTouchListener
import com.luck.lib.camerax.listener.CaptureListener
import com.luck.lib.camerax.listener.ClickListener
import com.luck.lib.camerax.listener.ImageCallbackListener
import com.luck.lib.camerax.listener.TypeListener
import com.luck.lib.camerax.permissions.PermissionChecker
import com.luck.lib.camerax.permissions.PermissionResultCallback
import com.luck.lib.camerax.permissions.SimpleXPermissionUtil
import com.luck.lib.camerax.utils.CameraUtils
import com.luck.lib.camerax.utils.DensityUtil
import com.luck.lib.camerax.utils.FileUtils
import com.luck.lib.camerax.utils.SimpleXSpUtils
import com.luck.lib.camerax.widget.CaptureLayout
import com.luck.lib.camerax.widget.FocusImageView
import org.jetbrains.annotations.NotNull
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.List
import java.util.Locale
import java.util.Objects
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

/**
 * @author：luck
 * @date：2020-01-04 13:41
 * @describe：自定义相机View
 */
class CustomCameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), CameraXOrientationEventListener.OnOrientationChangedListener {

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        /**
         * 闪关灯状态
         */
        private const val TYPE_FLASH_AUTO = 0x021
        private const val TYPE_FLASH_ON = 0x022
        private const val TYPE_FLASH_OFF = 0x023
    }

    private var typeFlash = TYPE_FLASH_OFF
    private lateinit var mCameraPreviewView: PreviewView
    private var mCameraProvider: ProcessCameraProvider? = null
    private var mImageCapture: ImageCapture? = null
    private var mImageAnalyzer: ImageAnalysis? = null
    private var mVideoCapture: VideoCapture? = null

    private var displayId = -1
    /**
     * 相机模式
     */
    private var buttonFeatures: Int = 0
    /**
     * 自定义拍照输出路径
     */
    private var outPutCameraDir: String? = null
    /**
     * 自定义拍照文件名
     */
    private var outPutCameraFileName: String? = null

    /**
     * 设置每秒的录制帧数
     */
    private var videoFrameRate: Int = 0

    /**
     * 设置编码比特率。
     */
    private var videoBitRate: Int = 0

    /**
     * 视频录制最小时长
     */
    private var recordVideoMinSecond: Int = 0

    /**
     * 是否显示录制时间
     */
    private var isDisplayRecordTime: Boolean = false

    /**
     * 图片文件类型
     */
    private var imageFormat: String? = null
    private var imageFormatForQ: String? = null

    /**
     * 视频文件类型
     */
    private var videoFormat: String? = null
    private var videoFormatForQ: String? = null
    /**
     * 相机模式
     */
    private var useCameraCases: Int = LifecycleCameraController.IMAGE_CAPTURE
    /**
     * 摄像头方向
     */
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    /**
     * 手指点击对焦
     */
    private var isManualFocus: Boolean = false

    /**
     * 双击可放大缩小
     */
    private var isZoomPreview: Boolean = false

    /**
     * 是否自动纠偏
     */
    var isAutoRotation: Boolean = false

    private var recordTime: Long = 0

    /**
     * 回调监听
     */
    private var mCameraListener: CameraListener? = null
    private var mOnClickListener: ClickListener? = null
    private var mImageCallbackListener: ImageCallbackListener? = null
    private lateinit var mImagePreview: ImageView
    private lateinit var mImagePreviewBg: View
    private lateinit var mSwitchCamera: ImageView
    private lateinit var mFlashLamp: ImageView
    private lateinit var tvCurrentTime: TextView
    private lateinit var mCaptureLayout: CaptureLayout
    private var mMediaPlayer: MediaPlayer? = null
    private lateinit var mTextureView: TextureView
    private lateinit var displayManager: DisplayManager
    private lateinit var displayListener: DisplayListener
    private var orientationEventListener: CameraXOrientationEventListener? = null
    private var mCameraInfo: CameraInfo? = null
    private var mCameraControl: CameraControl? = null
    private lateinit var focusImageView: FocusImageView
    private lateinit var mainExecutor: Executor
    private lateinit var activity: Activity

    private fun isImageCaptureEnabled(): Boolean {
        return useCameraCases == LifecycleCameraController.IMAGE_CAPTURE
    }

    init {
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.picture_camera_view, this)
        activity = context as Activity
        setBackgroundColor(ContextCompat.getColor(context, R.color.picture_color_black))
        mCameraPreviewView = findViewById(R.id.cameraPreviewView)
        mTextureView = findViewById(R.id.video_play_preview)
        focusImageView = findViewById(R.id.focus_view)
        mImagePreview = findViewById(R.id.cover_preview)
        mImagePreviewBg = findViewById(R.id.cover_preview_bg)
        mSwitchCamera = findViewById(R.id.image_switch)
        mFlashLamp = findViewById(R.id.image_flash)
        mCaptureLayout = findViewById(R.id.capture_layout)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        mSwitchCamera.setImageResource(R.drawable.picture_ic_camera)
        displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        displayListener = DisplayListener()
        displayManager.registerDisplayListener(displayListener, null)
        mainExecutor = ContextCompat.getMainExecutor(context)

        mCameraPreviewView.post {
            if (mCameraPreviewView != null) {
                val display = mCameraPreviewView.display
                if (display != null) {
                    displayId = display.displayId
                }
            }
        }

        mFlashLamp.setOnClickListener {
            typeFlash++
            if (typeFlash > 0x023) {
                typeFlash = TYPE_FLASH_AUTO
            }
            setFlashMode()
        }

        mSwitchCamera.setOnClickListener {
            toggleCamera()
        }

        mCaptureLayout.setCaptureListener(object : CaptureListener {
            override fun takePictures() {
                val imageCapture = mImageCapture
                if (imageCapture != null && mCameraProvider?.isBound(imageCapture) != true) {
                    bindCameraImageUseCases()
                }
                useCameraCases = LifecycleCameraController.IMAGE_CAPTURE
                mCaptureLayout.setButtonCaptureEnabled(false)
                mSwitchCamera.visibility = INVISIBLE
                mFlashLamp.visibility = INVISIBLE
                tvCurrentTime.visibility = GONE
                val metadata = ImageCapture.Metadata()
                metadata.setReversedHorizontal(isReversedHorizontal())
                val cameraFile: File
                val fileOptions: ImageCapture.OutputFileOptions
                if (isSaveExternal()) {
                    cameraFile = FileUtils.createTempFile(context, false)
                } else {
                    cameraFile = FileUtils.createCameraFile(
                        context, CameraUtils.TYPE_IMAGE,
                        outPutCameraFileName, imageFormat, outPutCameraDir
                    )
                }
                fileOptions = ImageCapture.OutputFileOptions.Builder(cameraFile)
                    .setMetadata(metadata).build()
                mImageCapture?.takePicture(
                    fileOptions, mainExecutor,
                    MyImageResultCallback(
                        this@CustomCameraView, mImagePreview, mImagePreviewBg,
                        mCaptureLayout, mImageCallbackListener, mCameraListener
                    )
                )
            }

            override fun recordStart() {
                val videoCapture = mVideoCapture
                if (videoCapture != null && mCameraProvider?.isBound(videoCapture as androidx.camera.core.UseCase) != true) {
                    bindCameraVideoUseCases()
                }
                useCameraCases = LifecycleCameraController.VIDEO_CAPTURE
                mSwitchCamera.visibility = INVISIBLE
                mFlashLamp.visibility = INVISIBLE
                tvCurrentTime.visibility = if (isDisplayRecordTime) VISIBLE else GONE
                val cameraFile: File
                val fileOptions: VideoCapture.OutputFileOptions
                if (isSaveExternal()) {
                    cameraFile = FileUtils.createTempFile(context, true)
                } else {
                    cameraFile = FileUtils.createCameraFile(
                        context, CameraUtils.TYPE_VIDEO,
                        outPutCameraFileName, videoFormat, outPutCameraDir
                    )
                }
                fileOptions = VideoCapture.OutputFileOptions.Builder(cameraFile).build()
                mVideoCapture?.startRecording(
                    fileOptions, mainExecutor,
                    object : VideoCapture.OnVideoSavedCallback {
                        override fun onVideoSaved(@NonNull @NotNull outputFileResults: VideoCapture.OutputFileResults) {
                            val minSecond = if (recordVideoMinSecond <= 0) CustomCameraConfig.DEFAULT_MIN_RECORD_VIDEO else recordVideoMinSecond
                            if (recordTime < minSecond || outputFileResults.savedUri == null) {
                                return
                            }
                            val savedUri = outputFileResults.savedUri ?: return
                            SimpleCameraX.putOutputUri(activity.intent, savedUri)
                            val outPutPath = if (FileUtils.isContent(savedUri.toString())) savedUri.toString() else savedUri.path
                            mTextureView.visibility = View.VISIBLE
                            tvCurrentTime.visibility = GONE
                            if (mTextureView.isAvailable) {
                                startVideoPlay(outPutPath ?: "")
                            } else {
                                mTextureView.surfaceTextureListener = surfaceTextureListener
                            }
                        }

                        override fun onError(
                            videoCaptureError: Int,
                            @NonNull @NotNull message: String,
                            @Nullable cause: Throwable?
                        ) {
                            if (videoCaptureError == VideoCapture.ERROR_RECORDING_TOO_SHORT || videoCaptureError == OnVideoSavedCallback.ERROR_MUXER) {
                                recordShort(0)
                            } else {
                                mCameraListener?.onError(videoCaptureError, message, cause)
                            }
                        }
                    }
                )
            }

            override fun changeTime(duration: Long) {
                if (isDisplayRecordTime && tvCurrentTime.visibility == VISIBLE) {
                    val format = String.format(
                        Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                    )
                    if (tvCurrentTime.text.toString() != format) {
                        tvCurrentTime.text = format
                    }
                    if (tvCurrentTime.text.toString() == "00:00") {
                        tvCurrentTime.visibility = GONE
                    }
                }
            }

            override fun recordShort(time: Long) {
                recordTime = time
                mSwitchCamera.visibility = VISIBLE
                mFlashLamp.visibility = VISIBLE
                tvCurrentTime.visibility = GONE
                mCaptureLayout.resetCaptureLayout()
                mCaptureLayout.setTextWithAnimation(context.getString(R.string.picture_recording_time_is_short))
                try {
                    mVideoCapture?.stopRecording()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun recordEnd(time: Long) {
                recordTime = time
                try {
                    mVideoCapture?.stopRecording()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun recordZoom(zoom: Float) {
            }

            override fun recordError() {
                mCameraListener?.onError(0, "An unknown error", null)
            }
        })

        mCaptureLayout.setTypeListener(object : TypeListener {
            override fun cancel() {
                onCancelMedia()
            }

            override fun confirm() {
                var outputPath = SimpleCameraX.getOutputPath(activity.intent)
                if (isSaveExternal()) {
                    outputPath = isMergeExternalStorageState(activity, outputPath)
                } else {
                    // 对前置镜头导致的镜像进行一个纠正
                    if (isImageCaptureEnabled() && isReversedHorizontal()) {
                        val cameraFile = FileUtils.createCameraFile(
                            context, CameraUtils.TYPE_IMAGE,
                            outPutCameraFileName, imageFormat, outPutCameraDir
                        )
                        if (FileUtils.copyPath(activity, outputPath, cameraFile.absolutePath)) {
                            outputPath = cameraFile.absolutePath
                            SimpleCameraX.putOutputUri(activity.intent, Uri.fromFile(cameraFile))
                        }
                    }
                }
                if (isImageCaptureEnabled()) {
                    mImagePreview.visibility = INVISIBLE
                    mImagePreviewBg.alpha = 0F
                    mCameraListener?.onPictureSuccess(outputPath)
                } else {
                    stopVideoPlay()
                    mCameraListener?.onRecordSuccess(outputPath)
                }
            }
        })
        mCaptureLayout.setLeftClickListener(object : ClickListener {
            override fun onClick() {
                mOnClickListener?.onClick()
            }
        })
    }

    private fun isMergeExternalStorageState(activity: Activity, outputPath: String): String {
        var resultPath = outputPath
        return try {
            // 对前置镜头导致的镜像进行一个纠正
            if (isImageCaptureEnabled() && isReversedHorizontal()) {
                val tempFile = FileUtils.createTempFile(activity, false)
                if (FileUtils.copyPath(activity, resultPath, tempFile.absolutePath)) {
                    resultPath = tempFile.absolutePath
                }
            }
            // 当用户未设置存储路径时，相片默认是存在外部公共目录下
            val externalSavedUri: Uri?
            if (isImageCaptureEnabled()) {
                val contentValues = CameraUtils.buildImageContentValues(outPutCameraFileName, imageFormatForQ)
                externalSavedUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            } else {
                val contentValues = CameraUtils.buildVideoContentValues(outPutCameraFileName, videoFormatForQ)
                externalSavedUri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            }
            if (externalSavedUri == null) {
                return resultPath
            }
            val outputStream = context.contentResolver.openOutputStream(externalSavedUri) ?: return resultPath
            val isWriteFileSuccess = FileUtils.writeFileFromIS(FileInputStream(resultPath), outputStream)
            if (isWriteFileSuccess) {
                FileUtils.deleteFile(context, resultPath)
                SimpleCameraX.putOutputUri(activity.intent, externalSavedUri)
                externalSavedUri.toString()
            } else {
                resultPath
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            resultPath
        }
    }

    private fun isSaveExternal(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && outPutCameraDir.isNullOrEmpty()
    }

    private fun isReversedHorizontal(): Boolean {
        return lensFacing == CameraSelector.LENS_FACING_FRONT
    }

    /**
     * 用户针对相机的一些参数配制
     *
     * @param intent
     */
    fun setCameraConfig(intent: Intent) {
        val extras = intent.extras ?: return
        val isCameraAroundState = extras.getBoolean(SimpleCameraX.EXTRA_CAMERA_AROUND_STATE, false)
        buttonFeatures = extras.getInt(SimpleCameraX.EXTRA_CAMERA_MODE, CustomCameraConfig.BUTTON_STATE_BOTH)
        lensFacing = if (isCameraAroundState) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        outPutCameraDir = extras.getString(SimpleCameraX.EXTRA_OUTPUT_PATH_DIR)
        outPutCameraFileName = extras.getString(SimpleCameraX.EXTRA_CAMERA_FILE_NAME)
        videoFrameRate = extras.getInt(SimpleCameraX.EXTRA_VIDEO_FRAME_RATE)
        videoBitRate = extras.getInt(SimpleCameraX.EXTRA_VIDEO_BIT_RATE)
        isManualFocus = extras.getBoolean(SimpleCameraX.EXTRA_MANUAL_FOCUS)
        isZoomPreview = extras.getBoolean(SimpleCameraX.EXTRA_ZOOM_PREVIEW)
        isAutoRotation = extras.getBoolean(SimpleCameraX.EXTRA_AUTO_ROTATION)

        val recordVideoMaxSecond = extras.getInt(SimpleCameraX.EXTRA_RECORD_VIDEO_MAX_SECOND, CustomCameraConfig.DEFAULT_MAX_RECORD_VIDEO)
        recordVideoMinSecond = extras.getInt(SimpleCameraX.EXTRA_RECORD_VIDEO_MIN_SECOND, CustomCameraConfig.DEFAULT_MIN_RECORD_VIDEO)
        imageFormat = extras.getString(SimpleCameraX.EXTRA_CAMERA_IMAGE_FORMAT, CameraUtils.JPEG)
        imageFormatForQ = extras.getString(SimpleCameraX.EXTRA_CAMERA_IMAGE_FORMAT_FOR_Q, CameraUtils.MIME_TYPE_IMAGE)
        videoFormat = extras.getString(SimpleCameraX.EXTRA_CAMERA_VIDEO_FORMAT, CameraUtils.MP4)
        videoFormatForQ = extras.getString(SimpleCameraX.EXTRA_CAMERA_VIDEO_FORMAT_FOR_Q, CameraUtils.MIME_TYPE_VIDEO)
        val captureLoadingColor = extras.getInt(SimpleCameraX.EXTRA_CAPTURE_LOADING_COLOR, 0xFF7D7DFF.toInt())
        isDisplayRecordTime = extras.getBoolean(SimpleCameraX.EXTRA_DISPLAY_RECORD_CHANGE_TIME, false)
        mCaptureLayout.setButtonFeatures(buttonFeatures)
        if (recordVideoMaxSecond > 0) {
            setRecordVideoMaxTime(recordVideoMaxSecond)
        }
        if (recordVideoMinSecond > 0) {
            setRecordVideoMinTime(recordVideoMinSecond)
        }
        val format = String.format(
            Locale.getDefault(), "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(recordVideoMaxSecond.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(recordVideoMaxSecond.toLong())
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(recordVideoMaxSecond.toLong()))
        )
        tvCurrentTime.text = format
        if (isAutoRotation && buttonFeatures != CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER) {
            orientationEventListener = CameraXOrientationEventListener(context, this)
            startCheckOrientation()
        }
        setCaptureLoadingColor(captureLoadingColor)
        setProgressColor(captureLoadingColor)
        val isCheckSelfPermission = PermissionChecker.checkSelfPermission(context, arrayOf(Manifest.permission.CAMERA))
        if (isCheckSelfPermission) {
            buildUseCameraCases()
        } else {
            if (CustomCameraConfig.explainListener != null) {
                if (!SimpleXSpUtils.getBoolean(context, Manifest.permission.CAMERA, false)) {
                    CustomCameraConfig.explainListener
                        ?.onPermissionDescription(context, this, Manifest.permission.CAMERA)
                }
            }
            PermissionChecker.getInstance().requestPermissions(
                activity, arrayOf(Manifest.permission.CAMERA),
                object : PermissionResultCallback {
                    override fun onGranted() {
                        buildUseCameraCases()
                        CustomCameraConfig.explainListener?.onDismiss(this@CustomCameraView)
                    }

                    override fun onDenied() {
                        if (CustomCameraConfig.deniedListener != null) {
                            SimpleXSpUtils.putBoolean(context, Manifest.permission.CAMERA, true)
                            CustomCameraConfig.deniedListener?.onDenied(context, Manifest.permission.CAMERA, PermissionChecker.PERMISSION_SETTING_CODE)
                            CustomCameraConfig.explainListener?.onDismiss(this@CustomCameraView)
                        } else {
                            SimpleXPermissionUtil.goIntentSetting(activity, PermissionChecker.PERMISSION_SETTING_CODE)
                        }
                    }
                }
            )
        }
    }

    /**
     * 检测手机方向
     */
    private fun startCheckOrientation() {
        orientationEventListener?.star()
    }

    /**
     * 停止检测手机方向
     */
    fun stopCheckOrientation() {
        orientationEventListener?.stop()
    }

    private fun getTargetRotation(): Int {
        return mImageCapture?.targetRotation ?: 0
    }

    override fun onOrientationChanged(orientation: Int) {
        mImageCapture?.setTargetRotation(orientation)
        mImageAnalyzer?.setTargetRotation(orientation)
    }

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private inner class DisplayListener : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
        }

        override fun onDisplayRemoved(displayId: Int) {
        }

        override fun onDisplayChanged(displayId: Int) {
            if (displayId == this@CustomCameraView.displayId) {
                mImageCapture?.setTargetRotation(mCameraPreviewView.display?.rotation ?: 0)
                mImageAnalyzer?.setTargetRotation(mCameraPreviewView.display?.rotation ?: 0)
            }
        }
    }

    /**
     * 开始打开相机预览
     */
    fun buildUseCameraCases() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                mCameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    /**
     * 初始相机预览模式
     */
    private fun bindCameraUseCases() {
        if (mCameraProvider != null && isBackCameraLevel3Device(mCameraProvider!!)) {
            if (CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER == buttonFeatures) {
                bindCameraVideoUseCases()
            } else {
                bindCameraImageUseCases()
            }
        } else {
            when (buttonFeatures) {
                CustomCameraConfig.BUTTON_STATE_ONLY_CAPTURE -> bindCameraImageUseCases()
                CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER -> bindCameraVideoUseCases()
                else -> bindCameraWithUserCases()
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun isBackCameraLevel3Device(cameraProvider: ProcessCameraProvider): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val cameraInfos = CameraSelector.DEFAULT_BACK_CAMERA
                .filter(cameraProvider.availableCameraInfos) as java.util.List<CameraInfo>
            if (cameraInfos.isNotEmpty()) {
                return Objects.equals(
                    Camera2CameraInfo.from(cameraInfos[0]).getCameraCharacteristic(
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL
                    ), CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                )
            }
        }
        return false
    }

    /**
     * bindCameraWithUserCases
     */
    private fun bindCameraWithUserCases() {
        try {
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // Preview
            val preview = Preview.Builder()
                .setTargetRotation(mCameraPreviewView.display?.rotation ?: 0)
                .build()
            // ImageCapture
            buildImageCapture()
            // VideoCapture
            buildVideoCapture()
            val useCase = UseCaseGroup.Builder()
            useCase.addUseCase(preview)
            mImageCapture?.let { useCase.addUseCase(it as androidx.camera.core.UseCase) }
            mVideoCapture?.let { useCase.addUseCase(it as androidx.camera.core.UseCase) }
            val useCaseGroup = useCase.build()
            // Must unbind the use-cases before rebinding them
            mCameraProvider?.unbindAll()
            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(mCameraPreviewView.surfaceProvider)
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            val camera = mCameraProvider?.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, useCaseGroup
            )
            // setFlashMode
            setFlashMode()
            mCameraInfo = camera?.cameraInfo
            mCameraControl = camera?.cameraControl
            initCameraPreviewListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * bindCameraImageUseCases
     */
    private fun bindCameraImageUseCases() {
        try {
            val screenAspectRatio = aspectRatio(DensityUtil.getScreenWidth(context), DensityUtil.getScreenHeight(context))
            val rotation = mCameraPreviewView.display?.rotation ?: 0
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // ImageCapture
            buildImageCapture()

            // ImageAnalysis
            mImageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()

            // Must unbind the use-cases before rebinding them
            mCameraProvider?.unbindAll()
            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(mCameraPreviewView.surfaceProvider)
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            val camera = mCameraProvider?.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, preview, mImageCapture!!, mImageAnalyzer!!
            )
            // setFlashMode
            setFlashMode()
            mCameraInfo = camera?.cameraInfo
            mCameraControl = camera?.cameraControl
            initCameraPreviewListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * bindCameraVideoUseCases
     */
    private fun bindCameraVideoUseCases() {
        try {
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            // Preview
            val preview = Preview.Builder()
                .setTargetRotation(mCameraPreviewView.display?.rotation ?: 0)
                .build()
            buildVideoCapture()
            // Must unbind the use-cases before rebinding them
            mCameraProvider?.unbindAll()
            // Attach the viewfinder's surface provider to preview use case
            preview.setSurfaceProvider(mCameraPreviewView.surfaceProvider)
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            val camera = mCameraProvider?.bindToLifecycle(
                context as LifecycleOwner, cameraSelector, preview, mVideoCapture as androidx.camera.core.UseCase
            )
            mCameraInfo = camera?.cameraInfo
            mCameraControl = camera?.cameraControl
            initCameraPreviewListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildImageCapture() {
        val screenAspectRatio = aspectRatio(DensityUtil.getScreenWidth(context), DensityUtil.getScreenHeight(context))
        mImageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(mCameraPreviewView.display?.rotation ?: 0)
            .build()
    }

    @SuppressLint("RestrictedApi")
    private fun buildVideoCapture() {
        val videoBuilder = VideoCapture.Builder()
        videoBuilder.setTargetRotation(mCameraPreviewView.display?.rotation ?: 0)
        if (videoFrameRate > 0) {
            videoBuilder.setVideoFrameRate(videoFrameRate)
        }
        if (videoBitRate > 0) {
            videoBuilder.setBitRate(videoBitRate)
        }
        mVideoCapture = videoBuilder.build()
    }

    private fun initCameraPreviewListener() {
        val zoomState: LiveData<ZoomState>? = mCameraInfo?.zoomState
        val cameraXPreviewViewTouchListener = CameraXPreviewViewTouchListener(context)
        cameraXPreviewViewTouchListener.setCustomTouchListener(object : CameraXPreviewViewTouchListener.CustomTouchListener {
            override fun zoom(delta: Float) {
                if (isZoomPreview) {
                    val currentZoomRatio = zoomState?.value?.zoomRatio
                    if (currentZoomRatio != null) {
                        mCameraControl?.setZoomRatio(currentZoomRatio * delta)
                    }
                }
            }

            override fun click(x: Float, y: Float) {
                if (isManualFocus) {
                    val factory = mCameraPreviewView.meteringPointFactory
                    val point = factory.createPoint(x, y)
                    val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
                        .setAutoCancelDuration(3, TimeUnit.SECONDS)
                        .build()
                    if (mCameraInfo?.isFocusMeteringSupported(action) == true) {
                        mCameraControl?.cancelFocusAndMetering()
                        focusImageView.setDisappear(false)
                        focusImageView.startFocus(Point(x.toInt(), y.toInt()))
                        val future = mCameraControl?.startFocusAndMetering(action)
                        future?.addListener({
                            try {
                                val result = future.get()
                                focusImageView.setDisappear(true)
                                if (result.isFocusSuccessful) {
                                    focusImageView.onFocusSuccess()
                                } else {
                                    focusImageView.onFocusFailed()
                                }
                            } catch (e: Exception) {
                                // ignored
                            }
                        }, mainExecutor)
                    }
                }
            }

            override fun doubleClick(x: Float, y: Float) {
                if (isZoomPreview) {
                    val zoomStateValue = zoomState?.value
                    if (zoomStateValue != null) {
                        val currentZoomRatio = zoomStateValue.zoomRatio
                        val minZoomRatio = zoomStateValue.minZoomRatio
                        if (currentZoomRatio > minZoomRatio) {
                            mCameraControl?.setLinearZoom(0f)
                        } else {
                            mCameraControl?.setLinearZoom(0.5f)
                        }
                    }
                }
            }
        })
        mCameraPreviewView.setOnTouchListener(cameraXPreviewViewTouchListener)
    }

    /**
     * [androidx.camera.core.ImageAnalysis.Builder] requires enum value of
     * [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     * <p>
     * Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     * of preview ratio to one of the provided values.
     *
     * @param width  - preview width
     * @param height - preview height
     * @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val aspect = maxOf(width, height).toDouble()
        val previewRatio = aspect / minOf(width, height).toDouble()
        return if (kotlin.math.abs(previewRatio - RATIO_4_3_VALUE) <= kotlin.math.abs(previewRatio - RATIO_16_9_VALUE)) {
            AspectRatio.RATIO_4_3
        } else {
            AspectRatio.RATIO_16_9
        }
    }

    /**
     * 拍照回调
     */
    private class MyImageResultCallback(
        cameraView: CustomCameraView,
        imagePreview: ImageView,
        imagePreviewBg: View,
        captureLayout: CaptureLayout,
        imageCallbackListener: ImageCallbackListener?,
        cameraListener: CameraListener?
    ) : ImageCapture.OnImageSavedCallback {
        private val mImagePreviewReference: WeakReference<ImageView> = WeakReference(imagePreview)
        private val mImagePreviewBgReference: WeakReference<View> = WeakReference(imagePreviewBg)
        private val mCaptureLayoutReference: WeakReference<CaptureLayout> = WeakReference(captureLayout)
        private val mImageCallbackListenerReference: WeakReference<ImageCallbackListener?> = WeakReference(imageCallbackListener)
        private val mCameraListenerReference: WeakReference<CameraListener?> = WeakReference(cameraListener)
        private val mCameraViewLayoutReference: WeakReference<CustomCameraView> = WeakReference(cameraView)

        override fun onImageSaved(@NonNull outputFileResults: ImageCapture.OutputFileResults) {
            val savedUri = outputFileResults.savedUri
            if (savedUri != null) {
                val customCameraView = mCameraViewLayoutReference.get()
                customCameraView?.stopCheckOrientation()
                val mImagePreview = mImagePreviewReference.get()
                if (mImagePreview != null) {
                    val context = mImagePreview.context
                    SimpleCameraX.putOutputUri((context as Activity).intent, savedUri)
                    mImagePreview.visibility = View.VISIBLE
                    if (customCameraView != null && customCameraView.isAutoRotation) {
                        val targetRotation = customCameraView.getTargetRotation()
                        // 这种角度拍出来的图片宽比高大，所以使用ScaleType.FIT_CENTER缩放模式
                        if (targetRotation == Surface.ROTATION_90 || targetRotation == Surface.ROTATION_270) {
                            mImagePreview.adjustViewBounds = true
                        } else {
                            mImagePreview.adjustViewBounds = false
                            mImagePreview.scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                        val mImagePreviewBackground = mImagePreviewBgReference.get()
                        mImagePreviewBackground?.animate()?.alpha(1F)?.setDuration(220)?.start()
                    }
                    val imageCallbackListener = mImageCallbackListenerReference.get()
                    if (imageCallbackListener != null) {
                        val outPutCameraPath = if (FileUtils.isContent(savedUri.toString())) savedUri.toString() else savedUri.path
                        imageCallbackListener.onLoadImage(outPutCameraPath ?: "", mImagePreview)
                    }
                }

                val captureLayout = mCaptureLayoutReference.get()
                captureLayout?.let {
                    it.setButtonCaptureEnabled(true)
                    it.startTypeBtnAnimator()
                }
            }
        }

        override fun onError(@NonNull exception: ImageCaptureException) {
            mCaptureLayoutReference.get()?.setButtonCaptureEnabled(true)
            mCameraListenerReference.get()?.onError(
                exception.imageCaptureError,
                exception.message ?: "", exception.cause
            )
        }
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            val outputPath = SimpleCameraX.getOutputPath(activity.intent)
            startVideoPlay(outputPath)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }
    }

    fun setCameraListener(cameraListener: CameraListener?) {
        this.mCameraListener = cameraListener
    }

    /**
     * 设置录制视频最大时长 秒
     */
    fun setRecordVideoMaxTime(maxDurationTime: Int) {
        mCaptureLayout.setDuration(maxDurationTime)
    }

    /**
     * 设置录制视频最小时长 秒
     */
    fun setRecordVideoMinTime(minDurationTime: Int) {
        mCaptureLayout.setMinDuration(minDurationTime)
    }

    /**
     * 设置拍照时loading色值
     *
     * @param color
     */
    fun setCaptureLoadingColor(color: Int) {
        mCaptureLayout.setCaptureLoadingColor(color)
    }

    /**
     * 设置录像时loading色值
     *
     * @param color
     */
    fun setProgressColor(color: Int) {
        mCaptureLayout.setProgressColor(color)
    }

    /**
     * 切换前后摄像头
     */
    fun toggleCamera() {
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
        bindCameraUseCases()
    }

    /**
     * 闪光灯模式
     */
    private fun setFlashMode() {
        if (mImageCapture == null) {
            return
        }
        when (typeFlash) {
            TYPE_FLASH_AUTO -> {
                mFlashLamp.setImageResource(R.drawable.picture_ic_flash_auto)
                mImageCapture?.setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            }
            TYPE_FLASH_ON -> {
                mFlashLamp.setImageResource(R.drawable.picture_ic_flash_on)
                mImageCapture?.setFlashMode(ImageCapture.FLASH_MODE_ON)
            }
            TYPE_FLASH_OFF -> {
                mFlashLamp.setImageResource(R.drawable.picture_ic_flash_off)
                mImageCapture?.setFlashMode(ImageCapture.FLASH_MODE_OFF)
            }
        }
    }

    /**
     * 关闭相机界面按钮
     *
     * @param clickListener
     */
    fun setOnCancelClickListener(clickListener: ClickListener?) {
        this.mOnClickListener = clickListener
    }

    fun setImageCallbackListener(mImageCallbackListener: ImageCallbackListener?) {
        this.mImageCallbackListener = mImageCallbackListener
    }

    /**
     * 重置状态
     */
    private fun resetState() {
        if (isImageCaptureEnabled()) {
            mImagePreview.visibility = INVISIBLE
            mImagePreviewBg.alpha = 0F
        } else {
            try {
                mVideoCapture?.stopRecording()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mSwitchCamera.visibility = VISIBLE
        mFlashLamp.visibility = VISIBLE
        mCaptureLayout.resetCaptureLayout()
    }

    /**
     * 开始循环播放视频
     *
     * @param url
     */
    private fun startVideoPlay(url: String) {
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = MediaPlayer()
            } else {
                mMediaPlayer?.reset()
            }
            if (FileUtils.isContent(url)) {
                mMediaPlayer?.setDataSource(context, Uri.parse(url))
            } else {
                mMediaPlayer?.setDataSource(url)
            }
            mMediaPlayer?.setSurface(Surface(mTextureView.surfaceTexture))
            mMediaPlayer?.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer?.setOnVideoSizeChangedListener { mp, width, height ->
                updateVideoViewSize(mMediaPlayer?.videoWidth?.toFloat() ?: 0f, mMediaPlayer?.videoHeight?.toFloat() ?: 0f)
            }
            mMediaPlayer?.setOnPreparedListener {
                mMediaPlayer?.start()
            }
            mMediaPlayer?.isLooping = true
            mMediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * updateVideoViewSize
     *
     * @param videoWidth
     * @param videoHeight
     */
    private fun updateVideoViewSize(videoWidth: Float, videoHeight: Float) {
        if (videoWidth > videoHeight) {
            val height = ((videoHeight / videoWidth) * width).toInt()
            val videoViewParam = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, height)
            videoViewParam.addRule(CENTER_IN_PARENT, TRUE)
            mTextureView.layoutParams = videoViewParam
        }
    }

    /**
     * 取消拍摄相关
     */
    fun onCancelMedia() {
        val outputPath = SimpleCameraX.getOutputPath(activity.intent)
        FileUtils.deleteFile(context, outputPath)
        stopVideoPlay()
        resetState()
        startCheckOrientation()
    }

    /**
     * 停止视频播放
     */
    private fun stopVideoPlay() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) {
            mMediaPlayer?.stop()
            mMediaPlayer?.release()
            mMediaPlayer = null
        }
        mTextureView.visibility = View.GONE
    }

    /**
     * onConfigurationChanged
     *
     * @param newConfig
     */
    fun handleConfigurationChanged(@NonNull newConfig: Configuration) {
        buildUseCameraCases()
    }

    /**
     * onDestroy
     */
    fun onDestroy() {
        displayManager.unregisterDisplayListener(displayListener)
        stopCheckOrientation()
        focusImageView.destroy()
    }
}

