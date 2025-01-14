package com.felinetech.localcat.view_model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.felinetech.localcat.enums.PayTypes
import com.felinetech.localcat.pojo.PayItem
import localcat.composeapp.generated.resources.*
import localcat.composeapp.generated.resources.Alipay
import localcat.composeapp.generated.resources.GooglePay
import localcat.composeapp.generated.resources.Res
import localcat.composeapp.generated.resources.WechatPay

object AboutViewModel {
    /**
     * 支付类型
     */
    val payTypeItemList = mutableStateListOf<PayItem>()


    init {
        payTypeItemList.add(PayItem(PayTypes.微信支付, "微信支付", mutableStateOf(false), Res.drawable.WechatPay))
        payTypeItemList.add(PayItem(PayTypes.支付宝, "支付宝支付", mutableStateOf(false), Res.drawable.Alipay))
        payTypeItemList.add(PayItem(PayTypes.GooglePlay, "Google Pay", mutableStateOf(false), Res.drawable.GooglePay))
        payTypeItemList.add(PayItem(PayTypes.ApplePay, "Apple Pay", mutableStateOf(false), Res.drawable.ApplePay))
    }

}