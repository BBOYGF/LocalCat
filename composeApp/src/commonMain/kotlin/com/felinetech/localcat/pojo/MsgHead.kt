package com.felinetech.localcat.pojo

import com.felinetech.localcat.enums.MsgType


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