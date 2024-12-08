package com.felinetech.localcat.pojo

import com.felinetech.localcat.enums.ConnectStatus

data class ClientVo(
    val number: Int,
    val ip: String,
    var connectStatus: ConnectStatus
)
