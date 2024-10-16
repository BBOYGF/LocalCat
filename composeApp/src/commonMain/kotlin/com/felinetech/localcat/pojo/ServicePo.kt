package com.felinetech.localcat.pojo

import com.felinetech.localcat.enums.ConnectButtonState
import com.felinetech.localcat.enums.ConnectStatus

data class ServicePo(val id:Int,val ip:String,val connectStatus: ConnectStatus,val buttonState: ConnectButtonState)
