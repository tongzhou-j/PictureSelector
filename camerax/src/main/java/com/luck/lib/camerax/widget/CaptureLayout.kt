package com.luck.lib.camerax.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.ColorFilter
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.luck.lib.camerax.CustomCameraConfig
import com.luck.lib.camerax.R
import com.luck.lib.camerax.listener.CaptureListener
import com.luck.lib.camerax.listener.ClickListener
import com.luck.lib.camerax.listener.TypeListener
import com.luck.lib.camerax.utils.DensityUtil

/**
 * @author：luck
 * @date：2019-01-04 13:41
 * @describe：CaptureLayout
 */
class CaptureLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var captureListener: CaptureListener? = null    //拍照按钮监听
    private var typeListener: TypeListener? = null          //拍照或录制后接结果按钮监听
    private var leftClickListener: ClickListener? = null    //左边按钮监听
    private var rightClickListener: ClickListener? = null   //右边按钮监听

    fun setTypeListener(typeListener: TypeListener?) {
        this.typeListener = typeListener
    }

    fun setCaptureListener(captureListener: CaptureListener?) {
        this.captureListener = captureListener
    }

    private lateinit var progress_bar: ProgressBar       // 拍照等待loading
    private lateinit var btn_capture: CaptureButton      //拍照按钮
    private lateinit var btn_confirm: TypeButton         //确认按钮
    private lateinit var btn_cancel: TypeButton          //取消按钮
    private lateinit var btn_return: ReturnButton        //返回按钮
    private lateinit var iv_custom_left: ImageView            //左边自定义按钮
    private lateinit var iv_custom_right: ImageView            //右边自定义按钮
    private lateinit var txt_tip: TextView               //提示文本

    private val layout_width: Int
    private val layout_height: Int
    private val button_size: Int
    private var iconLeft: Int = 0
    private var iconRight: Int = 0

    init {
        val screenWidth = DensityUtil.getScreenWidth(context)
        layout_width = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenWidth
        } else {
            screenWidth / 2
        }
        button_size = (layout_width / 4.5f).toInt()
        layout_height = button_size + (button_size / 5) * 2 + 100

        initView()
        initEvent()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(layout_width, layout_height)
    }

    fun initEvent() {
        //默认TypeButton为隐藏
        iv_custom_right.visibility = GONE
        btn_cancel.visibility = GONE
        btn_confirm.visibility = GONE
    }

    fun startTypeBtnAnimator() {
        //拍照录制结果后的动画
        if (this.iconLeft != 0)
            iv_custom_left.visibility = GONE
        else
            btn_return.visibility = GONE
        if (this.iconRight != 0)
            iv_custom_right.visibility = GONE
        btn_capture.visibility = GONE
        btn_cancel.visibility = VISIBLE
        btn_confirm.visibility = VISIBLE
        btn_cancel.isClickable = false
        btn_confirm.isClickable = false
        iv_custom_left.visibility = GONE
        val animator_cancel = ObjectAnimator.ofFloat(btn_cancel, "translationX", layout_width / 4f, 0f)
        val animator_confirm = ObjectAnimator.ofFloat(btn_confirm, "translationX", -layout_width / 4f, 0f)

        val set = AnimatorSet()
        set.playTogether(animator_cancel, animator_confirm)
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                btn_cancel.isClickable = true
                btn_confirm.isClickable = true
            }
        })
        set.duration = 500
        set.start()
    }

    private fun initView() {
        setWillNotDraw(false)
        //拍照按钮
        progress_bar = ProgressBar(context)
        val progress_bar_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        progress_bar_param.gravity = Gravity.CENTER
        progress_bar.layoutParams = progress_bar_param
        progress_bar.visibility = GONE

        btn_capture = CaptureButton(context, button_size)
        val btn_capture_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_capture_param.gravity = Gravity.CENTER
        btn_capture.layoutParams = btn_capture_param
        btn_capture.setCaptureListener(object : CaptureListener {
            override fun takePictures() {
                captureListener?.takePictures()
                startAlphaAnimation()
            }

            override fun recordShort(time: Long) {
                captureListener?.recordShort(time)
            }

            override fun recordStart() {
                captureListener?.recordStart()
                startAlphaAnimation()
            }

            override fun recordEnd(time: Long) {
                captureListener?.recordEnd(time)
                startTypeBtnAnimator()
            }

            override fun changeTime(time: Long) {
                captureListener?.changeTime(time)
            }

            override fun recordZoom(zoom: Float) {
                captureListener?.recordZoom(zoom)
            }

            override fun recordError() {
                captureListener?.recordError()
            }
        })

        //取消按钮
        btn_cancel = TypeButton(context, TypeButton.TYPE_CANCEL, button_size)
        val btn_cancel_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_cancel_param.gravity = Gravity.CENTER_VERTICAL
        btn_cancel_param.setMargins((layout_width / 4) - button_size / 2, 0, 0, 0)
        btn_cancel.layoutParams = btn_cancel_param
        btn_cancel.setOnClickListener {
            typeListener?.cancel()
        }

        //确认按钮
        btn_confirm = TypeButton(context, TypeButton.TYPE_CONFIRM, button_size)
        val btn_confirm_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        btn_confirm_param.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        btn_confirm_param.setMargins(0, 0, (layout_width / 4) - button_size / 2, 0)
        btn_confirm.layoutParams = btn_confirm_param
        btn_confirm.setOnClickListener {
            typeListener?.confirm()
        }

        //返回按钮
        btn_return = ReturnButton(context, (button_size / 2.5f).toInt())
        val btn_return_param = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        btn_return_param.gravity = Gravity.CENTER_VERTICAL
        btn_return_param.setMargins(layout_width / 6, 0, 0, 0)
        btn_return.layoutParams = btn_return_param
        btn_return.setOnClickListener {
            leftClickListener?.onClick()
        }
        //左边自定义按钮
        iv_custom_left = ImageView(context)
        val iv_custom_param_left = LayoutParams((button_size / 2.5f).toInt(), (button_size / 2.5f).toInt())
        iv_custom_param_left.gravity = Gravity.CENTER_VERTICAL
        iv_custom_param_left.setMargins(layout_width / 6, 0, 0, 0)
        iv_custom_left.layoutParams = iv_custom_param_left
        iv_custom_left.setOnClickListener {
            leftClickListener?.onClick()
        }

        //右边自定义按钮
        iv_custom_right = ImageView(context)
        val iv_custom_param_right = LayoutParams((button_size / 2.5f).toInt(), (button_size / 2.5f).toInt())
        iv_custom_param_right.gravity = Gravity.CENTER_VERTICAL or Gravity.RIGHT
        iv_custom_param_right.setMargins(0, 0, layout_width / 6, 0)
        iv_custom_right.layoutParams = iv_custom_param_right
        iv_custom_right.setOnClickListener {
            rightClickListener?.onClick()
        }

        txt_tip = TextView(context)
        val txt_param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        txt_param.gravity = Gravity.CENTER_HORIZONTAL
        txt_param.setMargins(0, 0, 0, 0)

        txt_tip.text = getCaptureTip()

        txt_tip.setTextColor(0xFFFFFFFF.toInt())
        txt_tip.gravity = Gravity.CENTER
        txt_tip.layoutParams = txt_param

        this.addView(btn_capture)
        this.addView(progress_bar)
        this.addView(btn_cancel)
        this.addView(btn_confirm)
        this.addView(btn_return)
        this.addView(iv_custom_left)
        this.addView(iv_custom_right)
        this.addView(txt_tip)
    }

    private fun getCaptureTip(): String {
        val buttonFeatures = btn_capture.getButtonFeatures()
        return when (buttonFeatures) {
            CustomCameraConfig.BUTTON_STATE_ONLY_CAPTURE -> context.getString(R.string.picture_photo_pictures)
            CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER -> context.getString(R.string.picture_photo_recording)
            else -> context.getString(R.string.picture_photo_camera)
        }
    }

    fun setButtonCaptureEnabled(enabled: Boolean) {
        this.progress_bar.visibility = if (enabled) GONE else VISIBLE
        this.btn_capture.setButtonCaptureEnabled(enabled)
    }

    fun setCaptureLoadingColor(color: Int) {
        val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_IN)
        progress_bar.indeterminateDrawable?.colorFilter = colorFilter
    }

    fun setProgressColor(color: Int) {
        this.btn_capture.setProgressColor(color)
    }

    fun resetCaptureLayout() {
        btn_capture.resetState()
        btn_cancel.visibility = GONE
        btn_confirm.visibility = GONE
        btn_capture.visibility = VISIBLE
        txt_tip.text = getCaptureTip()
        txt_tip.visibility = View.VISIBLE
        if (this.iconLeft != 0)
            iv_custom_left.visibility = VISIBLE
        else
            btn_return.visibility = VISIBLE
        if (this.iconRight != 0)
            iv_custom_right.visibility = VISIBLE
    }

    fun startAlphaAnimation() {
        txt_tip.visibility = View.INVISIBLE
    }

    fun setTextWithAnimation(tip: String) {
        txt_tip.text = tip
        val animator_txt_tip = ObjectAnimator.ofFloat(txt_tip, "alpha", 0f, 1f, 1f, 0f)
        animator_txt_tip.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                txt_tip.text = getCaptureTip()
                txt_tip.alpha = 1f
            }
        })
        animator_txt_tip.duration = 2500
        animator_txt_tip.start()
    }

    fun setDuration(duration: Int) {
        btn_capture.setMaxDuration(duration)
    }

    fun setMinDuration(duration: Int) {
        btn_capture.setMinDuration(duration)
    }

    fun setButtonFeatures(state: Int) {
        btn_capture.setButtonFeatures(state)
        txt_tip.text = getCaptureTip()
    }

    fun setTip(tip: String) {
        txt_tip.text = tip
    }

    fun showTip() {
        txt_tip.visibility = VISIBLE
    }

    fun setIconSrc(iconLeft: Int, iconRight: Int) {
        this.iconLeft = iconLeft
        this.iconRight = iconRight
        if (this.iconLeft != 0) {
            iv_custom_left.setImageResource(iconLeft)
            iv_custom_left.visibility = VISIBLE
            btn_return.visibility = GONE
        } else {
            iv_custom_left.visibility = GONE
            btn_return.visibility = VISIBLE
        }
        if (this.iconRight != 0) {
            iv_custom_right.setImageResource(iconRight)
            iv_custom_right.visibility = VISIBLE
        } else {
            iv_custom_right.visibility = GONE
        }
    }

    fun setLeftClickListener(leftClickListener: ClickListener?) {
        this.leftClickListener = leftClickListener
    }

    fun setRightClickListener(rightClickListener: ClickListener?) {
        this.rightClickListener = rightClickListener
    }
}

