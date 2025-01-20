package com.felinetech.localcat.view_model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.felinetech.localcat.enums.PayTypes
import com.felinetech.localcat.pojo.PayItem
import com.felinetech.localcat.utlis.aliPay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import localcat.composeapp.generated.resources.*

object AboutViewModel {
    /**
     * 支付类型
     */
    val payTypeItemList = mutableStateListOf<PayItem>()

    val showQsDialog = mutableStateOf(false)

    init {
        payTypeItemList.add(PayItem(PayTypes.微信支付, "微信支付", mutableStateOf(false), Res.drawable.WechatPay))
        payTypeItemList.add(PayItem(PayTypes.支付宝, "支付宝支付", mutableStateOf(false), Res.drawable.Alipay))
        payTypeItemList.add(PayItem(PayTypes.GooglePlay, "Google Pay", mutableStateOf(false), Res.drawable.GooglePay))
        payTypeItemList.add(PayItem(PayTypes.ApplePay, "Apple Pay", mutableStateOf(false), Res.drawable.ApplePay))

    }

    /**
     * 支付
     */
    fun pay() {
        val ioScope = CoroutineScope(Dispatchers.IO)
        val payItem = payTypeItemList.firstOrNull { payItem -> payItem.selected.value }
        if (payItem != null) {
            if (payItem.type == PayTypes.支付宝) {
                ioScope.launch {
                    aliPay("测试支付1", 9.0) { result, msg ->
                        if (result) {
                            // 如果是桌面显示支付二维码
                            println("二维码是：$msg")
                            // 如果是Android 调用insert
                        } else {
                            println("支付失败！")
                        }
                    }
                }
            }
        }
    }

}