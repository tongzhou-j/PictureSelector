package com.luck.lib.camerax.widget

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.luck.lib.camerax.CustomCameraConfig
import com.luck.lib.camerax.listener.CaptureListener
import com.luck.lib.camerax.listener.IObtainCameraView
import com.luck.lib.camerax.permissions.PermissionChecker
import com.luck.lib.camerax.permissions.PermissionResultCallback
import com.luck.lib.camerax.permissions.SimpleXPermissionUtil
import com.luck.lib.camerax.utils.DoubleUtils
import com.luck.lib.camerax.utils.SimpleXSpUtils

/**
 * @author：luck
 * @date：2019-01-04 13:41
 * @describe：CaptureLayout
 */
class CaptureButton @JvmOverloads constructor(
    context: Context,
    size: Int = 0
) : View(context) {
    companion object {
        /**
         * 空闲状态
         */
        const val STATE_IDLE = 0x001
        /**
         * 按下状态
         */
        const val STATE_PRESS = 0x002
        /**
         * 长按状态
         */
        const val STATE_LONG_PRESS = 0x003
        /**
         * 录制状态
         */
        const val STATE_RECORDER_ING = 0x004
        /**
         * 禁止状态
         */
        const val STATE_BAN = 0x005
    }

    /**
     * 当前按钮状态
     */
    private var state: Int = STATE_IDLE
    /**
     * 按钮可执行的功能状态（拍照,录制,两者）
     */
    private var buttonState: Int = CustomCameraConfig.BUTTON_STATE_BOTH
    /**
     * 录制进度外圈色值
     */
    private var progressColor: Int = 0xEE16AE16.toInt()
    private var event_Y: Float = 0f
    private var mPaint: Paint? = null
    /**
     * 进度条宽度
     */
    private var strokeWidth: Float = 0f
    /**
     * 长按外圆半径变大的Size
     */
    private var outside_add_size: Int = 0
    /**
     * 长安内圆缩小的Size
     */
    private var inside_reduce_size: Int = 0
    private var center_X: Float = 0f
    private var center_Y: Float = 0f
    /**
     * 按钮半径
     */
    private var button_radius: Float = 0f
    /**
     * 外圆半径
     */
    private var button_outside_radius: Float = 0f
    /**
     * 内圆半径
     */
    private var button_inside_radius: Float = 0f
    /**
     * 按钮大小
     */
    private var button_size: Int = 0
    /**
     * 录制视频的进度
     */
    private var progress: Float = 0f
    /**
     * 录制视频最大时间长度
     */
    private var maxDuration: Int = CustomCameraConfig.DEFAULT_MAX_RECORD_VIDEO
    /**
     * 最短录制时间限制
     */
    private var minDuration: Int = CustomCameraConfig.DEFAULT_MIN_RECORD_VIDEO
    /**
     * 记录当前录制的时间
     */
    private var currentRecordedTime: Int = 0
    private var rectF: RectF? = null
    private val longPressRunnable: LongPressRunnable
    /**
     * 按钮回调接口
     */
    private var captureListener: CaptureListener? = null
    /**
     * 计时器
     */
    private var timer: RecordCountDownTimer
    private var isTakeCamera: Boolean = true
    private val activity: Activity = context as Activity

    init {
        if (size > 0) {
            this.button_size = size
            button_radius = size / 2.0f
            button_outside_radius = button_radius
            button_inside_radius = button_radius * 0.75f
            strokeWidth = size / 15f
            outside_add_size = size / 8
            inside_reduce_size = size / 8
            mPaint = Paint().apply {
                isAntiAlias = true
            }
            progress = 0f
            longPressRunnable = LongPressRunnable()
            state = STATE_IDLE
            buttonState = CustomCameraConfig.BUTTON_STATE_BOTH
            maxDuration = CustomCameraConfig.DEFAULT_MAX_RECORD_VIDEO
            minDuration = CustomCameraConfig.DEFAULT_MIN_RECORD_VIDEO
            center_X = (button_size + outside_add_size * 2) / 2f
            center_Y = (button_size + outside_add_size * 2) / 2f
            rectF = RectF(
                center_X - (button_radius + outside_add_size - strokeWidth / 2),
                center_Y - (button_radius + outside_add_size - strokeWidth / 2),
                center_X + (button_radius + outside_add_size - strokeWidth / 2),
                center_Y + (button_radius + outside_add_size - strokeWidth / 2)
            )
            timer = RecordCountDownTimer(maxDuration.toLong(), (maxDuration / 360).toLong())
        } else {
            longPressRunnable = LongPressRunnable()
            timer = RecordCountDownTimer(maxDuration.toLong(), (maxDuration / 360).toLong())
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (button_size > 0) {
            setMeasuredDimension(button_size + outside_add_size * 2, button_size + outside_add_size * 2)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint?.let { paint ->
            paint.style = Paint.Style.FILL
            val outside_color = 0xEEDCDCDC.toInt()
            paint.color = outside_color
            canvas.drawCircle(center_X, center_Y, button_outside_radius, paint)
            val inside_color = 0xFFFFFFFF.toInt()
            paint.color = inside_color
            canvas.drawCircle(center_X, center_Y, button_inside_radius, paint)
            if (state == STATE_RECORDER_ING) {
                paint.color = progressColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                rectF?.let { canvas.drawArc(it, -90f, progress, false, paint) }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isTakeCamera) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (event.pointerCount > 1 || state != STATE_IDLE) {
                        return true
                    }
                    event_Y = event.y
                    state = STATE_PRESS
                    if (buttonState != CustomCameraConfig.BUTTON_STATE_ONLY_CAPTURE) {
                        postDelayed(longPressRunnable, 500)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (captureListener != null
                        && state == STATE_RECORDER_ING
                        && (buttonState == CustomCameraConfig.BUTTON_STATE_ONLY_RECORDER
                                || buttonState == CustomCameraConfig.BUTTON_STATE_BOTH)
                    ) {
                        captureListener?.recordZoom(event_Y - event.y)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    handlerPressByState()
                }
            }
        }
        return true
    }

    private fun getCustomCameraView(): ViewGroup? {
        return if (activity is IObtainCameraView) {
            (activity as IObtainCameraView).getCustomCameraView()
        } else {
            null
        }
    }

    private fun handlerPressByState() {
        removeCallbacks(longPressRunnable)
        when (state) {
            STATE_PRESS -> {
                if (captureListener != null && (buttonState == CustomCameraConfig.BUTTON_STATE_ONLY_CAPTURE || buttonState == CustomCameraConfig.BUTTON_STATE_BOTH)) {
                    startCaptureAnimation(button_inside_radius)
                } else {
                    state = STATE_IDLE
                }
            }
            STATE_LONG_PRESS, STATE_RECORDER_ING -> {
                if (PermissionChecker.checkSelfPermission(context, arrayOf(Manifest.permission.RECORD_AUDIO))) {
                    timer.cancel()
                    recordEnd()
                }
            }
        }
        state = STATE_IDLE
    }

    fun recordEnd() {
        captureListener?.let {
            if (currentRecordedTime < minDuration) {
                it.recordShort(currentRecordedTime.toLong())
            } else {
                it.recordEnd(currentRecordedTime.toLong())
            }
        }
        resetRecordAnim()
    }

    private fun resetRecordAnim() {
        state = STATE_BAN
        progress = 0f
        invalidate()
        startRecordAnimation(
            button_outside_radius,
            button_radius,
            button_inside_radius,
            button_radius * 0.75f
        )
    }

    private fun startCaptureAnimation(inside_start: Float) {
        val inside_anim = ValueAnimator.ofFloat(inside_start, inside_start * 0.75f, inside_start)
        inside_anim.addUpdateListener { animation ->
            button_inside_radius = animation.animatedValue as Float
            invalidate()
        }
        inside_anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                captureListener?.takePictures()
                state = STATE_BAN
            }
        })
        inside_anim.duration = 50
        inside_anim.start()
    }

    private fun startRecordAnimation(
        outside_start: Float,
        outside_end: Float,
        inside_start: Float,
        inside_end: Float
    ) {
        val outside_anim = ValueAnimator.ofFloat(outside_start, outside_end)
        val inside_anim = ValueAnimator.ofFloat(inside_start, inside_end)
        //外圆动画监听
        outside_anim.addUpdateListener { animation ->
            button_outside_radius = animation.animatedValue as Float
            invalidate()
        }
        inside_anim.addUpdateListener { animation ->
            button_inside_radius = animation.animatedValue as Float
            invalidate()
        }
        val set = AnimatorSet()
        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (DoubleUtils.isFastDoubleClick()) {
                    return
                }
                //设置为录制状态
                if (state == STATE_LONG_PRESS) {
                    captureListener?.recordStart()
                    state = STATE_RECORDER_ING
                    timer.start()
                } else {
                    state = STATE_IDLE
                }
            }
        })
        set.playTogether(outside_anim, inside_anim)
        set.duration = 100
        set.start()
    }

    private fun updateProgress(millisUntilFinished: Long) {
        currentRecordedTime = (maxDuration - millisUntilFinished).toInt()
        progress = 360f - millisUntilFinished / maxDuration.toFloat() * 360f
        invalidate()
        captureListener?.changeTime(millisUntilFinished)
    }

    private inner class RecordCountDownTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            updateProgress(millisUntilFinished)
        }

        override fun onFinish() {
            recordEnd()
        }
    }

    private inner class LongPressRunnable : Runnable {
        override fun run() {
            state = STATE_LONG_PRESS
            if (PermissionChecker.checkSelfPermission(context, arrayOf(Manifest.permission.RECORD_AUDIO))) {
                startRecordAnimation(
                    button_outside_radius,
                    button_outside_radius + outside_add_size,
                    button_inside_radius,
                    button_inside_radius - inside_reduce_size
                )
            } else {
                onExplainCallback()
                handlerPressByState()
                PermissionChecker.getInstance().requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    object : PermissionResultCallback {
                        override fun onGranted() {
                            postDelayed(longPressRunnable, 500)
                            val customCameraView = getCustomCameraView()
                            if (customCameraView != null && CustomCameraConfig.explainListener != null) {
                                CustomCameraConfig.explainListener?.onDismiss(customCameraView)
                            }
                        }

                        override fun onDenied() {
                            if (CustomCameraConfig.deniedListener != null) {
                                SimpleXSpUtils.putBoolean(context, Manifest.permission.RECORD_AUDIO, true)
                                CustomCameraConfig.deniedListener?.onDenied(
                                    context,
                                    Manifest.permission.RECORD_AUDIO,
                                    PermissionChecker.PERMISSION_RECORD_AUDIO_SETTING_CODE
                                )
                                val customCameraView = getCustomCameraView()
                                if (customCameraView != null && CustomCameraConfig.explainListener != null) {
                                    CustomCameraConfig.explainListener?.onDismiss(customCameraView)
                                }
                            } else {
                                SimpleXPermissionUtil.goIntentSetting(
                                    activity,
                                    PermissionChecker.PERMISSION_RECORD_AUDIO_SETTING_CODE
                                )
                            }
                        }
                    })
            }
        }
    }

    private fun onExplainCallback() {
        if (CustomCameraConfig.explainListener != null) {
            if (!SimpleXSpUtils.getBoolean(context, Manifest.permission.RECORD_AUDIO, false)) {
                val customCameraView = getCustomCameraView()
                if (customCameraView != null) {
                    CustomCameraConfig.explainListener?.onPermissionDescription(
                        context,
                        customCameraView,
                        Manifest.permission.RECORD_AUDIO
                    )
                }
            }
        }
    }

    fun setMaxDuration(duration: Int) {
        this.maxDuration = duration
        timer = RecordCountDownTimer(maxDuration.toLong(), (maxDuration / 360).toLong())
    }

    fun setMinDuration(duration: Int) {
        this.minDuration = duration
    }

    fun setCaptureListener(captureListener: CaptureListener?) {
        this.captureListener = captureListener
    }

    fun setProgressColor(progressColor: Int) {
        this.progressColor = progressColor
    }

    fun setButtonFeatures(state: Int) {
        this.buttonState = state
    }

    fun getButtonFeatures(): Int {
        return buttonState
    }

    fun isIdle(): Boolean {
        return state == STATE_IDLE
    }

    fun setButtonCaptureEnabled(enabled: Boolean) {
        this.isTakeCamera = enabled
    }

    fun resetState() {
        state = STATE_IDLE
    }
}

