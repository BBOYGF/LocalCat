package com.felinetech.fast_file.services

import co.touchlab.kermit.Logger
import com.felinetech.fast_file.Constants.HEART_BEAT_SERVER_POST
import com.felinetech.fast_file.enums.ConnectButtonState
import com.felinetech.fast_file.interfaces.KeepConnectService
import com.felinetech.fast_file.pojo.ServicePo
import com.felinetech.fast_file.view_model.HomeViewModel
import com.felinetech.fast_file.view_model.HomeViewModel.connectedIpAdd
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

class DesktopKeepConnectService : KeepConnectService {

    /**
     * io协程
     */
    private val ioScope = CoroutineScope(Dispatchers.IO)

    /**
     * 请求客户端
     */
    private var client: HttpClient

    private var logger: Logger = Logger.withTag("AndroidKeepConnectService")

    /**
     * 保持链接
     */
    private var keepConnect = false
    init {
        client = HttpClient() {
            install(ContentNegotiation) {
                gson()
            }
            install(HttpTimeout)
        }
    }

    override fun stareKeepConnect(servicePo: ServicePo) {
        ioScope.launch {
            client.get("http://${servicePo.ip}:$HEART_BEAT_SERVER_POST/login")
            updateServiceState(servicePo, ConnectButtonState.断开)
            connectedIpAdd = servicePo.ip
            HomeViewModel.keepConnect = true
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

}