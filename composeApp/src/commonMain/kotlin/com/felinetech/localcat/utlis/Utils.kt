package com.felinetech.localcat.utlis

import com.felinetech.localcat.database.Database
import com.felinetech.localcat.enums.FileType
import com.felinetech.localcat.po.FileEntity
import com.felinetech.localcat.pojo.FileItemVo
import java.io.File
import java.net.InetAddress
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

expect fun getLocalIp(): String

expect fun getSubnetMask(): String


expect fun getDatabase(): Database

expect fun getFileByDialog(): File?


/**
 * 获取广播位
 */
fun getBroadcastAddress(ipAddress: String, subnetMask: String): String {
    // 将 IP 地址和子网掩码转换为字节数组
    val ipBytes = InetAddress.getByName(ipAddress).address
    val maskBytes = InetAddress.getByName(subnetMask).address

    // 计算网络地址
    val networkBytes = ByteArray(ipBytes.size)
    for (i in ipBytes.indices) {
        networkBytes[i] = (ipBytes[i] and maskBytes[i])
    }

    // 计算广播地址
    val broadcastBytes = ByteArray(ipBytes.size)
    for (i in ipBytes.indices) {
        broadcastBytes[i] = (networkBytes[i] or (maskBytes[i].inv() and 0xFF.toByte()))
    }

    // 返回广播地址的字符串表示
    return InetAddress.getByAddress(broadcastBytes).hostAddress
}


/**
 * 获取文件类型
 */
fun getFileType(fileName: String): FileType {
    return FileType.entries.first { fileName.lowercase().endsWith(it.suffix) }
}

fun filePoToFileVo(it: FileEntity) =
    FileItemVo(it.fileId, getFileType(it.fileName), it.fileName, it.uploadState, 0, it.fileSize, fileFillName = it.fileFullName)
