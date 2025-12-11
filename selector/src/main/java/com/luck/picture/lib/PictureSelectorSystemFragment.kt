package com.luck.picture.lib

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.luck.picture.lib.basic.PictureCommonFragment
import com.luck.picture.lib.config.PermissionEvent
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.interfaces.OnRequestPermissionListener
import com.luck.picture.lib.manager.SelectedManager
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.permissions.PermissionConfig
import com.luck.picture.lib.permissions.PermissionResultCallback
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.ToastUtils

/**
 * @author：luck
 * @date：2022/1/16 10:22 下午
 * @describe：PictureSelectorSystemFragment
 */
class PictureSelectorSystemFragment : PictureCommonFragment() {
    override val resourceId: Int
        get() = R.layout.ps_empty

    override fun onPermissionExplainEvent(
        isDisplayExplain: Boolean,
        permissionArray: Array<String?>?
    ) {
        super.onPermissionExplainEvent(isDisplayExplain, permissionArray)
    }

    private var mDocMultipleLauncher: ActivityResultLauncher<String?>? = null

    private var mDocSingleLauncher: ActivityResultLauncher<String?>? = null

    private var mContentsLauncher: ActivityResultLauncher<String?>? = null

    private var mContentLauncher: ActivityResultLauncher<String?>? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createSystemContracts()
        if (PermissionChecker.Companion.isCheckReadStorage(
                selectorConfig?.chooseMode ?: 0,
                requireContext()
            )
        ) {
            openSystemAlbum()
        } else {
            val readPermissionArray =
                PermissionConfig.getReadPermissionArray(requireContext(), selectorConfig?.chooseMode ?: 0)
            onPermissionExplainEvent(true, readPermissionArray)
            if (selectorConfig?.onPermissionsEventListener != null) {
                onApplyPermissionsEvent(
                    PermissionEvent.EVENT_SYSTEM_SOURCE_DATA,
                    readPermissionArray
                )
            } else {
                PermissionChecker.Companion.instance.requestPermissions(
                    this,
                    readPermissionArray,
                    object : PermissionResultCallback {
                        override fun onGranted() {
                            openSystemAlbum()
                        }

                        override fun onDenied() {
                            handlePermissionDenied(readPermissionArray)
                        }
                    })
            }
        }
    }

    override fun onApplyPermissionsEvent(event: Int, permissionArray: Array<String?>?) {
        if (event == PermissionEvent.EVENT_SYSTEM_SOURCE_DATA) {
            selectorConfig?.onPermissionsEventListener?.requestPermission(
                this,
                PermissionConfig.getReadPermissionArray(requireContext(), selectorConfig?.chooseMode ?: 0),
                object : OnRequestPermissionListener {
                    override fun onCall(permissionArray: Array<String?>?, isResult: Boolean) {
                        if (isResult) {
                            openSystemAlbum()
                        } else {
                            handlePermissionDenied(permissionArray)
                        }
                    }
                })
        }
    }

    /**
     * 打开系统相册
     */
    private fun openSystemAlbum() {
        onPermissionExplainEvent(false, null)
        if (selectorConfig?.selectionMode == SelectModeConfig.SINGLE) {
            if (selectorConfig?.chooseMode == SelectMimeType.ofAll()) {
                mDocSingleLauncher!!.launch(SelectMimeType.SYSTEM_ALL)
            } else {
                mContentLauncher!!.launch(this.input)
            }
        } else {
            if (selectorConfig?.chooseMode == SelectMimeType.ofAll()) {
                mDocMultipleLauncher!!.launch(SelectMimeType.SYSTEM_ALL)
            } else {
                mContentsLauncher!!.launch(this.input)
            }
        }
    }

    /**
     * createSystemContracts
     */
    private fun createSystemContracts() {
        if (selectorConfig?.selectionMode == SelectModeConfig.SINGLE) {
            if (selectorConfig?.chooseMode == SelectMimeType.ofAll()) {
                createSingleDocuments()
            } else {
                createContent()
            }
        } else {
            if (selectorConfig?.chooseMode == SelectMimeType.ofAll()) {
                createMultipleDocuments()
            } else {
                createMultipleContents()
            }
        }
    }

    /**
     * 同时获取图片或视频(多选)
     *
     * 部分机型可能不支持多选操作
     */
    private fun createMultipleDocuments() {
        mDocMultipleLauncher = registerForActivityResult<String?, MutableList<Uri?>?>(object :
            ActivityResultContract<String?, MutableList<Uri?>?>() {
            override fun parseResult(resultCode: Int, intent: Intent?): MutableList<Uri?> {
                val result: MutableList<Uri?> = ArrayList<Uri?>()
                if (intent == null) {
                    return result
                }
                if (intent.clipData != null) {
                    val clipData = intent.clipData
                    val itemCount = clipData!!.itemCount
                    for (i in 0 until itemCount) {
                        val item = clipData.getItemAt(i)
                        val uri = item.uri
                        result.add(uri)
                    }
                } else if (intent.data != null) {
                    result.add(intent.data)
                }
                return result
            }

            override fun createIntent(context: Context, mimeTypes: String?): Intent {
                val intent = Intent(Intent.ACTION_PICK)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = mimeTypes
                return intent
            }
        }, object : ActivityResultCallback<MutableList<Uri?>?> {
            override fun onActivityResult(result: MutableList<Uri?>?) {
                if (result == null || result.size == 0) {
                    onKeyBackFragmentFinish()
                } else {
                    for (i in result.indices) {
                        val media = this@PictureSelectorSystemFragment.buildLocalMedia(result[i]?.toString())
                        media.path = if (SdkVersionUtils.isQ) media.path else media.realPath
                        selectorConfig?.addSelectResult(media)
                    }
                    this@PictureSelectorSystemFragment.dispatchTransformResult()
                }
            }
        })
    }


    /**
     * 同时获取图片或视频(单选)
     */
    private fun createSingleDocuments() {
        mDocSingleLauncher = registerForActivityResult<String?, Uri?>(object :
            ActivityResultContract<String?, Uri?>() {
            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                if (intent == null) {
                    return null
                }
                return intent.data
            }

            override fun createIntent(context: Context, mimeTypes: String?): Intent {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = mimeTypes
                return intent
            }
        }, object : ActivityResultCallback<Uri?> {
            override fun onActivityResult(result: Uri?) {
                if (result == null) {
                    onKeyBackFragmentFinish()
                } else {
                    val media = this@PictureSelectorSystemFragment.buildLocalMedia(result.toString())
                    media.path = if (SdkVersionUtils.isQ) media.path else media.realPath
                    val selectResultCode = confirmSelect(media, false)
                    if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                        this@PictureSelectorSystemFragment.dispatchTransformResult()
                    } else {
                        onKeyBackFragmentFinish()
                    }
                }
            }
        })
    }


    /**
     * 获取图片或视频
     *
     * 部分机型可能不支持多选操作
     */
    private fun createMultipleContents() {
        mContentsLauncher = registerForActivityResult<String?, MutableList<Uri?>?>(object :
            ActivityResultContract<String?, MutableList<Uri?>?>() {
            override fun parseResult(resultCode: Int, intent: Intent?): MutableList<Uri?> {
                val result: MutableList<Uri?> = ArrayList<Uri?>()
                if (intent == null) {
                    return result
                }
                if (intent.clipData != null) {
                    val clipData = intent.clipData
                    val itemCount = clipData!!.itemCount
                    for (i in 0 until itemCount) {
                        val item = clipData.getItemAt(i)
                        val uri = item.uri
                        result.add(uri)
                    }
                } else if (intent.data != null) {
                    result.add(intent.data)
                }
                return result
            }

            override fun createIntent(context: Context, mimeType: String?): Intent {
                val intent: Intent
                if (TextUtils.equals(SelectMimeType.SYSTEM_VIDEO, mimeType)) {
                    intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                } else if (TextUtils.equals(SelectMimeType.SYSTEM_AUDIO, mimeType)) {
                    intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                } else {
                    intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                }
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                return intent
            }
        }, object : ActivityResultCallback<MutableList<Uri?>?> {
            override fun onActivityResult(result: MutableList<Uri?>?) {
                if (result == null || result.size == 0) {
                    onKeyBackFragmentFinish()
                } else {
                    for (i in result.indices) {
                        val media = this@PictureSelectorSystemFragment.buildLocalMedia(result[i]?.toString())
                        media.path = if (SdkVersionUtils.isQ) media.path else media.realPath
                        selectorConfig?.addSelectResult(media)
                    }
                    this@PictureSelectorSystemFragment.dispatchTransformResult()
                }
            }
        })
    }

    /**
     * 单选图片或视频
     */
    private fun createContent() {
        mContentLauncher = registerForActivityResult<String?, Uri?>(object :
            ActivityResultContract<String?, Uri?>() {
            override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
                if (intent == null) {
                    return null
                }
                return intent.data
            }

            override fun createIntent(context: Context, mimeType: String?): Intent {
                val intent: Intent
                if (TextUtils.equals(SelectMimeType.SYSTEM_VIDEO, mimeType)) {
                    intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                } else if (TextUtils.equals(SelectMimeType.SYSTEM_AUDIO, mimeType)) {
                    intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                } else {
                    intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                }
                return intent
            }
        }, object : ActivityResultCallback<Uri?> {
            override fun onActivityResult(result: Uri?) {
                if (result == null) {
                    onKeyBackFragmentFinish()
                } else {
                    val media = this@PictureSelectorSystemFragment.buildLocalMedia(result.toString())
                    media.path = if (SdkVersionUtils.isQ) media.path else media.realPath
                    val selectResultCode = confirmSelect(media, false)
                    if (selectResultCode == SelectedManager.ADD_SUCCESS) {
                        this@PictureSelectorSystemFragment.dispatchTransformResult()
                    } else {
                        onKeyBackFragmentFinish()
                    }
                }
            }
        })
    }

    private val input: String
        /**
         * 获取选资源取类型
         *
         * @return
         */
        get() {
            if (selectorConfig?.chooseMode == SelectMimeType.ofVideo()) {
                return SelectMimeType.SYSTEM_VIDEO
            } else if (selectorConfig?.chooseMode == SelectMimeType.ofAudio()) {
                return SelectMimeType.SYSTEM_AUDIO
            } else {
                return SelectMimeType.SYSTEM_IMAGE
            }
        }

    override fun handlePermissionSettingResult(permissions: Array<String?>?) {
        onPermissionExplainEvent(false, null)
        val isCheckReadStorage: Boolean
        val listener = selectorConfig?.onPermissionsEventListener
        if (listener != null) {
            isCheckReadStorage = listener.hasPermissions(this, permissions)
        } else {
            isCheckReadStorage = PermissionChecker.Companion.isCheckReadStorage(
                selectorConfig?.chooseMode ?: 0,
                requireContext()
            )
        }
        if (isCheckReadStorage) {
            openSystemAlbum()
        } else {
            ToastUtils.showToast(requireContext(), getString(R.string.ps_jurisdiction))
            onKeyBackFragmentFinish()
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = arrayOf<String?>()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            onKeyBackFragmentFinish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mDocMultipleLauncher != null) {
            mDocMultipleLauncher!!.unregister()
        }
        if (mDocSingleLauncher != null) {
            mDocSingleLauncher!!.unregister()
        }
        if (mContentsLauncher != null) {
            mContentsLauncher!!.unregister()
        }
        if (mContentLauncher != null) {
            mContentLauncher!!.unregister()
        }
    }

    companion object {
        val TAG: String = PictureSelectorSystemFragment::class.java.simpleName

        fun newInstance(): PictureSelectorSystemFragment {
            return PictureSelectorSystemFragment()
        }
    }
}
