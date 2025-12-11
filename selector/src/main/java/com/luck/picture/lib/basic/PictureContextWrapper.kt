package com.luck.picture.lib.basic

import android.content.Context
import android.content.ContextWrapper
import com.luck.picture.lib.language.LanguageConfig
import com.luck.picture.lib.language.PictureLanguageUtils

/**
 * @author：luck
 * @date：2019-12-15 19:34
 * @describe：ContextWrapper
 */
class PictureContextWrapper(base: Context?) : ContextWrapper(base) {
    override fun getSystemService(name: String): Any? {
        if (Context.AUDIO_SERVICE == name) {
            return getApplicationContext().getSystemService(name)
        }
        return super.getSystemService(name)
    }

    companion object {
        fun wrap(context: Context?, language: Int, defaultLanguage: Int): ContextWrapper {
            if (language != LanguageConfig.UNKNOWN_LANGUAGE && context != null) {
                PictureLanguageUtils.setAppLanguage(context, language, defaultLanguage)
            }
            return PictureContextWrapper(context!!)
        }
    }
}
