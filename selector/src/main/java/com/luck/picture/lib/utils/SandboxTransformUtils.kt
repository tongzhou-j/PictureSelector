package com.luck.picture.lib.utils

import android.content.Context
import android.net.Uri
import com.luck.picture.lib.basic.PictureContentResolver
import com.luck.picture.lib.config.PictureMimeType
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

/**
 * @author：luck
 * @date：2019-11-08 19:25
 * @describe：SandboxTransformUtils
 */
object SandboxTransformUtils {
    /**
     * 把外部目录下的图片拷贝至沙盒内
     *
     * @param ctx
     * @param url
     * @param mineType
     * @param customFileName
     * @return
     */
    /**
     * 把外部目录下的图片拷贝至沙盒内
     *
     * @param ctx
     * @param url
     * @param mineType
     * @return
     */
    @JvmOverloads
    fun copyPathToSandbox(
        ctx: Context?,
        url: String?,
        mineType: String?,
        customFileName: String? = ""
    ): String? {
        try {
            if (PictureMimeType.isHasHttp(url)) {
                return null
            }
            val context = ctx ?: return null
            val inputStream: InputStream?
            val sandboxPath = PictureFileUtils.createFilePath(context, mineType, customFileName)
            if (PictureMimeType.isContent(url)) {
                inputStream = PictureContentResolver.openInputStream(context, Uri.parse(url))
            } else {
                inputStream = FileInputStream(url)
            }
            val copyFileSuccess =
                PictureFileUtils.writeFileFromIS(inputStream, FileOutputStream(sandboxPath))
            if (copyFileSuccess) {
                return sandboxPath
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
