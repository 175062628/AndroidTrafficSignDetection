package com.example.androidtrafficsigndetection.Activity

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.androidtrafficsigndetection.Classes.HttpDetection
import com.example.androidtrafficsigndetection.DataModel.SettingParams
import com.example.androidtrafficsigndetection.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

// MediaActivity.kt
class MediaActivity : AppCompatActivity() {
    private lateinit var mediaImage: ImageView
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.media_view)
        mediaImage = findViewById<ImageView>(R.id.detection_image)
        findViewById<Button>(R.id.reopen_media).setOnClickListener {
            val intent = Intent(this, MediaPickerActivity::class.java)
            startForResult.launch(intent)
        }

        val data = intent.getStringExtra("MainActivity")
        if (data == "DOUBLE_CLICK") {
            findViewById<Button>(R.id.reopen_media).performClick()
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imagePath = result.data?.getStringExtra("image_path") // 获取文件路径
            if (!imagePath.isNullOrEmpty()) {
                val imageFile = File(imagePath) // 构建 File 对象
                val imageUri = getImageUriFromFile(this, imageFile)
                Glide.with(this).load(imageUri).into(mediaImage)

                lifecycleScope.launch(Dispatchers.IO) { // 协程在 IO 线程启动
                    val resImage = HttpDetection.uploadMedia(imageFile) // 直接调用挂起函数（同线程）
                    withContext(Dispatchers.Main) { // 显式切回主线程
                        resImage?.let {
                            // 主线程操作
                            updateMediaStore(this@MediaActivity, it)
                            val savedFile = saveFileToCustomDir(it)
                            savedFile?.let {
                                val savedFileUri = getImageUriFromFile(this@MediaActivity, it)
                                Glide.with(this@MediaActivity).load(savedFileUri).into(mediaImage)
                            } ?: Toast.makeText(this@MediaActivity, "存储失败", Toast.LENGTH_SHORT).show()
                        } ?: Toast.makeText(this@MediaActivity, "上传服务器解析失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < SettingParams.DOUBLE_CLICK_TIME_DELTA) {
                startActivity(Intent(this, MainActivity::class.java))
                Toast.makeText(this, "相册已关闭", Toast.LENGTH_SHORT).show()
            }
            lastClickTime = currentTime
        }
        return super.onTouchEvent(event)
    }

    private fun getImageUriFromFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun saveFileToCustomDir(sourceFile: File): File? {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val customizedDir = File(dir, SettingParams.DEFAULT_STORAGE_PATH)
        if (!customizedDir.exists()) {
            if (!customizedDir.mkdirs()) {
                return null
            }
        }
        val targetFile = File(customizedDir, sourceFile.name)
        try {
            val inputStream = FileInputStream(sourceFile)
            val outputStream = FileOutputStream(targetFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return targetFile
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    // 新增函数，用于更新媒体库
    private fun updateMediaStore(context: Context, file: File) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + SettingParams.DEFAULT_STORAGE_PATH)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(it, values, null, null)
        }
    }
}