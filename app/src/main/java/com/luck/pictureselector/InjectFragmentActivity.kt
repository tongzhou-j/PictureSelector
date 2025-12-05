package com.luck.pictureselector

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.luck.picture.lib.PictureSelectorFragment
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.basic.IBridgePictureBehavior
import com.luck.picture.lib.basic.PictureCommonFragment
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.MediaExtraInfo
import com.luck.picture.lib.immersive.ImmersiveManager
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.MediaUtils
import java.util.ArrayList

/**
 * @author：luck
 * @date：2021/12/20 1:40 下午
 * @describe：InjectFragmentActivity
 */
class InjectFragmentActivity : AppCompatActivity(), IBridgePictureBehavior {
    companion object {
        private const val TAG = "PictureSelectorTag"
    }

    private lateinit var tvResult: TextView

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val color = ContextCompat.getColor(this, R.color.app_color_white)
        ImmersiveManager.immersiveAboveAPI23(this, color, color, true)
        setContentView(R.layout.activity_inject_fragment)
        tvResult = findViewById(R.id.tv_result)
        findViewById<View>(R.id.tvb_inject_fragment).setOnClickListener { v ->
            // 方式一
            PictureSelector.create(v.context)
                .openGallery(SelectMimeType.ofAll())
                .setImageEngine(GlideEngine.createGlideEngine())
                .buildLaunch(R.id.fragment_container, object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: ArrayList<LocalMedia>) {
                        setTranslucentStatusBar()
                        analyticalSelectResults(result)
                    }

                    override fun onCancel() {
                        setTranslucentStatusBar()
                        Log.i(TAG, "PictureSelector Cancel")
                    }
                })
        }

        findViewById<View>(R.id.tvb_inject_result_fragment).setOnClickListener { v ->
            // 方式二
            val selectorFragment = PictureSelector.create(v.context)
                .openGallery(SelectMimeType.ofAll())
                .setImageEngine(GlideEngine.createGlideEngine())
                .build()
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, selectorFragment, selectorFragment.fragmentTag)
                .addToBackStack(selectorFragment.fragmentTag)
                .commitAllowingStateLoss()
        }
    }

    override fun onSelectFinish(result: PictureCommonFragment.SelectorResult?) {
        setTranslucentStatusBar()
        if (result == null) {
            return
        }
        if (result.mResultCode == RESULT_OK) {
            val selectorResult = PictureSelector.obtainSelectorList(result.mResultData)
            analyticalSelectResults(selectorResult)
        } else if (result.mResultCode == RESULT_CANCELED) {
            Log.i(TAG, "onSelectFinish PictureSelector Cancel")
            setTranslucentStatusBar()
        }
    }

    /**
     * 处理选择结果
     *
     * @param result
     */
    private fun analyticalSelectResults(result: ArrayList<LocalMedia>) {
        val builder = StringBuilder()
        builder.append("Result").append("\n")
        for (media in result) {
            if (media.width == 0 || media.height == 0) {
                if (PictureMimeType.isHasImage(media.mimeType)) {
                    val imageExtraInfo = MediaUtils.getImageSize(this, media.path)
                    media.width = imageExtraInfo.width
                    media.height = imageExtraInfo.height
                } else if (PictureMimeType.isHasVideo(media.mimeType)) {
                    val videoExtraInfo = MediaUtils.getVideoSize(PictureAppMaster.getInstance().appContext, media.path)
                    media.width = videoExtraInfo.width
                    media.height = videoExtraInfo.height
                }
            }
            builder.append(media.availablePath).append("\n")
            Log.i(TAG, "文件名: " + media.fileName)
            Log.i(TAG, "是否压缩:" + media.isCompressed)
            Log.i(TAG, "压缩:" + media.compressPath)
            Log.i(TAG, "原图:" + media.path)
            Log.i(TAG, "绝对路径:" + media.realPath)
            Log.i(TAG, "是否裁剪:" + media.isCut)
            Log.i(TAG, "裁剪:" + media.cutPath)
            Log.i(TAG, "是否开启原图:" + media.isOriginal)
            Log.i(TAG, "原图路径:" + media.originalPath)
            Log.i(TAG, "沙盒路径:" + media.sandboxPath)
            Log.i(TAG, "原始宽高: " + media.width + "x" + media.height)
            Log.i(TAG, "裁剪宽高: " + media.cropImageWidth + "x" + media.cropImageHeight)
            Log.i(TAG, "文件大小: " + media.size)
        }
        tvResult.text = builder.toString()
    }

    /**
     * 设置状态栏字体颜色
     */
    private fun setTranslucentStatusBar() {
        ImmersiveManager.translucentStatusBar(this, true)
    }
}

