package com.felinetech.localcat.database

import androidx.room.RoomDatabase
import com.felinetech.localcat.dao.FileEntityDao
import com.felinetech.localcat.po.FileEntity

@androidx.room.Database(entities = [FileEntity::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun getFileEntityDao(): FileEntityDao
}