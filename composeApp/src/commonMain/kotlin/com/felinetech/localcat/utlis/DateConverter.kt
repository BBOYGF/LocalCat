package com.felinetech.localcat.utlis

import androidx.room.TypeConverter
import java.util.Date

/**
 * room 日期转换类
 */
class DateConverter {
    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun timestampToDate(timestamp: Long?): Date? {
        return if (timestamp == null) null else Date(timestamp)
    }
}