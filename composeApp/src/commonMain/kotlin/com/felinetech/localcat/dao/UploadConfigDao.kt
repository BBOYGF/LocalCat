package com.felinetech.localcat.dao

import androidx.room.*
import com.felinetech.localcat.po.UploadConfigItem

@Dao
interface UploadConfigDao {
    @Insert
    suspend fun insertUploadCon(vararg items: UploadConfigItem)

    @Update
    suspend fun updateUploadCon(vararg myUsers: UploadConfigItem)

    @Delete
    suspend fun deleteUploadCon(vararg myUsers: UploadConfigItem)

    @Query("delete from UploadConfigItem")
    suspend fun deleteAllUploadCon()

    @Query("select * from UploadConfigItem  ")
    suspend fun getAllUploadCon(): List<UploadConfigItem>

    /**
     * 在当前线程中调用
     */
    @Query("select * from UploadConfigItem  ")
    suspend fun getAllUpload(): List<UploadConfigItem?>?
}