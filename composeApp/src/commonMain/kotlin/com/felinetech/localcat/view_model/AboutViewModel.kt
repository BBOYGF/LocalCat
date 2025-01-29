package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.Constants.BASE_URI
import com.felinetech.localcat.enums.PayTypes
import com.felinetech.localcat.pojo.PayItem
import com.felinetech.localcat.pojo.RespBean
import com.felinetech.localcat.utlis.googlePay
import com.felinetech.localcat.utlis.startOtherAPP
import com.felinetech.localcat.view_model.MainViewModel.showDialog
import com.felinetech.localcat.view_model.MainViewModel.userID
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import localcat.composeapp.generated.resources.*
import org.slf4j.Logger
import java.util.*

object AboutViewModel {
    /**
     * 支付类型
     */
    val payTypeItemList = mutableStateListOf<PayItem>()

    /**
     * 显示支付框
     */
    var showQsDialog by mutableStateOf(false)

    /**
     * 等待
     */
    var waitingDialog by mutableStateOf(false)

    /**
     * 支付信息
     */
    var payMsg by mutableStateOf("请支付宝扫码！")

    /**
     * 二维码
     */
    var qrUrl: String = ""

    private val ioScope = CoroutineScope(Dispatchers.IO)


    private var logger: Logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    private val gson = Gson()

    private val client = HttpClient {
        install(ContentNegotiation) {
            Gson()
        }
    }

    init {
        payTypeItemList.add(
            PayItem(
                PayTypes.微信支付,
                "微信支付",
                mutableStateOf(false),
                Res.drawable.WechatPay
            )
        )
        payTypeItemList.add(
            PayItem(
                PayTypes.支付宝,
                "支付宝支付",
                mutableStateOf(false),
                Res.drawable.Alipay
            )
        )
        payTypeItemList.add(
            PayItem(
                PayTypes.GooglePlay,
                "Google Pay",
                mutableStateOf(false),
                Res.drawable.GooglePay
            )
        )
        payTypeItemList.add(
            PayItem(
                PayTypes.ApplePay,
                "Apple Pay",
                mutableStateOf(false),
                Res.drawable.ApplePay
            )
        )


    }

    /**
     * 支付
     */
    fun pay() {
        val ioScope = CoroutineScope(Dispatchers.IO)
        val payItem = payTypeItemList.firstOrNull { payItem -> payItem.selected.value }
        if (payItem != null) {
            waitingDialog = true
            if (payItem.type == PayTypes.支付宝) {
                ioScope.launch {
                    val payID = userID + ":" + Random().nextInt(1000).toString()
                    logger.info("patID:{}", payID)
                    aliPay(payID, 9.0) { result, msg ->
                        if (result) {
                            // 如果是桌面显示支付二维码
                            println("二维码是：$msg")
                            waitingDialog = false
                            qrUrl = msg
                            payMsg = "请用支付宝扫码！"
                            showQsDialog = true
                            // 如果是Android 调用支付宝
                            startOtherAPP(qrUrl)
                            // 异步请求判断是否已支付
                            ioScope.launch {
                                for (i in 0..30) {
                                    if (!showQsDialog) {
                                        break
                                    }
                                    try {
                                        delay(2000)
                                        val response = client.get("$BASE_URI/alipay/queryOrder") {
                                            // 设置查询参数
                                            parameter("orderId", payID)
                                            // 设置请求头
                                            accept(ContentType.Application.Json)
                                        }
                                        val bodyAsText = response.bodyAsText(Charsets.UTF_8)
                                        logger.info("响应字符串是：{}", bodyAsText)
                                        val repBean: RespBean =
                                            gson.fromJson(bodyAsText, RespBean::class.java)
                                        if (200L == repBean.code) {
                                            if ("等待支付" == repBean.message) {
                                                payMsg = "正在支付..."
                                            }
                                            if ("支付成功" == repBean.message) {
                                                payMsg = "支付已完成！"
                                                delay(1000)
                                                showQsDialog = false
                                                logger.info("已支付成功！")
                                                break
                                            }
                                            if ("交易超时" == repBean.message) {
                                                payMsg = "交易超时"
                                                delay(1000)
                                                showQsDialog = false
                                                logger.info("交易超时！")
                                                break
                                            }
                                            if ("交易结束" == repBean.message) {
                                                payMsg = "交易结束"
                                                delay(1000)
                                                showQsDialog = false
                                                logger.info("交易超时！")
                                                break
                                            }

                                        }
                                    } catch (e: Exception) {
                                        logger.info("查询订单状态异常", e)
                                    }
                                }
                            }
                        } else {
                            println("支付失败！$msg")
                            waitingDialog = false
                            // todo 异常消息 Dialog
                        }
                    }
                }
            } else if (payItem.type == PayTypes.GooglePlay) {
                googlePay()
            }
        }
    }

    /**
     * 阿里支付
     */
    suspend fun aliPay(
        userID: String,
        amount: Double,
        callback: (result: Boolean, msh: String) -> Unit
    ) {
        val job = ioScope.launch {
//            val response = client.get("$BASE_URI/alipay/payRewardQR") {
//                // 设置查询参数
//                parameter("userID", userID)
//                // 设置请求头
//                accept(ContentType.Application.Json)
//            }
//            val content = response.bodyAsText()
//            if (content.isEmpty()) {
//                callback(false, "支付失败！")
//            } else {
//                if (content.startsWith("https:")) {
//                    callback(true, content)
//                } else {
//                    callback(false, content)
//                }
//            }
            delay(1000)
            callback(true, "/alipay/payRewardQR")
        }
        job.join()
    }

}