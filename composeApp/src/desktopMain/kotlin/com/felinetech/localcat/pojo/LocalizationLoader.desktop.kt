package com.felinetech.localcat.pojo

import com.google.gson.Gson
import java.io.File

actual class LocalizationLoader {
    private var gson = Gson()
    actual fun loadLocalization(language: String): LocalizationData {
        val file = File("src/desktopMain/resources/localization/$language.json")
//        gson.fromJson(file.readText())
        return LocalizationData("测试")
    }
}