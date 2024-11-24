package com.felinetech.localcat.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.felinetech.localcat.enums.UploadState
import com.felinetech.localcat.po.FileEntity

@Dao
public interface FileEntityDao {


    @Insert
    suspend fun insert(file: FileEntity): Long

    @Update
    suspend fun update(file: FileEntity)

    @Query("delete  FROM files_entity WHERE file_id = :fileId")
    suspend fun deleteFileById(fileId: String?)

    @Query("SELECT * FROM files_entity")
    suspend fun getAllFiles(): List<FileEntity>

    @Query("SELECT * FROM files_entity WHERE file_id = :fileId")
    suspend fun getFileById(fileId: String?): FileEntity?

    @Query("SELECT * FROM files_entity WHERE user_id = :userId")
    suspend fun getFilesByUserId(userId: Int): List<FileEntity?>?

    @Query("SELECT  * FROM files_entity WHERE  file_full_name= :uri LIMIT 1")
    suspend fun getFileByUri(uri: String?): FileEntity?

    @Query("update files_entity set upload_state=:uploadState where file_id=:fileId")
    suspend fun updateStateByFileId(fileId: String?, uploadState: UploadState?)

    @Query("delete from files_entity ")
    suspend fun deleteAll()
}