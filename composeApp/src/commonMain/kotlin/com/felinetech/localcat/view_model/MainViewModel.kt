package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.felinetech.localcat.utlis.createSettings
import kotlinx.coroutines.flow.MutableStateFlow
import org.apache.commons.lang3.StringUtils
import java.util.UUID

object MainViewModel {
    val turnState = MutableStateFlow(false)
    var sendState: Boolean = false
    var receiveState: Boolean = true
    var bottomSheetVisible by mutableStateOf(false)
    var userID: String? = null

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