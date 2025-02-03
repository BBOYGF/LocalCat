package com.felinetech.fast_file.utlis

import com.felinetech.fast_file.database.Database
import com.felinetech.fast_file.enums.FileType
import com.felinetech.fast_file.po.FileEntity
import com.felinetech.fast_file.pojo.FileItemVo
import com.felinetech.fast_file.pojo.IpInfo
import java.io.File
import java.net.InetAddress
import java.util.Date
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or


expect fun getIpInfo(): IpInfo?

expect fun getDatabase(): Database

/**
 * 计算子网掩码
 */
fun getSubnetMask(prefixLength: Int): String {
    val mask = -0x1 shl (32 - prefixLength)
    return String.format(
        "%d.%d.%d.%d",
        (mask ushr 24) and 0xff,
        (mask ushr 16) and 0xff,
        (mask ushr 8) and 0xff,
        mask and 0xff
    )
}


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
    FileItemVo(
        it.fileId,
        getFileType(it.fileName),
        it.fileName,
        it.uploadState,
        0,
        it.fileSize,
        fileFillName = it.fileFullName
    )

fun fileVoToFilePo(fileItemVo: FileItemVo): FileEntity {
    return FileEntity(
        null,
        "1",
        fileItemVo.fileId,
        fileItemVo.fileName,
        fileItemVo.fileFillName,
        fileItemVo.fileSize,
        fileItemVo.state,
        fileItemVo.fileSize,
        Date(),
        Date()
    )
}

/**
 * 创建设置器
 */
expect fun createSettings(): Settings

/**
 * 创建外包目录
 */
expect fun createAppDir(dirName: String): File

/**
 * 扫描到文件
 */
expect fun scanFileUtil(
    path: String,
    filter: (fileName: String, date: Date) -> Boolean
): MutableList<FileEntity>

/**
 * 打开网页
 */
expect fun openUrl(url: String)

/**
 * 打开别的app
 */
expect fun startOtherAPP(qrUrl: String)

/**
 * google pay
 */

expect fun googlePay()