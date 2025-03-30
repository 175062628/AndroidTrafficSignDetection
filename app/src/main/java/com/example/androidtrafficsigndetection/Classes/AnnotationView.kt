package com.example.androidtrafficsigndetection.Classes

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.androidtrafficsigndetection.DataModel.DetectionResult
import com.example.androidtrafficsigndetection.DataModel.SettingParams
import kotlin.collections.mutableListOf
import kotlin.div
import kotlin.text.toFloat
import kotlin.times

class AnnotationView private constructor(context: Context) : View(context) {
    companion object : SingletonHolder<AnnotationView, Context>(::AnnotationView) {
        private const val LABEL_PADDING = 8f
        private const val DEFAULT_TEXT_SIZE = 24f
        private val colorPool = listOf(  // 预定义高对比度颜色池[9](@ref)
            Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
            Color.CYAN, Color.MAGENTA, Color.WHITE
        )
    }

    var textSize: Float = DEFAULT_TEXT_SIZE
    var strokeWidth: Float = SettingParams.BOX_WIDTH
        set(value) {
            field = value.coerceAtLeast(1f)  // 确保最小1px宽度
            invalidate()  // 参数修改时触发重绘
        }

    private var detections = mutableListOf<DetectionResult>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        textAlign = Paint.Align.LEFT
    }

    // 新增颜色生成逻辑
    private fun getRandomColor(): Int {
        return colorPool.random()  // 从预定义池随机选择（避免低对比度）
        // 或动态生成：Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
    }
    // 更新后的外部方法
    fun updateDetections(detectionResult: MutableList<DetectionResult>) {
        post {
            if(detections == detectionResult) {
                return@post
            }
            detections.clear()
            detections = detectionResult
            invalidate()
        }
    }
    fun detachFromWindow() {
        (context as? Activity)?.run {
            window.decorView.findViewById<ViewGroup>(android.R.id.content)?.removeView(this@AnnotationView)
        }
        detections.clear()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        detections.forEach { result ->
            drawBoundingBox(canvas, result)
            drawLabel(canvas, result)
        }
    }
    fun screenConvert(detectionResult: MutableList<DetectionResult>, width: Int, height: Int): MutableList<DetectionResult> {
        // 获取屏幕实际宽高
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        val stretchedWidth = width * (screenHeight.toFloat() / height)
        val cropHorizontal = (stretchedWidth - screenWidth) / 2

        detectionResult.forEach { detection ->
            // 创建可修改的坐标副本
            val revised_xyxy = detection.xyxy.toMutableList().apply {
                // 遍历x1,y1,x2,y2坐标
                indices.forEach { i ->
                    when (i % 2) {
                        0 -> {  // X方向处理（宽度映射）
                            val stretchedX = this[i] * stretchedWidth
                            val screenX = stretchedX - cropHorizontal
                            this[i] = (screenX / screenWidth).coerceIn(0f, 1f)
                        }
                        1 -> {  // Y方向保持原值（高度已占满）
                            this[i] = this[i]  // 显式保持原值
                        }
                    }
                }
            }
            detection.xyxy = revised_xyxy
        }
        return detectionResult
    }

    private fun drawBoundingBox(canvas: Canvas, result: DetectionResult) {
        val (left, top, right, bottom) = convertToScreenCoords(result.xyxy)

        paint.apply {
            color = getRandomColor()
            style = Paint.Style.STROKE  // 强制设为空心模式[6,7](@ref)
            strokeWidth = this@AnnotationView.strokeWidth  // 绑定宽度参数
            strokeCap = Paint.Cap.ROUND  // 圆角边框（可选美化）
        }

        canvas.drawRect(left, top, right, bottom, paint)
    }

    private fun drawLabel(canvas: Canvas, result: DetectionResult) {
        // 格式化置信度到3位小数
        val formattedConfidence = "%.3f".format(result.confidence)
        val label = "${result.cls}: $formattedConfidence"

        val (left, top) = convertToScreenCoords(result.xyxy).let {
            it[0] to it[1] - LABEL_PADDING
        }

        paint.textSize = textSize
        paint.style = Paint.Style.FILL
        canvas.drawText(label, left, top, paint)
    }

    private fun convertToScreenCoords(xyxy: List<Float>): FloatArray {
        return floatArrayOf(
            xyxy[0] * width,
            xyxy[1] * height,
            xyxy[2] * width,
            xyxy[3] * height
        )
    }
}