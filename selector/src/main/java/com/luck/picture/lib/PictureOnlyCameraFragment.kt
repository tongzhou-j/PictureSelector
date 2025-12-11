package com.luck.picture.lib

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.luck.picture.lib.basic.PictureCommonFragment
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.manager.SelectedManager
import com.luck.picture.lib.permissions.PermissionChecker
import com.luck.picture.lib.permissions.PermissionConfig
import com.luck.picture.lib.utils.SdkVersionUtils
import com.luck.picture.lib.utils.ToastUtils

/**
 * @author：luck
 * @date：2021/11/22 2:26 下午
 * @describe：PictureOnlyCameraFragment
 */
class PictureOnlyCameraFragment : PictureCommonFragment() {
    override fun handlePermissionDenied(permissionArray: Array<String?>?) {
        super.handlePermissionDenied(permissionArray ?: arrayOf())
    }

    override fun onPermissionExplainEvent(isDisplayExplain: Boolean, permissionArray: Array<String?>?) {
        super.onPermissionExplainEvent(isDisplayExplain, permissionArray ?: arrayOf())
    }

    override fun onCrop(result: ArrayList<LocalMedia?>?) {
        // Not used in camera-only mode
    }

    override fun onOldCrop(result: ArrayList<LocalMedia?>?) {
        // Not used in camera-only mode
    }

    override fun onCompress(result: ArrayList<LocalMedia?>?) {
        // Not used in camera-only mode
    }

    override fun onOldCompress(result: ArrayList<LocalMedia?>?) {
        // Not used in camera-only mode
    }

    override fun onResultEvent(result: ArrayList<LocalMedia?>?) {
        super.onResultEvent(result)
    }

    fun getFragmentTag(): String {
        return TAG
    }

    override val resourceId: Int
        get() = R.layout.ps_empty

    override fun confirmSelect(currentMedia: LocalMedia?, isSelected: Boolean): Int {
        if (currentMedia == null) return SelectedManager.INVALID
        return super.confirmSelect(currentMedia, isSelected)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 这里只有非内存回收状态下才走，否则当内存不足Fragment被回收后会重复执行
        if (savedInstanceState == null) {
            openSelectedCamera()
        }
    }

    override fun dispatchCameraMediaResult(media: LocalMedia?) {
        if (media == null) return
        val selectResultCode = confirmSelect(media, false)
        if (selectResultCode == SelectedManager.ADD_SUCCESS) {
            dispatchTransformResult()
        } else {
            onKeyBackFragmentFinish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            onKeyBackFragmentFinish()
        }
    }

    override fun handlePermissionSettingResult(permissions: Array<String?>?) {
        onPermissionExplainEvent(false, null)
        val config = selectorConfig ?: return
        var isHasPermissions: Boolean
        if (config.onPermissionsEventListener != null) {
            isHasPermissions = config.onPermissionsEventListener!!
                .hasPermissions(this, permissions)
        } else {
            val ctx = context ?: return
            isHasPermissions = PermissionChecker.isCheckCamera(ctx)
            if (!SdkVersionUtils.isQ) {
                isHasPermissions = PermissionChecker.isCheckWriteExternalStorage(ctx)
            }
        }
        if (isHasPermissions) {
            openSelectedCamera()
        } else {
            val ctx = context ?: return
            if (!PermissionChecker.isCheckCamera(ctx)) {
                ToastUtils.showToast(ctx, getString(R.string.ps_camera))
            } else {
                if (!PermissionChecker.isCheckWriteExternalStorage(ctx)) {
                    ToastUtils.showToast(ctx, getString(R.string.ps_jurisdiction))
                }
            }
            onKeyBackFragmentFinish()
        }
        PermissionConfig.CURRENT_REQUEST_PERMISSION = arrayOf<String?>()
    }

    companion object {
        @JvmField
        val TAG: String = PictureOnlyCameraFragment::class.java.simpleName

        @JvmStatic
        fun newInstance(): PictureOnlyCameraFragment {
            return PictureOnlyCameraFragment()
        }
    }
}
