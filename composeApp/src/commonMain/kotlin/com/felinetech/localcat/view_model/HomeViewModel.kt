package com.felinetech.localcat.view_model

import com.felinetech.localcat.enums.ConnectButtonState
import com.felinetech.localcat.enums.ConnectStatus
import com.felinetech.localcat.enums.FileType
import com.felinetech.localcat.enums.ServiceButtonState
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.pojo.ClientVo
import com.felinetech.localcat.pojo.FileItemVo
import com.felinetech.localcat.pojo.ServicePo
import com.felinetech.localcat.utlis.getNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException
import java.util.Locale

object HomeViewModel {
    /**
     * 待上传文件
     */
    val scanFileList = MutableStateFlow<List<FileItemVo>>(emptyList())

    var scanFile = MutableStateFlow(false)

    /**
     * 客户端列表
     */
    val clineList = MutableStateFlow<MutableList<ClientVo>>(mutableListOf())

    /**
     * 服务端列表
     */
    val serviceList = MutableStateFlow<MutableList<ServicePo>>(mutableListOf())


    private var list =
        mutableListOf(FileItemVo("点击", FileType.doc文档, "文件", UploadState.待上传, 50, 1024))

    /**
     * 接收按钮状态
     */
    val receiverButtonTitle =
        MutableStateFlow(getNames(Locale.getDefault().language).startReceiving)
    val receiverAnimation = MutableStateFlow(false)

    /**
     * 默认协程
     */
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    /**
     * 开始接收服务器
     */
    private var accept: Boolean = true

    /**
     * UDP 服务接口
     */
    private const val ACCEPT_SERVER_POST = 8200

    /**
     * 显示隐私权限
     */


    /**
     * 开始接收按钮被点击
     */
    fun clickReceiverButton() {
        if (ServiceButtonState.开始接收.name == receiverButtonTitle.value) {
            // 开始接收
            receiverButtonTitle.value = ServiceButtonState.关闭接收.name
            receiverAnimation.value = true
            // 开启接收客户端
            serverAccept()
            // 添加接受者
            receiverFileList()
        } else {
            // 关闭接收
            receiverButtonTitle.value = ServiceButtonState.开始接收.name
            receiverAnimation.value = false
            accept = false
        }
    }

    /**
     * 开启服务器接收
     */
    private fun serverAccept() {
        accept = true
        defaultScope.launch {
            var socket: DatagramSocket? = null
            try {
                while (accept) {
                    socket =
                        DatagramSocket(ACCEPT_SERVER_POST)
                    socket!!
                    socket.setSoTimeout(10000)
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    println("监听客户接收到数据来自: " + packet.address)
                    val response = "OK".toByteArray()
                    val responsePacket =
                        DatagramPacket(response, response.size, packet.address, packet.port)
                    socket.send(responsePacket)
                    val clientList = clineList.value.toMutableList()
                    clientList.add(
                        ClientVo(
                            clientList.size + 1,
                            packet.address.toString().replace("/", ""),
                            ConnectStatus.被发现
                        )
                    )
                    clineList.value = clientList
                }
            } catch (e: Exception) {
                if (e is SocketTimeoutException) {
                    println("监听客户超时...")
                } else {
                    println("监听客户产生了异常...${e.message}")
                }
                socket?.close()
                throw e
            }
        }
    }


    private fun receiverFileList() {
        val item = list[0]
        item.percent = (item.percent + 1)
        list[0] = item.copy()
        val newList = mutableListOf<FileItemVo>()
        list.forEach {
            newList.add(it)
        }
        scanFileList.value = newList
    }


    fun senderClick() {
        val list = serviceList.value.toMutableList()
        list.add(ServicePo(1, "192.168.1.1", ConnectStatus.未连接, ConnectButtonState.连接))
        serviceList.value = list


    }

    /**
     * 扫描文件
     */
    fun scanFile() {
        scanFile.value = !scanFile.value
        defaultScope.launch {
            for (i in 1..5) {
                delay(1000)
            }
            scanFile.emit(false)
        }
    }
}