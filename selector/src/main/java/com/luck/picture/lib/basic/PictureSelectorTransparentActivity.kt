package com.luck.picture.lib.basic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.luck.picture.lib.PictureOnlyCameraFragment
import com.luck.picture.lib.PictureOnlyCameraFragment.Companion.newInstance
import com.luck.picture.lib.PictureSelectorPreviewFragment
import com.luck.picture.lib.PictureSelectorSystemFragment
import com.luck.picture.lib.R
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.immersive.ImmersiveManager
import com.luck.picture.lib.utils.StyleUtils

/**
 * @author：luck
 * @date：2022/2/10 6:07 下午
 * @describe：PictureSelectorTransparentActivity
 */
class PictureSelectorTransparentActivity : AppCompatActivity() {
    private var selectorConfig: SelectorConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSelectorConfig()
        immersive()
        setContentView(R.layout.ps_empty)
        if (this.isExternalPreview) {
            // TODO ignore
        } else {
            setActivitySize()
        }
        setupFragment()
    }

    private fun initSelectorConfig() {
        selectorConfig = SelectorProviders.instance?.selectorConfig
    }

    private val isExternalPreview: Boolean
        get() {
            val modeTypeSource =
                getIntent().getIntExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE, 0)
            return modeTypeSource == PictureConfig.MODE_TYPE_EXTERNAL_PREVIEW_SOURCE
        }

    private fun immersive() {
        if (selectorConfig?.selectorStyle == null) {
            selectorConfig = SelectorProviders.instance?.selectorConfig
        }
        val mainStyle = selectorConfig?.selectorStyle?.selectMainStyle
        var statusBarColor = mainStyle?.statusBarColor ?: 0
        var navigationBarColor = mainStyle?.navigationBarColor ?: 0
        val isDarkStatusBarBlack = mainStyle?.isDarkStatusBarBlack ?: false
        if (!StyleUtils.checkStyleValidity(statusBarColor)) {
            statusBarColor = ContextCompat.getColor(this, R.color.ps_color_grey)
        }
        if (!StyleUtils.checkStyleValidity(navigationBarColor)) {
            navigationBarColor = ContextCompat.getColor(this, R.color.ps_color_grey)
        }
        ImmersiveManager.immersiveAboveAPI23(
            this,
            statusBarColor,
            navigationBarColor,
            isDarkStatusBarBlack
        )
    }

    private fun setupFragment() {
        val fragmentTag: String?
        var targetFragment: Fragment? = null
        val modeTypeSource = getIntent().getIntExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE, 0)
        if (modeTypeSource == PictureConfig.MODE_TYPE_SYSTEM_SOURCE) {
            fragmentTag = PictureSelectorSystemFragment.Companion.TAG
            targetFragment = PictureSelectorSystemFragment.Companion.newInstance() as Fragment
        } else if (modeTypeSource == PictureConfig.MODE_TYPE_EXTERNAL_PREVIEW_SOURCE) {
            if (selectorConfig?.onInjectActivityPreviewListener != null) {
                targetFragment =
                    selectorConfig?.onInjectActivityPreviewListener?.onInjectPreviewFragment()
            }
            if (targetFragment != null) {
                fragmentTag = PictureSelectorPreviewFragment.Companion.TAG
            } else {
                fragmentTag = PictureSelectorPreviewFragment.Companion.TAG
                targetFragment = PictureSelectorPreviewFragment.Companion.newInstance() as Fragment
            }
            val position = getIntent().getIntExtra(PictureConfig.EXTRA_PREVIEW_CURRENT_POSITION, 0)
            val selectedPreviewResult = selectorConfig?.selectedPreviewResult ?: arrayListOf()
            // 过滤掉 null 值并转换为 ArrayList<LocalMedia>
            val previewData = ArrayList<LocalMedia>()
            for (media in selectedPreviewResult) {
                if (media != null) {
                    previewData.add(media)
                }
            }
            val isDisplayDelete = getIntent()
                .getBooleanExtra(PictureConfig.EXTRA_EXTERNAL_PREVIEW_DISPLAY_DELETE, false)
            (targetFragment as PictureSelectorPreviewFragment).setExternalPreviewData(
                position,
                previewData.size,
                previewData,
                isDisplayDelete
            )
        } else {
            fragmentTag = PictureOnlyCameraFragment.TAG
            targetFragment = newInstance() as Fragment
        }
        val supportFragmentManager = getSupportFragmentManager()
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
        if (targetFragment != null) {
            FragmentInjectManager.injectSystemRoomFragment(
                supportFragmentManager,
                fragmentTag,
                targetFragment
            )
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun setActivitySize() {
        val window = window
        window.setGravity(Gravity.LEFT or Gravity.TOP)
        val params = window.attributes
        params.x = 0
        params.y = 0
        params.height = 1
        params.width = 1
        window.attributes = params
    }

    override fun finish() {
        super.finish()
        val modeTypeSource = getIntent().getIntExtra(PictureConfig.EXTRA_MODE_TYPE_SOURCE, 0)
        if (modeTypeSource == PictureConfig.MODE_TYPE_EXTERNAL_PREVIEW_SOURCE && !(selectorConfig?.isPreviewZoomEffect ?: false)) {
            val windowAnimationStyle = selectorConfig?.selectorStyle?.windowAnimationStyle
            overridePendingTransition(0, windowAnimationStyle?.activityExitAnimation ?: 0)
        } else {
            overridePendingTransition(0, R.anim.ps_anim_fade_out)
        }
    }
}
