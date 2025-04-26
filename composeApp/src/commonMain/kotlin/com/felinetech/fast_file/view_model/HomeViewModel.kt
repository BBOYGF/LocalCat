package com.felinetech.fast_file.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.fast_file.Constants.BROADCAST_PORT
import com.felinetech.fast_file.Constants.FILE_CHUNK_SIZE
import com.felinetech.fast_file.dao.FileChunkDao
import com.felinetech.fast_file.dao.FileEntityDao
import com.felinetech.fast_file.enums.*
import com.felinetech.fast_file.interfaces.DataService
import com.felinetech.fast_file.interfaces.KeepConnectService
import com.felinetech.fast_file.interfaces.ReceiverService
import com.felinetech.fast_file.interfaces.UploadService
import com.felinetech.fast_file.po.FileChunkEntity
import com.felinetech.fast_file.po.FileEntity
import com.felinetech.fast_file.pojo.ClientVo
import com.felinetech.fast_file.pojo.FileItemVo
import com.felinetech.fast_file.pojo.ServicePo
import com.felinetech.fast_file.pojo.TaskPo
import com.felinetech.fast_file.utlis.*
import com.felinetech.fast_file.view_model.HistoryViewModel.downloadedFileList
import com.felinetech.fast_file.view_model.HistoryViewModel.uploadedFileList
import com.felinetech.fast_file.view_model.SettingViewModel.ruleList
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
import org.slf4j.Logger
import java.io.IOException
import java.net.*
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


    /**
     * 开始上传文件
     */
    var startUpload by mutableStateOf(false)

    /**

     * 显示异常信息F
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
    val receiverAnimation = mutableStateOf(false)

    /**
     * 默认协程
     */
    private val defaultScope = CoroutineScope(Dispatchers.Default)


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


    private var logger: Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    /**
     * 请求客户端
     */
    private var client: HttpClient

    private var receiverService: ReceiverService? = null
    private var dataService: DataService? = null
    private var keepConnectService: KeepConnectService? = null
    private var uploadService: UploadService? = null

    /**
     * 当前链接的IP地址
     */
    var connectedIpAdd: String? = null

    /**
     * 保持链接
     */
    var keepConnect = false

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
        initReceiverService()
        initDataService()
        initKeepConnectService()
        initUploadService()
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
            if (receiverService == null) {
                receiverService = getReceiverService()
            }
            receiverService?.startReceiver()

            if (dataService == null) {
                dataService = getDataService()
            }
            dataService?.startDataService()

        } else {
            // 关闭接收
            receiverButtonTitle.value = ServiceButtonState.开始接收.name
            receiverAnimation.value = false
            clineList.clear()
            receiverService?.stopReceiver()
            dataService?.stopDataService()

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
            msg = getNames(Locale.getDefault().language).pleaseConnectTheReceiverFirst
            return
        }
        if (uploadService == null) {
            uploadService = getUploadService()
        }
        // 启动上传服务
        uploadService?.startUpload()
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
        uploadService?.stopUpload()
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
        uploadedFileList.clear()
        downloadedFileList.clear()
        toBeDownloadFileList.clear()
        ioScope.launch {
            fileEntityDao.deleteAll()
        }
    }

    /**
     * 链接数据源
     */
    fun startClientHeartbeat(servicePo: ServicePo) {
        println("链接服务器$servicePo")

        if (keepConnectService == null) {
            keepConnectService = getKeepConnectService()
        }
        keepConnectService?.stareKeepConnect(servicePo)
    }

    /**
     * 断开链接
     */
    fun closeDataSources(servicePo: ServicePo) {
        keepConnectService?.stopConnect(servicePo)
    }


    /**
     * 更新Button状态
     */
    public fun updateServiceState(servicePo: ServicePo, buttonState: ConnectButtonState) {
        serviceList.find { it == servicePo }.let {
            val index = serviceList.indexOf(servicePo)
            serviceList.removeAt(index)
            servicePo.buttonState = buttonState
            serviceList.add(index, servicePo.copy())
        }
    }
}