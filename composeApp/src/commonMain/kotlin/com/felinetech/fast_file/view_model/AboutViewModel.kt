package com.felinetech.fast_file.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import com.felinetech.fast_file.Constants.AILPAY_STRING
import com.felinetech.fast_file.Constants.BASE_URI
import com.felinetech.fast_file.Constants.releaseType
import com.felinetech.fast_file.enums.PayTypes
import com.felinetech.fast_file.enums.ReleaseType
import com.felinetech.fast_file.pojo.PayItem
import com.felinetech.fast_file.pojo.RespBean
import com.felinetech.fast_file.utlis.googlePay
import com.felinetech.fast_file.utlis.startOtherAPP
import com.felinetech.fast_file.view_model.MainViewModel.msgPair
import com.felinetech.fast_file.view_model.MainViewModel.userID
import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import localcat.composeapp.generated.resources.Alipay
import localcat.composeapp.generated.resources.ApplePay
import localcat.composeapp.generated.resources.GooglePay
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.WechatPay
import java.util.Random

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


    // Local
    val logger = Logger.withTag("SharedClass")

    private val gson = Gson()

    private val client = HttpClient {
        install(ContentNegotiation) {
            Gson()
        }
    }

    init {
        if (ReleaseType.GooglePlay == releaseType) {
            payTypeItemList.add(
                PayItem(
                    PayTypes.GooglePlay,
                    "Google Pay",
                    mutableStateOf(false),
                    Res.drawable.GooglePay
                )
            )
        } else if (ReleaseType.Android == releaseType) {
            payTypeItemList.add(
                PayItem(
                    PayTypes.支付宝,
                    "支付宝支付",
                    mutableStateOf(false),
                    Res.drawable.Alipay
                )
            )
        } else if (ReleaseType.Win == releaseType) {
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
                    PayTypes.微信支付,
                    "微信支付",
                    mutableStateOf(false),
                    Res.drawable.WechatPay
                )
            )
        } else if (ReleaseType.Mac == releaseType) {
            payTypeItemList.add(
                PayItem(
                    PayTypes.支付宝,
                    "支付宝支付",
                    mutableStateOf(false),
                    Res.drawable.Alipay
                )
            )
        } else {
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
                    logger.i("patID:$payID")
                    val (result, msg) = aliPay(payID, 9.0)
                    if (result) {
                        // 如果是桌面显示支付二维码
                        println("二维码是：$msg")
                        waitingDialog = false
                        qrUrl = msg
                        payMsg = "请用支付宝扫码！"
                        showQsDialog = true
                        // 如果是Android 调用支付宝
                        startOtherAPP(AILPAY_STRING + qrUrl)
                        // 异步请求判断是否已支付
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
                                logger.i("响应字符串是：$bodyAsText")
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
                                        logger.i("已支付成功！")
                                        break
                                    }
                                    if ("交易超时" == repBean.message) {
                                        payMsg = "交易超时"
                                        delay(1000)
                                        showQsDialog = false
                                        logger.i("交易超时！")
                                        break
                                    }
                                    if ("交易结束" == repBean.message) {
                                        payMsg = "交易结束"
                                        delay(1000)
                                        showQsDialog = false
                                        logger.i("交易超时！")
                                        break
                                    }

                                }
                            } catch (e: Exception) {
                                logger.i("查询订单状态异常", e)
                            }
                        }

                    } else {
                        println("支付失败！$msg")
                        waitingDialog = false
                        // todo 异常消息 Dialog
                        MainViewModel.showDialog = true
                        msgPair = Pair("提示", msg)
                    }

                }
            } else if (payItem.type == PayTypes.GooglePlay) {
                googlePay()
            } else if (payItem.type == PayTypes.微信支付) {
                ioScope.launch {
                    var payID = userID + "_" + Random().nextInt(1000).toString()
                    // 支付订单号不能打印32位
                    val length: Int = payID.length
                    if (length > 32) {
                        logger.i("订单号：$payID 大于32位自动截断")
                        val i = length - 32
                        payID = payID.substring(i, length)
                    }
                    logger.i("patID:$payID")
                    val (result, msg) = weChatPay(payID, 0.1)
                    if (result) {
                        // 如果是桌面显示支付二维码
                        println("二维码是：$msg")
                        waitingDialog = false
                        qrUrl = msg
                        payMsg = "请用微信扫码！"
                        showQsDialog = true
                        // 如果是Android 调用支付宝
//                        startOtherAPP(WECHATPAY_STRING + qrUrl)
                        // 异步请求判断是否已支付
                        for (i in 0..30) {
                            if (!showQsDialog) {
                                break
                            }
                            try {
                                delay(2000)
                                val response = client.get("$BASE_URI/wechatpay/queryOrder") {
                                    // 设置查询参数
                                    parameter("payOrderId", payID)
                                    // 设置请求头
                                    accept(ContentType.Application.Json)
                                }
                                val bodyAsText = response.bodyAsText(Charsets.UTF_8)
                                logger.i("响应字符串是：$bodyAsText")
                                val repBean: RespBean =
                                    gson.fromJson(bodyAsText, RespBean::class.java)
                                if (200L == repBean.code) {
                                    if ("用户支付" == repBean.message) {
                                        payMsg = "正在支付..."
                                    }
                                    if ("支付成功" == repBean.message) {
                                        payMsg = "支付已完成！"
                                        delay(1000)
                                        showQsDialog = false
                                        logger.i("已支付成功！")
                                        break
                                    }
                                    if ("订单关闭" == repBean.message) {
                                        payMsg = "交易超时"
                                        delay(1000)
                                        showQsDialog = false
                                        logger.i("交易超时！")
                                        break
                                    }
                                    if ("订单取消" == repBean.message) {
                                        payMsg = "交易结束"
                                        delay(1000)
                                        showQsDialog = false
                                        logger.i("交易超时！")
                                        break
                                    }

                                }
                            } catch (e: Exception) {
                                logger.i("查询订单状态异常", e)
                            }
                        }

                    } else {
                        println("支付失败！$msg")
                        waitingDialog = false
                        // todo 异常消息 Dialog
                        MainViewModel.showDialog = true
                        msgPair = Pair("提示", msg)
                    }
                }
            }
        }
    }

    /**
     * 阿里支付
     */
    suspend fun aliPay(
        userID: String,
        amount: Double
    ): Pair<Boolean, String> {
        val response = client.get("$BASE_URI/alipay/payRewardQR") {
            // 设置查询参数
            parameter("userID", userID)
            // 设置请求头
            accept(ContentType.Application.Json)
        }
        val content = response.bodyAsText()
        return if (content.isEmpty()) {
            Pair(false, "支付失败！")
        } else {
            if (content.startsWith("https:")) {
                Pair(true, content)
            } else {
                Pair(false, content)
            }
        }
    }

    /**
     * 阿里支付
     */
    suspend fun weChatPay(
        userID: String,
        amount: Double
    ): Pair<Boolean, String> {
        val response = client.get("$BASE_URI/wechatpay/payRewardQR") {
            // 设置查询参数
            parameter("userID", userID)
            // 设置请求头
            accept(ContentType.Application.Json)
        }
        val content = response.bodyAsText()
        return if (content.isEmpty()) {
            Pair(false, "支付失败！")
        } else {
            if (content.startsWith("weixin:")) {
                Pair(true, content)
            } else {
                Pair(false, content)
            }
        }
    }


}