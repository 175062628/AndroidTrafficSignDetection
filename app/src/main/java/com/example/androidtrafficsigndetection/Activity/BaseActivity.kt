package com.example.androidtrafficsigndetection.Activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.androidtrafficsigndetection.Classes.NavigationDrawerManager

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NavigationDrawerManager.attachToActivity(this)
    }

    override fun onResume() {
        super.onResume()
        NavigationDrawerManager.attachToActivity(this)
    }
}