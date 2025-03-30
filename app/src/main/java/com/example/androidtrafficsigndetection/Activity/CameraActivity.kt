package com.example.androidtrafficsigndetection.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_90
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.androidtrafficsigndetection.Classes.AnnotationView
import com.example.androidtrafficsigndetection.Classes.HttpDetection
import com.example.androidtrafficsigndetection.Classes.SocketIODetection
import com.example.androidtrafficsigndetection.DataModel.SettingParams
import com.example.androidtrafficsigndetection.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var viewFinder: PreviewView
    private lateinit var cameraExecutor: ExecutorService
    private var lastClickTime: Long = 0
    private lateinit var annotationView: AnnotationView
    private var lastAnalysisTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_activity)

        viewFinder = findViewById(R.id.viewFinder)
        HttpDetection.testConnection()

        startCamera()
        cameraExecutor = Executors.newSingleThreadExecutor()

        annotationView = AnnotationView.getInstance(this)
        (window.decorView as ViewGroup).addView(annotationView)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        AnnotationView.getInstance(this).detachFromWindow()
        closeCamera()
    }

    override fun onStop() {
        super.onStop()
        AnnotationView.getInstance(this).detachFromWindow()
        closeCamera()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < SettingParams.DOUBLE_CLICK_TIME_DELTA) {
                startActivity(Intent(this, MainActivity::class.java))
                Toast.makeText(this, "相机已关闭", Toast.LENGTH_SHORT).show()
            }
            lastClickTime = currentTime
        }
        return super.onTouchEvent(event)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // 用于绑定生命周期的相机提供者
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val aspectRatioStrategy = AspectRatioStrategy(
                AspectRatio.RATIO_4_3,
                AspectRatioStrategy.FALLBACK_RULE_AUTO
            )

            val preview = Preview.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setAspectRatioStrategy(aspectRatioStrategy)
                        .build()
                )
                .setTargetRotation(ROTATION_0)
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // 图像分析用例
            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(
                    ResolutionSelector.Builder()
                        .setAspectRatioStrategy(aspectRatioStrategy)
                        .build()
                )
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, ImageAnalyzer())
                }

            // 选择后置摄像头
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // 解除之前绑定的所有用例
                cameraProvider.unbindAll()
                // 将相机的生命周期绑定到生命周期所有者
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

            } catch (exc: Exception) {
                Toast.makeText(
                    this, "使用相机时出错：${exc.message}", Toast.LENGTH_SHORT
                ).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }
    private fun closeCamera() {
        try {
            // 解除 CameraX 绑定
            ProcessCameraProvider.getInstance(this).get().unbindAll()
            // 终止异步任务
            if (!cameraExecutor.isShutdown) {
                cameraExecutor.shutdownNow()
            }
            // 移除叠加层视图
            (window.decorView as ViewGroup).removeView(annotationView)
            HttpDetection.reachable = false
        } catch (e: Exception) {

        }
    }

    inner class ImageAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(image: ImageProxy) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastAnalysisTime < 1000 / SettingParams.retrieveFrameInterval()) {
                image.close()
                return
            }
            lastAnalysisTime = currentTime
            if(HttpDetection.reachable == true) {
                HttpDetection.uploadImage(image, SettingParams.retrieveCompress(), annotationView)
            }
            image.close()
        }
    }
}