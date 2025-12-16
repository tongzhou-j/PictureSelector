/*
 * Copyright (C) 2015 Bilibili
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.player

import android.annotation.TargetApi
import android.graphics.SurfaceTexture
import android.os.Build
import android.view.Surface
import android.view.SurfaceHolder

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
class TextureMediaPlayer(backEndMediaPlayer: IMediaPlayer) : MediaPlayerProxy(backEndMediaPlayer), ISurfaceTextureHolder {
    private var mSurfaceTexture: SurfaceTexture? = null
    private var mSurfaceTextureHost: ISurfaceTextureHost? = null

    fun releaseSurfaceTexture() {
        if (mSurfaceTexture != null) {
            if (mSurfaceTextureHost != null) {
                mSurfaceTextureHost!!.releaseSurfaceTexture(mSurfaceTexture)
            } else {
                mSurfaceTexture!!.release()
            }
            mSurfaceTexture = null
        }
    }

    //--------------------
    // IMediaPlayer
    //--------------------
    override fun reset() {
        super.reset()
        releaseSurfaceTexture()
    }

    override fun release() {
        super.release()
        releaseSurfaceTexture()
    }

    override fun setDisplay(sh: SurfaceHolder?) {
        if (mSurfaceTexture == null) {
            super.setDisplay(sh)
        }
    }

    override fun setSurface(surface: Surface?) {
        if (mSurfaceTexture == null) {
            super.setSurface(surface)
        }
    }

    //--------------------
    // ISurfaceTextureHolder
    //--------------------

    override fun setSurfaceTexture(surfaceTexture: SurfaceTexture?) {
        if (mSurfaceTexture == surfaceTexture) {
            return
        }

        releaseSurfaceTexture()
        mSurfaceTexture = surfaceTexture
        if (surfaceTexture == null) {
            super.setSurface(null)
        } else {
            super.setSurface(Surface(surfaceTexture))
        }
    }

    override fun getSurfaceTexture(): SurfaceTexture? {
        return mSurfaceTexture
    }

    override fun setSurfaceTextureHost(surfaceTextureHost: ISurfaceTextureHost?) {
        mSurfaceTextureHost = surfaceTextureHost
    }
}

