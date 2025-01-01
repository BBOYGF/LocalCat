package com.felinetech.localcat.utlis

import android.content.Context
import android.content.SharedPreferences

/**
 * Android端实现
 */
class AndroidSettings(context: Context) : Settings {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("local_cat.cache", Context.MODE_PRIVATE)

    override fun saveSetting(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getSetting(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
}