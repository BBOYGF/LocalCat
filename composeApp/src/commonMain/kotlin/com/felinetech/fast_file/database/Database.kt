package com.felinetech.fast_file.database

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.felinetech.fast_file.dao.FileChunkDao
import com.felinetech.fast_file.dao.FileEntityDao
import com.felinetech.fast_file.dao.UploadConfigDao
import com.felinetech.fast_file.po.FileChunkEntity
import com.felinetech.fast_file.po.FileEntity
import com.felinetech.fast_file.po.UploadConfigItem
import com.felinetech.fast_file.utlis.DateConverter

@androidx.room.Database(
    entities = [FileEntity::class, UploadConfigItem::class, FileChunkEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun getFileEntityDao(): FileEntityDao
    abstract fun getUploadConfigItemDao(): UploadConfigDao
    abstract fun getFileChunkEntityDao(): FileChunkDao
}