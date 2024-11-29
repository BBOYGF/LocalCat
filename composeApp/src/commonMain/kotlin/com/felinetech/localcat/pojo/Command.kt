package com.felinetech.localcat.pojo

data class Command(
    /**
     * 线程数量
     */
    var threadCount: Int,
    /**
     * 每次上传数据大小
     */
    var fileChunkSize: Long,

    /**
     * 文件列表
     */
    var taskPo: TaskPo,
)