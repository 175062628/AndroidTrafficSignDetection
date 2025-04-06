package com.example.androidtrafficsigndetection.DataModel

import com.google.gson.annotations.SerializedName

data class DetectionResult(
    @SerializedName("confidence")
    val confidence: Float,
    @SerializedName("xyxyn")
    var xyxy: List<Float>,
    @SerializedName("class")
    val cls: String
)