package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.Constants.ACCEPT_SERVER_POST
import com.felinetech.localcat.Constants.BROADCAST_PORT
import com.felinetech.localcat.Constants.FILE_CHUNK_SIZE
import com.felinetech.localcat.Constants.HEART_BEAT_SERVER_POST
import com.felinetech.localcat.dao.FileChunkDao
import com.felinetech.localcat.dao.FileEntityDao
import com.felinetech.localcat.enums.*
import com.felinetech.localcat.po.FileChunkEntity
import com.felinetech.localcat.po.FileEntity
import com.felinetech.localcat.pojo.ClientVo
import com.felinetech.localcat.pojo.FileItemVo
import com.felinetech.localcat.pojo.ServicePo
import com.felinetech.localcat.pojo.TaskPo
import com.felinetech.localcat.utlis.*
import com.felinetech.localcat.view_model.HistoryViewModel.downloadedFileList
import com.felinetech.localcat.view_model.HistoryViewModel.uploadedFileList
import com.felinetech.localcat.view_model.SettingViewModel.cachePosition
import com.felinetech.localcat.view_model.SettingViewModel.ruleList
import com.felinetech.localcat.view_model.SettingViewModel.savedPosition
import com.google.gson.Gson
import io.github.vinceglb.filekit.core.FileKit
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.*
import java.net.URLEncoder.encode
import java.nio.channels.FileChannel
import java.util.*


object HomeViewModel {
    /**
     * 待上传文件
     */
    val toBeUploadFileList = mutableStateListOf<FileItemVo>()

    /**
     * 下载中的文件
     */
    val toBeDownloadFileList = mutableStateListOf<FileItemVo>()

    var scanFile by mutableStateOf(false)

    val refresh = MutableStateFlow(false)

    /**
     * 开始上传文件
     */
    var startUpload by mutableStateOf(false)

    /**
     * 显示异常信息
     */
    var showMsg by mutableStateOf(false)
    var msg by mutableStateOf("")

    /**
     * 客户端列表
     */
    val clineList = mutableStateListOf<ClientVo>()


    /**
     * 服务端列表
     */
    var serviceList = mutableStateListOf<ServicePo>()

    /**
     * 本机网络
     */
    var netWork by mutableStateOf("无网络")

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
     * 开始接收客户端链接
     */
    private var accept: Boolean = true


    /**
     * 搜索服务器
     */
    var scanService by mutableStateOf(false)

    val fileEntityDao: FileEntityDao
    var fileChunkDao: FileChunkDao

