package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.Constants.BROADCAST_PORT
import com.felinetech.localcat.dao.FileEntityDao
import com.felinetech.localcat.enums.*
import com.felinetech.localcat.po.FileEntity
import com.felinetech.localcat.pojo.ClientVo
import com.felinetech.localcat.pojo.FileItemVo
import com.felinetech.localcat.pojo.ServicePo
import com.felinetech.localcat.utlis.*
import com.felinetech.localcat.view_model.SettingViewModel.ruleList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.*

object HomeViewModel {
    /**
     * 待上传文件
     */
    val scanFileList = mutableStateListOf<FileItemVo>()

    var scanFile by mutableStateOf(false)

    /**
     * 客户端列表
     */
    val clineList = mutableStateListOf<ClientVo>()


    /**
     * 服务端列表
     */
    var serviceList = mutableStateListOf<ServicePo>()

    /**
     * 本机IP地址
     */
    val ipAddress = MutableStateFlow("127.0.0.1")

    private var list = mutableListOf(FileItemVo("点击", FileType.doc文档, "文件", UploadState.待上传, 50, 1024))

    /**
     * 接收按钮状态
     */
    val receiverButtonTitle = MutableStateFlow(getNames(Locale.getDefault().language).startReceiving)
    val receiverAnimation = MutableStateFlow(false)

    /**
     * 默认协程
     */
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    /**
     * 开始接收客户端链接
     */
    private var accept: Boolean = true


    /**
     * UDP 服务接口
     */
    private const val ACCEPT_SERVER_POST = 8200

    /**
     * 搜索服务器
     */
    var scanService by mutableStateOf(false)

    private val fileEntityDao: FileEntityDao

    /**
     * io协程
     */
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    /**
     * 初始化
     */
    init {
        val database = getDatabase()
        fileEntityDao = database.getFileEntityDao()
        defaultData()
    }

    /**
     * 默认数据
     */
    private fun defaultData() {
        ioScope.launch {
            val allFile = fileEntityDao.getAllFiles()
            val fileList = allFile.filter { it.uploadState == UploadState.待上传 }.map {
                filePoToFileVo(it)
            }.toList()
            uiScope.launch {
                scanFileList.addAll(fileList)
            }
        }
    }

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
//            receiverFileList()
        } else {
            // 关闭接收
            receiverButtonTitle.value = ServiceButtonState.开始接收.name
            receiverAnimation.value = false
            accept = false
            clineList.clear()
        }
    }

    /**
     * 开启服务器接收
     */
    private fun serverAccept() {
        accept = true
        defaultScope.launch {
            var socket: DatagramSocket? = null

            while (accept) {
                try {
                    socket = DatagramSocket(ACCEPT_SERVER_POST)
                    socket!!
                    socket.soTimeout = 10000
                    val buffer = ByteArray(1024)
                    val packet = DatagramPacket(buffer, buffer.size)
                    socket.receive(packet)
                    println("监听客户接收到数据来自: " + packet.address)
                    val response = "OK".toByteArray()
                    val responsePacket = DatagramPacket(response, response.size, packet.address, packet.port)
                    socket.send(responsePacket)
                    clineList.add(
                        ClientVo(
                            clineList.size + 1, packet.address.toString().replace("/", ""), ConnectStatus.被发现
                        )
                    )
                    socket.close()
                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        println("监听客户超时...")
                    } else {
                        println("监听客户产生了异常...${e.message}")
                    }
                    socket?.close()
                }
            }
        }
    }


    fun senderClick() {
        serviceList.add(ServicePo(1, "192.168.1.1", ConnectStatus.未连接, ConnectButtonState.连接))
    }

    /**
     * 扫描文件
     */
    fun scanFile() {
        defaultScope.launch {
            uiScope.launch {
                scanFile = !scanFile
            }
            scanFileList.clear()
            // 遍历每个规则
            for (uploadConfigItem in ruleList) {
                val paramFile = File(uploadConfigItem.listeningDir)
                if (!paramFile.exists()) {
                    continue
                }
                val fileList = paramFile.listFiles { _, name ->
                    name.lowercase().endsWith(uploadConfigItem.matchingRule.split(".")[1])
                }
                for (file in fileList!!) {
                    val lastModified = file.lastModified()
                    // 将时间戳转换为可读的日期格式
                    val lastModifiedDate = Date(lastModified)
                    if (lastModifiedDate.after(uploadConfigItem.startDate)) {
                        continue
                    }

                    val filePo = FileEntity(
                        null,
                        "1",
                        UUID.randomUUID().toString(),
                        file.name,
                        file.absolutePath,
                        file.length(),
                        UploadState.待上传
                    )
                    val fileVo = filePoToFileVo(filePo)
                    fileEntityDao.insert(filePo)
                    uiScope.launch {
                        scanFileList.add(fileVo)
                    }
                }
            }
            uiScope.launch {
                scanFile = false
            }
        }
    }

    fun updateIpAddress() {
        defaultScope.launch {
            ipAddress.value = getLocalIp()
        }
    }

    /**
     * 开始扫描服务器
     */
    fun startScanService() {
        scanService = !scanService
        if (scanService) {
            serviceList.clear()
            println("开始扫描数据源...")
            defaultScope.launch {
                // 方案1 根据子网掩码 获取广播网位
                if (StringUtils.isEmpty(ipAddress.value)) {
                    ipAddress.value = getLocalIp()
                }
                val subnetMask = getSubnetMask()
                val broadcastIp = getBroadcastAddress(ipAddress.value, subnetMask)
                try {
                    val ipString = testConnectByIp(broadcastIp)
                    serviceList.add(
                        ServicePo(
                            serviceList.size + 1, ipString, ConnectStatus.未连接, buttonState = ConnectButtonState.连接
                        )
                    )
                    scanService = false
                    return@launch
                } catch (e: Exception) {
                    println("产生异常：${e.message}")
                }
                // 方案2 扫描默认广播位
                for (i in 0..25) {
                    if (!scanService) {
                        println("停止扫描服务器！")
                        return@launch
                    }
                    val ip = "192.168.$i.255"
                    try {
                        val ipString = testConnectByIp(ip)
                        serviceList.add(
                            ServicePo(
                                serviceList.size + 1,
                                ipString,
                                ConnectStatus.未连接,
                                buttonState = ConnectButtonState.连接
                            )
                        )
                        println("接收到响应来自: $ipString")
                        scanService = false
                        return@launch
                    } catch (e: IOException) {
                        println("产生异常：${e.message}")
                    }
                }
            }
        } else {
            println("停止扫描")
        }

    }

    private fun testConnectByIp(ip: String): String {
        val socket = DatagramSocket()
        socket.broadcast = true
        socket.soTimeout = 500
        val request = "HELLO".toByteArray()
        println("测试广播:$ip")
        val requestPacket = DatagramPacket(request, request.size, InetAddress.getByName(ip), BROADCAST_PORT)
        socket.send(requestPacket)
        println("链接成功:$ip")
        val buffer = ByteArray(1024)
        val responsePacket = DatagramPacket(buffer, buffer.size)
        socket.receive(responsePacket)
        val address = responsePacket.address
        val ipString = address.hostAddress
        return ipString
    }

    /**
     * 获取文件类型
     */
    private fun getFileType(fileName: String): FileType {
        return FileType.entries.first { fileName.lowercase().endsWith(it.suffix) }
    }

    private fun filePoToFileVo(it: FileEntity) =
        FileItemVo(it.fileId, getFileType(it.fileName), it.fileName, it.uploadState, 0, it.fileSize)

}