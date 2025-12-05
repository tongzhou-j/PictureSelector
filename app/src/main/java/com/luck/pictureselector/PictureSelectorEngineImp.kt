package com.luck.pictureselector

import android.content.Context
import android.util.Log
import com.luck.picture.lib.basic.IBridgeLoaderFactory
import com.luck.picture.lib.config.InjectResourceSource
import com.luck.picture.lib.engine.CompressEngine
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.engine.ExtendLoaderEngine
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.engine.MediaPlayerEngine
import com.luck.picture.lib.engine.PictureSelectorEngine
import com.luck.picture.lib.engine.SandboxFileEngine
import com.luck.picture.lib.engine.UriToFileTransformEngine
import com.luck.picture.lib.engine.VideoPlayerEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnInjectLayoutResourceListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.loader.IBridgeMediaLoader
import java.util.ArrayList

/**
 * @author：luck
 * @date：2020/4/22 12:15 PM
 * @describe：PictureSelectorEngineImp
 */
class PictureSelectorEngineImp : PictureSelectorEngine {
    companion object {
        private const val TAG = "PictureSelectorEngineImp"
    }

    /**
     * 重新创建[ImageEngine]引擎
     *
     * @return
     */
    override fun createImageLoaderEngine(): ImageEngine {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致ImageEngine被回收
        return GlideEngine.createGlideEngine()
    }

    /**
     * 重新创建[CompressEngine]引擎
     *
     * @return
     */
    override fun createCompressEngine(): CompressEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致CompressEngine被回收
        return null
    }

    /**
     * 重新创建[CompressEngine]引擎
     *
     * @return
     */
    override fun createCompressFileEngine(): CompressFileEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致CompressFileEngine被回收
        return null
    }

    /**
     * 重新创建[ExtendLoaderEngine]引擎
     *
     * @return
     */
    override fun createLoaderDataEngine(): ExtendLoaderEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致ExtendLoaderEngine被回收
        return null
    }

    override fun createVideoPlayerEngine(): VideoPlayerEngine<*>? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致VideoPlayerEngine被回收
        return null
    }

    /**
     *  重新创建[IBridgeMediaLoader]引擎
     * @return
     */
    override fun onCreateLoader(): IBridgeLoaderFactory? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致IBridgeLoaderFactory被回收
        return null
    }

    /**
     * 重新创建[SandboxFileEngine]引擎
     *
     * @return
     */
    override fun createSandboxFileEngine(): SandboxFileEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致SandboxFileEngine被回收
        return null
    }

    /**
     * 重新创建[UriToFileTransformEngine]引擎
     *
     * @return
     */
    override fun createUriToFileTransformEngine(): UriToFileTransformEngine? {
        // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致UriToFileTransformEngine被回收
        return null
    }

    /**
     * 如果出现内存不足导致OnInjectLayoutResourceListener被回收，需要重新引入自定义布局
     *
     * @return
     */
    override fun createLayoutResourceListener(): OnInjectLayoutResourceListener {
        return object : OnInjectLayoutResourceListener {
            override fun getLayoutResourceId(context: Context, resourceSource: Int): Int {
                return when (resourceSource) {
                    InjectResourceSource.MAIN_SELECTOR_LAYOUT_RESOURCE -> R.layout.ps_custom_fragment_selector
                    InjectResourceSource.PREVIEW_LAYOUT_RESOURCE -> R.layout.ps_custom_fragment_preview
                    InjectResourceSource.MAIN_ITEM_IMAGE_LAYOUT_RESOURCE -> R.layout.ps_custom_item_grid_image
                    InjectResourceSource.MAIN_ITEM_VIDEO_LAYOUT_RESOURCE -> R.layout.ps_custom_item_grid_video
                    InjectResourceSource.MAIN_ITEM_AUDIO_LAYOUT_RESOURCE -> R.layout.ps_custom_item_grid_audio
                    InjectResourceSource.ALBUM_ITEM_LAYOUT_RESOURCE -> R.layout.ps_custom_album_folder_item
                    InjectResourceSource.PREVIEW_ITEM_IMAGE_LAYOUT_RESOURCE -> R.layout.ps_custom_preview_image
                    InjectResourceSource.PREVIEW_ITEM_VIDEO_LAYOUT_RESOURCE -> R.layout.ps_custom_preview_video
                    InjectResourceSource.PREVIEW_GALLERY_ITEM_LAYOUT_RESOURCE -> R.layout.ps_custom_preview_gallery_item
                    else -> 0
                }
            }
        }
    }

    override fun getResultCallbackListener(): OnResultCallbackListener<LocalMedia> {
        return object : OnResultCallbackListener<LocalMedia> {
            override fun onResult(result: ArrayList<LocalMedia>) {
                // TODO 这种情况是内存极度不足的情况下，比如开启开发者选项中的不保留活动或后台进程限制，导致OnResultCallbackListener被回收
                // 可以在这里进行一些补救措施，通过广播或其他方式将结果推送到相应页面，防止结果丢失的情况
                Log.i(TAG, "onResult:" + result.size)
            }

            override fun onCancel() {
                Log.i(TAG, "PictureSelector onCancel")
            }
        }
    }
}

