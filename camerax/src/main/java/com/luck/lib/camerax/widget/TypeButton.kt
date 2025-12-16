package com.luck.lib.camerax.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.View

/**
 * @author：luck
 * @date：2019-01-04 13:41
 * @describe：TypeButton
 */
class TypeButton @JvmOverloads constructor(
    context: Context,
    type: Int = 0,
    size: Int = 0
) : View(context) {
    companion object {
        const val TYPE_CANCEL = 0x001
        const val TYPE_CONFIRM = 0x002
    }

    private var button_type: Int = type
    private var button_size: Int = size
    private var center_X: Float = 0f
    private var center_Y: Float = 0f
    private var button_radius: Float = 0f
    private var mPaint: Paint? = null
    private var path: Path? = null
    private var strokeWidth: Float = 0f
    private var index: Float = 0f
    private var rectF: RectF? = null

    init {
        if (size > 0) {
            this.button_type = type
            this.button_size = size
            button_radius = size / 2.0f
            center_X = size / 2.0f
            center_Y = size / 2.0f
            mPaint = Paint()
            path = Path()
            strokeWidth = size / 50f
            index = button_size / 12f
            rectF = RectF(center_X, center_Y - index, center_X + index * 2, center_Y + index)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(button_size, button_size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //如果类型为取消，则绘制内部为返回箭头
        if (button_type == TYPE_CANCEL) {
            mPaint?.let { paint ->
                paint.isAntiAlias = true
                paint.color = 0xEEDCDCDC.toInt()
                paint.style = Paint.Style.FILL
                canvas.drawCircle(center_X, center_Y, button_radius, paint)

                paint.color = Color.BLACK
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth

                path?.let { p ->
                    p.moveTo(center_X - index / 7, center_Y + index)
                    p.lineTo(center_X + index, center_Y + index)
                    rectF?.let { p.arcTo(it, 90f, -180f) }
                    p.lineTo(center_X - index, center_Y - index)
                    canvas.drawPath(p, paint)
                    paint.style = Paint.Style.FILL
                    p.reset()
                    p.moveTo(center_X - index, (center_Y - index * 1.5f))
                    p.lineTo(center_X - index, (center_Y - index / 2.3f))
                    p.lineTo((center_X - index * 1.6f), center_Y - index)
                    p.close()
                    canvas.drawPath(p, paint)
                }
            }
        }
        //如果类型为确认，则绘制绿色勾
        if (button_type == TYPE_CONFIRM) {
            mPaint?.let { paint ->
                paint.isAntiAlias = true
                paint.color = 0xFFFFFFFF.toInt()
                paint.style = Paint.Style.FILL
                canvas.drawCircle(center_X, center_Y, button_radius, paint)
                paint.isAntiAlias = true
                paint.style = Paint.Style.STROKE
                paint.color = 0xFF00CC00.toInt()
                paint.strokeWidth = strokeWidth

                path?.let { p ->
                    p.moveTo(center_X - button_size / 6f, center_Y)
                    p.lineTo(center_X - button_size / 21.2f, center_Y + button_size / 7.7f)
                    p.lineTo(center_X + button_size / 4.0f, center_Y - button_size / 8.5f)
                    p.lineTo(center_X - button_size / 21.2f, center_Y + button_size / 9.4f)
                    p.close()
                    canvas.drawPath(p, paint)
                }
            }
        }
    }
}

