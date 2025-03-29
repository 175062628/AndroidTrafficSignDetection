package com.example.androidtrafficsigndetection.DataModel

import com.google.gson.annotations.SerializedName

data class DetectionResult(
    @SerializedName("conf")
    val confidence: Float,
    @SerializedName("xyxyn")
    val xyxy: List<Float>,
    @SerializedName("cls")
    val cls: String
)
