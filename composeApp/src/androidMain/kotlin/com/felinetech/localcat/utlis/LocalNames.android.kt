package com.felinetech.localcat.utlis

import com.felinetech.localcat.MainActivity
import com.felinetech.localcat.R
import com.google.gson.Gson


private var gson = Gson()

actual fun loadLocalization(language: String): LocalNames {
    val context = MainActivity.instance
    val inputStream = context.resources.openRawResource(R.raw.names)
    val text = inputStream.bufferedReader().use { it.readText() }
    return gson.fromJson(text, LocalNames::class.java)
}

//}
