package com.luck.picture.lib.basic

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.luck.picture.lib.PictureOnlyCameraFragment.Companion.newInstance
import com.luck.picture.lib.R
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.config.Crop
import com.luck.picture.lib.config.CustomIntentKey
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.config.PermissionEvent
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectLimitType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.dialog.PhotoItemSelectedDialog
import com.luck.picture.lib.dialog.PictureLoadingDialog
import com.luck.picture.lib.dialog.RemindDialog
import com.luck.picture.lib.engine.PictureSelectorEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.immersive.ImmersiveManager
import com.luck.picture.lib.interfaces.OnCallbackIndexListener
import com.luck.picture.lib.interfaces.OnCallbackListener
import com.luck.picture.lib.interfaces.OnItemClickListener
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.interfaces.OnRecordAudioInterceptListener
import com.luck.picture.lib.interfaces.OnRequestPermissionListener
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.language.PictureLanguageUtils
import com.luck.picture.lib.loader.IBridgeMediaLoader
import com.luck.picture.lib.manager.SelectedManager
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.permissions.PermissionConfig
import com.luck.picture.lib.permissions.PermissionResultCallback
import com.luck.picture.lib.permissions.PermissionUtil
import com.luck.picture.lib.service.ForegroundService
import com.luck.picture.lib.thread.PictureThreadUtils
import com.luck.picture.lib.thread.PictureThreadUtils.SimpleTask
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.luck.picture.lib.utils.BitmapUtils
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.FileDirMap
import com.luck.picture.lib.utils.MediaStoreUtils
import com.luck.picture.lib.utils.MediaUtils
import com.luck.picture.lib.utils.PictureFileUtils
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.ToastUtils
import org.json.JSONArray
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * @author：luck
 * @date：2021/11/19 10:02 下午
 * @describe：PictureCommonFragment
 */
abstract class PictureCommonFragment : Fragment(), IPictureSelectorCommonEvent {
    /**
     * PermissionResultCallback
     */
    private var mPermissionResultCallback: PermissionResultCallback? = null

    /**
     * IBridgePictureBehavior
     */
    protected var iBridgePictureBehavior: IBridgePictureBehavior? = null

    /**
     * page
     */
    protected var mPage: Int = 1

    /**
     * Media Loader engine
     */
    protected var mLoader: IBridgeMediaLoader? = null

    /**
     * PictureSelector Config
     */
    protected var selectorConfig: SelectorConfig? = null

    /**
     * Loading Dialog
     */
    private var mLoadingDialog: Dialog? = null

    /**
     * click sound
     */
    private var soundPool: SoundPool? = null

    /**
     * click sound effect id
     */
    private var soundID = 0

    /**
     * fragment enter anim duration
     */
    private var enterAnimDuration: Long = 0

    /**
     * tipsDialog
     */
    protected var tipsDialog: Dialog? = null

    /**
     * Context
     */
    private var context: Context? = null

    override fun onCreateLoader() {
    }

    override val resourceId: Int
        get() = 0


    override fun onFragmentResume() {
    }

    override fun reStartSavedInstance(savedInstanceState: Bundle?) {
    }

    override fun onCheckOriginalChange() {
    }

    override fun dispatchCameraMediaResult(media: LocalMedia?) {
    }


    override fun onSelectedChange(isAddRemove: Boolean, currentMedia: LocalMedia?) {
    }

    override fun onFixedSelectedChange(oldLocalMedia: LocalMedia?) {
    }

    override fun sendChangeSubSelectPositionEvent(adapterChange: Boolean) {
    }

    override fun handlePermissionSettingResult(permissions: Array<String?>?) {
    }

    override fun onEditMedia(intent: Intent?) {
    }

    override fun onEnterFragment() {
    }

    override fun onExitFragment() {
    }

    protected val appContext: Context?
        get() {
            val ctx = context
            if (ctx != null) {
                return ctx
            } else {
                val appContext: Context? =
                    PictureAppMaster.Companion.instance.appContext
                if (appContext != null) {
                    return appContext
                }
            }
            return context
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val callback = mPermissionResultCallback
        if (callback != null && context != null) {
            PermissionChecker.Companion.instance.onRequestPermissionsResult(
                requireContext(),
                permissions.filterNotNull().toTypedArray(),
                grantResults,
                callback
            )
            mPermissionResultCallback = null
        }
    }

    /**
     * Set PermissionResultCallback
     *
     * @param callback
     */
    fun setPermissionsResultAction(callback: PermissionResultCallback?) {
        mPermissionResultCallback = callback
    }

    override fun handlePermissionDenied(permissionArray: Array<String?>?) {
        val permissions = permissionArray ?: arrayOf()
        PermissionConfig.CURRENT_REQUEST_PERMISSION = permissions
        val listener = selectorConfig?.onPermissionDeniedListener
        if (listener != null) {
            onPermissionExplainEvent(false, permissions)
            listener.onDenied(
                this, permissions.filterNotNull().toTypedArray(), PictureConfig.REQUEST_GO_SETTING,
                object : OnCallbackListener<Boolean> {
                    override fun onCall(isResult: Boolean) {
                        if (isResult) {
                            handlePermissionSettingResult(PermissionConfig.CURRENT_REQUEST_PERMISSION)
                        }
                    }
                })
        } else {
            PermissionUtil.goIntentSetting(this, PictureConfig.REQUEST_GO_SETTING)
        }
    }

