package com.felinetech.localcat.utlis

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

/**
 * 属性配置工具类
 */
class PropertiesConfigUtils(private val configFile: File) {
    private val properties: Properties = Properties()

    init {
        loadProperties()
    }

    // Load properties from the file
    private fun loadProperties() {
        if (configFile.exists()) {
            FileInputStream(configFile).use { input ->
                properties.load(input)
            }
        }
    }

    // Save properties to the file
    private fun saveProperties() {
        FileOutputStream(configFile).use { output ->
            properties.store(output, "Configuration Properties")
        }
    }

    // Set a value in the properties
    fun setValue(key: String, value: String) {
        properties.setProperty(key, value)
        saveProperties()
    }

    // Get a value from the properties
    fun getValue(key: String): String? {
        return properties.getProperty(key)
    }

    // Remove a value from the properties
    fun removeValue(key: String) {
        properties.remove(key)
        saveProperties()
    }

    // Clear all properties
    fun clear() {
        properties.clear()
        saveProperties()
    }

}