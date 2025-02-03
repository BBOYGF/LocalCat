package com.felinetech.fast_file.pojo

import com.felinetech.fast_file.enums.ConnectStatus

data class ClientVo(
    val number: Int,
    val ip: String,
    var connectStatus: ConnectStatus
)