    protected val isNormalDefaultEnter: Boolean
        /**
         * 使用PictureSelector 默认方式进入
         *
         * @return
         */
        get() = activity is PictureSelectorSupporterActivity || activity is PictureSelectorTransparentActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (resourceId != InjectResourceSource.DEFAULT_LAYOUT_RESOURCE) {
            return inflater.inflate(resourceId, container, false)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectorConfig = SelectorProviders.instance?.selectorConfig
        FileDirMap.init(view.context)
        if (selectorConfig?.viewLifecycle != null) {
            selectorConfig?.viewLifecycle?.onViewCreated(this, view, savedInstanceState)
        }
        if (selectorConfig?.onCustomLoadingListener != null) {
            mLoadingDialog = selectorConfig?.onCustomLoadingListener?.create(this.appContext!!)
        } else {
            mLoadingDialog = PictureLoadingDialog(this.appContext!!)
        }
        setRequestedOrientation()
        setTranslucentStatusBar()
        setRootViewKeyListener(requireView())
        if (selectorConfig?.isOpenClickSound == true && selectorConfig?.isOnlyCamera != true) {
            soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)
            soundID = soundPool!!.load(this.appContext!!, R.raw.ps_click_music, 1)
        }
    }


    /**
     * 设置透明状态栏
     */
    private fun setTranslucentStatusBar() {
        if (selectorConfig?.isPreviewFullScreenMode == true) {
            val selectMainStyle = selectorConfig?.selectorStyle?.selectMainStyle
            ImmersiveManager.translucentStatusBar(
                requireActivity(),
                selectMainStyle?.isDarkStatusBarBlack ?: false
            )
        }
    }

    /**
     * 设置回退监听
     *
     * @param view
     */
    fun setRootViewKeyListener(view: View) {
        if (selectorConfig!!.isNewKeyBackMode) {
            requireActivity().getOnBackPressedDispatcher()
                .addCallback(getViewLifecycleOwner(), object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        onKeyBackFragmentFinish()
                    }
                })
        } else {
            view.setFocusableInTouchMode(true)
            view.requestFocus()
            view.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                        onKeyBackFragmentFinish()
                        return true
                    }
                    return false
                }
            })
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initAppLanguage()
    }


    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val windowAnimationStyle = selectorConfig?.selectorStyle?.windowAnimationStyle
        val loadAnimation: Animation
        if (enter) {
            if (windowAnimationStyle?.activityEnterAnimation != 0) {
                loadAnimation = AnimationUtils.loadAnimation(
                    this.appContext!!,
                    windowAnimationStyle?.activityEnterAnimation ?: 0
                )
            } else {
                loadAnimation =
                    AnimationUtils.loadAnimation(this.appContext!!, R.anim.ps_anim_alpha_enter)
            }
            this.enterAnimationDuration = loadAnimation.duration
            onEnterFragment()
        } else {
            if (windowAnimationStyle?.activityExitAnimation != 0) {
                loadAnimation = AnimationUtils.loadAnimation(
                    this.appContext!!,
                    windowAnimationStyle?.activityExitAnimation ?: 0
                )
            } else {
                loadAnimation =
                    AnimationUtils.loadAnimation(this.appContext!!, R.anim.ps_anim_alpha_exit)
            }
            onExitFragment()
        }
        return loadAnimation
    }


    var enterAnimationDuration: Long
        get() {
            val duration =
                if (enterAnimDuration > 50) enterAnimDuration - 50 else enterAnimDuration
            return if (duration >= 0) duration else 0
        }
        set(duration) {
            this.enterAnimDuration = duration
        }


    override fun confirmSelect(currentMedia: LocalMedia?, isSelected: Boolean): Int {
        if (selectorConfig?.onSelectFilterListener != null) {
            if (selectorConfig?.onSelectFilterListener?.onSelectFilter(currentMedia) == true) {
                var isSelectLimit = false
                if (selectorConfig?.onSelectLimitTipsListener != null) {
                    isSelectLimit = selectorConfig?.onSelectLimitTipsListener
                        ?.onSelectLimitTips(
                            this.appContext,
                            currentMedia,
                            selectorConfig,
                            SelectLimitType.SELECT_NOT_SUPPORT_SELECT_LIMIT
                        ) ?: false
                }
                if (isSelectLimit) {
                } else {
                    ToastUtils.showToast(this.appContext!!, getString(R.string.ps_select_no_support))
                }
                return SelectedManager.INVALID
            }
        }
        val checkSelectValidity = isCheckSelectValidity(currentMedia ?: return SelectedManager.INVALID, isSelected)
        if (checkSelectValidity != SelectedManager.SUCCESS) {
            return SelectedManager.INVALID
        }
        val selectedResult: MutableList<LocalMedia?> = selectorConfig?.selectedResult ?: mutableListOf()
        val resultCode: Int
        if (isSelected) {
            selectedResult.remove(currentMedia)
            resultCode = SelectedManager.REMOVE
        } else {
            if (selectorConfig?.selectionMode == SelectModeConfig.SINGLE) {
                if (selectedResult.size > 0) {
                    sendFixedSelectedChangeEvent(selectedResult[0])
                    selectedResult.clear()
                }
            }
            if (currentMedia != null) {
                selectedResult.add(currentMedia)
                currentMedia.num = selectedResult.size
            }
            resultCode = SelectedManager.ADD_SUCCESS
            playClickEffect()
        }
        sendSelectedChangeEvent(resultCode == SelectedManager.ADD_SUCCESS, currentMedia)
        return resultCode
    }

    /**
     * 验证选择的合法性
     *
     * @param currentMedia 当前选中资源
     * @param isSelected   选中或是取消
     * @return
     */
    protected fun isCheckSelectValidity(currentMedia: LocalMedia, isSelected: Boolean): Int {
        val curMimeType = currentMedia.mimeType
        val curDuration = currentMedia.duration
        val curFileSize = currentMedia.size
        val selectedResult: MutableList<LocalMedia?> = selectorConfig?.selectedResult ?: mutableListOf()
        if (selectorConfig?.isWithVideoImage == true) {
            // 共选型模式
            var selectVideoSize = 0
            for (i in selectedResult.indices) {
                val mimeType = selectedResult.get(i)?.mimeType
                if (PictureMimeType.isHasVideo(mimeType)) {
                    selectVideoSize++
                }
            }
            if (checkWithMimeTypeValidity(
                    currentMedia,
                    isSelected,
                    curMimeType,
                    selectVideoSize,
                    curFileSize,
                    curDuration
                )
            ) {
                return SelectedManager.INVALID
            }
        } else {
            // 单一型模式
            if (checkOnlyMimeTypeValidity(
                    currentMedia,
                    isSelected,
                    curMimeType,
                    selectorConfig!!.resultFirstMimeType,
                    curFileSize,
                    curDuration
                )
            ) {
                return SelectedManager.INVALID
            }
        }
        return SelectedManager.SUCCESS
    }

    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    override fun checkWithMimeTypeValidity(
        media: LocalMedia?,
        isSelected: Boolean,
        curMimeType: String?,
        selectVideoSize: Int,
        fileSize: Long,
        duration: Long
    ): Boolean {
        if (selectorConfig!!.selectMaxFileSize > 0) {
            if (fileSize > selectorConfig!!.selectMaxFileSize) {
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext, media, selectorConfig,
                            SelectLimitType.SELECT_MAX_FILE_SIZE_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                val maxFileSize =
                    PictureFileUtils.formatFileSize(selectorConfig!!.selectMaxFileSize)
                showTipsDialog(getString(R.string.ps_select_max_size, maxFileSize))
                return true
            }
        }
        if (selectorConfig!!.selectMinFileSize > 0) {
            if (fileSize < selectorConfig!!.selectMinFileSize) {
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext, media, selectorConfig,
                            SelectLimitType.SELECT_MIN_FILE_SIZE_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                val minFileSize =
                    PictureFileUtils.formatFileSize(selectorConfig!!.selectMinFileSize)
                showTipsDialog(getString(R.string.ps_select_min_size, minFileSize))
                return true
            }
        }

        if (PictureMimeType.isHasVideo(curMimeType)) {
            if (selectorConfig!!.selectionMode == SelectModeConfig.MULTIPLE) {
                if (selectorConfig!!.maxVideoSelectNum <= 0) {
                    if (selectorConfig?.onSelectLimitTipsListener != null) {
                        val isSelectLimit = selectorConfig?.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                this.appContext,
                                media,
                                selectorConfig,
                                SelectLimitType.SELECT_NOT_WITH_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    // 如果视频可选数量是0
                    showTipsDialog(getString(R.string.ps_rule))
                    return true
                }

                if (!isSelected && selectorConfig!!.selectedResult.size >= selectorConfig!!.maxSelectNum) {
                    if (selectorConfig!!.onSelectLimitTipsListener != null) {
                        val isSelectLimit =                         selectorConfig!!.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                requireContext(),
                                media,
                                selectorConfig,
                                SelectLimitType.SELECT_MAX_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(
                        getString(
                            R.string.ps_message_max_num,
                            selectorConfig!!.maxSelectNum
                        )
                    )
                    return true
                }

                if (!isSelected && selectVideoSize >= selectorConfig!!.maxVideoSelectNum) {
                    // 如果选择的是视频
                    if (selectorConfig!!.onSelectLimitTipsListener != null) {
                        val isSelectLimit =                         selectorConfig!!.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                requireContext(),
                                media,
                                selectorConfig,
                                SelectLimitType.SELECT_MAX_VIDEO_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(
                        getTipsMsg(
                            this.appContext!!,
                            curMimeType,
                            selectorConfig?.maxVideoSelectNum ?: 0
                        )
                    )
                    return true
                }
            }

            if (!isSelected && selectorConfig!!.selectMinDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration
                ) < selectorConfig!!.selectMinDurationSecond
            ) {
                // 视频小于最低指定的长度
                if (selectorConfig?.onSelectLimitTipsListener != null) {
                    val isSelectLimit = selectorConfig?.onSelectLimitTipsListener
                        ?.onSelectLimitTips(
                            this.appContext, media, selectorConfig,
                            SelectLimitType.SELECT_MIN_VIDEO_SECOND_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_select_video_min_second,
                        selectorConfig!!.selectMinDurationSecond / 1000
                    )
                )
                return true
            }

            if (!isSelected && selectorConfig!!.selectMaxDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration
                ) > selectorConfig!!.selectMaxDurationSecond
            ) {
                // 视频时长超过了指定的长度
                if (selectorConfig?.onSelectLimitTipsListener != null) {
                    val isSelectLimit = selectorConfig?.onSelectLimitTipsListener
                        ?.onSelectLimitTips(
                            this.appContext, media, selectorConfig,
                            SelectLimitType.SELECT_MAX_VIDEO_SECOND_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_select_video_max_second,
                        selectorConfig!!.selectMaxDurationSecond / 1000
                    )
                )
                return true
            }
        } else {
            if (selectorConfig!!.selectionMode == SelectModeConfig.MULTIPLE) {
                if (!isSelected && selectorConfig!!.selectedResult.size >= selectorConfig!!.maxSelectNum) {
                    if (selectorConfig?.onSelectLimitTipsListener != null) {
                        val isSelectLimit = selectorConfig?.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                this.appContext, media, selectorConfig,
                                SelectLimitType.SELECT_MAX_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(
                        getString(
                            R.string.ps_message_max_num,
                            selectorConfig!!.maxSelectNum
                        )
                    )
                    return true
                }
            }
        }
        return false
    }

    @SuppressLint("StringFormatInvalid")
    override fun checkOnlyMimeTypeValidity(
        media: LocalMedia?,
        isSelected: Boolean,
        curMimeType: String?,
        existMimeType: String?,
        fileSize: Long,
        duration: Long
    ): Boolean {
        if (PictureMimeType.isMimeTypeSame(existMimeType, curMimeType)) {
            // ignore
        } else {
                    if (selectorConfig?.onSelectLimitTipsListener != null) {
                        val isSelectLimit = selectorConfig?.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                this.appContext,
                                media,
                                selectorConfig,
                                SelectLimitType.SELECT_NOT_WITH_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
            showTipsDialog(getString(R.string.ps_rule))
            return true
        }
        if (selectorConfig!!.selectMaxFileSize > 0) {
            if (fileSize > selectorConfig!!.selectMaxFileSize) {
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext, media, selectorConfig,
                            SelectLimitType.SELECT_MAX_FILE_SIZE_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                val maxFileSize =
                    PictureFileUtils.formatFileSize(selectorConfig!!.selectMaxFileSize)
                showTipsDialog(getString(R.string.ps_select_max_size, maxFileSize))
                return true
            }
        }
        if (selectorConfig!!.selectMinFileSize > 0) {
            if (fileSize < selectorConfig!!.selectMinFileSize) {
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext, media, selectorConfig,
                            SelectLimitType.SELECT_MIN_FILE_SIZE_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                val minFileSize =
                    PictureFileUtils.formatFileSize(selectorConfig!!.selectMinFileSize)
                showTipsDialog(getString(R.string.ps_select_min_size, minFileSize))
                return true
            }
        }
        if (PictureMimeType.isHasVideo(curMimeType)) {
            if (selectorConfig!!.selectionMode == SelectModeConfig.MULTIPLE) {
                selectorConfig!!.maxVideoSelectNum =
                    if (selectorConfig!!.maxVideoSelectNum > 0) selectorConfig!!.maxVideoSelectNum else selectorConfig!!.maxSelectNum
                if (!isSelected && selectorConfig!!.selectCount >= selectorConfig!!.maxVideoSelectNum) {
                    // 如果先选择的是视频
                    if (selectorConfig!!.onSelectLimitTipsListener != null) {
                        val isSelectLimit =                         selectorConfig!!.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                requireContext(),
                                media,
                                selectorConfig,
                                SelectLimitType.SELECT_MAX_VIDEO_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(
                        getTipsMsg(
                            this.appContext!!,
                            curMimeType,
                            selectorConfig?.maxVideoSelectNum ?: 0
                        )
                    )
                    return true
                }
            }
            if (!isSelected && selectorConfig!!.selectMinDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration
                ) < selectorConfig!!.selectMinDurationSecond
            ) {
                // 视频小于最低指定的长度
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext,
                            media,
                            selectorConfig,
                            SelectLimitType.SELECT_MIN_VIDEO_SECOND_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_select_video_min_second,
                        selectorConfig!!.selectMinDurationSecond / 1000
                    )
                )
                return true
            }

            if (!isSelected && selectorConfig!!.selectMaxDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration
                ) > selectorConfig!!.selectMaxDurationSecond
            ) {
                // 视频时长超过了指定的长度
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext,
                            media,
                            selectorConfig,
                            SelectLimitType.SELECT_MAX_VIDEO_SECOND_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_select_video_max_second,
                        selectorConfig!!.selectMaxDurationSecond / 1000
                    )
                )
                return true
            }
        } else if (PictureMimeType.isHasAudio(curMimeType)) {
            if (selectorConfig!!.selectionMode == SelectModeConfig.MULTIPLE) {
                if (!isSelected && selectorConfig!!.selectedResult.size >= selectorConfig!!.maxSelectNum) {
                    if (selectorConfig!!.onSelectLimitTipsListener != null) {
                        val isSelectLimit =                         selectorConfig!!.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                requireContext(),
                                media,
                                selectorConfig,
                                SelectLimitType.SELECT_MAX_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(
                        PictureCommonFragment.Companion.getTipsMsg(
                            requireContext(),
                            curMimeType,
                            selectorConfig!!.maxSelectNum
                        )
                    )
                    return true
                }
            }

            if (!isSelected && selectorConfig!!.selectMinDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration
                ) < selectorConfig!!.selectMinDurationSecond
            ) {
                // 音频小于最低指定的长度
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext,
                            media,
                            selectorConfig,
                            SelectLimitType.SELECT_MIN_AUDIO_SECOND_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_select_audio_min_second,
                        selectorConfig!!.selectMinDurationSecond / 1000
                    )
                )
                return true
            }
            if (!isSelected && selectorConfig!!.selectMaxDurationSecond > 0 && DateUtils.millisecondToSecond(
                    duration
                ) > selectorConfig!!.selectMaxDurationSecond
            ) {
                // 音频时长超过了指定的长度
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext,
                            media,
                            selectorConfig,
                            SelectLimitType.SELECT_MAX_AUDIO_SECOND_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_select_audio_max_second,
                        selectorConfig!!.selectMaxDurationSecond / 1000
                    )
                )
                return true
            }
        } else {
            if (selectorConfig!!.selectionMode == SelectModeConfig.MULTIPLE) {
                if (!isSelected && selectorConfig!!.selectedResult.size >= selectorConfig!!.maxSelectNum) {
                    if (selectorConfig!!.onSelectLimitTipsListener != null) {
                        val isSelectLimit =                         selectorConfig!!.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                requireContext(),
                                media,
                                selectorConfig,
                                SelectLimitType.SELECT_MAX_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(
                        PictureCommonFragment.Companion.getTipsMsg(
                            requireContext(),
                            curMimeType,
                            selectorConfig!!.maxSelectNum
                        )
                    )
                    return true
                }
            }
        }
        return false
    }

    /**
     * 提示Dialog
     *
     * @param tips
     */
    private fun showTipsDialog(tips: String?) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        try {
            if (tipsDialog != null && tipsDialog!!.isShowing()) {
                return
            }
            tipsDialog = RemindDialog.buildDialog(this.appContext!!, tips)
            tipsDialog?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun sendSelectedChangeEvent(isAddRemove: Boolean, currentMedia: LocalMedia?) {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            val fragments = activity?.supportFragmentManager?.fragments ?: emptyList()
            for (i in fragments.indices) {
                val fragment = fragments[i]
                if (fragment is PictureCommonFragment) {
                    fragment.onSelectedChange(isAddRemove, currentMedia)
                }
            }
        }
    }

    override fun sendFixedSelectedChangeEvent(currentMedia: LocalMedia?) {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            val fragments = activity?.supportFragmentManager?.fragments ?: emptyList()
            for (i in fragments.indices) {
                val fragment = fragments[i]
                if (fragment is PictureCommonFragment) {
                    fragment.onFixedSelectedChange(currentMedia)
                }
            }
        }
    }

    override fun sendSelectedOriginalChangeEvent() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            val fragments = activity?.supportFragmentManager?.fragments ?: emptyList()
            for (i in fragments.indices) {
                val fragment = fragments[i]
                if (fragment is PictureCommonFragment) {
                    fragment.onCheckOriginalChange()
                }
            }
        }
    }

    override fun openSelectedCamera() {
        when (selectorConfig!!.chooseMode) {
            SelectMimeType.TYPE_ALL -> if (selectorConfig!!.ofAllCameraType == SelectMimeType.ofImage()) {
                openImageCamera()
            } else if (selectorConfig!!.ofAllCameraType == SelectMimeType.ofVideo()) {
                openVideoCamera()
            } else {
                onSelectedOnlyCamera()
            }

            SelectMimeType.TYPE_IMAGE -> openImageCamera()
            SelectMimeType.TYPE_VIDEO -> openVideoCamera()
            SelectMimeType.TYPE_AUDIO -> openSoundRecording()
            else -> {}
        }
    }


    override fun onSelectedOnlyCamera() {
        val selectedDialog: PhotoItemSelectedDialog =
            PhotoItemSelectedDialog.Companion.newInstance()
        selectedDialog.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                when (position) {
                    PhotoItemSelectedDialog.Companion.IMAGE_CAMERA -> if (selectorConfig!!.onCameraInterceptListener != null) {
                        onInterceptCameraEvent(SelectMimeType.TYPE_IMAGE)
                    } else {
                        openImageCamera()
                    }

                    PhotoItemSelectedDialog.Companion.VIDEO_CAMERA -> if (selectorConfig!!.onCameraInterceptListener != null) {
                        onInterceptCameraEvent(SelectMimeType.TYPE_VIDEO)
                    } else {
                        openVideoCamera()
                    }

                    else -> {}
                }
            }
        })
        selectedDialog.setOnDismissListener(object : PhotoItemSelectedDialog.OnDismissListener {
            override fun onDismiss(isCancel: Boolean, dialog: DialogInterface?) {
                if (selectorConfig!!.isOnlyCamera && isCancel) {
                    onKeyBackFragmentFinish()
                }
            }
        })
        selectedDialog.show(getChildFragmentManager(), "PhotoItemSelectedDialog")
    }

    override fun openImageCamera() {
        onPermissionExplainEvent(true, PermissionConfig.CAMERA)
        if (selectorConfig!!.onPermissionsEventListener != null) {
            onApplyPermissionsEvent(PermissionEvent.EVENT_IMAGE_CAMERA, PermissionConfig.CAMERA)
        } else {
            PermissionChecker.Companion.instance.requestPermissions(
                this, PermissionConfig.CAMERA,
                object : PermissionResultCallback {
                    override fun onGranted() {
                        startCameraImageCapture()
                    }

                    override fun onDenied() {
                        handlePermissionDenied(PermissionConfig.CAMERA)
                    }
                })
        }
    }

    /**
     * Start ACTION_IMAGE_CAPTURE
     */
    protected fun startCameraImageCapture() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            onPermissionExplainEvent(false, null)
            if (selectorConfig!!.onCameraInterceptListener != null) {
                onInterceptCameraEvent(SelectMimeType.TYPE_IMAGE)
            } else {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (cameraIntent.resolveActivity(activity?.packageManager!!) != null) {
                    val ctx = requireContext()
                    val config = selectorConfig!!
                    ForegroundService.Companion.startForegroundService(
                        ctx,
                        config.isCameraForegroundService
                    )
                    val imageUri =
                        MediaStoreUtils.createCameraOutImageUri(ctx, config)
                    if (imageUri != null) {
                        if (selectorConfig!!.isCameraAroundState) {
                            cameraIntent.putExtra(
                                PictureConfig.CAMERA_FACING,
                                PictureConfig.CAMERA_BEFORE
                            )
                        }
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(cameraIntent, PictureConfig.REQUEST_CAMERA)
                    }
                }
            }
        }
    }


    override fun openVideoCamera() {
        onPermissionExplainEvent(true, PermissionConfig.CAMERA)
        if (selectorConfig!!.onPermissionsEventListener != null) {
            onApplyPermissionsEvent(PermissionEvent.EVENT_VIDEO_CAMERA, PermissionConfig.CAMERA)
        } else {
            PermissionChecker.Companion.instance.requestPermissions(
                this, PermissionConfig.CAMERA,
                object : PermissionResultCallback {
                    override fun onGranted() {
                        startCameraVideoCapture()
                    }

                    override fun onDenied() {
                        handlePermissionDenied(PermissionConfig.CAMERA)
                    }
                })
        }
    }

    /**
     * Start ACTION_VIDEO_CAPTURE
     */
    protected fun startCameraVideoCapture() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            onPermissionExplainEvent(false, null)
            if (selectorConfig!!.onCameraInterceptListener != null) {
                onInterceptCameraEvent(SelectMimeType.TYPE_VIDEO)
            } else {
                val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                if (cameraIntent.resolveActivity(activity?.packageManager!!) != null) {
                    val ctx = requireContext()
                    val config = selectorConfig!!
                    ForegroundService.Companion.startForegroundService(
                        ctx,
                        config.isCameraForegroundService
                    )
                    val videoUri =
                        MediaStoreUtils.createCameraOutVideoUri(ctx, config)
                    if (videoUri != null) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                        if (selectorConfig!!.isCameraAroundState) {
                            cameraIntent.putExtra(
                                PictureConfig.CAMERA_FACING,
                                PictureConfig.CAMERA_BEFORE
                            )
                        }
                        cameraIntent.putExtra(
                            PictureConfig.EXTRA_QUICK_CAPTURE,
                            selectorConfig!!.isQuickCapture
                        )
                        cameraIntent.putExtra(
                            MediaStore.EXTRA_DURATION_LIMIT,
                            selectorConfig!!.recordVideoMaxSecond
                        )
                        cameraIntent.putExtra(
                            MediaStore.EXTRA_VIDEO_QUALITY,
                            selectorConfig!!.videoQuality
                        )
                        startActivityForResult(cameraIntent, PictureConfig.REQUEST_CAMERA)
                    }
                }
            }
        }
    }


    override fun openSoundRecording() {
        if (selectorConfig!!.onRecordAudioListener != null) {
            val ctx = requireContext()
            val config = selectorConfig!!
            ForegroundService.Companion.startForegroundService(
                ctx,
                config.isCameraForegroundService
            )
            config.onRecordAudioListener?.onRecordAudio(this, PictureConfig.REQUEST_CAMERA)
        } else {
            throw NullPointerException(OnRecordAudioInterceptListener::class.java.simpleName + " interface needs to be implemented for recording")
        }
    }


    /**
     * 拦截相机事件并处理返回结果
     */
    override fun onInterceptCameraEvent(cameraMode: Int) {
        ForegroundService.Companion.startForegroundService(
            requireContext(),
            selectorConfig!!.isCameraForegroundService
        )
        selectorConfig!!.onCameraInterceptListener?.openCamera(
            this,
            cameraMode,
            PictureConfig.REQUEST_CAMERA
        )
    }

    /**
     * 权限申请
     *
     * @param permissionArray
     */
    override fun onApplyPermissionsEvent(event: Int, permissionArray: Array<String?>?) {
        val listener = selectorConfig!!.onPermissionsEventListener
        listener?.requestPermission(
            this, permissionArray,
            object : OnRequestPermissionListener {
                override fun onCall(permissionArray: Array<String?>?, isResult: Boolean) {
                    if (isResult) {
                        if (event == PermissionEvent.EVENT_VIDEO_CAMERA) {
                            startCameraVideoCapture()
                        } else {
                            startCameraImageCapture()
                        }
                    } else {
                        handlePermissionDenied(permissionArray)
                    }
                }
            })
    }

    /**
     * 权限说明
     *
     * @param permissionArray
     */
    override fun onPermissionExplainEvent(
        isDisplayExplain: Boolean,
        permissionArray: Array<String?>?
    ) {
        val permissions = permissionArray ?: arrayOf()
        if (selectorConfig!!.onPermissionDescriptionListener != null) {
            val appCtx = this.appContext
            val nonNullPermissions = permissions.mapNotNull { it }.toTypedArray()
            if (appCtx != null && PermissionChecker.Companion.isCheckSelfPermission(
                    appCtx, nonNullPermissions
                )
            ) {
                selectorConfig!!.onPermissionDescriptionListener!!.onDismiss(this)
            } else {
                if (isDisplayExplain && permissions.isNotEmpty()) {
                    val permissionStatus =
                        PermissionUtil.getPermissionStatus(requireActivity(), permissions[0] ?: "")
                    if (permissionStatus != PermissionUtil.REFUSE_PERMANENT) {
                        selectorConfig!!.onPermissionDescriptionListener!!.onPermissionDescription(
                            this,
                            permissions.filterNotNull().toTypedArray()
                        )
                    }
                } else {
                    selectorConfig!!.onPermissionDescriptionListener!!.onDismiss(this)
                }
            }
        }
    }

    /**
     * 点击选择的音效
     */
    private fun playClickEffect() {
        if (soundPool != null && selectorConfig!!.isOpenClickSound) {
            soundPool!!.play(soundID, 0.1f, 0.5f, 0, 1, 1f)
        }
    }

    /**
     * 释放音效资源
     */
    private fun releaseSoundPool() {
        try {
            if (soundPool != null) {
                soundPool!!.release()
                soundPool = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PictureConfig.REQUEST_CAMERA) {
                dispatchHandleCamera(data)
            } else if (requestCode == Crop.REQUEST_EDIT_CROP) {
                onEditMedia(data)
            } else if (requestCode == Crop.REQUEST_CROP) {
                val selectedResult: MutableList<LocalMedia?> = selectorConfig?.selectedResult ?: mutableListOf()
                try {
                    if (selectedResult.size == 1) {
                        val media = selectedResult[0]
                        if (media != null && data != null) {
                            val output = Crop.getOutput(data)
                            media.cutPath = output?.path ?: ""
                            media.setCut(!TextUtils.isEmpty(media.cutPath))
                            media.cropImageWidth = Crop.getOutputImageWidth(data)
                            media.cropImageHeight = Crop.getOutputImageHeight(data)
                            media.cropOffsetX = Crop.getOutputImageOffsetX(data)
                            media.cropOffsetY = Crop.getOutputImageOffsetY(data)
                            media.cropResultAspectRatio = Crop.getOutputCropAspectRatio(data)
                            media.customData = Crop.getOutputCustomExtraData(data)
                            media.sandboxPath = media.cutPath
                        }
                    } else {
                        var extra = data?.getStringExtra(MediaStore.EXTRA_OUTPUT)
                        if (TextUtils.isEmpty(extra)) {
                            extra = data?.getStringExtra(CustomIntentKey.EXTRA_OUTPUT_URI)
                        }
                        val array = JSONArray(extra ?: "[]")
                        if (array.length() == selectedResult.size) {
                            for (i in selectedResult.indices) {
                                val media = selectedResult[i]
                                if (media != null) {
                                    val item = array.optJSONObject(i)
                                    media.cutPath = item.optString(CustomIntentKey.EXTRA_OUT_PUT_PATH)
                                    media.setCut(!TextUtils.isEmpty(media.cutPath))
                                    media.cropImageWidth = item.optInt(CustomIntentKey.EXTRA_IMAGE_WIDTH)
                                    media.cropImageHeight = item.optInt(CustomIntentKey.EXTRA_IMAGE_HEIGHT)
                                    media.cropOffsetX = item.optInt(CustomIntentKey.EXTRA_OFFSET_X)
                                    media.cropOffsetY = item.optInt(CustomIntentKey.EXTRA_OFFSET_Y)
                                    media.cropResultAspectRatio =
                                        item.optDouble(CustomIntentKey.EXTRA_ASPECT_RATIO).toFloat()
                                    media.customData = item.optString(CustomIntentKey.EXTRA_CUSTOM_EXTRA_DATA)
                                    media.sandboxPath = media.cutPath
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val appCtx = this.appContext
                    if (appCtx != null) {
                        ToastUtils.showToast(appCtx, e.message)
                    }
                }

                val result = ArrayList<LocalMedia?>(selectedResult)
                if (checkCompressValidity()) {
                    onCompress(result)
                } else if (checkOldCompressValidity()) {
                    onOldCompress(result)
                } else {
                    onResultEvent(result)
                }
            }
        } else if (resultCode == Crop.RESULT_CROP_ERROR) {
            val throwable = if (data != null) Crop.getError(data) else Throwable("image crop error")
            if (throwable != null) {
                ToastUtils.showToast(requireContext(), throwable.message ?: "")
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == PictureConfig.REQUEST_CAMERA) {
                if (!TextUtils.isEmpty(selectorConfig!!.cameraPath)) {
                    MediaUtils.deleteUri(requireContext(), selectorConfig!!.cameraPath)
                    selectorConfig?.cameraPath = ""
                }
            } else if (requestCode == PictureConfig.REQUEST_GO_SETTING) {
                handlePermissionSettingResult(PermissionConfig.CURRENT_REQUEST_PERMISSION)
            }
        }
        ForegroundService.Companion.stopService(requireContext())
    }

    /**
     * 相机事件回调处理
     */
    private fun dispatchHandleCamera(intent: Intent?) {
        PictureThreadUtils.executeByIo<LocalMedia?>(object : SimpleTask<LocalMedia?>() {
            override fun doInBackground(): LocalMedia? {
                val outputPath = getOutputPath(intent)
                if (!TextUtils.isEmpty(outputPath)) {
                    selectorConfig?.cameraPath = outputPath
                }
                if (TextUtils.isEmpty(selectorConfig?.cameraPath)) {
                    return null
                }
                if (selectorConfig?.chooseMode == SelectMimeType.ofAudio()) {
                    copyOutputAudioToDir()
                }
                val media = buildLocalMedia(selectorConfig?.cameraPath ?: "")
                media.isCameraSource = true
                return media
            }

            override fun onSuccess(result: LocalMedia?) {
                PictureThreadUtils.cancel(this)
                if (result != null) {
                    onScannerScanFile(result)
                    dispatchCameraMediaResult(result)
                }
                selectorConfig?.cameraPath = ""
            }
        })
    }

    /**
     * copy录音文件至指定目录
     */
    private fun copyOutputAudioToDir() {
        try {
            if (!TextUtils.isEmpty(selectorConfig!!.outPutAudioDir)) {
                val inputStream = if (PictureMimeType.isContent(selectorConfig!!.cameraPath))
                    PictureContentResolver.openInputStream(
                        requireContext(),
                        Uri.parse(selectorConfig!!.cameraPath)
                    )
                else
                    FileInputStream(selectorConfig!!.cameraPath)
                val audioFileName: String?
                if (TextUtils.isEmpty(selectorConfig!!.outPutAudioFileName)) {
                    audioFileName = ""
                } else {
                    audioFileName = if (selectorConfig?.isOnlyCamera == true)
                        selectorConfig?.outPutAudioFileName
                    else
                        System.currentTimeMillis()
                            .toString() + "_" + selectorConfig?.outPutAudioFileName
                }
                val outputFile = PictureFileUtils.createCameraFile(
                    this.appContext!!,
                    selectorConfig?.chooseMode ?: 0, audioFileName ?: "", "", selectorConfig?.outPutAudioDir ?: ""
                )
                val outputStream = FileOutputStream(outputFile.absolutePath)
                if (PictureFileUtils.writeFileFromIS(inputStream, outputStream)) {
                    MediaUtils.deleteUri(this.appContext!!, selectorConfig?.cameraPath ?: "")
                    selectorConfig?.cameraPath = outputFile.absolutePath
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    /**
     * 尝试匹配查找自定义相机返回的路径
     *
     * @param data
     * @return
     */
    protected fun getOutputPath(data: Intent?): String? {
        if (data == null) {
            return null
        }
        var outPutUri = data.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
        val cameraPath = selectorConfig?.cameraPath
        val isCameraFileExists =
            TextUtils.isEmpty(cameraPath) || PictureMimeType.isContent(cameraPath) || File(
                cameraPath
            ).exists()
        if ((selectorConfig!!.chooseMode == SelectMimeType.ofAudio() || !isCameraFileExists) && outPutUri == null) {
            outPutUri = data.data
        }
        if (outPutUri == null) {
            return null
        }
        return if (PictureMimeType.isContent(outPutUri.toString())) outPutUri.toString() else outPutUri.path
    }

    /**
     * 刷新相册
     *
     * @param media 要刷新的对象
     */
    private fun onScannerScanFile(media: LocalMedia) {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        if (SdkVersionUtils.isQ) {
            if (PictureMimeType.isHasVideo(media.mimeType) && PictureMimeType.isContent(media.path)) {
                PictureMediaScannerConnection(requireActivity(), media.realPath ?: "")
            }
        } else {
            val path =
                if (PictureMimeType.isContent(media.path)) media.realPath else media.path
            PictureMediaScannerConnection(activity ?: return, path)
            if (PictureMimeType.isHasImage(media.mimeType)) {
                val dirFile = File(path)
                val lastImageId =
                    MediaUtils.getDCIMLastImageId(this.appContext!!, dirFile.parent)
                if (lastImageId != -1) {
                    MediaUtils.removeMedia(this.appContext!!, lastImageId)
                }
            }
        }
    }

    /**
     * buildLocalMedia
     *
     * @param absolutePath
     */
    protected fun buildLocalMedia(absolutePath: String?): LocalMedia {
        val media: LocalMedia =
            LocalMedia.Companion.generateLocalMedia(requireContext(), absolutePath ?: "")
        media.chooseModel = selectorConfig!!.chooseMode
        if (SdkVersionUtils.isQ && !PictureMimeType.isContent(absolutePath ?: "")) {
            media.sandboxPath = absolutePath
        } else {
            media.sandboxPath = null
        }
        if (selectorConfig!!.isCameraRotateImage && PictureMimeType.isHasImage(media.mimeType)) {
            BitmapUtils.rotateImage(this.appContext, absolutePath ?: "")
        }
        return media
    }

    /**
     * 验证完成选择的先决条件
     *
     * @return
     */
    private fun checkCompleteSelectLimit(): Boolean {
        if (selectorConfig!!.selectionMode != SelectModeConfig.MULTIPLE || selectorConfig!!.isOnlyCamera) {
            return false
        }
        if (selectorConfig!!.isWithVideoImage) {
            // 共选型模式
            val selectedResult = selectorConfig!!.selectedResult
            var selectImageSize = 0
            var selectVideoSize = 0
            for (i in selectedResult.indices) {
                val mimeType = selectedResult.get(i)?.mimeType
                if (PictureMimeType.isHasVideo(mimeType)) {
                    selectVideoSize++
                } else {
                    selectImageSize++
                }
            }
            if (selectorConfig!!.minSelectNum > 0) {
                if (selectImageSize < selectorConfig!!.minSelectNum) {
                    if (selectorConfig!!.onSelectLimitTipsListener != null) {
                        val isSelectLimit =                         selectorConfig!!.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                requireContext(),
                                null,
                                selectorConfig,
                                SelectLimitType.SELECT_MIN_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(
                        getString(
                            R.string.ps_min_img_num,
                            selectorConfig!!.minSelectNum.toString()
                        )
                    )
                    return true
                }
            }
            if (selectorConfig!!.minVideoSelectNum > 0) {
                if (selectVideoSize < selectorConfig!!.minVideoSelectNum) {
                    if (selectorConfig!!.onSelectLimitTipsListener != null) {
                        val isSelectLimit =                         selectorConfig!!.onSelectLimitTipsListener
                            ?.onSelectLimitTips(
                                requireContext(),
                                null,
                                selectorConfig,
                                SelectLimitType.SELECT_MIN_VIDEO_SELECT_LIMIT
                            ) ?: false
                        if (isSelectLimit) {
                            return true
                        }
                    }
                    showTipsDialog(
                        getString(
                            R.string.ps_min_video_num,
                            selectorConfig!!.minVideoSelectNum.toString()
                        )
                    )
                    return true
                }
            }
        } else {
            // 单类型模式
            val mimeType = selectorConfig!!.resultFirstMimeType
            if (PictureMimeType.isHasImage(mimeType) && selectorConfig!!.minSelectNum > 0 && selectorConfig!!.selectCount < selectorConfig!!.minSelectNum) {
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext,
                            null,
                            selectorConfig,
                            SelectLimitType.SELECT_MIN_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_min_img_num,
                        selectorConfig!!.minSelectNum.toString()
                    )
                )
                return true
            }
            if (PictureMimeType.isHasVideo(mimeType) && selectorConfig!!.minVideoSelectNum > 0 && selectorConfig!!.selectCount < selectorConfig!!.minVideoSelectNum) {
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext,
                            null,
                            selectorConfig,
                            SelectLimitType.SELECT_MIN_VIDEO_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_min_video_num,
                        selectorConfig!!.minVideoSelectNum.toString()
                    )
                )
                return true
            }

            if (PictureMimeType.isHasAudio(mimeType) && selectorConfig!!.minAudioSelectNum > 0 && selectorConfig!!.selectCount < selectorConfig!!.minAudioSelectNum) {
                val limitTipsListener = selectorConfig?.onSelectLimitTipsListener
                if (limitTipsListener != null) {
                    val isSelectLimit = limitTipsListener
                        .onSelectLimitTips(
                            this.appContext,
                            null,
                            selectorConfig,
                            SelectLimitType.SELECT_MIN_AUDIO_SELECT_LIMIT
                        ) ?: false
                    if (isSelectLimit) {
                        return true
                    }
                }
                showTipsDialog(
                    getString(
                        R.string.ps_min_audio_num,
                        selectorConfig!!.minAudioSelectNum.toString()
                    )
                )
                return true
            }
        }
        return false
    }

    /**
     * 分发处理结果，比如压缩、裁剪、沙盒路径转换
     */
    protected fun dispatchTransformResult() {
        if (checkCompleteSelectLimit()) {
            return
        }
        if (!isAdded()) {
            return
        }
        val selectedResult = selectorConfig?.selectedResult ?: mutableListOf()
        val result = ArrayList<LocalMedia?>(selectedResult.map { it as LocalMedia? })
        if (checkCropValidity()) {
            onCrop(result)
        } else if (checkOldCropValidity()) {
            onOldCrop(result)
        } else if (checkCompressValidity()) {
            onCompress(result)
        } else if (checkOldCompressValidity()) {
            onOldCompress(result)
        } else {
            onResultEvent(result)
        }
    }

    override fun onCrop(result: ArrayList<LocalMedia?>?) {
        val cropResult = result ?: arrayListOf()
        var srcUri: Uri? = null
        var destinationUri: Uri? = null
        val dataCropSource = ArrayList<String?>()
        for (i in cropResult.indices) {
            val media = cropResult[i]
            if (media != null) {
                dataCropSource.add(media.availablePath)
                if (srcUri == null && PictureMimeType.isHasImage(media.mimeType)) {
                    val currentCropPath = media.availablePath
                    if (PictureMimeType.isContent(currentCropPath) || PictureMimeType.isHasHttp(
                            currentCropPath
                        )
                    ) {
                        srcUri = Uri.parse(currentCropPath)
                    } else {
                        srcUri = Uri.fromFile(File(currentCropPath))
                    }
                    val fileName = DateUtils.getCreateFileName("CROP_") + ".jpg"
                    val context = this.appContext
                    if (context != null) {
                        val externalFilesDir =
                            File(FileDirMap.getFileDirPath(context, SelectMimeType.TYPE_IMAGE))
                        val outputFile = File(externalFilesDir.absolutePath, fileName)
                        destinationUri = Uri.fromFile(outputFile)
                    }
                }
            }
        }
        val cropEngine = selectorConfig?.cropFileEngine
        if (cropEngine != null) {
            cropEngine.onStartCrop(
                this,
                srcUri,
                destinationUri,
                dataCropSource,
                Crop.REQUEST_CROP
            )
        }
    }

    override fun onOldCrop(result: ArrayList<LocalMedia?>?) {
        val cropResult = result ?: arrayListOf()
        var currentLocalMedia: LocalMedia? = null
        for (i in cropResult.indices) {
            val item: LocalMedia? = cropResult[i]
            if (item != null && PictureMimeType.isHasImage(item.mimeType)) {
                currentLocalMedia = item
                break
            }
        }
        val cropEngine = selectorConfig?.cropEngine
        if (cropEngine != null) {
            cropEngine.onStartCrop(this, currentLocalMedia, cropResult, Crop.REQUEST_CROP)
        }
    }

    override fun onCompress(result: ArrayList<LocalMedia?>?) {
        val compressResult = result ?: arrayListOf()
        showLoading()
        val queue = ConcurrentHashMap<String?, LocalMedia?>()
        val source = ArrayList<Uri?>()
        for (i in compressResult.indices) {
            val media = compressResult[i]
            if (media != null) {
                val availablePath = media.availablePath
                if (PictureMimeType.isHasHttp(availablePath)) {
                    continue
                }
                val config = selectorConfig
                if (config != null && config.isCheckOriginalImage && config.isOriginalSkipCompress) {
                    continue
                }
                if (PictureMimeType.isHasImage(media.mimeType)) {
                    val uri =
                        if (PictureMimeType.isContent(availablePath)) Uri.parse(availablePath) else Uri.fromFile(
                            File(availablePath)
                        )
                    source.add(uri)
                    queue[availablePath] = media
                }
            }
        }
        if (queue.size == 0) {
            onResultEvent(compressResult)
        } else {
            val compressEngine = selectorConfig?.compressFileEngine
            val appCtx = this.appContext
            if (compressEngine != null && appCtx != null) {
                compressEngine.onStartCompress(
                    appCtx,
                    source,
                    object : OnKeyValueResultCallbackListener {
                        override fun onCallback(srcPath: String?, compressPath: String?) {
                            if (TextUtils.isEmpty(srcPath)) {
                                onResultEvent(compressResult)
                            } else {
                                val media = queue[srcPath]
                                if (media != null) {
                                    if (SdkVersionUtils.isQ) {
                                        if (!TextUtils.isEmpty(compressPath) && (compressPath!!.contains(
                                                "Android/data/"
                                            )
                                                    || compressPath.contains("data/user/"))
                                        ) {
                                            media.compressPath = compressPath
                                            media.setCompressed(!TextUtils.isEmpty(compressPath))
                                            media.sandboxPath = media.compressPath
                                        }
                                    } else {
                                        media.compressPath = compressPath
                                        media.setCompressed(!TextUtils.isEmpty(compressPath))
                                    }
                                    queue.remove(srcPath)
                                }
                                if (queue.size == 0) {
                                    onResultEvent(compressResult)
                                }
                            }
                        }
                    })
            }
        }
    }

    override fun onOldCompress(result: ArrayList<LocalMedia?>?) {
        val compressResult = result ?: arrayListOf()
        showLoading()
        val config = selectorConfig
        if (config != null && config.isCheckOriginalImage && config.isOriginalSkipCompress) {
            onResultEvent(compressResult)
        } else {
            val compressEngine = config?.compressEngine
            val appCtx = this.appContext
            if (compressEngine != null && appCtx != null) {
                compressEngine.onStartCompress(
                    appCtx, compressResult,
                    object : OnCallbackListener<ArrayList<LocalMedia?>?> {
                        override fun onCall(data: ArrayList<LocalMedia?>?) {
                            onResultEvent(data)
                        }
                    })
            }
        }
    }

    override fun checkCropValidity(): Boolean {
        val config = selectorConfig
        if (config != null && config.cropFileEngine != null) {
            val filterSet = HashSet<String?>()
            val filters = config.skipCropList
            if (filters != null && filters.size > 0) {
                filterSet.addAll(filters)
            }
            if (config.selectCount == 1) {
                val mimeType = config.resultFirstMimeType
                val isHasImage = PictureMimeType.isHasImage(mimeType)
                if (isHasImage) {
                    if (filterSet.contains(mimeType)) {
                        return false
                    }
                }
                return isHasImage
            } else {
                var notSupportCropCount = 0
                for (i in 0 until config.selectCount) {
                    val media = config.selectedResult[i]
                    if (media != null && PictureMimeType.isHasImage(media.mimeType)) {
                        if (filterSet.contains(media.mimeType)) {
                            notSupportCropCount++
                        }
                    }
                }
                return notSupportCropCount != selectorConfig!!.selectCount
            }
        }
        return false
    }

    override fun checkOldCropValidity(): Boolean {
        val config = selectorConfig
        if (config != null && config.cropEngine != null) {
            val filterSet = HashSet<String?>()
            val filters = config.skipCropList
            if (filters != null && filters.size > 0) {
                filterSet.addAll(filters)
            }
            if (config.selectCount == 1) {
                val mimeType = config.resultFirstMimeType
                val isHasImage = PictureMimeType.isHasImage(mimeType)
                if (isHasImage) {
                    if (filterSet.contains(mimeType)) {
                        return false
                    }
                }
                return isHasImage
            } else {
                var notSupportCropCount = 0
                for (i in 0 until config.selectCount) {
                    val media = config.selectedResult[i]
                    if (media != null && PictureMimeType.isHasImage(media.mimeType)) {
                        if (filterSet.contains(media.mimeType)) {
                            notSupportCropCount++
                        }
                    }
                }
                return notSupportCropCount != selectorConfig!!.selectCount
            }
        }
        return false
    }


    override fun checkCompressValidity(): Boolean {
        if (selectorConfig!!.compressFileEngine != null) {
            for (i in 0 until selectorConfig!!.selectCount) {
                val media = selectorConfig!!.selectedResult[i]
                if (media != null && PictureMimeType.isHasImage(media.mimeType)) {
                    return true
                }
            }
        }
        return false
    }

    override fun checkOldCompressValidity(): Boolean {
        if (selectorConfig!!.compressEngine != null) {
            for (i in 0 until selectorConfig!!.selectCount) {
                val media = selectorConfig!!.selectedResult[i]
                if (media != null && PictureMimeType.isHasImage(media.mimeType)) {
                    return true
                }
            }
        }
        return false
    }

    override fun checkTransformSandboxFile(): Boolean {
        return SdkVersionUtils.isQ && selectorConfig!!.uriToFileTransformEngine != null
    }

    override fun checkOldTransformSandboxFile(): Boolean {
        return SdkVersionUtils.isQ && selectorConfig!!.sandboxFileEngine != null
    }

    override fun checkAddBitmapWatermark(): Boolean {
        return selectorConfig!!.onBitmapWatermarkListener != null
    }

    override fun checkVideoThumbnail(): Boolean {
        return selectorConfig!!.onVideoThumbnailEventListener != null
    }

    /**
     * 处理视频的缩略图
     *
     * @param result
     */
    private fun videoThumbnail(result: ArrayList<LocalMedia?>) {
        val queue = ConcurrentHashMap<String?, LocalMedia?>()
        for (i in result.indices) {
            val media = result[i]
            if (media != null) {
                val availablePath = media.availablePath
                if (PictureMimeType.isHasVideo(media.mimeType) || PictureMimeType.isUrlHasVideo(
                        availablePath ?: ""
                    )
                ) {
                    queue[availablePath] = media
                }
            }
        }
        if (queue.size == 0) {
            onCallBackResult(result)
        } else {
            val listener = selectorConfig?.onVideoThumbnailEventListener
            if (listener != null) {
                for (entry in queue.entries) {
                    listener.onVideoThumbnail(
                        requireContext(),
                        entry.key,
                        object : OnKeyValueResultCallbackListener {
                            override fun onCallback(srcPath: String?, resultPath: String?) {
                                val media = queue.get(srcPath)
                                if (media != null) {
                                    media.videoThumbnailPath = resultPath
                                    queue.remove(srcPath)
                                }
                                if (queue.size == 0) {
                                    onCallBackResult(result)
                                }
                            }
                        })
                }
            }
        }
    }

    /**
     * 添加水印
     */
    private fun addBitmapWatermark(result: ArrayList<LocalMedia?>) {
        val queue = ConcurrentHashMap<String?, LocalMedia?>()
        for (i in result.indices) {
            val media = result[i]
            if (media != null) {
                if (PictureMimeType.isHasAudio(media.mimeType)) {
                    continue
                }
                val availablePath = media.availablePath
                queue[availablePath] = media
            }
        }
        if (queue.size == 0) {
            dispatchWatermarkResult(result)
        } else {
            val listener = selectorConfig?.onBitmapWatermarkListener
            if (listener != null) {
                for (entry in queue.entries) {
                    val srcPath = entry.key
                    val media: LocalMedia = entry.value!!
                    listener.onAddBitmapWatermark(
                        this.appContext!!,
                        srcPath, media.mimeType, object : OnKeyValueResultCallbackListener {
                            override fun onCallback(srcPath: String?, resultPath: String?) {
                                if (TextUtils.isEmpty(srcPath)) {
                                    dispatchWatermarkResult(result)
                                } else {
                                    val media = queue.get(srcPath)
                                    if (media != null) {
                                        media.setWatermarkPath(resultPath)
                                        queue.remove(srcPath)
                                    }
                                    if (queue.size == 0) {
                                        dispatchWatermarkResult(result)
                                    }
                                }
                            }
                        })
                }
            }
        }
    }

    /**
     * dispatchUriToFileTransformResult
     *
     * @param result
     */
    private fun dispatchUriToFileTransformResult(result: ArrayList<LocalMedia?>) {
        showLoading()
        if (checkAddBitmapWatermark()) {
            addBitmapWatermark(result)
        } else if (checkVideoThumbnail()) {
            videoThumbnail(result)
        } else {
            onCallBackResult(result)
        }
    }


    /**
     * dispatchWatermarkResult
     *
     * @param result
     */
    private fun dispatchWatermarkResult(result: ArrayList<LocalMedia?>) {
        if (checkVideoThumbnail()) {
            videoThumbnail(result)
        } else {
            onCallBackResult(result)
        }
    }

    /**
     * SDK > 29 把外部资源copy一份至应用沙盒内
     *
     * @param result
     */
    private fun uriToFileTransform29(result: ArrayList<LocalMedia>) {
        showLoading()
        val queue = ConcurrentHashMap<String?, LocalMedia?>()
        for (i in result.indices) {
            val media = result[i]
            if (media != null) {
                queue[media.path] = media
            }
        }
        if (queue.size == 0) {
            val resultList = result.map { it as LocalMedia? }.toMutableList() as ArrayList<LocalMedia?>
            dispatchUriToFileTransformResult(resultList)
        } else {
            PictureThreadUtils.executeByIo<ArrayList<LocalMedia?>?>(object :
                SimpleTask<ArrayList<LocalMedia?>?>() {
                override fun doInBackground(): ArrayList<LocalMedia?>? {
                    val config = selectorConfig
                    val engine = config?.uriToFileTransformEngine
                    if (engine != null) {
                        for (entry in queue.entries) {
                            val media: LocalMedia = entry.value!!
                            if (config.isCheckOriginalImage || TextUtils.isEmpty(media.sandboxPath)) {
                                engine.onUriToFileAsyncTransform(
                                    requireContext(),
                                    media.path,
                                    media.mimeType,
                                    object : OnKeyValueResultCallbackListener {
                                        override fun onCallback(srcPath: String?, resultPath: String?) {
                                            if (TextUtils.isEmpty(srcPath)) {
                                                return
                                            }
                                            val media = queue.get(srcPath)
                                            if (media != null) {
                                                if (TextUtils.isEmpty(media.sandboxPath)) {
                                                    media.sandboxPath = resultPath
                                                }
                                                if (config.isCheckOriginalImage) {
                                                    media.originalPath = resultPath
                                                    media.setOriginal(!TextUtils.isEmpty(resultPath))
                                                }
                                                queue.remove(srcPath)
                                            }
                                        }
                                    })
                            }
                        }
                    }
                    return ArrayList<LocalMedia?>(result.map { it as LocalMedia? })
                }

                override fun onSuccess(data: ArrayList<LocalMedia?>?) {
                    PictureThreadUtils.cancel(this)
                    val nonNullResult = ArrayList<LocalMedia?>(data ?: arrayListOf())
                    dispatchUriToFileTransformResult(nonNullResult)
                }
            })
        }
    }

    /**
     * SDK > 29 把外部资源copy一份至应用沙盒内
     *
     * @param result
     */
    @Deprecated("")
    private fun copyExternalPathToAppInDirFor29(result: ArrayList<LocalMedia>) {
        showLoading()
        PictureThreadUtils.executeByIo<ArrayList<LocalMedia?>?>(object :
            SimpleTask<ArrayList<LocalMedia?>?>() {
            override fun doInBackground(): ArrayList<LocalMedia?>? {
                val config = selectorConfig
                val engine = config?.sandboxFileEngine
                if (engine != null) {
                    for (i in result.indices) {
                        val media: LocalMedia? = result[i]
                        engine.onStartSandboxFileTransform(
                            requireContext(), config.isCheckOriginalImage, i,
                            media, object : OnCallbackIndexListener<LocalMedia?> {
                                override fun onCall(data: LocalMedia?, index: Int) {
                                    val media = result[index]
                                    if (data != null && media != null) {
                                        media.sandboxPath = data.sandboxPath
                                        if (config.isCheckOriginalImage) {
                                            media.originalPath = data.originalPath
                                            media.setOriginal(!TextUtils.isEmpty(data.originalPath))
                                        }
                                    }
                                }
                            })
                    }
                }
                return ArrayList<LocalMedia?>(result.map { it as LocalMedia? })
            }

            override fun onSuccess(data: ArrayList<LocalMedia?>?) {
                PictureThreadUtils.cancel(this)
                val nonNullResult = ArrayList<LocalMedia?>(data?.filterNotNull() ?: emptyList())
                dispatchUriToFileTransformResult(nonNullResult)
            }
        })
    }


    /**
     * 构造原图数据
     *
     * @param result
     */
    private fun mergeOriginalImage(result: ArrayList<LocalMedia>) {
        val config = selectorConfig
        if (config != null && config.isCheckOriginalImage) {
            for (i in result.indices) {
                val media = result[i]
                media.setOriginal(true)
                media.originalPath = media.path
            }
        }
    }

    /**
     * 返回处理完成后的选择结果
     */
    override fun onResultEvent(result: ArrayList<LocalMedia?>?) {
        if (result == null || result.isEmpty()) {
            return
        }
        // 过滤掉 null 值并转换为 ArrayList<LocalMedia>
        val nonNullResult = ArrayList<LocalMedia>()
        for (media in result) {
            if (media != null) {
                nonNullResult.add(media)
            }
        }
        if (nonNullResult.isEmpty()) {
            return
        }
        if (checkTransformSandboxFile()) {
            uriToFileTransform29(nonNullResult)
        } else if (checkOldTransformSandboxFile()) {
            copyExternalPathToAppInDirFor29(nonNullResult)
        } else {
            val nonNullList = nonNullResult.filterNotNull().toMutableList() as ArrayList<LocalMedia>
            mergeOriginalImage(nonNullList)
            val nullableList = nonNullResult.map { it as LocalMedia? }.toMutableList() as ArrayList<LocalMedia?>
            dispatchUriToFileTransformResult(nullableList)
        }
    }


    /**
     * 返回结果
     */
    private fun onCallBackResult(result: ArrayList<LocalMedia?>?) {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            dismissLoading()
            if (selectorConfig!!.isActivityResultBack) {
                activity!!.setResult(
                    Activity.RESULT_OK,
                    PictureSelector.Companion.putIntentResult(result)
                )
                onSelectFinish(Activity.RESULT_OK, result)
            } else {
                selectorConfig!!.onResultCallListener?.onResult(result ?: arrayListOf())
            }
            onExitPictureSelector()
        }
    }

    /**
     * set app language
     */
    override fun initAppLanguage() {
        if (selectorConfig == null) {
            selectorConfig = SelectorProviders.instance?.selectorConfig
        }
        val config = selectorConfig
        if (config != null && config.language != LanguageConfig.UNKNOWN_LANGUAGE) {
            PictureLanguageUtils.setAppLanguage(
                requireActivity(),
                config.language,
                config.defaultLanguage
            )
        }
    }

    override fun onRecreateEngine() {
        createImageLoaderEngine()
        createVideoPlayerEngine()
        createCompressEngine()
        createSandboxFileEngine()
        createLoaderDataEngine()
        createResultCallbackListener()
        createLayoutResourceListener()
    }

    override fun onKeyBackFragmentFinish() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            if (selectorConfig!!.isActivityResultBack) {
                activity!!.setResult(Activity.RESULT_CANCELED)
                onSelectFinish(Activity.RESULT_CANCELED, null)
            } else {
                selectorConfig!!.onResultCallListener?.onCancel()
            }
            onExitPictureSelector()
        }
    }

    override fun onDestroy() {
        releaseSoundPool()
        super.onDestroy()
    }

    override fun showLoading() {
        try {
            if (ActivityCompatHelper.isDestroy(activity)) {
                return
            }
            if (!mLoadingDialog!!.isShowing()) {
                mLoadingDialog!!.show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun dismissLoading() {
        try {
            if (ActivityCompatHelper.isDestroy(activity)) {
                return
            }
            if (mLoadingDialog!!.isShowing()) {
                mLoadingDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onAttach(context: Context) {
        initAppLanguage()
        onRecreateEngine()
        super.onAttach(context!!)
        this.context = context
        if (getParentFragment() is IBridgePictureBehavior) {
            iBridgePictureBehavior = getParentFragment() as IBridgePictureBehavior
        } else if (context is IBridgePictureBehavior) {
            iBridgePictureBehavior = context as IBridgePictureBehavior
        }
    }

    /**
     * setRequestedOrientation
     */
    protected fun setRequestedOrientation() {
        if (ActivityCompatHelper.isDestroy(activity)) {
            return
        }
        activity!!.setRequestedOrientation(selectorConfig!!.requestedOrientation)
    }

    /**
     * back current Fragment
     */
    protected fun onBackCurrentFragment() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            if (!isStateSaved()) {
                selectorConfig!!.viewLifecycle?.onDestroy(this)
                activity!!.getSupportFragmentManager().popBackStack()
            }

            val fragments = activity!!.supportFragmentManager.fragments
            for (i in fragments.indices) {
                val fragment = fragments[i]
                if (fragment is PictureCommonFragment) {
                    fragment.onFragmentResume()
                }
            }
        }
    }

    /**
     * onSelectFinish
     *
     * @param resultCode
     * @param result
     */
    protected fun onSelectFinish(resultCode: Int, result: ArrayList<LocalMedia?>?) {
        val behavior = iBridgePictureBehavior
        if (behavior != null) {
            val selectorResult = getResult(resultCode, result)
            behavior.onSelectFinish(selectorResult)
        }
    }

    /**
     * exit PictureSelector
     */
    protected open fun onExitPictureSelector() {
        if (!ActivityCompatHelper.isDestroy(activity)) {
            if (this.isNormalDefaultEnter) {
                selectorConfig!!.viewLifecycle?.onDestroy(this)
                activity!!.finish()
            } else {
                val fragments = activity!!.supportFragmentManager.fragments
                for (i in fragments.indices) {
                    val fragment = fragments[i]
                    if (fragment is PictureCommonFragment) {
                        onBackCurrentFragment()
                    }
                }
            }
        }
        SelectorProviders.instance?.destroy()
    }

    /**
     * Get the image loading engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createImageLoaderEngine() {
        val config = selectorConfig
        if (config != null && config.imageEngine == null) {
            val baseEngine: PictureSelectorEngine? =
                PictureAppMaster.instance?.pictureSelectorEngine
            if (baseEngine != null) {
                config.imageEngine = baseEngine.createImageLoaderEngine()
            }
        }
    }

    /**
     * Get the video player engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createVideoPlayerEngine() {
        val config = selectorConfig
        if (config != null && config.videoPlayerEngine == null) {
            val baseEngine: PictureSelectorEngine? =
                PictureAppMaster.instance?.pictureSelectorEngine
            if (baseEngine != null) {
                config.videoPlayerEngine = baseEngine.createVideoPlayerEngine()
            }
        }
    }

    /**
     * Get the image loader data engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createLoaderDataEngine() {
        val config = selectorConfig
        if (config != null) {
            if (config.isLoaderDataEngine) {
                if (config.loaderDataEngine == null) {
                    val baseEngine: PictureSelectorEngine? =
                        PictureAppMaster.instance?.pictureSelectorEngine
                    if (baseEngine != null) config.loaderDataEngine =
                        baseEngine.createLoaderDataEngine()
                }
            }

            if (config.isLoaderFactoryEngine) {
                if (config.loaderFactory == null) {
                    val baseEngine: PictureSelectorEngine? =
                        PictureAppMaster.instance?.pictureSelectorEngine
                    if (baseEngine != null) config.loaderFactory = baseEngine.onCreateLoader()
                }
            }
        }
    }

    /**
     * Get the image compress engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createCompressEngine() {
        val config = selectorConfig
        if (config != null && config.isCompressEngine) {
            if (config.compressFileEngine == null) {
                val baseEngine: PictureSelectorEngine? =
                    PictureAppMaster.instance?.pictureSelectorEngine
                if (baseEngine != null) config.compressFileEngine =
                    baseEngine.createCompressFileEngine()
            }
            if (config.compressEngine == null) {
                val baseEngine: PictureSelectorEngine? =
                    PictureAppMaster.instance?.pictureSelectorEngine
                if (baseEngine != null) config.compressEngine =
                    baseEngine.createCompressEngine()
            }
        }
    }


    /**
     * Get the Sandbox engine again, provided that the user implements the IApp interface in the Application
     */
    private fun createSandboxFileEngine() {
        val config = selectorConfig
        if (config != null && config.isSandboxFileEngine) {
            if (config.uriToFileTransformEngine == null) {
                val baseEngine: PictureSelectorEngine? =
                    PictureAppMaster.instance?.pictureSelectorEngine
                if (baseEngine != null) config.uriToFileTransformEngine =
                    baseEngine.createUriToFileTransformEngine()
            }
            if (config.sandboxFileEngine == null) {
                val baseEngine: PictureSelectorEngine? =
                    PictureAppMaster.instance?.pictureSelectorEngine
                if (baseEngine != null) config.sandboxFileEngine =
                    baseEngine.createSandboxFileEngine()
            }
        }
    }


    /**
     * Retrieve the result callback listener, provided that the user implements the IApp interface in the Application
     */
    private fun createResultCallbackListener() {
        val config = selectorConfig
        if (config != null && config.isResultListenerBack) {
            if (config.onResultCallListener == null) {
                val baseEngine: PictureSelectorEngine? =
                    PictureAppMaster.instance?.pictureSelectorEngine
                if (baseEngine != null) {
                    config.onResultCallListener = baseEngine.resultCallbackListener
                }
            }
        }
    }

    /**
     * Retrieve the layout callback listener, provided that the user implements the IApp interface in the Application
     */
    private fun createLayoutResourceListener() {
        val config = selectorConfig
        if (config != null && config.isInjectLayoutResource) {
            if (config.onLayoutResourceListener == null) {
                val baseEngine: PictureSelectorEngine? =
                    PictureAppMaster.instance?.pictureSelectorEngine
                if (baseEngine != null) {
                    config.onLayoutResourceListener =
                        baseEngine.createLayoutResourceListener()
                }
            }
        }
    }


    /**
     * generate result
     *
     * @param data result
     * @return
     */
    protected fun getResult(resultCode: Int, data: ArrayList<LocalMedia?>?): SelectorResult {
        val resultData = data?.map { it as LocalMedia? }?.toMutableList() as? ArrayList<LocalMedia?>
        return SelectorResult(
            resultCode,
            if (resultData != null) PictureSelector.Companion.putIntentResult(resultData) else null
        )
    }

    companion object {
        open val fragmentTag: String
            get() = PictureCommonFragment::class.java.simpleName

        /**
         * 根据类型获取相应的Toast文案
         *
         * @param context
         * @param mimeType
         * @param maxSelectNum
         * @return
         */
        @SuppressLint("StringFormatInvalid")
        fun getTipsMsg(context: Context, mimeType: String?, maxSelectNum: Int): String {
            if (PictureMimeType.isHasVideo(mimeType)) {
                return context.getString(R.string.ps_message_video_max_num, maxSelectNum.toString())
            } else if (PictureMimeType.isHasAudio(mimeType)) {
                return context.getString(R.string.ps_message_audio_max_num, maxSelectNum.toString())
            } else {
                return context.getString(R.string.ps_message_max_num, maxSelectNum.toString())
            }
        }
    }
}
