package com.felinetech.localcat.utlis

import java.net.InetAddress
import java.net.NetworkInterface

actual fun getLocalIp(): String {
    return try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val iface = interfaces.nextElement()
            val addresses = iface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                // 过滤掉环回地址和IPv6地址
                if (!address.isLoopbackAddress && address is InetAddress && address.address.size == 4) {
                    return address.hostAddress // 返回IPv4地址
                }
            }
        }
        return ""
    } catch (e: Exception) {
        println("产生异常 ${e.message}")
        return "异常"
    }
}