package com.luck.picture.lib.basic

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.io.OutputStream

/**
 * @author：luck
 * @date：2021/5/26 9:22 PM
 * @describe：PictureContentResolver
 */
object PictureContentResolver {
    /**
     * ContentResolver openInputStream
     *
     * @param context
     * @param uri
     * @return
     */
    fun openInputStream(context: Context, uri: Uri): InputStream? {
        try {
            return context.getContentResolver().openInputStream(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * ContentResolver OutputStream
     *
     * @param context
     * @param uri
     * @return
     */
    fun openOutputStream(context: Context, uri: Uri): OutputStream? {
        try {
            return context.getContentResolver().openOutputStream(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
