package com.example.androidtrafficsigndetection.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.example.androidtrafficsigndetection.Classes.PermissionManager
import com.example.androidtrafficsigndetection.R

class MainActivity : BaseActivity() {
    private lateinit var homePage: View

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        homePage = layoutInflater.inflate(R.layout.home_page, null)
        homePage.findViewById<Button>(R.id.open_camera).setOnClickListener {
            if(PermissionManager.getPermissionStatus()["CAMERA_PERMISSION"] == false) {
                Toast.makeText(this, "请打开相机权限", Toast.LENGTH_SHORT).show()
                PermissionManager.requestPermissions(this)
                return@setOnClickListener
            }
            startActivity(Intent(this, CameraActivity::class.java))
            Toast.makeText(this, "双击以关闭相机", Toast.LENGTH_SHORT).show()
        }
        setContentView(homePage)

        super.onCreate(savedInstanceState)
        PermissionManager.requestPermissions(this)
    }
}