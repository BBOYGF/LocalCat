package com.felinetech.localcat.utlis

import com.felinetech.localcat.enums.MsgType
import com.felinetech.localcat.pojo.MsgHead
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets


private val gson = Gson()


/**
 * 将消息头转换为二进制
 *
 * @param msgType    消息类型
 * @param bodyLength 方法体长度
 * @return 二进制
 */
private fun msgHeadToBytes(msgType: MsgType, bodyLength: Long): ByteArray {
    val msgSession: MsgHead = MsgHead()
    msgSession.setMsgType(msgType)
    msgSession.setDataLength(bodyLength)
    val msgSessionStr: String = gson.toJson(msgSession)

    val msgSessionStrBytes: ByteArray = msgSessionStr.toByteArray(StandardCharsets.UTF_8)
    val fullMsgHeadBytes = ByteArray(1000)
    System.arraycopy(msgSessionStrBytes, 0, fullMsgHeadBytes, 0, msgSessionStrBytes.size)
    for (i in msgSessionStrBytes.size..999) {
        fullMsgHeadBytes[i] = 32.toByte()
    }
    return fullMsgHeadBytes
}


/**
 * @param inputStream 输入字节流
 * @return 消息头
 * @throws IOException 异常
 */
@Throws(IOException::class)
fun readHead(inputStream: InputStream): MsgHead {
    var msgHead: MsgHead? = null
    var headMsgStr: String? = null
    try {
        val headMsgBytes = ByteArray(1000)
        val read = inputStream.read(headMsgBytes)
        if (read == -1) {
            throw IOException("链接断开!")
        }
        headMsgStr = String(headMsgBytes, StandardCharsets.UTF_8)
        msgHead = gson.fromJson(headMsgStr, MsgHead::class.java)
    } catch (e: Exception) {
        println("解析Gson json: $headMsgStr")
        throw e
    }
    return msgHead
}


/**
 * 读取消息体
 *
 * @param dataLength  数据长度
 * @param tClass      class类型
 * @param inputStream 数据读取流
 * @param <T>         类类型
 * @return 对象
 * @throws IOException io异常
</T> */
@Throws(IOException::class)
fun <T> readBody(dataLength: Int, tClass: Class<T>?, inputStream: InputStream): T {
    val taskListByte = ByteArray(dataLength)
    var bytesRead = 0
    var totalBytesRead = 0
    while (totalBytesRead < taskListByte.size) {
        bytesRead = inputStream.read(taskListByte, totalBytesRead, taskListByte.size - totalBytesRead)
        totalBytesRead += bytesRead
    }
    val taskStr = String(taskListByte, StandardCharsets.UTF_8)
    return gson.fromJson(taskStr, tClass)
}

/**
 * 发送消息头
 *
 * @param outputStream 输出流
 * @param msgType      消息类型
 * @param bodyLength   消息体长度
 * @throws IOException 异常
 */
@Throws(IOException::class)
fun sendHead(outputStream: OutputStream, msgType: MsgType, bodyLength: Long) {
    val data = msgHeadToBytes(msgType, bodyLength)
    outputStream.write(data)
    outputStream.flush()
}

/**
 * 发送头和发送体
 *
 * @param outputStream 输出流
 * @param msgType      消息类型
 * @param obj          对象
 * @throws IOException 异常
 */
@Throws(IOException::class)
fun sendHeadBody(outputStream: OutputStream, msgType: MsgType, obj: Any?) {
    val taskPoStr = gson.toJson(obj)
    val bodyData: ByteArray = taskPoStr.toByteArray(StandardCharsets.UTF_8)
    val headData = msgHeadToBytes(msgType, bodyData.size.toLong())
    outputStream.write(headData)
    outputStream.flush()
    outputStream.write(bodyData)
    outputStream.flush()
}