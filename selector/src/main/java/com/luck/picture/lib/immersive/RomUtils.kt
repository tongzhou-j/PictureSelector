package com.luck.picture.lib.immersive

import android.os.Build
import android.text.TextUtils
import com.luck.picture.lib.utils.ValueOf.toInt
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale
import java.util.regex.Pattern

/**
 * @author：luck
 * @data：2018/3/28 下午1:02
 * @描述: Rom版本管理
 */
object RomUtils {
    private val ROM_SAMSUNG = arrayOf<String>("samsung")
    private const val UNKNOWN = "unknown"

    private var romType: Int? = null

    val lightStatusBarAvailableRomType: Int
        get() {
            if (RomUtils.romType != null) {
                return RomUtils.romType!!
            }

            if (RomUtils.isMIUIV6OrAbove) {
                RomUtils.romType =
                    AvailableRomType.MIUI
                return RomUtils.romType!!
            }

            if (RomUtils.isFlymeV4OrAbove) {
                RomUtils.romType =
                    AvailableRomType.FLYME
                return RomUtils.romType!!
            }

            if (RomUtils.isAndroid5OrAbove) {
                RomUtils.romType =
                    AvailableRomType.ANDROID_NATIVE
                return RomUtils.romType!!
            }

            RomUtils.romType =
                AvailableRomType.NA
            return RomUtils.romType!!
        }

    private val isFlymeV4OrAbove: Boolean
        //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
        get() = (flymeVersion >= 4)


    val flymeVersion: Int
        //Flyme V4的displayId格式为 [Flyme OS 4.x.x.xA]
        get() {
            var displayId = Build.DISPLAY
            if (!TextUtils.isEmpty(displayId) && displayId!!.contains("Flyme")) {
                displayId = displayId.replace("Flyme".toRegex(), "")
                displayId = displayId.replace("OS".toRegex(), "")
                displayId = displayId.replace(" ".toRegex(), "")


                val version = displayId.substring(0, 1)

                return stringToInt(version)
            }
            return 0
        }

    private val isMIUIV6OrAbove: Boolean
        //MIUI V6对应的versionCode是4
        get() {
            val miuiVersionCodeStr: String? =
                RomUtils.systemProperty
            if (!TextUtils.isEmpty(miuiVersionCodeStr)) {
                try {
                    val miuiVersionCode =
                        toInt(miuiVersionCodeStr)
                    if (miuiVersionCode >= 4) {
                        return true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return false
        }


    val mIUIVersionCode: Int
        get() {
            val miuiVersionCodeStr: String? =
                RomUtils.systemProperty
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


    private val isAndroid5OrAbove: Boolean
        //Android Api 23以上
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP


    private val systemProperty: String?
        get() {
            var line: String?
            var input: BufferedReader? = null
            try {
                val p =
                    Runtime.getRuntime().exec("getprop " + "ro.miui.ui.version.code")
                input = BufferedReader(InputStreamReader(p.getInputStream()), 1024)
                line = input.readLine()
                input.close()
            } catch (ex: IOException) {
                return null
            } finally {
                if (input != null) {
                    try {
                        input.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            return line
        }

    val isSamsung: Boolean
        /**
         * Return whether the rom is made by samsung.
         *
         * @return `true`: yes<br></br>`false`: no
         */
        get() {
            val brand: String = RomUtils.brand
            val manufacturer: String = RomUtils.manufacturer
            return RomUtils.isRightRom(
                brand,
                manufacturer,
                *RomUtils.ROM_SAMSUNG
            )
        }

    private fun isRightRom(brand: String, manufacturer: String, vararg names: String): Boolean {
        for (name in names) {
            if (brand.contains(name) || manufacturer.contains(name)) {
                return true
            }
        }
        return false
    }

    private val manufacturer: String
        get() {
            try {
                val manufacturer = Build.MANUFACTURER
                if (!TextUtils.isEmpty(manufacturer)) {
                    return manufacturer!!.lowercase(Locale.getDefault())
                }
            } catch (ignore: Throwable) { /**/
            }
            return RomUtils.UNKNOWN
        }

    private val brand: String
        get() {
            try {
                val brand = Build.BRAND
                if (!TextUtils.isEmpty(brand)) {
                    return brand!!.lowercase(Locale.getDefault())
                }
            } catch (ignore: Throwable) { /**/
            }
            return RomUtils.UNKNOWN
        }

    /**
     * 匹配数值
     *
     * @param str
     * @return
     */
    fun stringToInt(str: String): Int {
        val pattern = Pattern.compile("^[-\\+]?[\\d]+$")
        return if (pattern.matcher(str).matches()) toInt(str) else 0
    }

    object AvailableRomType {
        const val MIUI: Int = 1
        const val FLYME: Int = 2
        const val ANDROID_NATIVE: Int = 3
        const val NA: Int = 4
    }
}
