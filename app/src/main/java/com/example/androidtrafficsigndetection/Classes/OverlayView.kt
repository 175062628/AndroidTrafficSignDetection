package com.example.androidtrafficsigndetection.Classes

// OverlayView.kt
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val lock = Any()
    private val rectPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4.0f
    }
    private var drawingRects = emptyList<RectF>()

    fun updateRects(rects: List<RectF>) {
        synchronized(lock) {
            drawingRects = ArrayList(rects)
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(lock) {
            drawingRects.forEach { rect ->
                canvas.drawRect(rect, rectPaint)
            }
        }
    }
}