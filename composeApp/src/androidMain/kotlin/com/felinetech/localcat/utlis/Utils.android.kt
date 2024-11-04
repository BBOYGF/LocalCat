package com.felinetech.localcat.utlis

import android.content.Context
import android.net.wifi.WifiManager
import com.felinetech.localcat.MainActivity

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