package com.example.androidtrafficsigndetection.DataModel

import android.app.Application
import com.example.androidtrafficsigndetection.Classes.DetectionMap

object SettingParams {
    const val TAB_BAR_PERCENTAGE = 0.45
    const val SLICE_SETTING = 100
    const val DOUBLE_CLICK_TIME_DELTA = 300
    const val BOX_WIDTH = 3f

    const val DEFAULT_URL = "http://10.0.2.2:8080"
    val Default_ArgumentList = mutableListOf(
        ArgumentPair("conf", "0.3"),
        ArgumentPair("iou", "0.6")
    )
    const val DEFAULT_FRAME_INTERVAL = 1f
    const val DEFAULT_COMPRESS = true


    var URL = "";
    val detectionMap = DetectionMap()
    var FRAME_INTERVAL = 1f
    var COMPRESS = true

    fun init() {
        if(URL == "") URL = DEFAULT_URL
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
            return DEFAULT_URL
        }
        return URL
    }
    fun updateURL(url: String) {
        URL = url
    }
    fun retrieveFrameInterval(): Float {
        if (FRAME_INTERVAL <= 0) return DEFAULT_FRAME_INTERVAL
        return FRAME_INTERVAL
    }
    fun updateFrameInterval(frame_interval: Float) {
        FRAME_INTERVAL = frame_interval
    }
    fun retrieveCompress(): Boolean {
        return COMPRESS
    }
    fun updateCompress(compress: Boolean) {
        COMPRESS = compress
    }
}