    /**
     * io协程
     */
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)


    /**
     * 保持链接
     */
    private var keepConnect = false


    /**
     * 链接服务器
     */
    private var acceptSocket: DatagramSocket? = null

    private var logger: Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    /**
     * 请求客户端
     */
    private var client: HttpClient

    /**
     * 服务端
     */
    private var service: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? =
        null

    /**
     * 当前链接的IP地址
     */
    var connectedIpAdd: String? = null

    /**
     * 初始化
     */
    init {
        val database = getDatabase()
        fileEntityDao = database.getFileEntityDao()
        fileChunkDao = database.getFileChunkEntityDao()
        defaultData()
        // 客户端
        client = HttpClient() {
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout)
        }
    }

    /**
     * 默认数据
     */
    private fun defaultData() {
        ioScope.launch {
            val allFile = fileEntityDao.getAllFiles()
            val toUploadFiles = allFile.filter { it.uploadState == UploadState.待上传 }.map {
                filePoToFileVo(it)
            }.toList()

            val uploadedFiles = allFile.filter { it.uploadState == UploadState.已上传 }.map {
                filePoToFileVo(it)
            }.toList()

            val downLoadedFiles = allFile.filter { it.uploadState == UploadState.已下载 }.map {
                filePoToFileVo(it)
            }.toList()

            uiScope.launch {
                toBeUploadFileList.addAll(toUploadFiles)
                uploadedFileList.addAll(uploadedFiles)
                downloadedFileList.addAll(downLoadedFiles)
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
            // 开启心跳线程
            serviceHeartbeat()
        } else {
            // 关闭接收
            receiverButtonTitle.value = ServiceButtonState.开始接收.name
            receiverAnimation.value = false
            accept = false
            clineList.clear()
            service?.let {
                defaultScope.launch {
                    it.stop()
                    logger.info("关闭心跳服务！")
                }
            }
            acceptSocket?.apply {
                close()
                logger.info("关闭接收服务！")

            }
        }
    }

    /**
     * 心跳线程
     */
    private fun serviceHeartbeat() {
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
                    // ping
                    get("/ping") {
                        try {
                            call.respondText("回复心跳！")
                        } catch (e: Exception) {
                            logger.error("心跳异常：", e)
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
                                (bytesRead.toDouble() / totalBytes!!.toDouble() * 100).toInt()
                            toBeDownloadFileList.indexOfFirst { it.fileId == fileItemVo.fileId }
                                .takeIf { it != -1 }?.let { index ->
                                    val item = toBeDownloadFileList[index]
                                    // 直接更新percent
                                    toBeDownloadFileList[index] = item.copy(percent = progress)
                                }
                            println("上传进度：$progress%")
                        }
                        println("数据接收成功！")
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


    /**
     * 合并文件
     */
    private suspend fun mergeFile(fileEntity: FileEntity): Boolean {
        val chunkList = fileChunkDao.getFileChunksByFileId(fileEntity.fileId)
        if (chunkList.isEmpty()) {
            println("没有找到文件块，合并失败！")
            return false
        }
        chunkList.sortedBy { fileChunkEntity -> fileChunkEntity.chunkIndex }
        // 目标地址
        val targetFile = File(savedPosition, fileEntity.fileName)
        val outputStream = FileOutputStream(targetFile)
        val targetChannel: FileChannel = outputStream.getChannel()
        for ((_, fileId, chunkIndex) in chunkList) {
            val chunkName: String = fileEntity.fileName + fileId + chunkIndex + ".cache"
            try {
                // 打开源文件的通道
                val sourceChannel = FileInputStream(File(cachePosition, chunkName)).channel
                withContext(Dispatchers.IO) {
                    targetChannel.transferFrom(
                        sourceChannel, targetChannel.size(), sourceChannel.size()
                    )
                }
                // 关闭源文件的通道
                sourceChannel.close()
            } catch (e: java.lang.Exception) {
                logger.error("合并文件产生异常!${e.message}", e)
                return false
            }
        }
        return true
    }


    /**
     * 开启服务器接收
     */
    private fun serverAccept() {
        accept = true
        defaultScope.launch {
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
                        clineList.add(
                            ClientVo(
                                clineList.size + 1,
                                packet.address.toString().replace("/", ""),
                                ConnectStatus.被发现
                            )
                        )
                    }
                    socket.disconnect()
                    socket.close()
                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        logger.error("等待下一个客户...")
                    } else if (e is BindException) {
                        logger.error("接收服务端口$ACCEPT_SERVER_POST 被占用", e)
                        acceptSocket?.reuseAddress = true;
                        acceptSocket?.disconnect()
                    } else {
                        logger.error("监听客户产生了异常...${e.message} ", e)
                    }
                    acceptSocket?.close()
                }
            }
        }
    }

    /**
     * 开始上传文件
     */
    fun startUploadClick() {
        // 如果没链接请先链接数据源
        if (keepConnect) {
            startUpload = true
        } else {
            // 请先链接数据源
            startUpload = false
            showMsg = true
            msg = "请先链接数据源！"
            return
        }
        ioScope.launch {
            // 找到之前没上传完的数据
//                val taskPo = getTaskPo()
            val fileItemList = toBeUploadFileList.toList()
            for (fileItemVo in fileItemList) {
                // 上传数据
                val response = client.post(
                    "http://${connectedIpAdd}:${HEART_BEAT_SERVER_POST}/upload/${
                        encode(
                            fileItemVo.fileName, Charsets.UTF_8
                        )
                    }"
                ) {
                    timeout {
                        requestTimeoutMillis = 60000
                    }
                    setBody(
                        File(fileItemVo.fileFillName).readBytes()
                    )
                    onUpload { bytesSentTotal, contentLength ->
                        println("Sent $bytesSentTotal bytes from $contentLength ${bytesSentTotal.toDouble() / contentLength!!.toDouble()}")
                        val progress =
                            (bytesSentTotal.toDouble() / contentLength!!.toDouble() * 100).toInt()
                        toBeUploadFileList.indexOfFirst { fileItemVo -> fileItemVo.fileId == fileItemVo.fileId }
                            .takeIf { it != -1 }?.let {
                                val fileItemVo = toBeUploadFileList[it]
                                toBeUploadFileList[it] = fileItemVo.copy(percent = progress)
                            }
                        if (!startUpload) {
                            cancel()
                            return@onUpload
                        }
                    }
                }
                if (response.status == HttpStatusCode.OK) {
                    val responseStr = response.body<String>()
                    println("客户端上传结果：$responseStr")
                    // 下载结束后
                    toBeUploadFileList.indexOfFirst { fileItemVo -> fileItemVo.fileId == fileItemVo.fileId }
                        .takeIf { it != -1 }?.let { index ->
                            val fileItemVo = toBeUploadFileList[index]
                            toBeUploadFileList.removeAt(index)
                            uploadedFileList.add(fileItemVo)
                            val fileItemPo = fileEntityDao.getFileById(fileItemVo.fileId)
                            fileItemPo?.let {
                                it.uploadState = UploadState.已上传
                                fileEntityDao.update(it)
                            }
                        }
                    // 上传成功
                } else {
                    showMsg = true
                    msg = "上传结束！"
                    startUpload = false
                }
            }
            showMsg = true
            msg = "上传结束！"
            startUpload = false
        }
    }

    /**
     * 查看当前未上传或者待上传的数据
     *
     * @return 任务
     */
    suspend fun getTaskPo(): TaskPo? {
        var taskPo: TaskPo? = null
        val uploadInFile: Optional<FileEntity> =
            fileEntityDao.getAllFiles().stream().filter { fileEntity ->
                UploadState.上传中 == fileEntity.uploadState
            }.findFirst()
        if (uploadInFile.isPresent) {
            val fileEntity = uploadInFile.get()
            val fileChunks = fileChunkDao.getFileChunksByFileId(fileEntity.fileId)
            if (fileChunks.isEmpty()) {
                // 如果没有上传中的文件那么修改文件状态为已上传
                fileEntity.uploadState = UploadState.已上传
                fileEntityDao.updateStateByFileId(fileEntity.fileId, UploadState.已上传)
            } else {
                // 如果有就将内容分布发到服务器确认 同步上传状态
                taskPo = TaskPo(fileEntity, fileChunks, FILE_CHUNK_SIZE)
            }
        } else {
            // 如果没有上传中的数据 就创建数据
            val noUploadFile: Optional<FileEntity> = fileEntityDao.getAllFiles().stream()
                .filter { fileEntity -> UploadState.待上传 == fileEntity.uploadState }.findFirst()
            if (noUploadFile.isPresent) {
                val fileEntity = noUploadFile.get()
                // 根据配置生成文件块
                // 生成待上传的块
                val fileChunkEntities: List<FileChunkEntity> =
                    generateFileChunkEntity(fileEntity, FILE_CHUNK_SIZE)
                // 保存到数据库
                for (fileChunk in fileChunkEntities) {
                    fileChunkDao.insert(fileChunk)
                }
                // 修改状态为上传中
                fileEntity.uploadState = UploadState.上传中
                fileEntityDao.updateStateByFileId(fileEntity.fileId, fileEntity.uploadState)
                taskPo = TaskPo(fileEntity, fileChunkEntities, FILE_CHUNK_SIZE)
            } else {
                // 没有待上传的数据
                return null
            }
        }
        return taskPo
    }

    /**
     * 将一个文件生成数据快并插入数据库
     *
     * @param file 文件对象
     * @return 数据快集合
     */
    private fun generateFileChunkEntity(file: FileEntity, blockSize: Long): List<FileChunkEntity> {
        val fileSize = file.fileSize
        val fileChunkEntities = ArrayList<FileChunkEntity>()
        val count = fileSize / (blockSize)
        val lastBlockSize = fileSize % blockSize
        // 拆分文件块1M一个文件块
        for (i in 0 until count) {
            val fileChunkEntity = FileChunkEntity()
            fileChunkEntity.fileId = file.fileId
            fileChunkEntity.chunkIndex = i.toInt()
            fileChunkEntity.chunkSize = blockSize
            fileChunkEntity.uploadStatus = UploadState.未上传
            fileChunkEntity.createdAt = Date()
            fileChunkEntity.updatedAt = Date()
            fileChunkEntities.add(fileChunkEntity)
        }

        if (lastBlockSize != 0L) {
            val fileChunkEntity = FileChunkEntity()
            fileChunkEntity.fileId = file.fileId
            fileChunkEntity.chunkIndex = count.toInt()
            fileChunkEntity.chunkSize = lastBlockSize
            fileChunkEntity.uploadStatus = UploadState.未上传
            fileChunkEntity.createdAt = Date()
            fileChunkEntity.updatedAt = Date()
            fileChunkEntities.add(fileChunkEntity)
        }
        return fileChunkEntities
    }


    /**
     * 结束上传文件
     */
    fun closeUploadFile() {
        startUpload = false
    }

    /**
     * 扫描文件
     */
    fun scanFile() {
        defaultScope.launch {
            scanFile = !scanFile
            delay(500)
            // 遍历每个规则
            for (uploadConfigItem in ruleList) {
                val fileEntityList =
                    scanFileUtil(uploadConfigItem.listeningDir) { fileName, createDate ->
                        fileName.lowercase()
                            .endsWith(uploadConfigItem.matchingRule.split(".")[1]) && uploadConfigItem.startDate.before(
                            createDate
                        )
                    }
                for (filePo in fileEntityList) {
                    val fileVo = filePoToFileVo(filePo)
                    val any =
                        toBeUploadFileList.any { fileItemVo -> fileItemVo.fileName == fileVo.fileName }
                    if (any) {
                        continue
                    }
                    fileEntityDao.insert(filePo)
                    toBeUploadFileList.add(fileVo)
                }
            }
            scanFile = false
        }
    }

    fun updateIpAddress() {
        defaultScope.launch {
            val ipInfo = getIpInfo()
            ipInfo?.let {
                netWork = it.netName
            }
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
                val ipInfo = getIpInfo()
                ipInfo?.let {
                    val subnetMask = it.subnetMask
                    val broadcastIp = getBroadcastAddress(it.ip, subnetMask)
                    try {
                        val ipString = testConnectByIp(broadcastIp)
                        serviceList.add(
                            ServicePo(
                                serviceList.size + 1,
                                ipString,
                                ConnectStatus.未连接,
                                buttonState = ConnectButtonState.连接
                            )
                        )
                        scanService = false
                        return@launch
                    } catch (e: Exception) {
                        println("产生异常：${e.message}")
                    }
                }
                // 方案2 扫描默认广播位
                for (i in 0..25) {
                    if (!scanService) {
                        logger.info("停止扫描服务器！")
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
                        logger.info("接收到响应来自: $ipString")
                        scanService = false
                        return@launch
                    } catch (e: IOException) {
                        logger.error("产生异常：${e.message}", e)
                    }
                }
                scanService = false
            }
        } else {
            logger.info("停止扫描")
        }
    }

    private fun testConnectByIp(ip: String): String {
        val socket = DatagramSocket()
        socket.broadcast = true
        socket.soTimeout = 500
        val request = "HELLO".toByteArray()
        println("测试广播:$ip")
        val requestPacket =
            DatagramPacket(request, request.size, InetAddress.getByName(ip), BROADCAST_PORT)
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
     * 清除历史记录
     */
    fun cleanHistory() {
        toBeUploadFileList.clear()
        ioScope.launch {
            fileEntityDao.deleteAll()
        }
    }

    /**
     * 链接数据源
     */
    fun startClientHeartbeat(servicePo: ServicePo) {
        println("链接服务器$servicePo")
        keepConnect = true
        // 与接收者链接发送心跳信息
        // 启动心跳协程
        ioScope.launch {
            updateServiceState(servicePo, ConnectButtonState.断开)
            connectedIpAdd = "${servicePo.ip}"
            while (keepConnect) {
                try {
                    val pingResult =
                        client.get("http://${servicePo.ip}:${HEART_BEAT_SERVER_POST}/ping")
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

    /**
     * 断开链接
     */
    fun closeDataSources(servicePo: ServicePo) {
        keepConnect = false
        updateServiceState(servicePo, ConnectButtonState.连接)
    }


    /**
     * 更新Button状态
     */
    private fun updateServiceState(servicePo: ServicePo, buttonState: ConnectButtonState) {
        serviceList.find { it == servicePo }.let {
            val index = serviceList.indexOf(servicePo)
            serviceList.removeAt(index)
            servicePo.buttonState = buttonState
            serviceList.add(index, servicePo.copy())
        }
    }
}