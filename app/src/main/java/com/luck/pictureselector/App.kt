package com.luck.pictureselector

import android.app.Application
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.annotation.NonNull
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import coil.ComponentRegistry
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.luck.picture.lib.app.IApp
import com.luck.picture.lib.app.PictureAppMaster
import com.luck.picture.lib.engine.PictureSelectorEngine
import java.io.File

/**
 * @author：luck
 * @date：2019-12-03 22:53
 * @describe：Application
 */
class App : Application(), IApp, CameraXConfig.Provider, ImageLoaderFactory {
    companion object {
        private const val TAG = "App"
    }

    override fun onCreate() {
        super.onCreate()
        PictureAppMaster.getInstance().setApp(this)
    }

    override fun getAppContext(): Context {
        return this
    }

    override fun getPictureSelectorEngine(): PictureSelectorEngine {
        return PictureSelectorEngineImp()
    }

    @NonNull
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(Log.ERROR).build()
    }

    @NonNull
    override fun newImageLoader(): ImageLoader {
        val imageLoader = ImageLoader.Builder(getAppContext())
        val newBuilder = ComponentRegistry().newBuilder()
        newBuilder.add(VideoFrameDecoder.Factory())
        if (SDK_INT >= 28) {
            newBuilder.add(ImageDecoderDecoder.Factory())
        } else {
            newBuilder.add(GifDecoder.Factory())
        }
        val componentRegistry = newBuilder.build()
        imageLoader.components(componentRegistry)
        imageLoader.memoryCache(MemoryCache.Builder(getAppContext())
            .maxSizePercent(0.25).build())
        imageLoader.diskCache(DiskCache.Builder()
            .directory(File(cacheDir, "image_cache"))
            .maxSizePercent(0.02)
            .build())
        return imageLoader.build()
    }
}

