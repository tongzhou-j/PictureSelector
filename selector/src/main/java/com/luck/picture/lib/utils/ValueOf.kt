package com.luck.picture.lib.utils

/**
 * @author：luck
 * @date：2019-11-12 14:27
 * @describe：类型转换工具类
 */
object ValueOf {
    @JvmStatic
    fun toString(o: Any?): String {
        var value = ""
        try {
            value = o?.toString() ?: ""
        } catch (e: Exception) {
        }
        return value
    }

    @JvmStatic
    fun toDouble(o: Any?): Double {
        return toDouble(o, 0)
    }

    @JvmStatic
    fun toDouble(o: Any?, defaultValue: Int): Double {
        if (o == null) {
            return defaultValue.toDouble()
        }
        val value: Double
        value = try {
            o.toString().trim().toDouble()
        } catch (e: Exception) {
            defaultValue.toDouble()
        }
        return value
    }

    @JvmStatic
    fun toLong(o: Any?, defaultValue: Long): Long {
        if (o == null) {
            return defaultValue
        }
        var value = 0L
        value = try {
            val s = o.toString().trim()
            if (s.contains(".")) {
                s.substring(0, s.lastIndexOf(".")).toLong()
            } else {
                s.toLong()
            }
        } catch (e: Exception) {
            defaultValue
        }
        return value
    }

    @JvmStatic
    fun toLong(o: Any?): Long {
        return toLong(o, 0)
    }

    @JvmStatic
    fun toFloat(o: Any?, defaultValue: Long): Float {
        if (o == null) {
            return defaultValue.toFloat()
        }
        var value = 0f
        value = try {
            val s = o.toString().trim()
            s.toFloat()
        } catch (e: Exception) {
            defaultValue.toFloat()
        }
        return value
    }

    @JvmStatic
    fun toFloat(o: Any?): Float {
        return toFloat(o, 0)
    }

    @JvmStatic
    fun toInt(o: Any?, defaultValue: Int): Int {
        if (o == null) {
            return defaultValue
        }
        val value: Int
        value = try {
            val s = o.toString().trim()
            if (s.contains(".")) {
                s.substring(0, s.lastIndexOf(".")).toInt()
            } else {
                s.toInt()
            }
        } catch (e: Exception) {
            defaultValue
        }
        return value
    }

    @JvmStatic
    fun toInt(o: Any?): Int {
        return toInt(o, 0)
    }

    @JvmStatic
    fun toBoolean(o: Any?): Boolean {
        return toBoolean(o, false)
    }

    @JvmStatic
    fun toBoolean(o: Any?, defaultValue: Boolean): Boolean {
        if (o == null) {
            return false
        }
        val value: Boolean
        value = try {
            val s = o.toString().trim()
            s.trim() != "false"
        } catch (e: Exception) {
            defaultValue
        }
        return value
    }

    @JvmStatic
    fun <T> to(o: Any?, defaultValue: T): T {
        if (o == null) {
            return defaultValue
        }
        @Suppress("UNCHECKED_CAST")
        return o as T
    }
}

