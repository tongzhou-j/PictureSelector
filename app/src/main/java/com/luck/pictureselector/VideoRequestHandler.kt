package com.luck.pictureselector

import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.Nullable
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import java.io.IOException

/**
 * @author：luck
 * @date：2020/4/30 11:32 AM
 * @describe：VideoRequestHandler
 */
class VideoRequestHandler : RequestHandler() {
    val SCHEME_VIDEO = "video"

    override fun canHandleRequest(data: Request): Boolean {
        val scheme = data.uri.scheme
        return SCHEME_VIDEO == scheme
    }

    @Nullable
    @Throws(IOException::class)
    override fun load(request: Request, networkPolicy: Int): Result? {
        val uri = request.uri
        val path = uri.path
        return if (!TextUtils.isEmpty(path)) {
            val bm = ThumbnailUtils.createVideoThumbnail(path!!, MediaStore.Images.Thumbnails.MINI_KIND)
            bm?.let { Result(it, Picasso.LoadedFrom.DISK) }
        } else {
            null
        }
    }
}

