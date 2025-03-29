package com.example.androidtrafficsigndetection.Classes

import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.androidtrafficsigndetection.DataModel.SettingParams
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.WebSocket
import okio.ByteString
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow

object SocketIODetection {
//    private const val PING_INTERVAL = 10L // 心跳间隔(秒)
//    private var webSocket: WebSocket? = null
//    private val client = OkHttpClient.Builder()
//        .pingInterval(PING_INTERVAL, TimeUnit.SECONDS) // 自动心跳保活[4](@ref)
//        .retryOnConnectionFailure(true) // 底层自动重连
//        .build()
//
//    // 初始化WebSocket连接（单例模式）
//    fun initConnection() {
//        val request = Request.Builder()
//            .url(SettingParams.retrieveURL())
//            .apply { addParams(this) } // 附加初始化参数
//            .build()
//
//        webSocket = client.newWebSocket(request, object : WebSocketListener() {
//            override fun onOpen(webSocket: WebSocket, response: Response) {
//                Log.d("WebSocket", "Connected to server")
//            }
//
//            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//                handleServerResponse(bytes) // 处理服务端返回的检测结果
//            }
//
//            private fun handleServerResponse(bytes: okio.ByteString) {}
//
//            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//                Log.d("WebSocket", "Connection closed: $reason")
//            }
//
//            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                Log.e("WebSocket", "Connection failed", t)
//                scheduleReconnect() // 实现自定义重连策略[5](@ref)
//            }
//        })
//    }
//
//    fun sendFrameToServer(imageProxy: ImageProxy, compress: Boolean) {
//        check(webSocket != null) { "WebSocket not initialized" }
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val frameData = processImageProxy(imageProxy, compress)
//                webSocket?.send(frameData)
//            } catch (e: Exception) {
//                Log.e("Detection", "Frame processing failed", e)
//            } finally {
//                imageProxy.close()
//            }
//        }
//    }
//
//    fun closeConnection() {
//        webSocket?.close(1000, "Normal closure")
//        client.dispatcher.executorService.shutdown()
//    }
//
//    private fun addParams(builder: Request.Builder) {
//        val params = SettingParams.detectionMap.getMap()
//            .map { "${it.key}=${it.value}" }
//            .joinToString("&")
//        builder.url("${SettingParams.retrieveURL()}?$params")
//    }
//    private fun processImageProxy(imageProxy: ImageProxy, compress: Boolean): ByteString {
//        val image = imageProxy.image ?: throw IllegalStateException("Invalid ImageProxy")
//        return when {
//            compress -> convertToJpegByteString(image)
//            else -> convertToYuvByteString(image)//.also {
//                //sendMetaData(image.width, image.height) // 单独发送元数据[1](@ref)
//            //}
//        }
//    }
//    private fun convertToJpegByteString(image: Image): ByteString {
//        val yuvData = image.toNV21ByteArray()
//        val yuvImage = YuvImage(yuvData, ImageFormat.NV21, image.width, image.height, null)
//        return ByteArrayOutputStream().use { os ->
//            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 80, os)
//            ByteString.of(*os.toByteArray())
//        }
//    }
//    private fun Image.toNV21ByteArray(): ByteArray {
//        // 优化后的YUV转换逻辑（避免中间数组拷贝）
//        return planes.foldIndexed(ByteArray(0)) { i, acc, plane ->
//            val buffer = plane.buffer.duplicate()
//            when (i) {
//                0 -> ByteArray(buffer.remaining()).also { buffer.get(it) }
//                1 -> acc + ByteArray(buffer.remaining()).also { buffer.get(it) }
//                2 -> {
//                    val swapped = ByteArray(buffer.remaining())
//                    buffer.get(swapped)
//                    acc + swapped // 交换UV平面[6](@ref)
//                }
//                else -> acc
//            }
//        }
//    }
//    private fun scheduleReconnect(attempt: Int = 0) {
//        val delay = minOf(500 * (2.0.pow(attempt)).toLong(), 30000)
//        CoroutineScope(Dispatchers.IO).launch {
//            delay(delay)
//        }
//    }
//    private fun sendMetaData(width: Int, height: Int) {
//        val json = JSONObject().apply {
//            put("width", width)
//            put("height", height)
//            put("format", "NV21")
//        }
//        webSocket?.send(json.toString())
//    }
//    private fun convertToYuvByteString(image: Image): ByteString {
//        // 优化YUV数据提取（直接操作ByteBuffer）
//        val planes = image.planes
//        val yBuffer = planes[0].buffer
//        val uBuffer = planes[1].buffer
//        val vBuffer = planes[2].buffer
//
//        // 获取各平面有效数据大小
//        val ySize = yBuffer.remaining()
//        val uSize = uBuffer.remaining()
//        val vSize = vBuffer.remaining()
//
//        // 创建符合NV21格式的字节数组
//        val nv21Data = ByteArray(ySize + vSize + uSize).apply {
//            // Y平面直接拷贝
//            yBuffer.get(this, 0, ySize)
//            // 交换UV平面（NV21需要V在前U在后）
//            vBuffer.get(this, ySize, vSize)
//            uBuffer.get(this, ySize + vSize, uSize)
//        }
//
//        return ByteString.of(*nv21Data)
//    }
//
//
//    private val opts = IO.Options().apply {
//        reconnection = true  // 启用自动重连
//        transports = arrayOf("websocket")  // 强制使用 WebSocket 传输
//        query = "platform=android"  // 附加认证参数（可选）
//    }
//
//    val socket: Socket = IO.socket(SettingParams.retrieveURL(), opts)
//    fun socketConnect() {
//        socket.on(Socket.EVENT_CONNECT) {
//            val jsonObject = JSONObject().apply {
//                SettingParams.detectionMap.getMap().forEach { k, v ->
//                    put(k.toString(), v.toString())
//                }
//            }
//            // socket.emit("init_yolo", jsonObject)
//        }
//        socket.on("connection_ack") { args ->
//            Log.d("SocketIO", "连接成功: ${args[0]}")
//        }
//        socket.on(Socket.EVENT_CONNECT_ERROR) { args ->
//            Log.e("SocketIO", "连接失败: ${args[0]}")
//        }
//    }
//
//    fun frameDetect(imageProxy: ImageProxy, compress: Boolean) {
//        // val frameData = processImageProxy(imageProxy, compress)
//
//        // socket.emit("video_frame", frameData)
//        socket.emit("video_frame", "frameData")
//        socket.on("processing_result") { args ->
//            val result = args[0] as JSONObject
//            Log.d("SocketIO", "收到处理结果: ${result.getString("status")}")
//        }
//    }
//
//    fun socketDisconnect() {
//        socket.disconnect()
//
//        socket.off(Socket.EVENT_CONNECT)
//        socket.off("processing_result")
//
//        // 错误日志细化
//        socket.on(Socket.EVENT_DISCONNECT) {
//            Log.w("SocketIO", "连接已断开，代码：${it[0]} 原因：${it[1]}")
//        }
//    }
}