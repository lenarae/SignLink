package com.signlink.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


enum class MessageSource {
    SIGN,    // From wristband (gesture → text)
    SPEECH,  // From microphone (voice → text)
    TTS,     // From user typing (text → speech)
    SYSTEM   // App-generated info message
}

/**
 * A single message stored in the local chat history.
 *
 * @param id          Auto-generated
 * @param text        The message content
 * @param source      Where the message came from (SIGN, SPEECH, SYSTEM)
 * @param confidence  Classifier confidence 0–1 (null for non-SIGN messages)
 * @param sessionId   Groups messages belonging to the same session
 * @param timestampMs Unix timestamp in milliseconds when saved
 */
@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["timestamp_ms"])
    ]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id:          Long           = 0,

    @ColumnInfo(name = "text")
    val text:        String,

    @ColumnInfo(name = "source")
    val source:      MessageSource  = MessageSource.SIGN,

    @ColumnInfo(name = "confidence")
    val confidence:  Float?         = null,

    @ColumnInfo(name = "session_id")
    val sessionId:   String         = "",

    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long           = System.currentTimeMillis()
)