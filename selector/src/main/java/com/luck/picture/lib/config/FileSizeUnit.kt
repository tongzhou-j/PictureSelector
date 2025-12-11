package com.luck.picture.lib.config

/**
 * @author：luck
 * @date：2022/6/3 1:49 下午
 * @describe：文件大小单位
 */
object FileSizeUnit {
    const val KB: Long = 1024
    val MB: Long = (1024 * 1024).toLong()
    val GB: Long = (1024 * 1024 * 1024).toLong()

    val ACCURATE_GB: Int = 1000 * 1000 * 1000
    val ACCURATE_MB: Int = 1000 * 1000
    const val ACCURATE_KB: Int = 1000
}
