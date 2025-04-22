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
import com.felinetech.fast_file.Constants.ACCEPT_SERVER_POST
import com.felinetech.fast_file.Constants.CHANNEL_ID
import com.felinetech.fast_file.Constants.NOTIFICATION_ID
import com.felinetech.fast_file.R
import com.felinetech.fast_file.enums.ConnectStatus
import com.felinetech.fast_file.interfaces.ReceiverService
import com.felinetech.fast_file.pojo.ClientVo
import com.felinetech.fast_file.view_model.HomeViewModel.clineList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.BindException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException


class AndroidReceiverServices : Service(), ReceiverService {
    /**
     * 开始接收客户端链接
     */
    private var accept: Boolean = true

    /**
     * 链接服务器
     */
    private var acceptSocket: DatagramSocket? = null

    private var logger: Logger = Logger.withTag("AndroidReceiverServices")

    /**
     * 协程
     */
    private val scope = CoroutineScope(Dispatchers.Default)

    private lateinit var notificationManager: NotificationManager

    companion object {
        lateinit var receiverService: AndroidReceiverServices
        const val ACTION_START = "ACTION_START"
    }

    override fun onCreate() {
        super.onCreate()
        receiverService = this
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun startReceiver() {
        accept = true
        scope.launch {
            while (accept) {
                try {
                    acceptSocket = DatagramSocket(ACCEPT_SERVER_POST)
                    val socket = acceptSocket!!
                    socket.reuseAddress = true
                    socket.soTimeout = 10000
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    println("监听客户接收到数据来自: " + packet.address)
                    val response = "OK".toByteArray()
                    val responsePacket =
                        DatagramPacket(response, response.size, packet.address, packet.port)
                    socket.send(responsePacket)
                    val ip = packet.address.toString().replace("/", "")
                    if (!clineList.any { clientVo -> clientVo.ip == ip }) {
                        val element = ClientVo(
                            clineList.size + 1,
                            packet.address.toString().replace("/", ""),
                            ConnectStatus.被发现
                        )
                        clineList.add(
                            element
                        )
                        updateNotification("接收者模式", "发现用户：" + element.ip)
                    }
                    socket.disconnect()
                    socket.close()
                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        logger.e("等待下一个客户...")
                    } else if (e is BindException) {
                        logger.e("接收服务端口$ACCEPT_SERVER_POST 被占用", e)
                        acceptSocket?.reuseAddress = true;
                        acceptSocket?.disconnect()
                    } else {
                        logger.e("监听客户产生了异常...${e.message} ", e)
                    }
                    acceptSocket?.close()
                }
            }
        }
    }

    override fun stopReceiver() {
        logger.i("停止接收")
        accept = false
        scope.cancel()
        acceptSocket?.apply {
            close()
            logger.i("关闭接收服务！")
        }
        updateNotification("接收者模式", "关闭接收")
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

    private fun buildProgressNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.cat9)
            .setProgress(100, 1, false)// todo
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val notification = buildContentNotification(title, content)
        notificationManager.notify(NOTIFICATION_ID, notification)
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
}