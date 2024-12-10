package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.Constants.ACCEPT_SERVER_POST
import com.felinetech.localcat.Constants.BROADCAST_PORT
import com.felinetech.localcat.Constants.DATA_UPLOAD_SERVER_POST
import com.felinetech.localcat.Constants.FILE_CHUNK_SIZE
import com.felinetech.localcat.Constants.HEART_BEAT_SERVER_POST
import com.felinetech.localcat.Constants.THREAD_COUNT
import com.felinetech.localcat.dao.FileChunkDao
import com.felinetech.localcat.dao.FileEntityDao
import com.felinetech.localcat.enums.*
import com.felinetech.localcat.po.FileChunkEntity
import com.felinetech.localcat.po.FileEntity
import com.felinetech.localcat.pojo.*
import com.felinetech.localcat.utlis.*
import com.felinetech.localcat.view_model.HistoryViewModel.downloadedFileList
import com.felinetech.localcat.view_model.SettingViewModel.cachePosition
import com.felinetech.localcat.view_model.SettingViewModel.ruleList
import com.felinetech.localcat.view_model.SettingViewModel.savedPosition
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.commons.lang3.StringUtils
import java.io.*
import java.net.*
import java.nio.channels.FileChannel
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import kotlin.math.floor


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
     * 搜索服务器
     */
    var scanService by mutableStateOf(false)

    private val fileEntityDao: FileEntityDao
    private var fileChunkDao: FileChunkDao

    /**
     * io协程
     */
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val uiScope = CoroutineScope(Dispatchers.Main)

    /**
     * &
     * 任务队列
     */
    private val commandQueue: Queue<Command> = LinkedList()

    /**
     * 保持链接
     */
    private var keepConnect = false

    /**
     * 心跳ServiceSocket
     */
    private var heartServerSocket: ServerSocket? = null

    /**
     * 链接服务器
     */
    private var acceptSocket: DatagramSocket? = null

    /**
     * 初始化
     */
    init {
        val database = getDatabase()
        fileEntityDao = database.getFileEntityDao()
        fileChunkDao = database.getFileChunkEntityDao()
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
                toBeUploadFileList.addAll(fileList)
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
            heartServerSocket?.let {
                it.close()
                println("关闭心跳服务！")
            }
            acceptSocket?.apply {
                close()
                println("关闭接收服务！")
            }
        }
    }

    /**
     * 心跳线程
     */
    private fun serviceHeartbeat() {
        ioScope.launch {
            while (receiverAnimation.value) {
                var connectedIpAdd: String? = null
                try {
                    heartServerSocket = ServerSocket(HEART_BEAT_SERVER_POST)
                    heartServerSocket!!.setSoTimeout(10000)
                    val socket: Socket = heartServerSocket!!.accept()
                    socket.soTimeout = 2000
                    connectedIpAdd = socket.inetAddress.toString().replace("/", "")
                    println("被连接的ip地址是:$connectedIpAdd")
                    clineList.find { clientVo -> connectedIpAdd == clientVo.ip }?.let {
                        val index = clineList.indexOf(it)
                        clineList.removeAt(index)
                        it.connectStatus = ConnectStatus.已连接
                        clineList.add(index, it.copy())
                    }
                    val inputStream = socket.getInputStream()
                    val outputStream = socket.getOutputStream()

                    // 连接成功后就可以开始心跳
                    while (receiverAnimation.value) {
                        val pingMsgHead: MsgHead = readHead(inputStream)
                        if (MsgType.心跳 == pingMsgHead.msgType) {
                            // 向客户端发送确认消息
                            sendHead(outputStream, MsgType.心跳, 0)
                            println("接收到客户端:$connectedIpAdd 的心跳！")
                        } else if (MsgType.更新列表 == pingMsgHead.msgType) {
                            val command: Command =
                                readBody(pingMsgHead.dataLength.toInt(), Command::class.java, inputStream)
                            println("收到客户端需要${command.threadCount} 个线程上传数据,每个数据大小为${command.fileChunkSize} byte")
                            // 比对列表
                            val syncedData: TaskPo = syncData(command.taskPo)
                            command.taskPo = syncedData
                            // 异步执行文件上传
                            val portTask: PortAndTask = asyncStartDownloadFile(command)
                            // 同步数据
                            sendHeadBody(outputStream, MsgType.传输数据, portTask)
                        }
                    }
                    inputStream.close()
                    outputStream.close()
                    heartServerSocket!!.close()
                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        println("监听客户超时...")
                    } else if (e is BindException) {
                        println("心跳服务端口$ACCEPT_SERVER_POST 被占用")
                    } else {
                        println("监听客户产生了异常...${e.message} ")
                    }
                    heartServerSocket?.close()
                    println("服务端心跳异常：${e.message}")
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
        }
    }

    private fun asyncStartDownloadFile(command: Command): PortAndTask {
        val portList = mutableListOf<Int>()
        for (i in 1..command.threadCount) {
            val port: Int = DATA_UPLOAD_SERVER_POST + i
            portList.add(port)
        }
        val portTask = PortAndTask(portList, command.taskPo)
        // 开启异步下载数据线程，等等客户端链接并下载
        asyncDownloadFile(portList, command)

        return portTask
    }

    /**
     * 异步下载数据
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun asyncDownloadFile(portList: MutableList<Int>, command: Command) {
        defaultScope.launch {
            val downloadResult = mutableListOf<Deferred<Boolean>>()
            for (port in portList) {
                downloadResult.add(downFile(port, command))
            }
            downloadResult.awaitAll()
            // 合并文件
            val unfinished = downloadResult.any { deferred -> !deferred.getCompleted() }
            // 有未完成的就直接退出
            if (unfinished) {
                println("部分线程执行失败！不合并文件！")
                return@launch
            }
            // 开始合并文件
            val mergeSuccessful = mergeFile(command.taskPo.fileEntity)
            // 合并文件成功后更新状态
            if (mergeSuccessful) {
                val fileId = command.taskPo.fileEntity.fileId
                toBeDownloadFileList.find { fileItemVo -> fileItemVo.fileId == fileId }?.let {
                    val index = toBeDownloadFileList.indexOf(it)
                    toBeDownloadFileList.removeAt(index)
                    it.state = UploadState.已下载
                    downloadedFileList.add(it)
                    fileEntityDao.updateStateByFileId(fileId, UploadState.已下载);
                }
            } else {
                println("合并文件失败！")
            }
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
                targetChannel.transferFrom(sourceChannel, targetChannel.size(), sourceChannel.size())
                // 关闭源文件的通道
                sourceChannel.close()
            } catch (e: java.lang.Exception) {
                println("合并文件产生异常!${e.message}")
                return false
            }
        }
        return true
    }

    /**
     * 下载文件
     */
    private fun
            downFile(port: Int, command: Command): Deferred<Boolean> = ioScope.async {
        var serverSocket: ServerSocket? = null
        var socket: Socket? = null
        try {
            serverSocket = ServerSocket(port);
            socket = serverSocket.accept();
            val dataInputStream = socket.getInputStream();
            val dataOutputStream = socket.getOutputStream();
            while (receiverAnimation.value) {
                // 2、开始接受数据
                val msgHead: MsgHead = readHead(dataInputStream)
                if (MsgType.传输数据 != msgHead.msgType) {
                    println("传输数据不符合预期")
                    return@async false
                }
                // 接收数据对象
                val fileChunkEntity: FileChunkEntity =
                    readBody(msgHead.dataLength.toInt(), FileChunkEntity::class.java, dataInputStream)
                // 4、再次接收数据头
                val msgHead1: MsgHead = readHead(dataInputStream)
                if (MsgType.传输数据 != msgHead1.msgType) {
                    println("传输数据不符合预期")
                    return@async false
                }
                val bytes = ByteArray(msgHead1.dataLength.toInt())
                var bytesRead: Int
                var totalBytesRead = 0
                while (totalBytesRead < bytes.size) {
                    bytesRead = dataInputStream.read(bytes, totalBytesRead, bytes.size - totalBytesRead)
                    totalBytesRead += bytesRead
                }
                val fileId = command.taskPo.fileEntity.fileId
                // 保存文件
                val chunkName =
                    "${command.taskPo.fileEntity.fileName}${fileChunkEntity.fileId}${fileChunkEntity.chunkIndex}.cache"
                val cacheFile = File(cachePosition, chunkName)
                val fileOutputStream: OutputStream = FileOutputStream(File(cachePosition, chunkName))
                fileOutputStream.write(bytes)
                fileOutputStream.flush()
                fileOutputStream.close()
                if (cacheFile.exists()) {
                    // 回复传输成功
                    fileChunkEntity.uploadStatus = UploadState.已下载
                    // 更新数据库
                    fileChunkDao.updateFileChunkByFileId(
                        fileChunkEntity.fileId,
                        fileChunkEntity.chunkIndex,
                        UploadState.已下载
                    )
                    sendHead(dataOutputStream, MsgType.传输成功, 0)
                    // 更新待下载列表
                    toBeDownloadFileList.find { fileItemVo -> fileItemVo.fileId == fileId }?.let {
                        val index = toBeDownloadFileList.indexOf(it)
                        val fileChunksByFileId = fileChunkDao.getFileChunksByFileId(fileId)
                        val size = fileChunksByFileId.size
                        val downloadSize = fileChunksByFileId.stream()
                            .filter { fileChunkEntity1: FileChunkEntity -> UploadState.已下载 == fileChunkEntity1.uploadStatus }
                            .count().toInt()
                        val percent = Math.round(downloadSize.toDouble() / size.toDouble() * 100).toInt()
                        it.percent = percent
                        it.state = UploadState.下载中
                        toBeDownloadFileList.removeAt(index)
                        toBeDownloadFileList.add(index, it.copy())
                    }
                }


                // 判断上传结束信息判断是否继续上传
                val endMsgHead: MsgHead = readHead(dataInputStream)
                if (MsgType.OK == endMsgHead.msgType) {
                    receiverAnimation.value = false
                } else if (MsgType.继续上传 == endMsgHead.msgType) {
                    // 继续等待接收数据
                    println("下载数据线程:{}继续接收到数据...")
                    receiverAnimation.value = true
                } else {
                    println("下载数据线程:{}发送了不知道的异常:")
                }
            }
            dataInputStream.close()
            dataOutputStream.close()
            socket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            println("产生异常：${e.message}")
            socket?.close()
            serverSocket?.close()
            return@async false
        }
        return@async true

    }

    /**
     * 同步数据
     *
     * @param taskPo 文件task
     * @return 同步后
     */
    private suspend fun syncData(taskPo: TaskPo): TaskPo {
        val fileEntity = taskPo.fileEntity
        val clientFileChunkEntityList = taskPo.fileChunkEntityList
        // 两种大类 1、是没有这个文件，2、是有这个文件 2.1，有这个文件但是部分上传了，2.2 有这个文件全未上传
        val file: FileEntity? = fileEntityDao.getFileById(fileEntity.fileId)
        // 1、类没有
        if (file == null) {
            fileEntity.id = null
            fileEntityDao.insert(fileEntity)
            for (i in clientFileChunkEntityList.indices) {
                val fileChunkEntity = clientFileChunkEntityList[i]
                val fileChunkById = fileChunkDao.getFileChunkById(fileChunkEntity.id)
                if (fileChunkById == null) {
                    fileChunkDao.insert(fileChunkEntity)
                } else {
                    fileChunkDao.update(fileChunkById)
                }
            }
        } else {
            // 2、是有这个文件 2.1，有这个文件但是部分上传了，2.2 有这个文件全未上传
            val serviceFileChunkEntities = fileChunkDao.getFileChunksByFileId(fileEntity.fileId)
            if (serviceFileChunkEntities.isEmpty()) {
                for (i in clientFileChunkEntityList.indices) {
                    val fileChunkEntity = clientFileChunkEntityList[i]
                    val fileChunkById = fileChunkDao.getFileChunkById(fileChunkEntity.id)
                    if (fileChunkById == null) {
                        fileChunkDao.insert(fileChunkEntity)
                    } else {
                        fileChunkDao.update(fileChunkById)
                    }
                }
            } else {
                // 状态同步成服务器的
                clientFileChunkEntityList.forEach(Consumer { clientFileChunkEntity: FileChunkEntity ->
                    serviceFileChunkEntities.stream() // 将服务端文件块实体流转换为流
                        .filter { serviceFileChunkEntity: FileChunkEntity -> serviceFileChunkEntity.fileId == clientFileChunkEntity.fileId } // 过滤出文件id相同的文件块实体
                        .findAny() // 找到文件id相同的文件块实体
                        .ifPresent { fileChunkEntity12: FileChunkEntity ->
                            clientFileChunkEntity.uploadStatus = fileChunkEntity12.uploadStatus
                        }
                })
            }
        }
        // 都不需要上传
        return TaskPo(fileEntity, clientFileChunkEntityList, taskPo.chunkSize)
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
                    val responsePacket = DatagramPacket(response, response.size, packet.address, packet.port)
                    socket.send(responsePacket)
                    val ip = packet.address.toString().replace("/", "")
                    if (!clineList.any { clientVo -> clientVo.ip == ip }) {
                        clineList.add(
                            ClientVo(
                                clineList.size + 1, packet.address.toString().replace("/", ""), ConnectStatus.被发现
                            )
                        )
                    }
                    socket.disconnect()
                    socket.close()
                } catch (e: Exception) {
                    if (e is SocketTimeoutException) {
                        println("监听客户超时...")
                    } else if (e is BindException) {
                        println("接收服务端口$ACCEPT_SERVER_POST 被占用")
                        acceptSocket?.reuseAddress = true;
                        acceptSocket?.disconnect()
                    } else {
                        println("监听客户产生了异常...${e.message} ")
                    }
                    acceptSocket?.close()
                }
            }
        }
    }

    /**
     * 开始文件文件
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
        defaultScope.launch {
            // 找到之前没上传完的数据
            while (startUpload) {
                val taskPo = getTaskPo()
                if (taskPo == null) {
                    showMsg = true
                    msg = "上传结束！"
                } else {
                    // 添加到任务列表中
                    val command = Command(THREAD_COUNT, FILE_CHUNK_SIZE, taskPo)
                    commandQueue.offer(command)
                }
                // todo 上传完之后再发送
                startUpload = false
            }

        }
    }

    /**
     * 查看当前未上传或者待上传的数据
     *
     * @return 任务
     */
    private suspend fun getTaskPo(): TaskPo? {
        var taskPo: TaskPo? = null
        val uploadInFile: Optional<FileEntity> = fileEntityDao.getAllFiles().stream().filter { fileEntity ->
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
                val fileChunkEntities: List<FileChunkEntity> = generateFileChunkEntity(fileEntity, FILE_CHUNK_SIZE)
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
                    if (lastModifiedDate.before(uploadConfigItem.startDate)) {
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
                    val any = toBeUploadFileList.any { fileItemVo -> fileItemVo.fileName == file.name }
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
            var socket: Socket? = null
            while (keepConnect) {
                try {
                    socket = Socket(servicePo.ip, HEART_BEAT_SERVER_POST)
                    socket.setSoTimeout(10000)
                    updateServiceState(servicePo, ConnectButtonState.断开)
                    val inputStream = socket.getInputStream()
                    val outputStream = socket.getOutputStream()
                    // 发送心跳
                    sendHead(outputStream, MsgType.心跳, 0)
                    while (keepConnect) {
                        // 接收心跳
                        val msgHead: MsgHead = readHead(inputStream)
                        if (MsgType.心跳 == msgHead.msgType) {
                            // 收到确认消息，连接正常
                            println("接收心跳")
                        } else if (MsgType.传输数据 == msgHead.msgType) {
                            println("传输数据...")
                            val portTask: PortAndTask =
                                readBody(msgHead.dataLength.toInt(), PortAndTask::class.java, inputStream)
                            uploadData(portTask, servicePo)
                        }

                        // 如果有命令 那么就执行命令
                        if (commandQueue.isNotEmpty()) {
                            val command: Command = commandQueue.poll()
                            println("run: 当前命令为")
                            // 响应结果
                            sendHeadBody(outputStream, MsgType.更新列表, command)
                        } else {
                            // 每秒发送心跳
                            delay(1000)
                            sendHead(outputStream, MsgType.心跳, 0)
                        }
                    }
                } catch (e: Exception) {
                    println("链接失败${e}")
                    updateServiceState(servicePo, ConnectButtonState.连接)
                    keepConnect = false
                }
            }
            // 退出循环后关闭链接
            socket?.close()
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
     * 上传数据
     */
    private fun uploadData(portTask: PortAndTask, servicePo: ServicePo) {
        val portList: List<Int> = portTask.portList
        val taskPo: TaskPo = portTask.taskPo
        // 根据端口号的数量分配线程去发送数据
        val fileChunkEntityList = taskPo.fileChunkEntityList
        val eachGroupCount = floor(fileChunkEntityList.size.toDouble() / portList.size).toInt()
        val serviceTaskMap = HashMap<ServiceInfo, TaskPo>()
        for (i in portList.indices) {
            val integer = portList[i]
            val fromIndex = i * eachGroupCount
            var toIndex = (i + 1) * eachGroupCount
            if (i == portList.size - 1) {
                toIndex = fileChunkEntityList.size
            }
            val fileChunkEntities = fileChunkEntityList.subList(fromIndex, toIndex)
            val currTaskPo = TaskPo(taskPo.fileEntity, fileChunkEntities, taskPo.chunkSize)
            val serviceInfo = ServiceInfo(servicePo.ip, integer)
            serviceTaskMap[serviceInfo] = currTaskPo
        }
        val results = mutableListOf<Deferred<Boolean>>()
        // 异步执行
        ioScope.launch {
            for (mutableEntry in serviceTaskMap) {
                val result = syncUploadFile(mutableEntry.key, mutableEntry.value)
                results.add(result)
            }
            results.awaitAll()
            println("所有文件发送完毕！！！")
        }

    }

    /**
     * 异步发送数据
     */
    private fun syncUploadFile(serviceInfo: ServiceInfo, taskPo: TaskPo): Deferred<Boolean> = ioScope.async {
        try {
            // 执行异步上传逻辑成功返回true
            val dataSocket = Socket(serviceInfo.ip, serviceInfo.port)
            val outputStream: OutputStream = dataSocket.getOutputStream()
            val inputStream: InputStream = dataSocket.getInputStream()
            val fileChunkEntities: List<FileChunkEntity> =
                taskPo.fileChunkEntityList.stream().filter { fileChunkEntity ->
                    UploadState.已上传 != fileChunkEntity.uploadStatus
                }.collect(Collectors.toList())
            val file = File(taskPo.fileEntity.fileFullName)
            val fileInputStream = FileInputStream(file)

            for ((i, fileChunkEntity) in fileChunkEntities.withIndex()) {
                if (!keepConnect) {
                    return@async false
                }
                // 1、发送我要传输数据了
                sendHeadBody(outputStream, MsgType.传输数据, fileChunkEntity);
                val jump: Long = fileChunkEntity.chunkIndex.toLong() * taskPo.fileEntity.chunkSize

                val skip = fileInputStream.skip(jump)
                if (skip != jump) {
                    println("跳转失败!文件块" + fileChunkEntity.chunkIndex)
                    return@async false
                }

                val chunkSize = fileChunkEntity.chunkSize.toInt()
                val bodyData = ByteArray(chunkSize)
                val o = fileInputStream.read(bodyData)
                if (o != chunkSize) {
                    println("文件读取失败!文件块" + fileChunkEntity.chunkIndex.toString() + "要读取的文件大小是:" + chunkSize + "实际读取的文件块是:" + o)
                    return@async false
                }
                // 3、再次发送传输数据
                sendHead(outputStream, MsgType.传输数据, bodyData.size.toLong())
                outputStream.write(bodyData)
                outputStream.flush()
                // 发送完毕 接收
                val msgHead1: MsgHead = readHead(inputStream)
                if (msgHead1.msgType == MsgType.传输成功) {
                    fileChunkEntity.uploadStatus = UploadState.已上传
                    fileChunkDao.updateFileChunkByFileId(
                        fileChunkEntity.fileId, fileChunkEntity.chunkIndex, UploadState.已上传
                    )
                    // 更新进度
                    toBeUploadFileList.find { it.fileId == fileChunkEntity.fileId }?.let {
                        val index = toBeUploadFileList.indexOf(it)
                        val chunkEntities: List<FileChunkEntity> =
                            fileChunkDao.getFileChunksByFileId(taskPo.fileEntity.fileId)
                        val count = chunkEntities.stream().filter { fileChunk: FileChunkEntity ->
                            UploadState.已上传 == fileChunk.uploadStatus
                        }.count()
                        val percent = floor(count.toDouble() / chunkEntities.size.toDouble() * 100).toInt()
                        it.percent = percent
                        toBeUploadFileList.removeAt(index)
                        toBeUploadFileList.add(index, it.copy())
                        fileChunkDao.delete(fileChunkEntity);
                    }
                } else {
                    println("run: 文件块上传失败!" + fileChunkEntity.fileId + "|" + fileChunkEntity.chunkIndex)
                    return@async false
                }
                if (i == fileChunkEntities.size - 1) {
                    sendHead(outputStream, MsgType.OK, 0)
                    println("run: 数据已上传结束")
                } else {
                    sendHead(outputStream, MsgType.继续上传, 0)
                }
            }


        } catch (e: Exception) {
            println("产生异常！$e")
            return@async false
        }
        return@async true
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