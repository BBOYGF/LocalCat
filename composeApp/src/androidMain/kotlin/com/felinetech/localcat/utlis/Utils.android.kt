package com.felinetech.localcat.utlis

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Environment
import android.provider.MediaStore
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.felinetech.localcat.MainActivity
import com.felinetech.localcat.MainActivity.Companion.instance
import com.felinetech.localcat.database.Database
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.po.FileEntity
import java.io.File
import java.util.Date
import java.util.UUID

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