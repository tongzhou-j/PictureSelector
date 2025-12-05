package com.luck.pictureselector

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.annotation.NonNull
import com.luck.picture.lib.R
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import java.io.File

/**
 * @author：luck
 * @date：2020/4/30 10:54 AM
 * @describe：Picasso加载引擎
 */
class PicassoEngine private constructor() : ImageEngine {
    /**
     * 加载图片
     *
     * @param context
     * @param url
     * @param imageView
     */
    override fun loadImage(@NonNull context: Context, @NonNull url: String, @NonNull imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        val videoRequestHandler = VideoRequestHandler()
        when {
            PictureMimeType.isContent(url) || PictureMimeType.isHasHttp(url) -> {
                Picasso.get().load(Uri.parse(url)).into(imageView)
            }
            PictureMimeType.isUrlHasVideo(url) -> {
                val picasso = Picasso.Builder(context.applicationContext)
                    .addRequestHandler(videoRequestHandler)
                    .build()
                picasso.load("${videoRequestHandler.SCHEME_VIDEO}:$url")
                    .into(imageView)
            }
            else -> {
                Picasso.get().load(File(url)).into(imageView)
            }
        }
    }

    override fun loadImage(context: Context, imageView: ImageView, url: String, maxWidth: Int, maxHeight: Int) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        val picasso = Picasso.Builder(context)
            .build()
        val request: RequestCreator = picasso.load(if (PictureMimeType.isContent(url)) Uri.parse(url) else Uri.fromFile(File(url)))
        request.config(Bitmap.Config.RGB_565)
        if (maxWidth > 0 && maxHeight > 0) {
            request.resize(maxWidth, maxHeight)
        }
        request.into(imageView)
    }

    /**
     * 加载相册目录
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadAlbumCover(@NonNull context: Context, @NonNull url: String, @NonNull imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        val videoRequestHandler = VideoRequestHandler()
        when {
            PictureMimeType.isContent(url) -> {
                Picasso.get()
                    .load(Uri.parse(url))
                    .resize(180, 180)
                    .centerCrop()
                    .noFade()
                    .transform(RoundedCornersTransform(8f))
                    .placeholder(R.drawable.ps_image_placeholder)
                    .into(imageView)
            }
            PictureMimeType.isUrlHasVideo(url) -> {
                val picasso = Picasso.Builder(context.applicationContext)
                    .addRequestHandler(videoRequestHandler)
                    .build()
                picasso.load("${videoRequestHandler.SCHEME_VIDEO}:$url")
                    .resize(180, 180)
                    .centerCrop()
                    .noFade()
                    .transform(RoundedCornersTransform(8f))
                    .placeholder(R.drawable.ps_image_placeholder)
                    .into(imageView)
            }
            else -> {
                Picasso.get()
                    .load(File(url))
                    .resize(180, 180)
                    .centerCrop()
                    .noFade()
                    .transform(RoundedCornersTransform(8f))
                    .placeholder(R.drawable.ps_image_placeholder)
                    .into(imageView)
            }
        }
    }

    /**
     * 加载图片列表图片
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadGridImage(@NonNull context: Context, @NonNull url: String, @NonNull imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        val videoRequestHandler = VideoRequestHandler()
        when {
            PictureMimeType.isContent(url) -> {
                Picasso.get()
                    .load(Uri.parse(url))
                    .resize(200, 200)
                    .centerCrop()
                    .noFade()
                    .placeholder(R.drawable.ps_image_placeholder)
                    .into(imageView)
            }
            PictureMimeType.isUrlHasVideo(url) -> {
                val picasso = Picasso.Builder(context.applicationContext)
                    .addRequestHandler(videoRequestHandler)
                    .build()
                picasso.load("${videoRequestHandler.SCHEME_VIDEO}:$url")
                    .resize(200, 200)
                    .centerCrop()
                    .noFade()
                    .placeholder(R.drawable.ps_image_placeholder)
                    .into(imageView)
            }
            else -> {
                Picasso.get()
                    .load(File(url))
                    .resize(200, 200)
                    .centerCrop()
                    .noFade()
                    .placeholder(R.drawable.ps_image_placeholder)
                    .into(imageView)
            }
        }
    }

    override fun pauseRequests(context: Context) {
        Picasso.get().pauseTag(context)
    }

    override fun resumeRequests(context: Context) {
        Picasso.get().resumeTag(context)
    }

    companion object {
        private object InstanceHolder {
            val instance = PicassoEngine()
        }

        @JvmStatic
        fun createPicassoEngine(): PicassoEngine {
            return InstanceHolder.instance
        }
    }
}

