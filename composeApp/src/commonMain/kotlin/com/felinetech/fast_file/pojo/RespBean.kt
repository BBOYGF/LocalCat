package com.felinetech.fast_file.pojo

/**
 * 响应类
 */
data class RespBean(
    var code: Long = 0,

    val message: String = "",

    val obj: Any? = null
)
