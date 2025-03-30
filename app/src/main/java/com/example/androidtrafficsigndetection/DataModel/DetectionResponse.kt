package com.example.androidtrafficsigndetection.DataModel

import com.google.gson.annotations.SerializedName

data class DetectionResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("results")
    val results: List<DetectionResult> = emptyList(),
    @SerializedName("status")
    val status: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int
)
