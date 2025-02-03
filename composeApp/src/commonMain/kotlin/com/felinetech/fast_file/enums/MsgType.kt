package com.felinetech.fast_file.enums
/**
 * 消息类型
 *
 * @Author guofan
 * @Create 2023/6/6
 */
enum class MsgType(val value: Int) {
    登录(1),
    更新列表(2),
    传输数据(3),
    心跳(4),
    建立数据链接(5),
    传输成功(6),
    传输失败(7),
    OK(8),
    继续上传(9);
}