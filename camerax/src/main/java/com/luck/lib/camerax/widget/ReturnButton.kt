package com.luck.lib.camerax.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/**
 * @author：luck
 * @date：2019-01-04 13:41
 * @describe：ReturnButton
 */
class ReturnButton @JvmOverloads constructor(
    context: Context,
    size: Int = 0
) : View(context) {
    private var size: Int = size
    private var center_X: Int = 0
    private var center_Y: Int = 0
    private var strokeWidth: Float = 0f
    private var paint: Paint? = null
    private var path: Path? = null

    init {
        if (size > 0) {
            this.size = size
            center_X = size / 2
            center_Y = size / 2
            strokeWidth = size / 15f
            paint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                style = Paint.Style.STROKE
                this.strokeWidth = this@ReturnButton.strokeWidth
            }
            path = Path()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(size, size / 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        path?.let { p ->
            p.moveTo(strokeWidth, strokeWidth / 2)
            p.lineTo(center_X.toFloat(), center_Y - strokeWidth / 2)
            p.lineTo(size - strokeWidth, strokeWidth / 2)
            paint?.let { canvas.drawPath(p, it) }
        }
    }
}

