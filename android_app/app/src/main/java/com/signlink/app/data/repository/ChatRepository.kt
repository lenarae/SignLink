// ============================================================
// File: data/repository/ChatRepository.kt  [FIXED + COMPLETE]
// Purpose: Single source of truth for all chat persistence.
//
// FIXES applied:
//   1. storageEnabled is now checked before every insert.
//      If the user turns off "Save chat history" in Settings,
//      no messages are written to the DB at all.
//   2. retentionPolicy is now read from AppSettingsDataStore
//      and applied automatically on every app start via
//      applyStartupRetention(), called from SignLinkApp.
//   3. AppSettingsDataStore is injected (not coupled to Settings
//      screen) — repository owns the business logic.
// ============================================================

package com.signlink.app.data.repository

import com.signlink.app.data.local.AppSettings
import com.signlink.app.data.local.AppSettingsDataStore
import com.signlink.app.data.local.ChatDao
import com.signlink.app.data.local.ChatMessage
import com.signlink.app.data.local.MessageSource
import com.signlink.app.data.local.ChatRetentionPolicy
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

    /**
     * Save a gesture translation.
     * NO-OPs silently if storage is disabled in Settings.
     */
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

    /**
     * Save a speech-to-text result.
     * NO-OPs silently if storage is disabled in Settings.
     */
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

    /**
     * Save a system message (device connected, session started, etc).
     * System messages always save regardless of storageEnabled —
     * they are operational logs, not user content.
     */
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

    /**
     * Apply data retention policy — deletes messages older than the cutoff.
     * Called from:
     *   (a) SignLinkApp.onCreate() on every app start   ← NEW automatic call
     *   (b) SettingsViewModel when user changes policy  ← existing call
     */
    suspend fun applyRetentionPolicy(policy: ChatRetentionPolicy) {
        val cutoffMs: Long = when (policy) {
            ChatRetentionPolicy.FOREVER  -> return   // nothing to delete
            ChatRetentionPolicy.DISABLED -> return   // handled at insert time
            ChatRetentionPolicy.ONE_DAY  ->
                System.currentTimeMillis() - 24L * 60 * 60 * 1000
            ChatRetentionPolicy.ONE_MONTH ->
                System.currentTimeMillis() - 30L * 24L * 60 * 60 * 1000
        }
        chatDao.deleteMessagesBefore(cutoffMs)
    }

    /**
     * Called once on app start from SignLinkApp.
     * Reads the user's saved retention policy and applies it
     * so old messages are pruned automatically each launch.
     */
    fun applyStartupRetention() {
        scope.launch {
            val settings = settingsDataStore.settings.first()
            applyRetentionPolicy(settings.retentionPolicy)
        }
    }

    // ── PRIVATE ───────────────────────────────────────────────

    /**
     * Read the storageEnabled setting from DataStore.
     * Uses .first() to get the current snapshot synchronously
     * inside a suspend function.
     */
    private suspend fun isStorageEnabled(): Boolean =
        settingsDataStore.settings.first().storageEnabled
}