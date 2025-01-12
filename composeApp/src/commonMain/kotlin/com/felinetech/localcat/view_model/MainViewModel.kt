package com.felinetech.localcat.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow

object MainViewModel {
    val turnState = MutableStateFlow(false)
    var sendState: Boolean = false
    var receiveState: Boolean = true
    var bottomSheetVisible by mutableStateOf(false)
    fun turnFun(boolean: Boolean) {
        turnState.value = boolean
    }


}