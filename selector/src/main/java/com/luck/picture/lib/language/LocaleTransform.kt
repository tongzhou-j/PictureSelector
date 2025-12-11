package com.luck.picture.lib.language

import java.util.Locale

/**
 * @author：luck
 * @date：2019-11-25 21:58
 * @describe：语言转换
 */
object LocaleTransform {
    fun getLanguage(language: Int): Locale {
        when (language) {
            LanguageConfig.ENGLISH ->                 // 英语-美国
                return Locale.ENGLISH

            LanguageConfig.TRADITIONAL_CHINESE ->                 // 繁体中文
                return Locale.TRADITIONAL_CHINESE

            LanguageConfig.KOREA ->                 // 韩语
                return Locale.KOREA

            LanguageConfig.GERMANY ->                 // 德语
                return Locale.GERMANY

            LanguageConfig.FRANCE ->                 // 法语
                return Locale.FRANCE

            LanguageConfig.JAPAN ->                 // 日语
                return Locale.JAPAN

            LanguageConfig.VIETNAM ->                 // 越南语
                return Locale("vi")

            LanguageConfig.SPANISH ->                 // 西班牙语
                return Locale("es", "ES")

            LanguageConfig.PORTUGAL ->                 // 葡萄牙语
                return Locale("pt", "PT")

            LanguageConfig.AR ->                 // 阿拉伯语
                return Locale("ar", "AE")

            LanguageConfig.RU ->                 // 俄语
                return Locale("ru", "rRU")

            LanguageConfig.CS ->                 // 捷克
                return Locale("cs", "rCZ")

            LanguageConfig.KK ->                 // 哈萨克斯坦
                return Locale("kk", "rKZ")

            else ->                 // 简体中文
                return Locale.CHINESE
        }
    }
}
