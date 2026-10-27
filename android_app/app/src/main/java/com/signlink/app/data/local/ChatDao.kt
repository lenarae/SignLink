// ============================================================
// File: data/local/ChatDao.kt
// Purpose: Data Access Object for chat_messages table.
//
// DAOs contain the SQL queries for your database.
// Room generates the actual SQLite implementation at compile time.
//
// ANNOTATIONS:
//   @Dao      → tells Room this interface provides database access
//   @Query    → custom SQL SELECT/DELETE statement
//   @Insert   → INSERT INTO chat_messages
//   @Delete   → DELETE by object identity
//
// All functions return Flow<> so the UI auto-updates when data changes.
// ============================================================

package com.signlink.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // ── INSERT ─────────────────────────────────────────────────

    /**
     * Insert a new message. Returns the new row ID.
     * OnConflictStrategy.REPLACE: if a row with same PK exists, replace it.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    /**
     * Insert multiple messages at once (bulk import).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessage>)

    // ── SELECT ─────────────────────────────────────────────────

    /**
     * Get ALL messages, newest first.
     * Returns Flow — emits a new list whenever the table changes.
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp_ms DESC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    /**
     * Get messages for a specific session.
     * @param sessionId The session UUID string
     */
    @Query("SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY timestamp_ms ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<ChatMessage>>

    /**
     * Get all unique session IDs (for the session list view).
     * Provides the latest message text, count, and timestamp for each session.
     */
    @Query("""
        SELECT session_id, 
               MAX(timestamp_ms) as latest, 
               COUNT(*) as count,
               (SELECT text FROM chat_messages WHERE session_id = m.session_id ORDER BY timestamp_ms DESC LIMIT 1) as snippet
        FROM chat_messages m
        GROUP BY session_id
        ORDER BY latest DESC
    """)
    fun getSessionIds(): Flow<List<SessionSummary>>

    /**
     * Count total messages in the database.
     */
    @Query("SELECT COUNT(*) FROM chat_messages")
    fun getMessageCount(): Flow<Int>

    /**
     * Search messages containing the query string (case-insensitive).
     */
    @Query("SELECT * FROM chat_messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp_ms DESC")
    fun searchMessages(query: String): Flow<List<ChatMessage>>

    // ── DELETE ─────────────────────────────────────────────────

    /**
     * Delete a specific message by object identity.
     */
    @Delete
    suspend fun deleteMessage(message: ChatMessage)

    /**
     * Delete all messages in a specific session.
     */
    @Query("DELETE FROM chat_messages WHERE session_id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    /**
     * Delete ALL messages (full history wipe).
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()

    /**
     * Auto-delete messages older than a given timestamp.
     * Used by the data retention setting.
     * @param beforeTimestampMs Delete messages with timestamp < this value
     */
    @Query("DELETE FROM chat_messages WHERE timestamp_ms < :beforeTimestampMs")
    suspend fun deleteMessagesBefore(beforeTimestampMs: Long)
}

/**
 * Helper data class for the session list query.
 * Room maps the query result into this automatically.
 */
data class SessionSummary(
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "latest")     val latest:    Long,
    @ColumnInfo(name = "count")      val count:     Int,
    @ColumnInfo(name = "snippet")    val snippet:   String?
)