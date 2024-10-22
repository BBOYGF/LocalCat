package com.felinetech.localcat.view_model

import com.felinetech.localcat.enums.ConnectButtonState
import com.felinetech.localcat.enums.ConnectStatus
import com.felinetech.localcat.enums.FileType
import com.felinetech.localcat.enums.ServiceButtonState
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.pojo.ClientVo
import com.felinetech.localcat.pojo.FileItemVo
import com.felinetech.localcat.pojo.ServicePo
import com.felinetech.localcat.utlis.LocalNames
import com.felinetech.localcat.utlis.loadLocalization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
    val receiverButtonTitle = MutableStateFlow("开始接收")
    val receiverAnimation = MutableStateFlow(false)

    /**
     * 默认协程
     */
    private val defaultScope = CoroutineScope(Dispatchers.Default)

    /**
     * 开始接收按钮被点击
     */
    fun clickReceiverButton() {
        if (ServiceButtonState.开始接收.name == receiverButtonTitle.value) {
            receiverButtonTitle.value = ServiceButtonState.关闭接收.name
            receiverAnimation.value = true
            clickService()
            receiverClick()
        } else {
            receiverButtonTitle.value = ServiceButtonState.开始接收.name
            receiverAnimation.value = false

        }
    }


    private fun clickService() {
        val item = list[0]
        item.percent = (item.percent + 1)
        list[0] = item.copy()
        val newList = mutableListOf<FileItemVo>()
        list.forEach {
            newList.add(it)
        }
        scanFileList.value = newList
    }


    private fun receiverClick() {
        val list = clineList.value.toMutableList()
        list.add(ClientVo(1, "192.168.1.1", ConnectStatus.被发现))
        clineList.value = list
    }

    fun senderClick() {
        val list = serviceList.value.toMutableList()
        list.add(ServicePo(1, "192.168.1.1", ConnectStatus.未连接, ConnectButtonState.连接))
        serviceList.value = list
        var localNames: LocalNames = loadLocalization("" )

        println("结果是：${localNames.aboutAppOtherText}")

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