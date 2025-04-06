package com.example.androidtrafficsigndetection.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class MediaPickerActivity : AppCompatActivity() {

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri = result.data?.data // 获取图片 Uri（content 类型）
            if (imageUri != null) {
                // 通过 MediaStore 获取文件路径（API 34 下直接查询 DATA 字段）
                val imagePath = getImagePathFromUri(imageUri)
                if (!imagePath.isNullOrEmpty()) {
                    // 返回文件路径给 MediaActivity
                    val resultIntent = Intent()
                    resultIntent.putExtra("image_path", imagePath)
                    setResult(RESULT_OK, resultIntent)
                } else {
                    setResult(RESULT_CANCELED)
                    Toast.makeText(this, "文件路径获取失败", Toast.LENGTH_SHORT).show()
                }
            } else {
                setResult(RESULT_CANCELED)
                Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 直接启动相册选择界面（无需布局文件，无按钮）
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    // 通过 Uri 获取文件路径（API 34 专用，简化版）
    private fun getImagePathFromUri(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA) // 直接获取文件路径字段
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return cursor.getString(columnIndex)
            }
        }
        return "" // 正常情况下不会返回空（用户已选择图片）
    }
}