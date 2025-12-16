package tv.danmaku.ijk.media.player

import android.annotation.TargetApi
import android.media.MediaCodecInfo
import android.media.MediaCodecInfo.CodecCapabilities
import android.media.MediaCodecInfo.CodecProfileLevel
import android.os.Build
import android.text.TextUtils
import android.util.Log
import java.util.Locale
import java.util.TreeMap
import kotlin.jvm.JvmStatic
import kotlin.math.max

class IjkMediaCodecInfo {
    companion object {
        private const val TAG = "IjkMediaCodecInfo"

        const val RANK_MAX = 1000
        const val RANK_TESTED = 800
        const val RANK_ACCEPTABLE = 700
        const val RANK_LAST_CHANCE = 600
        const val RANK_SECURE = 300
        const val RANK_SOFTWARE = 200
        const val RANK_NON_STANDARD = 100
        const val RANK_NO_SENSE = 0

        @Volatile
        private var sKnownCodecList: TreeMap<String, Int>? = null

        @Synchronized
        private fun getKnownCodecList(): TreeMap<String, Int> {
            if (sKnownCodecList != null) {
                return sKnownCodecList!!
            }

            sKnownCodecList = TreeMap(String.CASE_INSENSITIVE_ORDER)

            // ----- Nvidia -----
            // Tegra3
            // Nexus 7 (2012)
            // Tegra K1
            // Nexus 9
            sKnownCodecList!!["OMX.Nvidia.h264.decode"] = RANK_TESTED
            sKnownCodecList!!["OMX.Nvidia.h264.decode.secure"] = RANK_SECURE

            // ----- Intel -----
            // Atom Z3735
            // Teclast X98 Air
            sKnownCodecList!!["OMX.Intel.hw_vd.h264"] = RANK_TESTED + 1
            // Atom Z2560
            // Dell Venue 7 3730
            sKnownCodecList!!["OMX.Intel.VideoDecoder.AVC"] = RANK_TESTED

            // ----- Qualcomm -----
            // MSM8260
            // Xiaomi MI 1S
            sKnownCodecList!!["OMX.qcom.video.decoder.avc"] = RANK_TESTED
            sKnownCodecList!!["OMX.ittiam.video.decoder.avc"] = RANK_NO_SENSE

            // ----- Samsung -----
            // Exynos 3110
            // Nexus S
            sKnownCodecList!!["OMX.SEC.avc.dec"] = RANK_TESTED
            sKnownCodecList!!["OMX.SEC.AVC.Decoder"] = RANK_TESTED - 1
            // OMX.SEC.avcdec doesn't reorder output pictures on GT-9100
            sKnownCodecList!!["OMX.SEC.avcdec"] = RANK_TESTED - 2
            sKnownCodecList!!["OMX.SEC.avc.sw.dec"] = RANK_SOFTWARE
            // Exynos 5 ?
            sKnownCodecList!!["OMX.Exynos.avc.dec"] = RANK_TESTED
            sKnownCodecList!!["OMX.Exynos.AVC.Decoder"] = RANK_TESTED - 1

            // ------ Huawei hisilicon ------
            // Kirin 910, Mali 450 MP
            // Huawei HONOR 3C (H30-L01)
            sKnownCodecList!!["OMX.k3.video.decoder.avc"] = RANK_TESTED
            // Kirin 920, Mali T624
            // Huawei HONOR 6
            sKnownCodecList!!["OMX.IMG.MSVDX.Decoder.AVC"] = RANK_TESTED

            // ----- TI -----
            // TI OMAP4460
            // Galaxy Nexus
            sKnownCodecList!!["OMX.TI.DUCATI1.VIDEO.DECODER"] = RANK_TESTED

            // ------ RockChip ------
            // Youku TVBox
            sKnownCodecList!!["OMX.rk.video_decoder.avc"] = RANK_TESTED

            // ------ AMLogic -----
            // MiBox1, 1s, 2
            sKnownCodecList!!["OMX.amlogic.avc.decoder.awesome"] = RANK_TESTED

            // ------ Marvell ------
            // Lenovo A788t
            sKnownCodecList!!["OMX.MARVELL.VIDEO.HW.CODA7542DECODER"] = RANK_TESTED
            sKnownCodecList!!["OMX.MARVELL.VIDEO.H264DECODER"] = RANK_SOFTWARE

            // ----- TODO: need test -----
            sKnownCodecList!!.remove("OMX.Action.Video.Decoder")
            sKnownCodecList!!.remove("OMX.allwinner.video.decoder.avc")
            sKnownCodecList!!.remove("OMX.BRCM.vc4.decoder.avc")
            sKnownCodecList!!.remove("OMX.brcm.video.h264.hw.decoder")
            sKnownCodecList!!.remove("OMX.brcm.video.h264.decoder")
            sKnownCodecList!!.remove("OMX.cosmo.video.decoder.avc")
            sKnownCodecList!!.remove("OMX.duos.h264.decoder")
            sKnownCodecList!!.remove("OMX.hantro.81x0.video.decoder")
            sKnownCodecList!!.remove("OMX.hantro.G1.video.decoder")
            sKnownCodecList!!.remove("OMX.hisi.video.decoder")
            sKnownCodecList!!.remove("OMX.LG.decoder.video.avc")
            sKnownCodecList!!.remove("OMX.MS.AVC.Decoder")
            sKnownCodecList!!.remove("OMX.RENESAS.VIDEO.DECODER.H264")
            sKnownCodecList!!.remove("OMX.RTK.video.decoder")
            sKnownCodecList!!.remove("OMX.sprd.h264.decoder")
            sKnownCodecList!!.remove("OMX.ST.VFM.H264Dec")
            sKnownCodecList!!.remove("OMX.vpu.video_decoder.avc")
            sKnownCodecList!!.remove("OMX.WMT.decoder.avc")
            sKnownCodecList!!.remove("OMX.bluestacks.hw.decoder")

            // ---------------
            // Useless codec
            // ----- google -----
            sKnownCodecList!!["OMX.google.h264.decoder"] = RANK_SOFTWARE
            sKnownCodecList!!["OMX.google.h264.lc.decoder"] = RANK_SOFTWARE
            // ----- huawei k920 -----
            sKnownCodecList!!["OMX.k3.ffmpeg.decoder"] = RANK_SOFTWARE
            sKnownCodecList!!["OMX.ffmpeg.video.decoder"] = RANK_SOFTWARE
            // ----- unknown -----
            sKnownCodecList!!["OMX.sprd.soft.h264.decoder"] = RANK_SOFTWARE

            return sKnownCodecList!!
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @JvmStatic
        fun setupCandidate(codecInfo: MediaCodecInfo?, mimeType: String): IjkMediaCodecInfo? {
            if (codecInfo == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                return null
            }

            var name = codecInfo.name
            if (TextUtils.isEmpty(name)) {
                return null
            }

            name = name.lowercase(Locale.US)
            val rank = when {
                !name.startsWith("omx.") -> RANK_NON_STANDARD
                name.startsWith("omx.pv") -> RANK_SOFTWARE
                name.startsWith("omx.google.") -> RANK_SOFTWARE
                name.startsWith("omx.ffmpeg.") -> RANK_SOFTWARE
                name.startsWith("omx.k3.ffmpeg.") -> RANK_SOFTWARE
                name.startsWith("omx.avcodec.") -> RANK_SOFTWARE
                name.startsWith("omx.ittiam.") -> RANK_NO_SENSE
                name.startsWith("omx.mtk.") -> {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        RANK_NO_SENSE
                    } else {
                        RANK_TESTED
                    }
                }
                else -> {
                    val knownRank = getKnownCodecList()[name]
                    if (knownRank != null) {
                        knownRank
                    } else {
                        try {
                            val cap = codecInfo.getCapabilitiesForType(mimeType)
                            if (cap != null) {
                                RANK_ACCEPTABLE
                            } else {
                                RANK_LAST_CHANCE
                            }
                        } catch (e: Throwable) {
                            RANK_LAST_CHANCE
                        }
                    }
                }
            }

            val candidate = IjkMediaCodecInfo()
            candidate.mCodecInfo = codecInfo
            candidate.mRank = rank
            candidate.mMimeType = mimeType
            return candidate
        }

        @JvmStatic
        fun getProfileLevelName(profile: Int, level: Int): String {
            return String.format(Locale.US, " %s Profile Level %s (%d,%d)",
                getProfileName(profile), getLevelName(level), profile, level)
        }

        @JvmStatic
        fun getProfileName(profile: Int): String {
            return when (profile) {
                CodecProfileLevel.AVCProfileBaseline -> "Baseline"
                CodecProfileLevel.AVCProfileMain -> "Main"
                CodecProfileLevel.AVCProfileExtended -> "Extends"
                CodecProfileLevel.AVCProfileHigh -> "High"
                CodecProfileLevel.AVCProfileHigh10 -> "High10"
                CodecProfileLevel.AVCProfileHigh422 -> "High422"
                CodecProfileLevel.AVCProfileHigh444 -> "High444"
                else -> "Unknown"
            }
        }

        @JvmStatic
        fun getLevelName(level: Int): String {
            return when (level) {
                CodecProfileLevel.AVCLevel1 -> "1"
                CodecProfileLevel.AVCLevel1b -> "1b"
                CodecProfileLevel.AVCLevel11 -> "11"
                CodecProfileLevel.AVCLevel12 -> "12"
                CodecProfileLevel.AVCLevel13 -> "13"
                CodecProfileLevel.AVCLevel2 -> "2"
                CodecProfileLevel.AVCLevel21 -> "21"
                CodecProfileLevel.AVCLevel22 -> "22"
                CodecProfileLevel.AVCLevel3 -> "3"
                CodecProfileLevel.AVCLevel31 -> "31"
                CodecProfileLevel.AVCLevel32 -> "32"
                CodecProfileLevel.AVCLevel4 -> "4"
                CodecProfileLevel.AVCLevel41 -> "41"
                CodecProfileLevel.AVCLevel42 -> "42"
                CodecProfileLevel.AVCLevel5 -> "5"
                CodecProfileLevel.AVCLevel51 -> "51"
                65536 -> "52" // CodecProfileLevel.AVCLevel52:
                else -> "0"
            }
        }
    }

    @JvmField
    var mCodecInfo: MediaCodecInfo? = null
    @JvmField
    var mRank = 0
    @JvmField
    var mMimeType: String? = null

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun dumpProfileLevels(mimeType: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return
        }

        try {
            val caps = mCodecInfo?.getCapabilitiesForType(mimeType)
            var maxProfile = 0
            var maxLevel = 0
            if (caps != null) {
                if (caps.profileLevels != null) {
                    for (profileLevel in caps.profileLevels) {
                        if (profileLevel == null) continue

                        maxProfile = max(maxProfile, profileLevel.profile)
                        maxLevel = max(maxLevel, profileLevel.level)
                    }
                }
            }

            Log.i(TAG, String.format(Locale.US, "%s", getProfileLevelName(maxProfile, maxLevel)))
        } catch (e: Throwable) {
            Log.i(TAG, "profile-level: exception")
        }
    }
}

