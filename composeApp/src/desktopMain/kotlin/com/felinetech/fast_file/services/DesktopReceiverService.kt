package com.felinetech.fast_file.services

import co.touchlab.kermit.Logger
import com.felinetech.fast_file.Constants.ACCEPT_SERVER_POST
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

class DesktopReceiverService : ReceiverService {

    /**
     * 开始接收客户端链接
     */
    private var accept: Boolean = true

    /**
     * 链接服务器
     */
    private var acceptSocket: DatagramSocket? = null

    /**
     * 日志
     */
    private var logger: Logger = Logger.withTag("DesktopReceiverServices")

    /**
     * 协程
     */
    private val scope = CoroutineScope(Dispatchers.Default)

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
    }
}