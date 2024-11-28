package com.felinetech.localcat.pojo

data class PortAndTask(
    /**
     * 端口列表
     */
    var portList: List<Int> = ArrayList(),
    /**
     * 任务
     */
    var taskPo: TaskPo,
)
