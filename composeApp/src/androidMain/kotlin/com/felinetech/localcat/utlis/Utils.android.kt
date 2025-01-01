package com.felinetech.localcat.utlis

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Environment
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.felinetech.localcat.MainActivity
import com.felinetech.localcat.MainActivity.Companion.instance
import com.felinetech.localcat.database.Database
import java.io.File

actual fun getLocalIp(): String {
    val wifiManager: WifiManager =
        MainActivity.instance.getSystemService(Context.WIFI_SERVICE) as WifiManager
    var substring: String = ""
    if (wifiManager.isWifiEnabled) {
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null) {
            val ssid = wifiInfo.getSSID()
            substring = ssid
            if (ssid.length > 2 && ssid[0] == '"' && ssid[ssid.length - 1] == '"') {
                substring = ssid.substring(1, ssid.length - 1)
            }
            if ("<unknown ssid>" == substring) {
                substring = "无权限"
            }
        }
    }
    return substring
}

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

actual fun getFileByDialog(): File? {
//    var selectedDirectory by remember { mutableStateOf<Uri?>(null) }
//    val context = LocalContext.current
//    val getContent =
//        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
//            if (uri != null) {
//                selectedDirectory = uri
//                // 这里可以存储或处理所选目录的 URI
//            }
//        }
//    getContent.launch(null)
//    selectedDirectory?.let {
//        Text("选定的目录: $it")
//    } ?: Text("未选择目录")
    return null
}

actual fun getSubnetMask(): String {
    TODO("Not yet implemented")
    return ""
}

actual fun createSettings(): Settings {
    return AndroidSettings(context = instance)
}

/**
 * 创建外包目录
 */
actual fun createAppDir(dirName: String): File {
    if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
        val dir = File(instance.getExternalFilesDir(null), dirName)
        if (!dir.exists()) {
            dir.mkdirs() // 创建目录
        }
        return dir
    }
    return File(dirName).apply { mkdirs() }
}