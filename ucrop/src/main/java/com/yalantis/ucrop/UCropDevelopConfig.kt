package com.yalantis.ucrop

import kotlin.jvm.JvmField
import kotlin.jvm.JvmStatic

/**
 * @author：luck
 * @date：2021/12/2 10:23 上午
 * @describe：UCropDevelopConfig
 */
object UCropDevelopConfig {
    /**
     * 图片加载引擎
     */
    @JvmField
    var imageEngine: UCropImageEngine? = null

    /**
     * 释放监听器
     */
    @JvmStatic
    fun destroy() {
        imageEngine = null
    }
}

