package com.example.androidtrafficsigndetection.Classes

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.camera.core.ImageProxy
import com.example.androidtrafficsigndetection.DataModel.DetectionResponse
import com.example.androidtrafficsigndetection.DataModel.DetectionResult
import com.example.androidtrafficsigndetection.DataModel.SettingParams
import com.example.androidtrafficsigndetection.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

object HttpDetection {
    private val client = OkHttpClient()
    private val gson = Gson()
    var reachable = false

    fun testConnection() {
        val request = Request.Builder()
            .url(SettingParams.retrieveURL() + "/api/test")
            .head() // 使用 HEAD 方法（仅检查连接，不传输响应体）[1,3](@ref)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                reachable = true
                Log.d("HttpDetection", "Reachable:" + reachable)
            }
        })
    }
    // 上传图像数据
    fun uploadImage(imageProxy: ImageProxy, compress: Boolean, annotationView: AnnotationView) {
        client.newCall(processImageProxy(imageProxy, compress)).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val detectionResponse = gson.fromJson(response.body?.string(), DetectionResponse::class.java)
                    annotationView.updateDetections(annotationView.screenConvert(detectionResponse.results as MutableList<DetectionResult>, detectionResponse.width, detectionResponse.height))
                } catch (e: Exception) {

                } finally {
                    response.close() // 必须关闭响应体防止内存泄漏[4](@ref)
                }
            }
        })
    }

    suspend fun uploadMedia(file: File): File? = withContext(Dispatchers.IO) { // 在 IO 线程执行
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(SettingParams.retrieveURL() + "/api/detectionMedia")
            .post(
                MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        file.name,
                        file.asRequestBody("image/jpeg".toMediaType())
                    )
                    .apply {
                        SettingParams.detectionMap.getMap().forEach { (key, value) ->
                            addFormDataPart(
                                key.toString(),
                                null,
                                value.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            )
                        }
                    }
                    .build()
            )
            .build()

        return@withContext try {
            val response = client.newCall(request).execute() // 仍用同步方法，但在协程的 IO 线程中执行
            if (response.isSuccessful) {
                val responseBody = response.body
                if (responseBody != null) {
                    val inputStream = responseBody.byteStream()
                    Log.d("HttpDetection", "res: " + inputStream)
                    val outputFile = File.createTempFile("response", ".jpg")
                    FileOutputStream(outputFile).use { outputStream -> // 自动关闭流
                        inputStream.copyTo(outputStream)
                    }
                    outputFile // 返回文件对象
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: IOException) {
            Log.d("HttpDetection", "res: " + e)
            e.printStackTrace()
            null
        }
    }
//    fun uploadMedia(file: File): File? {
//        val client = OkHttpClient()
//        val request = Request.Builder()
//            .url(SettingParams.retrieveURL() + "/api/detectionMedia")
//            .post(
//                MultipartBody.Builder()
//                    .setType(MultipartBody.FORM)
//                    .addFormDataPart(
//                        "image",
//                        file.name,
//                        file.asRequestBody("image/jpeg".toMediaType())
//                    )
//                    .apply {
//                        SettingParams.detectionMap.getMap().forEach { (key, value) ->
//                            addFormDataPart(
//                                key.toString(),
//                                null,
//                                value.toString()
//                                    .toRequestBody("text/plain".toMediaTypeOrNull())
//                            )
//                        }
//                    }
//                    .build()
//            )
//            .build()
//
//        try {
//            val response = client.newCall(request).execute()
//            if (response.isSuccessful) {
//                val responseBody = response.body
//                if (responseBody != null) {
//                    val inputStream = responseBody.byteStream()
//                    val outputFile = File.createTempFile("response", ".jpg")
//                    val outputStream = FileOutputStream(outputFile)
//                    inputStream.copyTo(outputStream)
//                    outputStream.close()
//                    inputStream.close()
//                    return outputFile
//                }
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        return null
//    }

    private fun processImageProxy(imageProxy: ImageProxy, compress: Boolean): Request {
        val image = imageProxy.image ?: throw IllegalStateException("Invalid ImageProxy")
        val request = when {
            compress -> {
                Request.Builder()
                    .url(SettingParams.retrieveURL() + "/api/detection")
                    .post(uploadJpegFrame(convertToJpegByteString(image)))
                    .build()
            }

            else -> {
                Request.Builder()
                    .url(SettingParams.retrieveURL() + "/api/detection")
                    .post(uploadYuvFrame(convertToYuvByteString(image), image.width, image.height))
                    .build()
            }
        }
        return request
    }
    private fun convertToYuvByteString(image: Image): ByteString {
        // 优化YUV数据提取（直接操作ByteBuffer）
        val planes = image.planes
        val yBuffer = planes[0].buffer
        val uBuffer = planes[1].buffer
        val vBuffer = planes[2].buffer

        // 获取各平面有效数据大小
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        // 创建符合NV21格式的字节数组
        val nv21Data = ByteArray(ySize + vSize + uSize).apply {
            // Y平面直接拷贝
            yBuffer.get(this, 0, ySize)
            // 交换UV平面（NV21需要V在前U在后）
            vBuffer.get(this, ySize, vSize)
            uBuffer.get(this, ySize + vSize, uSize)
        }

        return ByteString.of(*nv21Data)
    }
    private fun convertToJpegByteString(image: Image): ByteString {
        val yuvData = image.toNV21ByteArray()
        val yuvImage = YuvImage(yuvData, ImageFormat.NV21, image.width, image.height, null)
        return ByteArrayOutputStream().use { os ->
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 80, os)
            ByteString.of(*os.toByteArray())
        }
    }
