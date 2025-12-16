package com.luck.lib.camerax

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.luck.lib.camerax.listener.CameraListener
import com.luck.lib.camerax.listener.ClickListener
import com.luck.lib.camerax.listener.IObtainCameraView
import com.luck.lib.camerax.listener.ImageCallbackListener
import com.luck.lib.camerax.permissions.PermissionChecker
import com.luck.lib.camerax.permissions.PermissionResultCallback
import com.luck.lib.camerax.utils.SimpleXSpUtils

/**
 * @author：luck
 * @date：2021/11/29 7:50 下午
 * @describe：PictureCameraActivity
 */
class PictureCameraActivity : AppCompatActivity(), IObtainCameraView {
    /**
     * PermissionResultCallback
     */
    private var mPermissionResultCallback: PermissionResultCallback? = null

    private lateinit var mCameraView: CustomCameraView

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        super.onCreate(savedInstanceState)
        mCameraView = CustomCameraView(this)
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        mCameraView.layoutParams = layoutParams
        setContentView(mCameraView)
        mCameraView.post {
            mCameraView.setCameraConfig(intent)
        }
        mCameraView.setImageCallbackListener(object : ImageCallbackListener {
            override fun onLoadImage(url: String, imageView: ImageView) {
                CustomCameraConfig.imageEngine?.loadImage(imageView.context, url, imageView)
            }
        })
        mCameraView.setCameraListener(object : CameraListener {
            override fun onPictureSuccess(@NonNull url: String) {
                handleCameraSuccess()
            }

            override fun onRecordSuccess(@NonNull url: String) {
                handleCameraSuccess()
            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                Toast.makeText(
                    this@PictureCameraActivity.applicationContext,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        mCameraView.setOnCancelClickListener(object : ClickListener {
            override fun onClick() {
                handleCameraCancel()
            }
        })
    }

    private fun handleCameraSuccess() {
        val uri = intent.getParcelableExtra<Uri>(MediaStore.EXTRA_OUTPUT)
        val intent = Intent()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        setResult(RESULT_OK, this.intent)
        onBackPressedDispatcher.onBackPressed()
    }

    private fun handleCameraCancel() {
        setResult(RESULT_CANCELED)
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mCameraView.onCancelMedia()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onConfigurationChanged(@NonNull newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mCameraView.onConfigurationChanged(newConfig)
    }

    override fun onBackPressed() {
        CustomCameraConfig.destroy()
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        CustomCameraConfig.explainListener?.onDismiss(mCameraView)
        when (requestCode) {
            PermissionChecker.PERMISSION_SETTING_CODE -> {
                if (PermissionChecker.checkSelfPermission(this, arrayOf(Manifest.permission.CAMERA))) {
                    mCameraView.buildUseCameraCases()
                } else {
                    SimpleXSpUtils.putBoolean(this, Manifest.permission.CAMERA, true)
                    handleCameraCancel()
                }
            }
            PermissionChecker.PERMISSION_RECORD_AUDIO_SETTING_CODE -> {
                if (!PermissionChecker.checkSelfPermission(this, arrayOf(Manifest.permission.RECORD_AUDIO))) {
                    SimpleXSpUtils.putBoolean(this, Manifest.permission.RECORD_AUDIO, true)
                    Toast.makeText(applicationContext, "Missing recording permission", Toast.LENGTH_LONG).show()
                }
            }
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

    override fun onDestroy() {
        mCameraView.onDestroy()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermissionResultCallback?.let {
            PermissionChecker.getInstance().onRequestPermissionsResult(grantResults, it)
            mPermissionResultCallback = null
        }
    }

    override fun getCustomCameraView(): ViewGroup {
        return mCameraView
    }
}

