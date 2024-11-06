package com.felinetech.localcat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.po.FileChunkEntity

@Dao
public interface FileChunkDao {
    @Insert
    suspend  fun insert(fileChunk: FileChunkEntity?): Long

    @Update
    suspend  fun update(fileChunk: FileChunkEntity?)

    @Delete
    suspend  fun delete(fileChunk: FileChunkEntity?)

    @Query("SELECT * FROM file_chunks")
    suspend fun getAllFileChunks(): List<FileChunkEntity?>?

    @Query("SELECT * FROM file_chunks WHERE id = :fileChunkId")
    suspend  fun getFileChunkById(fileChunkId: Int): FileChunkEntity?

    @Query("SELECT * FROM file_chunks WHERE file_id = :fileId")
    suspend  fun getFileChunksByFileId(fileId: String?): List<FileChunkEntity?>?

    @Query("update file_chunks set upload_status=:uploadState where file_id=:fileId and chunk_index=:chunkIndex")
    suspend  fun updateFileChunkByFileId(fileId: String?, chunkIndex: Int, uploadState: UploadState?)

    @Query("delete from file_chunks ")
    suspend  fun deleteAll()
}