//    private fun Image.toNV21ByteArray(): ByteArray {
//        val planes = this.planes
//        val width = this.width
//        val height = this.height
//        val yPlane = planes[0]
//        val uPlane = planes[1]
//        val vPlane = planes[2]
//
//        val yBuffer = yPlane.buffer
//        val uBuffer = uPlane.buffer
//        val vBuffer = vPlane.buffer
//
//        val ySize = yPlane.rowStride * height
//        val uvSize = width * height / 2
//        val nv21 = ByteArray(ySize + uvSize)
//
//        var yIndex = 0
//        for (row in 0 until height) {
//            yBuffer.position(row * yPlane.rowStride)
//            yBuffer.get(nv21, yIndex, width)
//            yIndex += width
//        }
//
//        var uvIndex = ySize
//        val uvRowStride = uPlane.rowStride
//        val uvPixelStride = uPlane.pixelStride
//        val uvWidth = (width + uvPixelStride - 1) / uvPixelStride
//        val uvHeight = (height + uvPixelStride - 1) / uvPixelStride
//
//        for (row in 0 until uvHeight) {
//            vBuffer.position(row * uvRowStride)
//            uBuffer.position(row * uvRowStride)
//            for (col in 0 until uvWidth) {
//                // 检查索引是否越界
//                if (uvIndex < nv21.size) {
//                    nv21[uvIndex++] = vBuffer.get()
//                    if (uvIndex < nv21.size) {
//                        nv21[uvIndex++] = uBuffer.get()
//                    } else {
//                        // 处理越界情况，例如记录日志
//                        Log.e("NV21Conversion", "Index out of bounds when writing U data")
//                        break
//                    }
//                } else {
//                    // 处理越界情况，例如记录日志
//                    Log.e("NV21Conversion", "Index out of bounds when writing V data")
//                    break
//                }
//            }
//        }
//        return nv21
//    }
    private fun Image.toNV21ByteArray(): ByteArray {
        val planes = this.planes
        require(planes.size >= 3) { "Invalid YUV image format" }

        // 获取各平面参数
        val yPlane = planes[0]
        val uPlane = planes[1]
        val vPlane = planes[2]

        // 动态适应不同设备的采样特性
        val width = this.width
        val height = this.height
        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride.coerceAtMost(vPlane.rowStride) // 取较小步长
        val uvPixelStride = uPlane.pixelStride.coerceAtMost(vPlane.pixelStride) // 兼容交错/独立平面

        // 计算缓冲区尺寸（包含硬件对齐的冗余字节）
        val yBuffer = yPlane.buffer.rewind()
        val uBuffer = uPlane.buffer.rewind()
        val vBuffer = vPlane.buffer.rewind()

        // 按奈奎斯特准则计算有效数据量[3](@ref)
        val ySize = width * height
        val uvSize = (width * height) / 2
        val nv21 = ByteArray(ySize + uvSize)

        // Y分量处理（考虑行填充字节）
        copyPlaneData(
            srcBuffer = yBuffer as ByteBuffer,
            dest = nv21,
            destOffset = 0,
            width = width,
            height = height,
            srcPixelStride = yPlane.pixelStride,
            srcRowStride = yRowStride
        )

        // UV分量交错处理（动态适应采样模式）
        interleaveUVPlanes(
            uBuffer = uBuffer as ByteBuffer,
            vBuffer = vBuffer as ByteBuffer,
            dest = nv21,
            destOffset = ySize,
            width = width / 2,  // NV21的UV分辨率是Y的1/4
            height = height / 2,
            srcPixelStride = uvPixelStride,
            srcRowStride = uvRowStride
        )

        return nv21
    }

    private fun copyPlaneData(
        srcBuffer: ByteBuffer,
        dest: ByteArray,
        destOffset: Int,
        width: Int,
        height: Int,
        srcPixelStride: Int,
        srcRowStride: Int
    ) {
        var destIndex = destOffset
        for (row in 0 until height) {
            srcBuffer.position(row * srcRowStride)
            val rowData = ByteArray(width)
            srcBuffer.get(rowData, 0, width)
            System.arraycopy(rowData, 0, dest, destIndex, width)
            destIndex += width
        }
    }

    private fun interleaveUVPlanes(
        uBuffer: ByteBuffer,
        vBuffer: ByteBuffer,
        dest: ByteArray,
        destOffset: Int,
        width: Int,
        height: Int,
        srcPixelStride: Int,
        srcRowStride: Int
    ) {
        var destIndex = destOffset
        for (row in 0 until height) {
            uBuffer.position(row * srcRowStride)
            vBuffer.position(row * srcRowStride)

            for (col in 0 until width) {
                // 动态适应不同采样模式（如4:2:0、4:2:2等）
                val uvOffset = col * srcPixelStride
                if (uvOffset < srcRowStride) {
                    vBuffer.position(row * srcRowStride + uvOffset)
                    uBuffer.position(row * srcRowStride + uvOffset)

                    // 边界检查（防止某些设备的异常对齐）
                    if (destIndex < dest.size) dest[destIndex++] = vBuffer.get()
                    if (destIndex < dest.size) dest[destIndex++] = uBuffer.get()
                }
            }
        }
    }
    private fun uploadYuvFrame(yuvData: ByteString, width: Int, height: Int): MultipartBody {
        val metadata = """
        {"format":"NV21","width":$width,"height":$height}
    """.trimIndent()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "metadata",
                "frame_info.json",
                metadata.toRequestBody("application/json".toMediaType())
            )
            .addFormDataPart(
                "yuv_data",
                "frame_${System.currentTimeMillis()}.yuv",
                yuvData.toRequestBody("application/octet-stream".toMediaType())
            )
            .apply {
                SettingParams.detectionMap.getMap().forEach { (key, value) ->
                    // 添加文本类型字段（无文件名）
                    addFormDataPart(
                        key.toString(),
                        null,  // 文件名设为 null 表示纯文本字段
                        value.toString()
                            .toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                }
            }
            .build()
        return requestBody
    }
    private fun uploadJpegFrame(jpegData: ByteString, quality: Int = 80): MultipartBody {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "compressed_image",
                "photo_${System.currentTimeMillis()}.jpg",
                jpegData.toRequestBody("image/jpeg".toMediaType())
            )
            .addFormDataPart(
                "quality",
                quality.toString().toRequestBody("text/plain".toMediaType()).toString()
            )
            .apply {
                SettingParams.detectionMap.getMap().forEach { (key, value) ->
                    // 添加文本类型字段（无文件名）
                    addFormDataPart(
                        key.toString(),
                        null,  // 文件名设为 null 表示纯文本字段
                        value.toString()
                            .toRequestBody("text/plain".toMediaTypeOrNull())
                    )
                }
            }
            .build()
        return requestBody
    }
}
