package com.felinetech.localcat.view_model

import kotlinx.coroutines.flow.MutableStateFlow

object MainViewModel {
    val turnState = MutableStateFlow<Boolean>(false)
    var sendState: Boolean = false
    var receiveState: Boolean = true

    fun turnFun(boolean: Boolean) {
        turnState.value = boolean
    }


}