package com.felinetech.localcat.utlis

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.felinetech.localcat.database.Database
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JFrame


actual fun getLocalIp(): String {
    return try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        println("=============")
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

actual fun getSubnetMask(): String {
    var subnetMask: String = ""
    try {
        // 获取局域网名称
        val localHost = InetAddress.getLocalHost()
        val hostName = localHost.hostName
        println("局域网名称 (主机名): $hostName")

        // 获取局域网子网掩码
        val interfaces: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (networkInterface in interfaces) {
            // 确保网络接口是启用的，并且不是虚拟接口
            if (networkInterface.isUp && !networkInterface.isLoopback) {
                for (address in networkInterface.interfaceAddresses) {
                    val inetAddress = address.address
                    if (inetAddress.isSiteLocalAddress) { // 只考虑局域网地址
                        println("接口: " + networkInterface.displayName)
                        println("局域网 IP: " + inetAddress.hostAddress)

                        // 获取子网掩码
                        subnetMask = getSubnetMask(address.networkPrefixLength.toInt())
                        println("子网掩码: $subnetMask")

                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return subnetMask
}

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


actual fun getDatabase(): Database {
    // C:\Users\Administrator\AppData\Local\Temp 数据库保存路径
    val dbFile = File(System.getProperty("user.home"), "local_cat/data/local_cat_database.db")
    return Room.databaseBuilder<Database>(
        name = dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

actual fun getFileByDialog(): File? {
    val frame = JFrame()
    val dialog = JFileChooser()
    dialog.dialogTitle = "选择目录"
    dialog.dialogType = JFileChooser.DIRECTORIES_ONLY
    dialog.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    val returnValue = dialog.showOpenDialog(frame)
    if (returnValue == JFileChooser.APPROVE_OPTION) {
        // 获取所选文件夹
        val selectedDirectory = dialog.selectedFile
        return selectedDirectory
    } else {
        return null
    }

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