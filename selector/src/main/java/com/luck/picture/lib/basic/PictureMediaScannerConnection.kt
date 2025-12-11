package com.luck.picture.lib.basic

import android.content.Context
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.MediaScannerConnectionClient
import android.net.Uri
import android.text.TextUtils

/**
 * @author：luck
 * @date：2019-12-03 10:41
 * @describe：刷新相册
 */
class PictureMediaScannerConnection : MediaScannerConnectionClient {
    interface ScanListener {
        fun onScanFinish()
    }

    private val mMs: MediaScannerConnection
    private val mPath: String?
    private var mListener: ScanListener? = null

    constructor(context: Context, path: String?, l: ScanListener?) {
        this.mListener = l
        this.mPath = path
        this.mMs = MediaScannerConnection(context.getApplicationContext(), this)
        this.mMs.connect()
    }

    constructor(context: Context, path: String?) {
        this.mPath = path
        this.mMs = MediaScannerConnection(context.getApplicationContext(), this)
        this.mMs.connect()
    }

    override fun onMediaScannerConnected() {
        if (!TextUtils.isEmpty(mPath)) {
            mMs.scanFile(mPath, null)
        }
    }

    override fun onScanCompleted(path: String?, uri: Uri?) {
        mMs.disconnect()
        if (mListener != null) {
            mListener!!.onScanFinish()
        }
    }
}
