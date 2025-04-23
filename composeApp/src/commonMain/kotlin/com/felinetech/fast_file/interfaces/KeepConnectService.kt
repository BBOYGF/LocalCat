package com.felinetech.fast_file.interfaces

import com.felinetech.fast_file.pojo.ServicePo

interface KeepConnectService {

    fun stareKeepConnect(servicePo: ServicePo)

    fun stopConnect(servicePo: ServicePo)

}