package com.signlink.app.data.local

import androidx.room.*

@Database(
    entities     = [ChatMessage::class],
    version      = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SignLinkDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

class Converters {

    /** MessageSource enum → String stored in SQLite */
    @TypeConverter
    fun fromMessageSource(source: MessageSource): String = source.name

    /** String from SQLite → MessageSource enum */
    @TypeConverter
    fun toMessageSource(value: String): MessageSource =
        runCatching { MessageSource.valueOf(value) }.getOrDefault(MessageSource.SIGN)
}