// ============================================================
// File: data/local/SignLinkDatabase.kt  [FIXED]
// Purpose: Room database — single source of truth.
//
// FIX: Removed the getInstance() companion object pattern.
// DatabaseModule (Hilt) is now the ONLY place that creates the DB.
// Having two construction paths caused a potential double-init bug.
// Now: DatabaseModule calls Room.databaseBuilder() directly and
// provides the result as a @Singleton — guaranteed one instance.
// ============================================================

package com.signlink.app.data.local

import androidx.room.*

@Database(
    entities     = [ChatMessage::class],
    version      = 1,
    exportSchema = false   // set true + add schemas/ dir for production
)
@TypeConverters(Converters::class)
abstract class SignLinkDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

/**
 * Type converters so Room can store our custom types.
 * Room only natively stores: Int, Long, Float, Double, String, ByteArray.
 * Everything else needs a converter pair (to/from a native type).
 */
class Converters {

    /** MessageSource enum → String stored in SQLite */
    @TypeConverter
    fun fromMessageSource(source: MessageSource): String = source.name

    /** String from SQLite → MessageSource enum */
    @TypeConverter
    fun toMessageSource(value: String): MessageSource =
        runCatching { MessageSource.valueOf(value) }.getOrDefault(MessageSource.SIGN)
}