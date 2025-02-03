package com.felinetech.fast_file.pojo

import com.felinetech.fast_file.enums.MsgType


data class MsgHead(
    /**
     * 消息类型
     */
    var msgType: MsgType,

    /**
     * 数据体长度
     */
    var dataLength: Long = 0
)