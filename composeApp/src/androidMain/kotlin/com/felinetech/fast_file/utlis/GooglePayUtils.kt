package com.felinetech.fast_file.utlis

import co.touchlab.kermit.Logger
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.felinetech.fast_file.Constants.product1year
import com.felinetech.fast_file.MainActivity.Companion.mainActivity
import kotlinx.coroutines.suspendCancellableCoroutine

import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * 谷歌支付工具类
 */
class GooglePayUtils {

    /**
     * 日志类
     */
    val logger = Logger.withTag("SharedClass")

    var billingClient: BillingClient? = null

    /**
     * 购买成功监听器
     */
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        // To be implemented in a later section.
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                // 消耗产品
                handlePurchase(purchase)
                logger.i("getPurchaseToken: " + purchase.purchaseToken)
                logger.i("getSignature: " + purchase.signature)
                logger.i("getSignature: " + purchase.signature)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // 用户取消支付
            logger.i("用户取消操作！")
        } else {
            logger.i("其他事件")
        }
    }

    /**
     * 消耗产品
     *
     * @param purchase 购买的产品
     */
    private fun handlePurchase(purchase: Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        val listener =
            ConsumeResponseListener { billingResult: BillingResult, purchaseToken: String? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // todo 去获取 Licence 注册
                    val orderId = purchase.orderId
                    val products = purchase.products
                    var payType = ""
                    payType = if (products.contains(product1year)) {
                        "1年"
                    } else {
                        "3年"
                    }
//                getLicenceByGoogle(orderId, purchaseToken, payType)
                    // 1、先去创建支付订单
                    // 2、将订单标记为已支付
                    // 3、生成对于的 Licence
                    println("getPurchaseToken: " + purchase.purchaseToken)
                    println("getSignature: " + purchase.signature)
                    println("getSignature: " + purchase.signature)
                }
            }
        billingClient?.consumeAsync(consumeParams, listener)
    }

    /**
     * 1、初始化Google Pay
     */
    private suspend fun connectGooglePlay(): BillingClient = suspendCoroutine { continuation ->
        val billingClient: BillingClient = BillingClient.newBuilder(mainActivity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases() // 关键修复点
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    // 连接成功 回调
                    logger.i("连接Google Play 成功！")
                    continuation.resume(billingClient)
                } else {
                    continuation.resumeWithException(Exception("连接失败！" + billingResult.debugMessage))
                }
            }

            override fun onBillingServiceDisconnected() {
                continuation.resumeWithException(Exception("关闭连接！"))
                // 连接端口后回调
                logger.i("连接Google Play 失败！")
            }
        })
    }

    /**
     * 封装商品查询为挂起函数 1_fast_cat
     */
    private suspend fun queryProductDetails(
        billingClient: BillingClient,
        productID: String
    ): ProductDetails =
        suspendCoroutine { continuation ->
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            billingClient.queryProductDetailsAsync(params) { _, productDetailsList ->
                if (productDetailsList.isEmpty()) {
                    continuation.resumeWithException(Exception("没有找到匹配的产品！"))
                } else {
                    continuation.resume(productDetailsList[0])
                }
            }
        }

    /**
     * 调用支付
     */
    private suspend fun launchBillingFlowAndWait(
        billingClient: BillingClient,
        productDetails: ProductDetails
    ): Purchase? = suspendCancellableCoroutine { continuation ->
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        val billingResult = billingClient.launchBillingFlow(mainActivity, params)

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            continuation.resumeWithException(Exception("Launch billing failed: ${billingResult.debugMessage}"))
            return@suspendCancellableCoroutine
        }
    }

    /**
     * 支付
     */
    suspend fun pay(productID: String): Pair<Boolean, String> {
        try {
            billingClient = connectGooglePlay()
            billingClient?.let {
                val queryProductDetails = queryProductDetails(it, productID)
                launchBillingFlowAndWait(it, queryProductDetails)
            }
            return Pair(true, "ok")
        } catch (e: Exception) {
            println("google支付产生异常：${e.message}" + e.toString())
            logger.e("谷歌支付异常", e)
            return Pair(false, "${e.message}")
        }
    }

}