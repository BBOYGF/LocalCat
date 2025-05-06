package com.felinetech.fast_file

import com.felinetech.fast_file.enums.ReleaseType

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
     * 请求支付宝 Install
     */
    val AILPAY_STRING = "alipays://platformapi/startapp?saId=10000007&qrcode="

    val WECHATPAY_STRING = "weixin://dl/business/?appid=wx128a8920d271e734&qrcode="

    private val SUB_URL = "/alipay/buyMember?buyType="


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
    var productHome = "https://felinetech.cn/#/"

    /**
     * 桌面端下载
     */
    var desktopDownload = "https://felinetech.cn/#/p?id=1"

    /**
     * 问题反馈
     */
    var feedback = "https://felinetech.cn/#/p?id=1"

    /**
     * 客户端扫描服务器时用的端口
     */
    val BROADCAST_PORT = 8200

    /**
     * 文件保存路径
     */
    val SAVE_FILE = "save_file"

    val CACHE_FILE = "cache_file"

    /**
     * UDP 服务接口
     */
    const val ACCEPT_SERVER_POST = 8200

    /**
     * 心跳端口号
     */
    const val HEART_BEAT_SERVER_POST: Int = 8201

    /**
     * 数据上传线程
     */
    const val DATA_UPLOAD_SERVER_POST: Int = 8022

    /**
     * 每个文件块大小
     */
    val FILE_CHUNK_SIZE = 1024 * 1024L

    val THREAD_COUNT = 1

    const val BASE_URI = "https://felinetech.cn:81"

    // 发版
    val releaseType: ReleaseType = ReleaseType.GooglePlay

    const val CHANNEL_ID = "LocalCat消息通道"
    const val NOTIFICATION_ID = 101
}