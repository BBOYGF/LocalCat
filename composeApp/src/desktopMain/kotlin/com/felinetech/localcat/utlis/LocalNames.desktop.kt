package com.felinetech.localcat.utlis

import com.google.gson.Gson
import java.io.File

private var gson = Gson()

var localName: LocalNames? = null
var prevStr: String = ""
actual fun getNames(language: String): LocalNames {
    return if (localName == null || prevStr != language) {
        val text = File("src/commonMain/composeResources/files/names_${language}.json")
            .run {
                readText(Charsets.UTF_8)
            }
        localName = gson.fromJson(text, LocalNames::class.java)
        prevStr = language
        localName as LocalNames
    } else {
        localName as LocalNames
    }
}

