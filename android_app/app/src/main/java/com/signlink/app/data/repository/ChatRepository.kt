package com.signlink.app.data.repository

import com.signlink.app.data.local.AppSettingsDataStore
import com.signlink.app.data.local.ChatDao
import com.signlink.app.data.local.ChatMessage
import com.signlink.app.data.local.MessageSource
import com.signlink.app.data.local.SessionSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class RetentionPolicy {
    ONE_DAY,    // Delete messages older than 24 hours
    ONE_WEEK,   // Delete messages older than 7 days
    ONE_MONTH,  // Delete messages older than 30 days
    FOREVER     // Never auto-delete
}

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao:           ChatDao,
    private val settingsDataStore: AppSettingsDataStore
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Current session ID — changes each time startNewSession() is called
    private var currentSessionId: String = generateSessionId()

    fun startNewSession() { currentSessionId = generateSessionId() }
    fun getCurrentSessionId(): String = currentSessionId
    private fun generateSessionId(): String = UUID.randomUUID().toString()

    // ── READ ──────────────────────────────────────────────────

    fun getAllMessages(): Flow<List<ChatMessage>>                    = chatDao.getAllMessages()
    fun getMessagesBySession(id: String): Flow<List<ChatMessage>>   = chatDao.getMessagesBySession(id)
    fun getSessionList(): Flow<List<SessionSummary>>                = chatDao.getSessionIds()
    fun getMessageCount(): Flow<Int>                                = chatDao.getMessageCount()
    fun searchMessages(q: String): Flow<List<ChatMessage>>          = chatDao.searchMessages(q)

    // ── WRITE (all respect storageEnabled setting) ────────────


    suspend fun saveTranslation(text: String, confidence: Float? = null) {
        if (!isStorageEnabled()) return
        chatDao.insertMessage(
            ChatMessage(
                text       = text,
                source     = MessageSource.SIGN,
                confidence = confidence,
                sessionId  = currentSessionId
            )
        )
    }


    suspend fun saveSpeechResult(text: String) {
        if (!isStorageEnabled()) return
        chatDao.insertMessage(
            ChatMessage(
                text      = text,
                source    = MessageSource.SPEECH,
                sessionId = currentSessionId
            )
        )
    }


    suspend fun saveTtsResult(text: String) {
        if (!isStorageEnabled()) return
        chatDao.insertMessage(
            ChatMessage(
                text      = text,
                source    = MessageSource.TTS,
                sessionId = currentSessionId
            )
        )
    }


    suspend fun saveSystemMessage(text: String) {
        chatDao.insertMessage(
            ChatMessage(
                text      = text,
                source    = MessageSource.SYSTEM,
                sessionId = currentSessionId
            )
        )
    }

    // ── DELETE ─────────────────────────────────────────────────

    suspend fun deleteMessage(message: ChatMessage) = chatDao.deleteMessage(message)
    suspend fun deleteSession(sessionId: String)    = chatDao.deleteSession(sessionId)
    suspend fun deleteAllMessages()                 = chatDao.deleteAllMessages()


    suspend fun applyRetentionPolicy(policy: RetentionPolicy) {
        val cutoffMs: Long = when (policy) {
            RetentionPolicy.FOREVER   -> return   // nothing to delete
            RetentionPolicy.ONE_DAY   ->
                System.currentTimeMillis() - 24L * 60 * 60 * 1000
            RetentionPolicy.ONE_WEEK  ->
                System.currentTimeMillis() - 7L * 24L * 60 * 60 * 1000
            RetentionPolicy.ONE_MONTH ->
                System.currentTimeMillis() - 30L * 24L * 60 * 60 * 1000
        }
        chatDao.deleteMessagesBefore(cutoffMs)
    }


    fun applyStartupRetention() {
        scope.launch {
            val settings = settingsDataStore.settings.first()
            applyRetentionPolicy(settings.retentionPolicy)
        }
    }

    // ── PRIVATE ───────────────────────────────────────────────
    private suspend fun isStorageEnabled(): Boolean =
        settingsDataStore.settings.first().storageEnabled
}