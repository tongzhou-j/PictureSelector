package com.luck.picture.lib.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.luck.picture.lib.config.PictureMimeType
import java.io.File

/**
 * @author：luck
 * @date：2023/3/25 6:21 下午
 * @describe：IntentUtils
 */
object IntentUtils {
    fun startSystemPlayerVideo(context: Context, path: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val isParseUri = PictureMimeType.isContent(path) || PictureMimeType.isHasHttp(path)
        val data: Uri?
        if (SdkVersionUtils.isQ) {
            data = if (isParseUri) Uri.parse(path) else Uri.fromFile(File(path))
        } else if (SdkVersionUtils.isMaxN) {
            data = if (isParseUri) Uri.parse(path) else FileProvider.getUriForFile(
                context,
                context.packageName + ".luckProvider",
                File(path)
            )
        } else {
            data = if (isParseUri) Uri.parse(path) else Uri.fromFile(File(path))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(data, "video/*")
        context.startActivity(intent)
    }
}
