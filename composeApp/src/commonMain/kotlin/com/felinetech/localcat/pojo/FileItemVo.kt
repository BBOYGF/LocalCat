package com.felinetech.localcat.pojo

import com.felinetech.localcat.enums.FileType
import com.felinetech.localcat.enums.UploadState

data class FileItemVo(
    val fileId: String,
    /**
     * 文件类型
     */
    var fileType: FileType,
    /**
     * 文件名
     */

    val fileName: String,
    /**
     * 状态
     */

    var state: UploadState,

    /**
     * 当前进度
     */

    var percent: Int,
    /**
     * 文件大小
     */

    val fileSize: Long = 0
)
