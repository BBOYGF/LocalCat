package com.felinetech.fast_file.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.IBinder
import androidx.core.app.NotificationCompat
import co.touchlab.kermit.Logger
import com.felinetech.fast_file.Constants.CHANNEL_ID
import com.felinetech.fast_file.Constants.HEART_BEAT_SERVER_POST
import com.felinetech.fast_file.Constants.NOTIFICATION_ID
import com.felinetech.fast_file.R
import com.felinetech.fast_file.enums.ConnectStatus
import com.felinetech.fast_file.enums.FileType
import com.felinetech.fast_file.enums.UploadState
import com.felinetech.fast_file.interfaces.DataService
import com.felinetech.fast_file.pojo.FileItemVo
import com.felinetech.fast_file.utlis.fileVoToFilePo
import com.felinetech.fast_file.view_model.HistoryViewModel.downloadedFileList
import com.felinetech.fast_file.view_model.HomeViewModel.clineList
import com.felinetech.fast_file.view_model.HomeViewModel.connectedIpAdd
import com.felinetech.fast_file.view_model.HomeViewModel.fileEntityDao
import com.felinetech.fast_file.view_model.HomeViewModel.toBeDownloadFileList
import com.felinetech.fast_file.view_model.SettingViewModel.savedPosition
import com.google.gson.Gson
import io.ktor.http.HttpHeaders
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class AndroidDataService : Service(), DataService {

    private lateinit var notificationManager: NotificationManager

    /**
     * 服务端
     */

    private var service: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    /**
     * io协程
     */
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var logger: Logger = Logger.withTag("AndroidDataServices")

    companion object {
        lateinit var dataService: DataService
        const val ACTION_START = "ACTION_START"
    }

    override fun onCreate() {
        super.onCreate()
        dataService = this
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // 创建通知通道
        createNotificationChannel()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground()
            }
        }
        return START_STICKY
    }

    /**
     * 服务端首次启动
     */
    private fun startForeground() {
        val notification = buildContentNotification("LocalCat", "正在后端运行...")
        // 添加 foregroundServiceType 参数（根据实际使用场景选择类型）
        startForeground(
            NOTIFICATION_ID,
            notification,
            FOREGROUND_SERVICE_TYPE_DATA_SYNC // 选择适合你的服务类型
        )
    }


    /**
     * 构建显示内容通知
     */
    private fun buildContentNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.cat9)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val notification = buildContentNotification(title, content)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }


    /**
     * 创建通知通道
     */
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Progress Updates",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows progress updates"
        }
        notificationManager.createNotificationChannel(channel)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun startDataService() {
        ioScope.launch {
            service = embeddedServer(Netty, port = HEART_BEAT_SERVER_POST, host = "0.0.0.0") {
                // 安装内容协商以支持 JSON
                install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                    Gson()
                }
                routing {
                    // 登录
                    get("/login") {
                        connectedIpAdd = call.request.local.remoteAddress
                        clineList.find { clientVo -> connectedIpAdd == clientVo.ip }?.let {
                            val index = clineList.indexOf(it)
                            clineList.removeAt(index)
                            it.connectStatus = ConnectStatus.已连接
                            clineList.add(index, it.copy())
                        }

                    }
                    get("/logout") {
                        connectedIpAdd = call.request.local.remoteAddress
                        clineList.find { clientVo -> connectedIpAdd == clientVo.ip }?.let {
                            val index = clineList.indexOf(it)
                            clineList.removeAt(index)

                            it.connectStatus = ConnectStatus.被发现
                            clineList.add(index, it.copy())
                        }
                    }
                    // ping
                    get("/ping") {
                        try {
                            call.respondText("回复心跳！")
                        } catch (e: Exception) {
                            logger.e("心跳异常：", e)
                            connectedIpAdd?.let {
                                clineList.find { clientVo -> connectedIpAdd == clientVo.ip }?.let {
                                    val index = clineList.indexOf(it)
                                    clineList.removeAt(index)
                                    it.connectStatus = ConnectStatus.未连接
                                    clineList.add(index, it.copy())
                                }
                            }
                        }
                    }
                    // 下载文件
                    post("/upload/{fileName}") {
                        val filename = call.parameters["fileName"]
                        val file = File(savedPosition, filename!!)
                        val outputChannel = file.writeChannel()
                        // 创建输出流
                        val inputChannel = call.receiveChannel()
                        // 获取 Content-Length 头
                        val totalBytes = call.request.headers[HttpHeaders.ContentLength]
                        val fileItemVo = FileItemVo(
                            UUID.randomUUID().toString(),
                            FileType.doc文档,
                            file.name,
                            UploadState.下载中,
                            0,
                            totalBytes!!.toLong(),
                            fileFillName = file.absolutePath
                        )

                        toBeDownloadFileList.add(fileItemVo)
                        println("读取的总字节数：$totalBytes")
                        var bytesRead = 0L
                        val bufferSize = 1024 // 每次读取的字节数
                        val buffer = ByteArray(bufferSize)
                        // 循环读取输入通道并写入输出通道
                        while (true) {
                            // 从输入通道读取数据
                            val readCount = inputChannel.readAvailable(buffer)
                            if (readCount == -1) break // 输入通道结束
                            // 将读取的数据写入输出通道
                            outputChannel.writeFully(buffer, 0, readCount)
                            bytesRead += readCount

                            // 计算并打印上传进度
                            val progress =
                                (bytesRead.toDouble() / totalBytes.toDouble() * 100).toInt()
                            toBeDownloadFileList.indexOfFirst { it.fileId == fileItemVo.fileId }
                                .takeIf { it != -1 }?.let { index ->

                                    val item = toBeDownloadFileList[index]
                                    // 直接更新percent
                                    toBeDownloadFileList[index] = item.copy(percent = progress)

                                }

                        }
                        toBeDownloadFileList.indexOfFirst { it.fileId == fileItemVo.fileId }
                            .takeIf { it != -1 }?.let { index ->
                                val itemVo = toBeDownloadFileList[index]
                                toBeDownloadFileList.remove(itemVo)
                                downloadedFileList.add(itemVo)
                                val filePo = fileVoToFilePo(itemVo)
                                filePo.uploadState = UploadState.已下载
                                fileEntityDao.insert(filePo)
                            }
                        outputChannel.flushAndClose()
                        call.respondText("A file is uploaded")
                    }
                }
            }
            service?.start(wait = true)
        }
    }

    override fun stopDataService() {
        service?.let {
            ioScope.launch {
                it.stop()
                logger.i("关闭心跳服务！")
            }
        }
        updateNotification("接收者模式", "关闭心跳")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopDataService()
    }
}