package com.luck.picture.lib.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.luck.picture.lib.R
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.PictureFileUtils
import com.luck.picture.lib.utils.StyleUtils

/**
 * @author：luck
 * @date：2021/11/17 10:46 上午
 * @describe：BottomNavBar
 */
open class BottomNavBar : RelativeLayout, View.OnClickListener {
    protected var tvPreview: TextView? = null
    protected var tvImageEditor: TextView? = null
    private var originalCheckbox: CheckBox? = null
    protected var config: SelectorConfig? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    protected fun init() {
        inflateLayout()
        setClickable(true)
        setFocusable(true)
        config = SelectorProviders.Companion.instance?.selectorConfig
        tvPreview = findViewById<TextView>(R.id.ps_tv_preview)
        tvImageEditor = findViewById<TextView>(R.id.ps_tv_editor)
        originalCheckbox = findViewById<CheckBox>(R.id.cb_original)
        tvPreview!!.setOnClickListener(this)
        tvImageEditor!!.setVisibility(GONE)
        setBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_grey))
        originalCheckbox!!.isChecked = config!!.isCheckOriginalImage
        originalCheckbox!!.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(compoundButton: CompoundButton?, isChecked: Boolean) {
                config!!.isCheckOriginalImage = isChecked
                originalCheckbox!!.isChecked = config!!.isCheckOriginalImage
                if (bottomNavBarListener != null) {
                    bottomNavBarListener!!.onCheckOriginalChange()
                    if (isChecked && config!!.selectCount == 0) {
                        bottomNavBarListener!!.onFirstCheckOriginalSelectedChange()
                    }
                }
            }
        })
        handleLayoutUI()
    }

    protected open fun inflateLayout() {
        inflate(context, R.layout.ps_bottom_nav_bar, this)
    }

    protected open fun handleLayoutUI() {
    }

    open fun setBottomNavBarStyle() {
        if (config!!.isDirectReturnSingle) {
            setVisibility(GONE)
            return
        }
        val selectorStyle = config!!.selectorStyle
        val bottomBarStyle = selectorStyle?.bottomBarStyle
        if (config!!.isOriginalControl) {
            originalCheckbox!!.setVisibility(VISIBLE)
            val originalDrawableLeft = bottomBarStyle?.bottomOriginalDrawableLeft ?: 0
            if (StyleUtils.checkStyleValidity(originalDrawableLeft)) {
                originalCheckbox!!.setButtonDrawable(originalDrawableLeft)
            }
            val bottomOriginalText =
                if (StyleUtils.checkStyleValidity(bottomBarStyle?.bottomOriginalTextResId ?: 0))
                    context.getString(bottomBarStyle!!.bottomOriginalTextResId)
                else
                    bottomBarStyle?.bottomOriginalText
            if (StyleUtils.checkTextValidity(bottomOriginalText)) {
                originalCheckbox!!.setText(bottomOriginalText)
            }
            val originalTextSize = bottomBarStyle?.bottomOriginalTextSize ?: 0
            if (StyleUtils.checkSizeValidity(originalTextSize)) {
                originalCheckbox!!.setTextSize(originalTextSize.toFloat())
            }
            val originalTextColor = bottomBarStyle?.bottomOriginalTextColor ?: 0
            if (StyleUtils.checkStyleValidity(originalTextColor)) {
                originalCheckbox!!.setTextColor(originalTextColor)
            }
        }

        val narBarHeight = bottomBarStyle?.bottomNarBarHeight ?: 0
        if (StyleUtils.checkSizeValidity(narBarHeight)) {
            getLayoutParams().height = narBarHeight
        } else {
            getLayoutParams().height = DensityUtil.dip2px(context, 46f)
        }

        val backgroundColor = bottomBarStyle?.bottomNarBarBackgroundColor ?: 0
        if (StyleUtils.checkStyleValidity(backgroundColor)) {
            setBackgroundColor(backgroundColor)
        }

        val previewNormalTextColor = bottomBarStyle?.bottomPreviewNormalTextColor ?: 0
        if (StyleUtils.checkStyleValidity(previewNormalTextColor)) {
            tvPreview!!.setTextColor(previewNormalTextColor)
        }
        val previewTextSize = bottomBarStyle?.bottomPreviewNormalTextSize ?: 0
        if (StyleUtils.checkSizeValidity(previewTextSize)) {
            tvPreview!!.setTextSize(previewTextSize.toFloat())
        }
        val bottomPreviewText =
            if (StyleUtils.checkStyleValidity(bottomBarStyle?.bottomPreviewNormalTextResId ?: 0))
                context.getString(bottomBarStyle!!.bottomPreviewNormalTextResId)
            else
                bottomBarStyle?.bottomPreviewNormalText
        if (StyleUtils.checkTextValidity(bottomPreviewText)) {
            tvPreview!!.setText(bottomPreviewText)
        }

        val editorText =
            if (StyleUtils.checkStyleValidity(bottomBarStyle?.bottomEditorTextResId ?: 0))
                context.getString(bottomBarStyle!!.bottomEditorTextResId)
            else
                bottomBarStyle?.bottomEditorText
        if (StyleUtils.checkTextValidity(editorText)) {
            tvImageEditor!!.setText(editorText)
        }
        val editorTextSize = bottomBarStyle?.bottomEditorTextSize ?: 0
        if (StyleUtils.checkSizeValidity(editorTextSize)) {
            tvImageEditor!!.setTextSize(editorTextSize.toFloat())
        }
        val editorTextColor = bottomBarStyle?.bottomEditorTextColor ?: 0
        if (StyleUtils.checkStyleValidity(editorTextColor)) {
            tvImageEditor!!.setTextColor(editorTextColor)
        }

        val originalDrawableLeft = bottomBarStyle?.bottomOriginalDrawableLeft ?: 0
        if (StyleUtils.checkStyleValidity(originalDrawableLeft)) {
            originalCheckbox!!.setButtonDrawable(originalDrawableLeft)
        }

        val originalText =
            if (StyleUtils.checkStyleValidity(bottomBarStyle?.bottomOriginalTextResId ?: 0))
                context.getString(bottomBarStyle!!.bottomOriginalTextResId)
            else
                bottomBarStyle?.bottomOriginalText
        if (StyleUtils.checkTextValidity(originalText)) {
            originalCheckbox!!.setText(originalText)
        }

        val originalTextSize = bottomBarStyle?.bottomOriginalTextSize ?: 0
        if (StyleUtils.checkSizeValidity(originalTextSize)) {
            originalCheckbox!!.setTextSize(originalTextSize.toFloat())
        }

        val originalTextColor = bottomBarStyle?.bottomOriginalTextColor ?: 0
        if (StyleUtils.checkStyleValidity(originalTextColor)) {
            originalCheckbox!!.setTextColor(originalTextColor)
        }
    }

    /**
     * 原图选项发生变化
     */
    fun setOriginalCheck() {
        originalCheckbox!!.isChecked = config!!.isCheckOriginalImage
    }

    /**
     * 选择结果发生变化
     */
    fun setSelectedChange() {
        calculateFileTotalSize()
        val selectorStyle = config!!.selectorStyle
        val bottomBarStyle = selectorStyle?.bottomBarStyle
        if (config!!.selectCount > 0) {
            tvPreview!!.setEnabled(true)
            val previewSelectTextColor = bottomBarStyle?.bottomPreviewSelectTextColor ?: 0
            if (StyleUtils.checkStyleValidity(previewSelectTextColor)) {
                tvPreview!!.setTextColor(previewSelectTextColor)
            } else {
                tvPreview!!.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.ps_color_fa632d
                    )
                )
            }
            val previewSelectText =
                if (StyleUtils.checkStyleValidity(bottomBarStyle?.bottomPreviewSelectTextResId ?: 0))
                    context.getString(bottomBarStyle!!.bottomPreviewSelectTextResId)
                else
                    bottomBarStyle?.bottomPreviewSelectText
            if (StyleUtils.checkTextValidity(previewSelectText)) {
                val formatCount = StyleUtils.getFormatCount(previewSelectText.toString())
                if (formatCount == 1) {
                    tvPreview!!.setText(
                        java.lang.String.format(
                            previewSelectText!!,
                            config!!.selectCount
                        )
                    )
                } else if (formatCount == 2) {
                    tvPreview!!.setText(
                        java.lang.String.format(
                            previewSelectText!!,
                            config!!.selectCount,
                            config!!.maxSelectNum
                        )
                    )
                } else {
                    tvPreview!!.setText(previewSelectText)
                }
            } else {
                tvPreview!!.setText(
                    context.getString(
                        R.string.ps_preview_num,
                        config!!.selectCount
                    )
                )
            }
        } else {
            tvPreview!!.setEnabled(false)
            val previewNormalTextColor = bottomBarStyle?.bottomPreviewNormalTextColor ?: 0
            if (StyleUtils.checkStyleValidity(previewNormalTextColor)) {
                tvPreview!!.setTextColor(previewNormalTextColor)
            } else {
                tvPreview!!.setTextColor(ContextCompat.getColor(context, R.color.ps_color_9b))
            }
            val previewText =
                if (StyleUtils.checkStyleValidity(bottomBarStyle?.bottomPreviewNormalTextResId ?: 0))
                    context.getString(bottomBarStyle!!.bottomPreviewNormalTextResId)
                else
                    bottomBarStyle?.bottomPreviewNormalText
            if (StyleUtils.checkTextValidity(previewText)) {
                tvPreview!!.setText(previewText)
            } else {
                tvPreview!!.setText(context.getString(R.string.ps_preview))
            }
        }
    }

    /**
     * 计算原图大小
     */
    private fun calculateFileTotalSize() {
        if (config!!.isOriginalControl) {
            var totalSize: Long = 0
            for (i in 0..<config!!.selectCount) {
                val media = config!!.selectedResult.get(i)
                totalSize += media?.size ?: 0L
            }
            if (totalSize > 0) {
                val fileSize = PictureFileUtils.formatAccurateUnitFileSize(totalSize)
                originalCheckbox!!.setText(
                    context.getString(
                        R.string.ps_original_image,
                        fileSize
                    )
                )
            } else {
                originalCheckbox!!.setText(context.getString(R.string.ps_default_original_image))
            }
        } else {
            originalCheckbox!!.setText(context.getString(R.string.ps_default_original_image))
        }
    }

    override fun onClick(view: View) {
        if (bottomNavBarListener == null) {
            return
        }
        val id = view.id
        if (id == R.id.ps_tv_preview) {
            bottomNavBarListener!!.onPreview()
        }
    }

    protected var bottomNavBarListener: OnBottomNavBarListener? = null

    /**
     * 预览NarBar的功能事件回调
     *
     * @param listener
     */
    fun setOnBottomNavBarListener(listener: OnBottomNavBarListener?) {
        this.bottomNavBarListener = listener
    }

    open class OnBottomNavBarListener {
        /**
         * 预览
         */
        open fun onPreview() {
        }

        /**
         * 编辑图片
         */
        open fun onEditImage() {
        }

        /**
         * 原图发生变化
         */
        open fun onCheckOriginalChange() {
        }

        /**
         * 首次选择原图并加入选择结果
         */
        open fun onFirstCheckOriginalSelectedChange() {
        }
    }
}
