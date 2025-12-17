package com.yalantis.ucrop.statusbar

import android.os.Build
import android.text.TextUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.regex.Pattern
import kotlin.jvm.JvmStatic

/**
 * @author：luck
 * @data：2018/3/28 下午1:02
 * @描述: Rom版本管理
 */
object RomUtils {
    private val ROM_SAMSUNG = arrayOf("samsung")
    private const val UNKNOWN = "unknown"

    object AvailableRomType {
        const val MIUI = 1
        const val FLYME = 2
        const val ANDROID_NATIVE = 3
        const val NA = 4
    }

    @Volatile
    private var romType: Int? = null

    @JvmStatic
    fun getLightStatausBarAvailableRomType(): Int {
        if (romType != null) {
            return romType!!
        }

        romType = when {
            isMIUIV6OrAbove() -> AvailableRomType.MIUI
            isFlymeV4OrAbove() -> AvailableRomType.FLYME
            isAndroid5OrAbove() -> AvailableRomType.ANDROID_NATIVE
            else -> AvailableRomType.NA
        }
        return romType!!
    }

    //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
    //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
    private fun isFlymeV4OrAbove(): Boolean {
        return getFlymeVersion() >= 4
    }

    //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
    //Flyme V5的displayId格式为 [Flyme 5.x.x.x beta]
    @JvmStatic
    fun getFlymeVersion(): Int {
        var displayId = Build.DISPLAY
        if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
            displayId = displayId.replace("Flyme", "")
            displayId = displayId.replace("OS", "")
            displayId = displayId.replace(" ", "")

            val version = displayId.substring(0, 1)
            return stringToInt(version)
        }
        return 0
    }

    //MIUI V6对应的versionCode是4
    //MIUI V7对应的versionCode是5
    private fun isMIUIV6OrAbove(): Boolean {
        val miuiVersionCodeStr = getSystemProperty()
        if (!TextUtils.isEmpty(miuiVersionCodeStr)) {
            try {
                val miuiVersionCode = toInt(miuiVersionCodeStr)
                if (miuiVersionCode >= 4) {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    @JvmStatic
    fun getMIUIVersionCode(): Int {
        val miuiVersionCodeStr = getSystemProperty()
        var miuiVersionCode = 0
        if (!TextUtils.isEmpty(miuiVersionCodeStr)) {
            try {
                miuiVersionCode = toInt(miuiVersionCodeStr)
                return miuiVersionCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return miuiVersionCode
    }

    //Android Api 23以上
    private fun isAndroid5OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }

    private fun getSystemProperty(): String? {
        var line: String?
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop ro.miui.ui.version.code")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            return null
        } finally {
            input?.close()
        }
        return line
    }

    /**
     * Return whether the rom is made by samsung.
     *
     * @return {@code true}: yes<br>{@code false}: no
     */
    @JvmStatic
    fun isSamsung(): Boolean {
        val brand = getBrand()
        val manufacturer = getManufacturer()
        return isRightRom(brand, manufacturer, *ROM_SAMSUNG)
    }

    private fun isRightRom(brand: String, manufacturer: String, vararg names: String): Boolean {
        for (name in names) {
            if (brand.contains(name) || manufacturer.contains(name)) {
                return true
            }
        }
        return false
    }

    private fun getManufacturer(): String {
        return try {
            val manufacturer = Build.MANUFACTURER
            if (!TextUtils.isEmpty(manufacturer)) {
                manufacturer.lowercase()
            } else {
                UNKNOWN
            }
        } catch (ignore: Throwable) {
            UNKNOWN
        }
    }

    private fun getBrand(): String {
        return try {
            val brand = Build.BRAND
            if (!TextUtils.isEmpty(brand)) {
                brand.lowercase()
            } else {
                UNKNOWN
            }
        } catch (ignore: Throwable) {
            UNKNOWN
        }
    }

    /**
     * 匹配数值
     *
     * @param str
     * @return
     */
    @JvmStatic
    fun stringToInt(str: String): Int {
        val pattern = Pattern.compile("^[-\\+]?[\\d]+$")
        return if (pattern.matcher(str).matches()) {
            toInt(str)
        } else {
            0
        }
    }

    @JvmStatic
    fun toInt(o: Any?): Int {
        return toInt(o, 0)
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
                Integer.parseInt(s.substring(0, s.lastIndexOf(".")))
            } else {
                Integer.parseInt(s)
            }
        } catch (e: Exception) {
            defaultValue
        }
        return value
    }
}

