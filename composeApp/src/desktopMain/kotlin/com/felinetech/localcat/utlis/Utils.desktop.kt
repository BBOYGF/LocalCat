package com.felinetech.localcat.utlis

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.felinetech.localcat.database.Database
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.po.FileEntity
import com.felinetech.localcat.pojo.IpInfo
import kotlinx.coroutines.Dispatchers
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*


actual fun getDatabase(): Database {
    // C:\Users\Administrator\AppData\Local\Temp 数据库保存路径
    val dbFile = File(System.getProperty("user.home"), "local_cat/data/local_cat_database.db")
    return Room.databaseBuilder<Database>(
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}


actual fun createSettings(): Settings {
    val localCatFile = createAppDir("local_cat")
    val cacheDir = File(localCatFile, "cache")
    if (!cacheDir.exists()) {
        cacheDir.mkdir()
    }
    val configFile = File(cacheDir, "local_cat.cache")
    if (!configFile.exists()) {
        configFile.createNewFile()
    }
    return DesktopSettings(configFile.absolutePath)
}


/**
 * 创建外包目录
 */
actual fun createAppDir(dirName: String): File {
    val localCatFile = File(System.getProperty("user.home"), dirName)
    if (!localCatFile.exists()) {
        localCatFile.mkdir()
    }
    return localCatFile
}

/**
 * 扫描到文件
 */
actual fun scanFileUtil(
    path: String,
    filter: (fileName: String, date: Date) -> Boolean
): MutableList<FileEntity> {
    val list = mutableListOf<FileEntity>()
    val fileDir = File(path)
    val fileList = fileDir.listFiles()
    for (file in fileList!!) {
        val lastModified = file.lastModified()
        // 将时间戳转换为可读的日期格式
        val lastModifiedDate = Date(lastModified)
        if (!filter(file.name, lastModifiedDate)) {
            continue
        }
        val filePo = FileEntity(
            null,
            "1",
            UUID.randomUUID().toString(),
            file.name,
            file.absolutePath,
            file.length(),
            UploadState.待上传
        )
        list.add(filePo)
    }
    return list
}

actual fun getIpInfo(): IpInfo? {
    var ip = ""
    var subnetMask = ""
    var netName = ""
    try {
        // 获取所有网络接口
        val networkInterfaces = NetworkInterface.getNetworkInterfaces()
        for (networkInterface in networkInterfaces) {
            // 过滤掉回环接口和未启用的接口
            if (networkInterface.isLoopback || !networkInterface.isUp) continue
            // 获取接口名称
            netName = networkInterface.displayName
            // 获取 IP 地址和子网掩码
            for (address in networkInterface.interfaceAddresses) {
                val add = address.address
                if (add is Inet4Address) {
                    ip = address.address.hostAddress ?: ""
                    subnetMask = getSubnetMask(address.networkPrefixLength.toInt()) // 子网掩码长度
                    netName = ip
                    println("id:$ip 子网掩码：$subnetMask 网络名称：$netName")
                    break
                }
            }
        }
    } catch (e: Exception) {
        println("获取网络异常：${e.message}")
    }
    return IpInfo(ip, subnetMask, netName)
}
