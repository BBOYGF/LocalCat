package com.felinetech.fast_file.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.fast_file.utlis.createSettings
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.commons.lang3.StringUtils
import java.util.UUID

object MainViewModel {
    val turnState = MutableStateFlow(false)
    var sendState: Boolean = false
    var receiveState: Boolean = true
    var bottomSheetVisible by mutableStateOf(false)
    var userID: String? = null

    /**
     * 显示消息框
     */
    var showDialog by mutableStateOf(false)

    /**
     * 消息类型
     */
    var msgPair by mutableStateOf(Pair("", ""))

    init {
        val settings = createSettings()
        userID = settings.getSetting("userID")
        if (StringUtils.isEmpty(userID)) {
            userID = UUID.randomUUID().toString()
            settings.saveSetting("userID", userID!!)
        }
    }

    fun turnFun(boolean: Boolean) {
        turnState.value = boolean
    }


}