package com.felinetech.fast_file.po

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.felinetech.fast_file.enums.UploadState
import java.util.Date

@Entity(tableName = "file_chunks")
data class FileChunkEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    @ColumnInfo(name = "file_id")
    var fileId: String? = null,

    @ColumnInfo(name = "chunk_index")
    var chunkIndex: Int = 0,

    @ColumnInfo(name = "chunk_size")
    var chunkSize: Long = 0,

    @ColumnInfo(name = "upload_status")
    var uploadStatus: UploadState? = null,

    @ColumnInfo(name = "created_at")
    var createdAt: Date? = null,

    @ColumnInfo(name = "updated_at")
    var updatedAt: Date? = null,
)
