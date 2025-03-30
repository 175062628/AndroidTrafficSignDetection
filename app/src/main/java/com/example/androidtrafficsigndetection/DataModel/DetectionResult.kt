package com.example.androidtrafficsigndetection.DataModel

import com.google.gson.annotations.SerializedName

data class DetectionResult(
    @SerializedName("conf")
    val confidence: Float,
    @SerializedName("xyxyn")
    var xyxy: List<Float>,
    @SerializedName("cls")
    val cls: String
)