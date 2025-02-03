package com.felinetech.fast_file.pojo

import com.felinetech.fast_file.enums.ConnectButtonState
import com.felinetech.fast_file.enums.ConnectStatus

data class ServicePo(var id:Int,var ip:String,var connectStatus: ConnectStatus,var buttonState: ConnectButtonState)
