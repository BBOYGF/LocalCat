package com.felinetech.localcat.database

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.felinetech.localcat.dao.FileEntityDao
import com.felinetech.localcat.po.FileChunkEntity
import com.felinetech.localcat.po.FileEntity
import com.felinetech.localcat.po.UploadConfigItem
import com.felinetech.localcat.utlis.DateConverter

@androidx.room.Database(
    entities = [FileEntity::class, UploadConfigItem::class, FileChunkEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun getFileEntityDao(): FileEntityDao
}