package com.luck.picture.lib.language

import android.content.Context
import com.luck.picture.lib.utils.SpUtils
import java.util.Locale

/**
 * @author：luck
 * @data：2018/3/28 下午1:00
 * @描述: PictureLanguageUtils
 */
class PictureLanguageUtils private constructor() {
    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }

    companion object {
        private const val KEY_LOCALE = "KEY_LOCALE"
        private const val VALUE_FOLLOW_SYSTEM = "VALUE_FOLLOW_SYSTEM"

        /**
         * init app the language
         *
         * @param context
         * @param languageId
         * @param defaultLanguageId
         */
        fun setAppLanguage(context: Context, languageId: Int, defaultLanguageId: Int) {
            if (languageId >= 0) {
                applyLanguage(context, LocaleTransform.getLanguage(languageId))
            } else {
                if (defaultLanguageId >= 0) {
                    applyLanguage(context, LocaleTransform.getLanguage(defaultLanguageId))
                } else {
                    PictureLanguageUtils.Companion.setDefaultLanguage(context!!)
                }
            }
        }

        /**
         * Apply the language.
         *
         * @param locale The language of locale.
         */
        private fun applyLanguage(
            context: Context, locale: Locale,
            isFollowSystem: Boolean = false
        ) {
            if (isFollowSystem) {
                SpUtils.putString(
                    context,
                    PictureLanguageUtils.Companion.KEY_LOCALE,
                    PictureLanguageUtils.Companion.VALUE_FOLLOW_SYSTEM
                )
            } else {
                val localLanguage = locale.language
                val localCountry = locale.country
                SpUtils.putString(
                    context,
                    PictureLanguageUtils.Companion.KEY_LOCALE,
                    localLanguage + "$" + localCountry
                )
            }
            PictureLanguageUtils.Companion.updateLanguage(context, locale)
        }


        private fun updateLanguage(context: Context, locale: Locale) {
            val resources = context.resources
            val config = resources.configuration
            val contextLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                config.locales[0]
            } else {
                @Suppress("DEPRECATION")
                config.locale
            }
            if (PictureLanguageUtils.Companion.equals(
                    contextLocale.language,
                    locale.language
                )
                && PictureLanguageUtils.Companion.equals(
                    contextLocale.country,
                    locale.country
                )
            ) {
                return
            }
            val dm = resources.displayMetrics
            config.setLocale(locale)
            context.createConfigurationContext(config)
            resources.updateConfiguration(config, dm)
        }

        /**
         * set default language
         *
         * @param context
         */
        private fun setDefaultLanguage(context: Context) {
            val resources = context.resources
            val config = resources.configuration
            val dm = resources.displayMetrics
            config.setLocale(Locale.getDefault())
            context.createConfigurationContext(config)
            resources.updateConfiguration(config, dm)
        }

        private fun equals(s1: CharSequence?, s2: CharSequence?): Boolean {
            if (s1 === s2) return true
            if (s1 != null && s2 != null) {
                val length = s1.length
                if (length == s2.length) {
                    if (s1 is String && s2 is String) {
                        return s1 == s2
                    } else {
                        for (i in 0 until length) {
                            if (s1[i] != s2[i]) return false
                        }
                        return true
                    }
                }
            }
            return false
        }
    }
}
