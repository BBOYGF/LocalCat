package com.felinetech.localcat.pojo

import com.felinetech.localcat.enums.ConnectButtonState
import com.felinetech.localcat.enums.ConnectStatus

data class ServicePo(var id:Int,var ip:String,var connectStatus: ConnectStatus,var buttonState: ConnectButtonState)
