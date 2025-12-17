package com.yalantis.ucrop.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.RectF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.NonNull
import com.yalantis.ucrop.R
import com.yalantis.ucrop.callback.CropBoundsChangeListener
import com.yalantis.ucrop.callback.OverlayViewChangeListener

class UCropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var mGestureCropImageView: GestureCropImageView
    private val mViewOverlay: OverlayView

    init {
        LayoutInflater.from(context).inflate(R.layout.ucrop_view, this, true)
        mGestureCropImageView = findViewById(R.id.image_view_crop)
        mViewOverlay = findViewById(R.id.view_overlay)

        val a = context.obtainStyledAttributes(attrs, R.styleable.ucrop_UCropView)
        mViewOverlay.processStyledAttributes(a)
        mGestureCropImageView.processStyledAttributes(a)
        a.recycle()

        setListenersToViews()
    }

    private fun setListenersToViews() {
        mGestureCropImageView.setCropBoundsChangeListener(object : CropBoundsChangeListener {
            override fun onCropAspectRatioChanged(cropRatio: Float) {
                mViewOverlay.setTargetAspectRatio(cropRatio)
            }
        })
        mViewOverlay.setOverlayViewChangeListener(object : OverlayViewChangeListener {
            override fun onCropRectUpdated(cropRect: RectF) {
                mGestureCropImageView.setCropRect(cropRect)
            }

            override fun postTranslate(deltaX: Float, deltaY: Float) {
                mGestureCropImageView.postTranslate(deltaX, deltaY)
            }
        })
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    @NonNull
    fun getCropImageView(): GestureCropImageView {
        return mGestureCropImageView
    }

    @NonNull
    fun getOverlayView(): OverlayView {
        return mViewOverlay
    }

    /**
     * Method for reset state for UCropImageView such as rotation, scale, translation.
     * Be careful: this method recreate UCropImageView instance and reattach it to layout.
     */
    fun resetCropImageView() {
        removeView(mGestureCropImageView)
        mGestureCropImageView = GestureCropImageView(context)
        setListenersToViews()
        mGestureCropImageView.setCropRect(getOverlayView().getCropViewRect())
        addView(mGestureCropImageView, 0)
    }
}

