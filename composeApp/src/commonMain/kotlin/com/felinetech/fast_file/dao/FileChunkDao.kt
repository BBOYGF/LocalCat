package com.felinetech.fast_file.dao

import androidx.room.*
import com.felinetech.fast_file.enums.UploadState
import com.felinetech.fast_file.po.FileChunkEntity

@Dao
public interface FileChunkDao {
    @Insert
    suspend  fun insert(fileChunk: FileChunkEntity): Long

    @Update
    suspend  fun update(fileChunk: FileChunkEntity)

    @Delete
    suspend  fun delete(fileChunk: FileChunkEntity)

    @Query("SELECT * FROM file_chunks")
    suspend fun getAllFileChunks(): List<FileChunkEntity>?

    @Query("SELECT * FROM file_chunks WHERE id = :fileChunkId")
    suspend  fun getFileChunkById(fileChunkId: Int): FileChunkEntity?

    @Query("SELECT * FROM file_chunks WHERE file_id = :fileId")
    suspend  fun getFileChunksByFileId(fileId: String?): List<FileChunkEntity>

    @Query("update file_chunks set upload_status=:uploadState where file_id=:fileId and chunk_index=:chunkIndex")
    suspend  fun updateFileChunkByFileId(fileId: String?, chunkIndex: Int, uploadState: UploadState?)

    @Query("delete from file_chunks ")
    suspend  fun deleteAll()
}