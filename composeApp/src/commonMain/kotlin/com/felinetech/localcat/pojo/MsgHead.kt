package com.felinetech.localcat.pojo

import com.felinetech.localcat.enums.MsgType



class MsgHead {
    /**
     * 消息类型
     */
    private var msgType: MsgType? = null

    /**
     * 数据体长度
     */
    private var dataLength: Long = 0

    fun getMsgType(): MsgType? {
        return msgType
    }

    fun setMsgType(msgType: MsgType?) {
        this.msgType = msgType
    }


    fun getDataLength(): Long {
        return dataLength
    }

    fun setDataLength(dataLength: Long) {
        this.dataLength = dataLength
    }

    override fun toString(): String {
        return "MsgHead{msgType=$msgType, dataLength=$dataLength}"
    }
}