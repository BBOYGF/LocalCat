package com.felinetech.fast_file.utlis

import com.felinetech.fast_file.MainActivity
import com.felinetech.fast_file.R

import com.google.gson.Gson


private var gson = Gson()

var localName: LocalNames? = null

var prevStr: String = ""
actual fun getNames(language: String): LocalNames {

    if (localName == null || prevStr != language) {

        val context = MainActivity.mainActivity
        val inputStream = if (language == "zh") {
            context.resources.openRawResource(R.raw.names_zh)
        } else {
            context.resources.openRawResource(R.raw.names_en)
        }
        val text = inputStream.bufferedReader().use { it.readText() }
        localName = try {
            gson.fromJson(text, LocalNames::class.java)
        } catch (e: Exception) {
            println("解析失败！${e.message}")
            LocalNames()
        }
        prevStr = language
        return localName as LocalNames
    } else {
        return localName as LocalNames
    }
}
