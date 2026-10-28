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


    @Query("SELECT COUNT(*) FROM chat_messages")
    fun getMessageCount(): Flow<Int>


    @Query("SELECT * FROM chat_messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp_ms DESC")
    fun searchMessages(query: String): Flow<List<ChatMessage>>

    // ── DELETE ─────────────────────────────────────────────────

    @Delete
    suspend fun deleteMessage(message: ChatMessage)
    @Query("DELETE FROM chat_messages WHERE session_id = :sessionId")
    suspend fun deleteSession(sessionId: String)
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




data class SessionSummary(
    @ColumnInfo(name = "session_id") val sessionId: String,
    @ColumnInfo(name = "latest")     val latest:    Long,
    @ColumnInfo(name = "count")      val count:     Int,
    @ColumnInfo(name = "snippet")    val snippet:   String?
)