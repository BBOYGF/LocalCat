package com.felinetech.localcat.utlis

import com.google.gson.Gson
import java.io.File

private var gson = Gson()
actual fun loadLocalization(language: String): LocalNames {
    val text = File("src/commonMain/composeResources/files/names.json").run {
        readText(Charsets.UTF_8)
    }
    return gson.fromJson(text, LocalNames::class.java)
}

//}
