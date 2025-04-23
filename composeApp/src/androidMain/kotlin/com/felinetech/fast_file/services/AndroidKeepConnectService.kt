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
import com.felinetech.fast_file.enums.ConnectButtonState
import com.felinetech.fast_file.interfaces.KeepConnectService
import com.felinetech.fast_file.pojo.ServicePo
import com.felinetech.fast_file.view_model.HomeViewModel.connectedIpAdd
import com.felinetech.fast_file.view_model.HomeViewModel.keepConnect
import com.felinetech.fast_file.view_model.HomeViewModel.updateServiceState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AndroidKeepConnectService : Service(), KeepConnectService {
    private lateinit var notificationManager: NotificationManager

    /**
     * io协程
     */
    private val ioScope = CoroutineScope(Dispatchers.IO)

    /**
     * 请求客户端
     */
    private lateinit var client: HttpClient

    private var logger: Logger = Logger.withTag("AndroidKeepConnectService")


    companion object {
        lateinit var keepConnectService: KeepConnectService
        const val ACTION_START = "ACTION_START"
    }

    override fun onCreate() {
        super.onCreate()
        client = HttpClient() {
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout)
        }
        keepConnectService = this
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // 创建通知通道
        createNotificationChannel()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            AndroidDataService.ACTION_START -> {
                startForeground()
            }
        }
        return START_STICKY
    }

    /**
     * 服务端首次启动
     */
    private fun startForeground() {
        val notification = buildContentNotification("LocalCat", "与发送者心跳中...")
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


    override fun stareKeepConnect(servicePo: ServicePo) {
        ioScope.launch {
            client.get("http://${servicePo.ip}:$HEART_BEAT_SERVER_POST/login")
            updateServiceState(servicePo, ConnectButtonState.断开)
            connectedIpAdd = servicePo.ip
            keepConnect = true
            while (keepConnect) {
                try {
                    val pingResult =
                        client.get("http://${servicePo.ip}:$HEART_BEAT_SERVER_POST/ping")
                    if (pingResult.status == HttpStatusCode.OK) {
                        println("请求结果是${pingResult.body<String>()}")
                        delay(1000)
                    } else {
                        keepConnect = false
                    }
                } catch (e: Exception) {
                    println("请求异常！${e.message}")
                    keepConnect = false
                    updateServiceState(servicePo, ConnectButtonState.连接)
                }
            }
        }
    }

    override fun stopConnect(servicePo: ServicePo) {
        keepConnect = false
        updateServiceState(servicePo, ConnectButtonState.连接)
        ioScope.launch {
            client.get("http://${servicePo.ip}:${HEART_BEAT_SERVER_POST}/logout")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        keepConnect = false

    }
}