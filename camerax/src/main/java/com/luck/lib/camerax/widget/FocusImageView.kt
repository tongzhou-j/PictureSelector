package com.luck.lib.camerax.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.luck.lib.camerax.R

/**
 * @author：luck
 * @date：2022-02-12 13:41
 * @describe：FocusImageView
 */
class FocusImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private val DELAY_MILLIS: Long = 1000
    private var mFocusImg: Int
    private var mFocusSucceedImg: Int
    private var mFocusFailedImg: Int
    private val mAnimation = AnimationUtils.loadAnimation(context, R.anim.focusview_show)
    private val mHandler = Handler(Looper.getMainLooper())
    @Volatile
    private var isDisappear: Boolean = false

    init {
        visibility = View.INVISIBLE
        val typedArray: TypedArray? = attrs?.let {
            context.obtainStyledAttributes(it, R.styleable.FocusImageView)
        }
        mFocusImg = typedArray?.getResourceId(
            R.styleable.FocusImageView_focus_focusing,
            R.drawable.focus_focusing
        ) ?: R.drawable.focus_focusing
        mFocusSucceedImg = typedArray?.getResourceId(
            R.styleable.FocusImageView_focus_success,
            R.drawable.focus_focused
        ) ?: R.drawable.focus_focused
        mFocusFailedImg = typedArray?.getResourceId(
            R.styleable.FocusImageView_focus_error,
            R.drawable.focus_failed
        ) ?: R.drawable.focus_failed
        typedArray?.recycle()
    }

    fun setDisappear(disappear: Boolean) {
        isDisappear = disappear
    }

    fun startFocus(point: Point) {
        val params = layoutParams as RelativeLayout.LayoutParams
        params.topMargin = point.y - measuredHeight / 2
        params.leftMargin = point.x - measuredWidth / 2
        layoutParams = params
        visibility = View.VISIBLE
        setFocusResource(mFocusImg)
        startAnimation(mAnimation)
    }

    fun onFocusSuccess() {
        if (isDisappear) {
            setFocusResource(mFocusSucceedImg)
        }
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed({
            setFocusGone()
        }, DELAY_MILLIS)
    }

    fun onFocusFailed() {
        if (isDisappear) {
            setFocusResource(mFocusFailedImg)
        }
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed({
            setFocusGone()
        }, DELAY_MILLIS)
    }

    private fun setFocusResource(@DrawableRes resId: Int) {
        setImageResource(resId)
    }

    private fun setFocusGone() {
        if (isDisappear) {
            visibility = View.INVISIBLE
        }
    }

    fun destroy() {
        mHandler.removeCallbacksAndMessages(null)
        visibility = View.INVISIBLE
    }
}

