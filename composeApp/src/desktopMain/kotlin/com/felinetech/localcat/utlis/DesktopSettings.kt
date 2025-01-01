package com.felinetech.localcat.utlis

import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * 桌面端实现
 */
class DesktopSettings(private val filePath: String) : Settings {
    private val properties = Properties()

    init {
        try {
            FileInputStream(filePath).use { inputStream ->
                properties.load(inputStream)
            }
        } catch (e: Exception) {
            // 处理异常，例如文件不存在等
        }
    }

    override fun saveSetting(key: String, value: String) {
        properties.setProperty(key, value)
        FileOutputStream(filePath).use { outputStream ->
            properties.store(outputStream, null)
        }
    }

    override fun getSetting(key: String): String? {
        return properties.getProperty(key)
    }
}