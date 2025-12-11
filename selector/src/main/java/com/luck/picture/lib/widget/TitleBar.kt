package com.luck.picture.lib.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.luck.picture.lib.R
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectorConfig
import com.luck.picture.lib.config.SelectorProviders
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.utils.StyleUtils

/**
 * @author：luck
 * @date：2021/11/17 10:45 上午
 * @describe：TitleBar
 */
open class TitleBar : RelativeLayout, View.OnClickListener {
    protected var rlAlbumBg: RelativeLayout? = null
    protected var ivLeftBack: ImageView? = null
    protected var ivArrow: ImageView? = null
    protected var ivDelete: ImageView? = null
    protected var tvTitle: MarqueeTextView? = null
    protected var tvCancel: TextView? = null

    /**
     * title bar line
     *
     * @return
     */
    var titleBarLine: View? = null
        protected set
    protected var viewAlbumClickArea: View? = null
    protected var config: SelectorConfig? = null
    protected var viewTopStatusBar: View? = null
    protected var titleBarLayout: RelativeLayout? = null

    open val titleCancelView: TextView
        get() = tvCancel!!

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
        config = SelectorProviders.instance?.selectorConfig
        viewTopStatusBar = findViewById<View>(R.id.top_status_bar)
        titleBarLayout = findViewById<RelativeLayout>(R.id.rl_title_bar)
        ivLeftBack = findViewById<ImageView>(R.id.ps_iv_left_back)
        rlAlbumBg = findViewById<RelativeLayout>(R.id.ps_rl_album_bg)
        ivDelete = findViewById<ImageView>(R.id.ps_iv_delete)
        viewAlbumClickArea = findViewById<View>(R.id.ps_rl_album_click)
        tvTitle = findViewById<MarqueeTextView>(R.id.ps_tv_title)
        ivArrow = findViewById<ImageView>(R.id.ps_iv_arrow)
        tvCancel = findViewById<TextView>(R.id.ps_tv_cancel)
        titleBarLine = findViewById<View>(R.id.title_bar_line)
        ivLeftBack!!.setOnClickListener(this)
        tvCancel!!.setOnClickListener(this)
        rlAlbumBg!!.setOnClickListener(this)
        titleBarLayout!!.setOnClickListener(this)
        viewAlbumClickArea!!.setOnClickListener(this)
        setBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_grey))
        handleLayoutUI()
        if (TextUtils.isEmpty(config!!.defaultAlbumName)) {
            setTitle(
                if (config!!.chooseMode == SelectMimeType.ofAudio()) context.getString(R.string.ps_all_audio) else context.getString(
                    R.string.ps_camera_roll
                )
            )
        } else {
            setTitle(config!!.defaultAlbumName)
        }
    }

    protected open fun inflateLayout() {
        LayoutInflater.from(context!!).inflate(R.layout.ps_title_bar, this)
    }

    protected fun handleLayoutUI() {
    }

    val imageArrow: ImageView?
        get() = ivArrow

    val imageDelete: ImageView?
        get() = ivDelete

    /**
     * Set title
     *
     * @param title
     */
    fun setTitle(title: String?) {
        tvTitle!!.text = title
    }

    val titleText: String
        /**
         * Get title text
         */
        get() = tvTitle!!.text.toString()

    open fun setTitleBarStyle() {
        val config = config ?: return
        if (config.isPreviewFullScreenMode) {
            val layoutParams = viewTopStatusBar!!.layoutParams
            layoutParams.height = DensityUtil.getStatusBarHeight(context)
        }
        val selectorStyle = config.selectorStyle ?: return
        val titleBarStyle = selectorStyle.titleBarStyle
        val titleBarHeight = titleBarStyle?.titleBarHeight ?: 0
        if (StyleUtils.checkSizeValidity(titleBarHeight)) {
            titleBarLayout!!.layoutParams.height = titleBarHeight
        } else {
            titleBarLayout!!.layoutParams.height = DensityUtil.dip2px(context, 48f)
        }

        if (titleBarLine != null) {
            if (titleBarStyle?.isDisplayTitleBarLine == true) {
                titleBarLine!!.visibility = VISIBLE
                val lineColor = titleBarStyle?.titleBarLineColor ?: 0
                if (StyleUtils.checkStyleValidity(lineColor)) {
                    titleBarLine!!.setBackgroundColor(lineColor)
                }
            } else {
                titleBarLine!!.visibility = GONE
            }
        }

        val backgroundColor = titleBarStyle?.titleBackgroundColor ?: 0
        if (StyleUtils.checkStyleValidity(backgroundColor)) {
            setBackgroundColor(backgroundColor)
        }
        val backResId = titleBarStyle?.titleLeftBackResource ?: 0
        if (StyleUtils.checkStyleValidity(backResId)) {
            ivLeftBack!!.setImageResource(backResId)
        }
        val titleDefaultText =
            if (StyleUtils.checkStyleValidity(titleBarStyle?.titleDefaultTextResId ?: 0))
                context.getString(titleBarStyle!!.titleDefaultTextResId)
            else
                titleBarStyle?.titleDefaultText
        if (StyleUtils.checkTextValidity(titleDefaultText)) {
            tvTitle!!.text = titleDefaultText
        }
        val titleTextSize = titleBarStyle?.titleTextSize ?: 0
        if (StyleUtils.checkSizeValidity(titleTextSize)) {
            tvTitle!!.textSize = titleTextSize.toFloat()
        }
        val titleTextColor = titleBarStyle?.titleTextColor ?: 0
        if (StyleUtils.checkStyleValidity(titleTextColor)) {
            tvTitle!!.setTextColor(titleTextColor)
        }
        if (config.isOnlySandboxDir) {
            ivArrow!!.setImageResource(R.drawable.ps_ic_trans_1px)
        } else {
            val arrowResId = titleBarStyle?.titleDrawableRightResource ?: 0
            if (StyleUtils.checkStyleValidity(arrowResId)) {
                ivArrow!!.setImageResource(arrowResId)
            }
        }
        val albumBackgroundRes = titleBarStyle?.titleAlbumBackgroundResource ?: 0
        if (StyleUtils.checkStyleValidity(albumBackgroundRes)) {
            rlAlbumBg!!.setBackgroundResource(albumBackgroundRes)
        }

        if (titleBarStyle?.isHideCancelButton == true) {
            tvCancel!!.visibility = GONE
        } else {
            tvCancel!!.visibility = VISIBLE
            val titleCancelBackgroundResource = titleBarStyle?.titleCancelBackgroundResource ?: 0
            if (StyleUtils.checkStyleValidity(titleCancelBackgroundResource)) {
                tvCancel!!.setBackgroundResource(titleCancelBackgroundResource)
            }
            val titleCancelText =
                if (StyleUtils.checkStyleValidity(titleBarStyle?.titleCancelTextResId ?: 0))
                    context.getString(titleBarStyle!!.titleCancelTextResId)
                else
                    titleBarStyle?.titleCancelText
            if (StyleUtils.checkTextValidity(titleCancelText)) {
                tvCancel!!.text = titleCancelText
            }
            val titleCancelTextColor = titleBarStyle?.titleCancelTextColor ?: 0
            if (StyleUtils.checkStyleValidity(titleCancelTextColor)) {
                tvCancel!!.setTextColor(titleCancelTextColor)
            }
            val titleCancelTextSize = titleBarStyle?.titleCancelTextSize ?: 0
            if (StyleUtils.checkSizeValidity(titleCancelTextSize)) {
                tvCancel!!.textSize = titleCancelTextSize.toFloat()
            }
        }

        val deleteBackgroundResource = titleBarStyle?.previewDeleteBackgroundResource ?: 0
        if (StyleUtils.checkStyleValidity(deleteBackgroundResource)) {
            ivDelete!!.setBackgroundResource(deleteBackgroundResource)
        } else {
            ivDelete!!.setBackgroundResource(R.drawable.ps_ic_delete)
        }
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.ps_iv_left_back || id == R.id.ps_tv_cancel) {
            if (titleBarListener != null) {
                titleBarListener!!.onBackPressed()
            }
        } else if (id == R.id.ps_rl_album_bg || id == R.id.ps_rl_album_click) {
            if (titleBarListener != null) {
                titleBarListener!!.onShowAlbumPopWindow(this)
            }
        } else if (id == R.id.rl_title_bar) {
            if (titleBarListener != null) {
                titleBarListener!!.onTitleDoubleClick()
            }
        }
    }

    protected var titleBarListener: OnTitleBarListener? = null

    /**
     * TitleBar的功能事件回调
     *
     * @param listener
     */
    fun setOnTitleBarListener(listener: OnTitleBarListener?) {
        this.titleBarListener = listener
    }

    open class OnTitleBarListener {
        /**
         * 双击标题栏
         */
        open fun onTitleDoubleClick() {
        }

        /**
         * 关闭页面
         */
        open fun onBackPressed() {
        }

        /**
         * 显示专辑列表
         */
        open fun onShowAlbumPopWindow(anchor: View?) {
        }
    }
}
