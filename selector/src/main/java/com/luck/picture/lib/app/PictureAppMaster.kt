package com.luck.picture.lib.app

import android.content.Context
import com.luck.picture.lib.engine.PictureSelectorEngine

/**
 * @author：luck
 * @date：2019-12-03 15:12
 * @describe：PictureAppMaster
 */
class PictureAppMaster private constructor() : IApp {
    override val appContext: Context?
        get() {
            if (app == null) {
                return null
            }
            return app!!.appContext
        }

    override val pictureSelectorEngine: PictureSelectorEngine?
        get() {
            if (app == null) {
                return null
            }
            return app!!.pictureSelectorEngine
        }

    var app: IApp? = null

    companion object {
        private var mInstance: PictureAppMaster? = null

        val instance: PictureAppMaster
            get() {
                if (PictureAppMaster.Companion.mInstance == null) {
                    synchronized(PictureAppMaster::class.java) {
                        if (PictureAppMaster.Companion.mInstance == null) {
                            PictureAppMaster.Companion.mInstance = PictureAppMaster()
                        }
                    }
                }
                return PictureAppMaster.Companion.mInstance!!
            }
    }
}
