package com.felinetech.fast_file.pojo

import com.felinetech.fast_file.po.FileChunkEntity
import com.felinetech.fast_file.po.FileEntity

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