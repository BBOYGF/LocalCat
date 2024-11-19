package com.felinetech.localcat.utlis

import androidx.compose.runtime.Composable
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.felinetech.localcat.database.Database
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.net.InetAddress
import java.net.NetworkInterface
import javax.swing.JFileChooser
import javax.swing.JFrame

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