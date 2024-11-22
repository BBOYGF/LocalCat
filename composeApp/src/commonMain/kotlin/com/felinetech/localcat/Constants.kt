package com.felinetech.localcat

object Constants {
    /**
     * 基础域名
     */
    val BASE_URL = "https://felinetech.cn:81"

    /**
     * 设备ID
     */
    val DEVICE_ID = "DeviceID"

    /**
     * 隐私权限
     */
    val PRIVACY = "Privacy_Permissions"

    /**
     * 访问的网站
     */
    val PRIVACY_URL = "https://felinetech.cn/#/privacy"

    /**
     * 请求支付宝Install
     */
    val AILPAY_STRING = "alipays://platformapi/startapp?saId=10000007&qrcode="

    private val SUB_URL = "/alipay/buyMember?buyType="

    /**
     * Google Play 是否是 google支付
     */
    val GOOGLE_PLAY = true

    /**
     * 产品3年定价
     */
    var product3year = "1_fast_cat"

    /**
     * 产品1年定价
     */
    var product1year = "2_fast_cat"

    /**
     * 产品主页
     */
    var productHume = "https://felinetech.cn/#/"

    /**
     * 桌面端下载
     */
    var desktopDownload = "https://felinetech.cn/#/"

    /**
     * 问题反馈
     */
    var feedback = "https://felinetech.cn/#/"

    /**
     * 客户端扫描服务器时用的端口
     */
    val BROADCAST_PORT = 8200

    /**
     * 文件保存路径
     */
    val SAVE_FILE = "save_file"

    val CACHE_FILE = "cache_file"
}