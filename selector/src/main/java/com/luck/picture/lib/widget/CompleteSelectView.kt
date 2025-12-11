package com.luck.picture.lib.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.luck.picture.lib.R
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.utils.StyleUtils
import com.luck.picture.lib.utils.ValueOf.toString

/**
 * @author：luck
 * @date：2021/11/21 11:28 下午
 * @describe：CompleteSelectView
 */
open class CompleteSelectView : LinearLayout {
    private var tvSelectNum: TextView? = null
    private var tvComplete: TextView? = null
    private var numberChangeAnimation: Animation? = null
    private var config: SelectorConfig? = null

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

    private fun init() {
        inflateLayout()
        orientation = HORIZONTAL
        tvSelectNum = findViewById<TextView>(R.id.ps_tv_select_num)
        tvComplete = findViewById<TextView>(R.id.ps_tv_complete)
        gravity = Gravity.CENTER_VERTICAL
        numberChangeAnimation = AnimationUtils.loadAnimation(context, R.anim.ps_anim_modal_in)
        config = SelectorProviders.Companion.instance?.selectorConfig
    }

    protected open fun inflateLayout() {
        LayoutInflater.from(context!!).inflate(R.layout.ps_complete_selected_layout, this)
    }

    /**
     * 完成选择按钮样式
     */
    open fun setCompleteSelectViewStyle() {
        val selectorStyle = config?.selectorStyle ?: return
        val selectMainStyle = selectorStyle.selectMainStyle ?: return
        if (StyleUtils.checkStyleValidity(selectMainStyle.selectNormalBackgroundResources)) {
            setBackgroundResource(selectMainStyle.selectNormalBackgroundResources)
        }
        val selectNormalText =
            if (StyleUtils.checkStyleValidity(selectMainStyle.selectNormalTextResId))
                context.getString(selectMainStyle.selectNormalTextResId)
            else
                selectMainStyle.selectNormalText
        if (StyleUtils.checkTextValidity(selectNormalText)) {
            val formatCount = StyleUtils.getFormatCount(selectNormalText!!)
            if (formatCount == 1) {
                tvComplete?.text = java.lang.String.format(selectNormalText!!, config!!.selectCount)
            } else if (formatCount == 2) {
                tvComplete?.text = java.lang.String.format(
                    selectNormalText!!,
                    config!!.selectCount,
                    config!!.maxSelectNum
                )
            } else {
                tvComplete?.text = selectNormalText
            }
        }

        val selectNormalTextSize = selectMainStyle.selectNormalTextSize
        if (StyleUtils.checkSizeValidity(selectNormalTextSize)) {
            tvComplete?.textSize = selectNormalTextSize.toFloat()
        }

        val selectNormalTextColor = selectMainStyle.selectNormalTextColor
        if (StyleUtils.checkStyleValidity(selectNormalTextColor)) {
            tvComplete?.setTextColor(selectNormalTextColor)
        }

        val bottomBarStyle = selectorStyle.bottomBarStyle

        if (bottomBarStyle?.isCompleteCountTips == true) {
            val selectNumRes = bottomBarStyle.bottomSelectNumResources
            if (StyleUtils.checkStyleValidity(selectNumRes)) {
                tvSelectNum?.setBackgroundResource(selectNumRes)
            }
            val selectNumTextSize = bottomBarStyle.bottomSelectNumTextSize
            if (StyleUtils.checkSizeValidity(selectNumTextSize)) {
                tvSelectNum?.textSize = selectNumTextSize.toFloat()
            }

            val selectNumTextColor = bottomBarStyle.bottomSelectNumTextColor
            if (StyleUtils.checkStyleValidity(selectNumTextColor)) {
                tvSelectNum?.setTextColor(selectNumTextColor)
            }
        }
    }

