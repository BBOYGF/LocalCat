package com.felinetech.localcat.pojo

data class IpInfo(
    /**
     * ip地址
     */
    var ip: String,
    /**
     * 子网掩码
     */
    var subnetMask: String,
    /**
     * 网络名称
     */
    var netName: String
)
