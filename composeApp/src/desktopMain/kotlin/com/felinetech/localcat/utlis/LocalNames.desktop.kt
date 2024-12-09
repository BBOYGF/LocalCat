package com.felinetech.localcat.utlis

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

private var gson = Gson()

var localName: LocalNames? = null
var prevStr: String = ""

actual fun getNames(language: String): LocalNames {
    return if (localName == null || prevStr != language) {
        val rootPath = "composeResources/localcat.composeapp.generated.resources"
        val inputStream =
            object {}.javaClass.classLoader.getResourceAsStream("$rootPath/files/names_${language}.json")
        val text = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use {
            it.readText()
        }
        localName = gson.fromJson(text, LocalNames::class.java)
        prevStr = language
        localName as LocalNames
    } else {
        localName as LocalNames
    }
}