    /**
     * 选择结果发生变化
     */
    fun setSelectedChange(isPreview: Boolean) {
        val selectorStyle = config?.selectorStyle ?: return
        val selectMainStyle = selectorStyle.selectMainStyle ?: return
        if (config!!.selectCount > 0) {
            isEnabled = true
            val selectBackground = selectMainStyle.selectBackgroundResources
            if (StyleUtils.checkStyleValidity(selectBackground)) {
                setBackgroundResource(selectBackground)
            } else {
                setBackgroundResource(R.drawable.ps_ic_trans_1px)
            }
            val selectText =
                if (StyleUtils.checkStyleValidity(selectMainStyle.selectTextResId))
                    context.getString(selectMainStyle.selectTextResId)
                else
                    selectMainStyle.selectText
            if (StyleUtils.checkTextValidity(selectText)) {
                val formatCount = StyleUtils.getFormatCount(selectText!!)
                if (formatCount == 1) {
                    tvComplete?.text = java.lang.String.format(selectText!!, config!!.selectCount)
                } else if (formatCount == 2) {
                    tvComplete?.text = java.lang.String.format(
                        selectText!!,
                        config!!.selectCount,
                        config!!.maxSelectNum
                    )
                } else {
                    tvComplete?.text = selectText
                }
            } else {
                tvComplete?.text = context.getString(R.string.ps_completed)
            }
            val selectTextSize = selectMainStyle.selectTextSize
            if (StyleUtils.checkSizeValidity(selectTextSize)) {
                tvComplete?.textSize = selectTextSize.toFloat()
            }
            val selectTextColor = selectMainStyle.selectTextColor
            if (StyleUtils.checkStyleValidity(selectTextColor)) {
                tvComplete?.setTextColor(selectTextColor)
            } else {
                tvComplete?.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.ps_color_fa632d
                    )
                )
            }
            if (selectorStyle.bottomBarStyle?.isCompleteCountTips == true) {
                if (tvSelectNum?.visibility == GONE || tvSelectNum?.visibility == INVISIBLE) {
                    tvSelectNum?.visibility = VISIBLE
                }
                if (TextUtils.equals(
                        toString(config!!.selectCount),
                        tvSelectNum?.text
                    )
                ) {
                    // ignore
                } else {
                    tvSelectNum?.text = toString(config!!.selectCount)
                    config?.onSelectAnimListener?.onSelectAnim(tvSelectNum)
                        ?: tvSelectNum?.startAnimation(numberChangeAnimation)
                }
            } else {
                tvSelectNum?.visibility = GONE
            }
        } else {
            if (isPreview && selectMainStyle.isCompleteSelectRelativeTop) {
                isEnabled = true
                val selectBackground = selectMainStyle.selectBackgroundResources
                if (StyleUtils.checkStyleValidity(selectBackground)) {
                    setBackgroundResource(selectBackground)
                } else {
                    setBackgroundResource(R.drawable.ps_ic_trans_1px)
                }
                val selectTextColor = selectMainStyle.selectTextColor
                if (StyleUtils.checkStyleValidity(selectTextColor)) {
                    tvComplete?.setTextColor(selectTextColor)
                } else {
                    tvComplete?.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.ps_color_9b
                        )
                    )
                }
            } else {
                isEnabled = config!!.isEmptyResultReturn
                val normalBackground = selectMainStyle.selectNormalBackgroundResources
                if (StyleUtils.checkStyleValidity(normalBackground)) {
                    setBackgroundResource(normalBackground)
                } else {
                    setBackgroundResource(R.drawable.ps_ic_trans_1px)
                }
                val normalTextColor = selectMainStyle.selectNormalTextColor
                if (StyleUtils.checkStyleValidity(normalTextColor)) {
                    tvComplete?.setTextColor(normalTextColor)
                } else {
                    tvComplete?.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.ps_color_9b
                        )
                    )
                }
            }

            tvSelectNum?.visibility = GONE
            val selectNormalText =
                if (StyleUtils.checkStyleValidity(selectMainStyle.selectNormalTextResId))
                    context.getString(selectMainStyle.selectNormalTextResId)
                else
                    selectMainStyle.selectNormalText
            if (StyleUtils.checkTextValidity(selectNormalText)) {
                val formatCount = StyleUtils.getFormatCount(selectNormalText!!)
                if (formatCount == 1) {
                    tvComplete?.text = java.lang.String.format(
                        selectNormalText!!,
                        config!!.selectCount
                    )
                } else if (formatCount == 2) {
                    tvComplete?.text = java.lang.String.format(
                        selectNormalText!!,
                        config!!.selectCount,
                        config!!.maxSelectNum
                    )
                } else {
                    tvComplete?.text = selectNormalText
                }
            } else {
                tvComplete?.text = context.getString(R.string.ps_please_select)
            }
            val normalTextSize = selectMainStyle.selectNormalTextSize
            if (StyleUtils.checkSizeValidity(normalTextSize)) {
                tvComplete?.textSize = normalTextSize.toFloat()
            }
        }
    }
}
