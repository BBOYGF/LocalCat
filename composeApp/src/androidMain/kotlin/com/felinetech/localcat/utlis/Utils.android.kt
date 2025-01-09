package com.felinetech.localcat.utlis

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.felinetech.localcat.MainActivity
import com.felinetech.localcat.MainActivity.Companion.instance
import com.felinetech.localcat.database.Database
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.po.FileEntity
import com.felinetech.localcat.pojo.IpInfo
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.Date
import java.util.UUID


actual fun getDatabase(): Database {
    val dbFile = MainActivity.instance.getDatabasePath("local_cat_database.db")
    return Room.databaseBuilder(
        MainActivity.instance,
        Database::class.java,
        dbFile.absolutePath
    ).setDriver(BundledSQLiteDriver())
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()
}


actual fun createSettings(): Settings {
    return AndroidSettings(context = instance)
}

/**
 * 创建外包目录
 */
actual fun  createAppDir(dirName: String): File {
    if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath, dirName)
        if (!dir.exists()) {
            dir.mkdirs() // 创建目录
        }
        return dir
    }
    return File(dirName).apply { mkdirs() }
}

/**
 * 扫描到文件
 */
actual fun scanFileUtil(
    path: String,
    filter: (fileName: String, date: Date) -> Boolean
): MutableList<FileEntity> {
    val fileList = mutableListOf<FileEntity>()
    val contentResolver = instance.contentResolver
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.DATE_ADDED
    )
    val selection = "${MediaStore.Images.Media.DATA} LIKE ?"
    val selectionArgs = arrayOf("%${path.split(":")[1]}%") // 指定目录
    // 查询目录下的文件
    val cursor: Cursor? = contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )

    cursor?.use {
        val id = it.getColumnIndex(MediaStore.Images.Media._ID)
        val sizeIndex = it.getColumnIndex(MediaStore.Images.Media.SIZE)
        val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
        val fileFillNameIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
        val addedDateIndex = it.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
        while (it.moveToNext()) {
            val id = it.getString(id)
            val fileName = it.getString(nameIndex)
            val fileSize = it.getLong(sizeIndex)
            val lastModified = it.getLong(addedDateIndex) * 1000 // 转换为毫秒
            val lastModifiedDate = Date(lastModified)
            val fileFillName = it.getString(fileFillNameIndex)
            if (!filter(fileName, lastModifiedDate)) {
                continue // 如果过滤条件不满足，跳过文件
            }
            val fileEntity = FileEntity(
                null, // ID 或其他字段可以根据需要填充
                "1", // userId 示例
                UUID.randomUUID().toString(),
                fileName,
                fileFillName, // 这里使用 URI 作为文件路径
                fileSize,
                UploadState.待上传
            )
            fileList.add(fileEntity)
        }
    }
    return fileList
}

/**
 * 获取IP地址信息
 */
actual fun getIpInfo(): IpInfo? {
    var ip = ""
    var subnetMask = ""
    var netName = ""
    val connectivityManager =
        instance.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork: Network? = connectivityManager.activeNetwork
    if (activeNetwork != null) {
        val linkProperties: LinkProperties? = connectivityManager.getLinkProperties(activeNetwork)
        if (linkProperties != null) {
            val firstAdd =
                linkProperties.linkAddresses.firstOrNull { linkAddress -> linkAddress.address is Inet4Address }
            firstAdd?.let {
                ip = it.address.hostAddress!!
                subnetMask = intToIpAddress(it.prefixLength)
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                val isWifiConnected = activeNetworkInfo != null &&
                        activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI &&
                        activeNetworkInfo.isConnected
                // 如果当前连接的是 Wi-Fi，获取 SSID
                if (isWifiConnected) {
                    val wifiManager: WifiManager =
                        (instance.application.getSystemService(Context.WIFI_SERVICE)) as WifiManager
                    val ssid = getWifiSsid(wifiManager)
                    if (ssid != null) {
                        netName = ssid
                    } else {
                        netName = "NO"
                    }
                } else {
                    netName = it.address.hostName
                }

            }
        }
    }
    return IpInfo(ip, subnetMask, netName)
}

fun intToIpAddress(ipInt: Int): String {
    // 将整数转换为无符号的 32 位整数
    val unsignedInt = ipInt.toLong() and 0xFFFFFFFF

    // 提取 4 个字节
    val byte1 = (unsignedInt shr 24) and 0xFF
    val byte2 = (unsignedInt shr 16) and 0xFF
    val byte3 = (unsignedInt shr 8) and 0xFF
    val byte4 = unsignedInt and 0xFF

    // 拼接为 IP 地址
    return "$byte4.$byte3.$byte2.$byte1"
}

private fun getWifiSsid(wifiManager: WifiManager): String? {
    return if (wifiManager.isWifiEnabled) {
        val wifiInfo = wifiManager.connectionInfo
        var ssid = wifiInfo.ssid
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length - 1)
        }
        ssid
    } else {
        null
    }
}

/**
 * 打开网页
 */
actual fun openUrl(url: String) {
    val context = instance
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}