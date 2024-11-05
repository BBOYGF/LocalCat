package com.felinetech.localcat.po

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.felinetech.localcat.enums.UploadState

@Entity(tableName = "files_entity")
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "user_id")
    var userId: String? = null,

    @ColumnInfo(name = "file_id")
    var fileId: String? = null,

    @ColumnInfo(name = "file_name")
    var fileName: String? = null,

    @ColumnInfo(name = "file_full_name")
    var fileFullName: String? = null,

    @ColumnInfo(name = "file_size")
    var fileSize: Long = 0,

    @ColumnInfo(name = "upload_state")
    var uploadState: UploadState? = null,

    /**
     * 文件块大小
     */
    @ColumnInfo(name = "chunk_size")
    val chunkSize: Long = 0,


//    @ColumnInfo(name = "created_at")
//    var createdAt: Date? = null,
//
//    @ColumnInfo(name = "updated_at")
//    var updatedAt: Date? = null

)
