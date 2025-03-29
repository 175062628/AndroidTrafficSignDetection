package com.example.androidtrafficsigndetection

import android.app.Application
import com.example.androidtrafficsigndetection.Classes.NavigationDrawerManager
import com.example.androidtrafficsigndetection.Classes.PermissionManager
import com.example.androidtrafficsigndetection.DataModel.SettingParams

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NavigationDrawerManager.init(this)
        PermissionManager.init(this)
        SettingParams.init()
    }
}