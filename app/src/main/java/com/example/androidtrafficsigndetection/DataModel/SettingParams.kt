package com.example.androidtrafficsigndetection.DataModel

import android.app.Application
import com.example.androidtrafficsigndetection.Classes.DetectionMap

object SettingParams {
    const val DEFAULTURL = "http://10.0.2.2:8080"
    const val DEFAULTMETHOD = "POST"
    val Default_ArgumentList = mutableListOf(
        ArgumentPair("conf", "0.3"),
        ArgumentPair("iou", "0.6")
    )
    // 侧滑菜单宽度
    const val TAB_BAR_PERCENTAGE = 0.45
    // 侧滑菜单唤起阈值
    const val SLICE_SETTING = 100
    // 双击时间间隔
    const val DOUBLE_CLICK_TIME_DELTA = 300

    const val BOX_WIDTH = 3f
    // 图像识别帧率
    var FRAME_INTERVAL = 10
    // 客户端压缩数据
    var COMPRESS = true
    var URL = "";
    var METHOD = ""
    val detectionMap = DetectionMap()

    fun init() {
        if(URL == "") URL = DEFAULTURL
        if(METHOD == "") METHOD = DEFAULTMETHOD
        if(detectionMap.getMap().isEmpty()) detectionMap.setMap(Default_ArgumentList)
    }
    fun changeMap2List(): MutableList<ArgumentPair>{
        val list: MutableList<ArgumentPair> = mutableListOf()
        detectionMap.getMap().forEach { (k, v) ->
            list.add(ArgumentPair(k.toString(), v.toString()))
        }
        return list
    }
    fun empty2Default(target: Any, default: Any): Any {
        if(target == "") return default
        return target
    }

    fun retrieveURL(): String {
        if (URL == "") {
            return DEFAULTURL
        }
        return URL
    }
    fun retrieveMETHOD(): String {
        if (METHOD == "") {
            return DEFAULTMETHOD;
        }
        return METHOD;
    }
    fun updateMethod(method: String) {
        METHOD = method
    }
    fun updateURL(url: String) {
        URL = url
    }
}