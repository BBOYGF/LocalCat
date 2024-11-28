package com.felinetech.localcat.pojo

import com.felinetech.localcat.po.FileChunkEntity
import com.felinetech.localcat.po.FileEntity

/**
 * 文件块
 */
data class TaskPo(
    /**
     * 当前上传的任务
     */
    var fileEntity: FileEntity,

    /**
     * 当前上传数据块
     */
    var fileChunkEntityList: List<FileChunkEntity>,
    /**
     * 文件块大小
     */
    var chunkSize: Long
)