package com.luck.picture.lib.utils

import android.os.Build

/**
 * @author：luck
 * @date：2019-07-17 15:12
 * @describe：Android Sdk版本判断
 */
object SdkVersionUtils {
    val isMinM: Boolean
        /**
         * 判断是否是低于Android LOLLIPOP版本
         */
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.M

    val isO: Boolean
        /**
         * 判断是否是Android O版本
         */
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O


    val isMaxN: Boolean
        /**
         * 判断是否是Android N版本
         */
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N


    val isN: Boolean
        /**
         * 判断是否是Android N版本
         */
        get() = Build.VERSION.SDK_INT == Build.VERSION_CODES.N

    val isP: Boolean
        /**
         * 判断是否是Android P版本
         */
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    val isQ: Boolean
        /**
         * 判断是否是Android Q版本
         */
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val isR: Boolean
        /**
         * 判断是否是Android R版本
         */
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    val isTIRAMISU: Boolean
        /**
         * 判断是否是Android TIRAMISU版本
         */
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    val isUPSIDE_DOWN_CAKE: Boolean
        /**
         * 判断是否是Android UPSIDE_DOWN_CAKE版本
         */
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}
