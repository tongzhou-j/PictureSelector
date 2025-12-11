package com.luck.picture.lib.engine

import android.content.Context
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnCallbackListener

/**
 * @author：luck
 * @date：2021/5/19 9:36 AM
 * @describe：CompressEngine Please use [CompressFileEngine]
 */
@Deprecated("")
interface CompressEngine {
    /**
     * Custom compression engine
     *
     *
     * Users can implement this interface, and then access their own compression framework to plug
     * the compressed path into the [LocalMedia] object;
     *
     *
     *
     *
     *
     * 1、LocalMedia media = new LocalMedia();
     * media.setCompressed(true);
     * media.compressPath = "Your compressed path");
     *
     *
     *
     * 2、listener.onCall( "you result" );
     *
     *
     * @param context
     * @param list
     * @param listener
     */
    fun onStartCompress(
        context: Context?,
        list: ArrayList<LocalMedia?>?,
        listener: OnCallbackListener<ArrayList<LocalMedia?>?>?
    )
}
