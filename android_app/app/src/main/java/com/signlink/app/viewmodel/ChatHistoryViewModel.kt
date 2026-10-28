package com.signlink.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signlink.app.data.local.ChatMessage
import com.signlink.app.data.local.ChatDao
import com.signlink.app.data.local.MessageSource
import com.signlink.app.data.local.SessionSummary
import com.signlink.app.data.repository.ChatRepository
import com.signlink.app.data.repository.RetentionPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HistoryViewMode { ALL_MESSAGES, BY_SESSION }

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val chatDao:        ChatDao           // injected separately for demo seeding
) : ViewModel() {

    private val _viewMode = MutableStateFlow(HistoryViewMode.ALL_MESSAGES)
    val viewMode: StateFlow<HistoryViewMode> = _viewMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val displayedMessages: StateFlow<List<ChatMessage>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) chatRepository.getAllMessages()
            else                 chatRepository.searchMessages(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sessionList: StateFlow<List<SessionSummary>> =
        chatRepository.getSessionList()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val messageCount: StateFlow<Int> =
        chatRepository.getMessageCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _selectedSessionId = MutableStateFlow<String?>(null)
    val selectedSessionId: StateFlow<String?> = _selectedSessionId.asStateFlow()

    val selectedSessionMessages: StateFlow<List<ChatMessage>> = _selectedSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) chatRepository.getMessagesBySession(sessionId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _showDeleteAllDialog = MutableStateFlow(false)
    val showDeleteAllDialog: StateFlow<Boolean> = _showDeleteAllDialog.asStateFlow()

    fun setViewMode(mode: HistoryViewMode)  { _viewMode.value = mode }
    fun setSearchQuery(query: String)        { _searchQuery.value = query }
    fun selectSession(sessionId: String)     { _selectedSessionId.value = sessionId }
    fun clearSessionSelection()              { _selectedSessionId.value = null }
    fun showDeleteAllDialog()                { _showDeleteAllDialog.value = true }
    fun hideDeleteAllDialog()                { _showDeleteAllDialog.value = false }

    fun deleteMessage(message: ChatMessage) {
        viewModelScope.launch { chatRepository.deleteMessage(message) }
    }
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (_selectedSessionId.value == sessionId) clearSessionSelection()
        }
    }
    fun deleteAllMessages() {
        viewModelScope.launch { chatRepository.deleteAllMessages(); hideDeleteAllDialog() }
    }
    fun applyRetentionPolicy(policy: RetentionPolicy) {
        viewModelScope.launch { chatRepository.applyRetentionPolicy(policy) }
    }

    /** Load demo data so the history screen isn't empty during testing */
    fun seedDemoData() {
        viewModelScope.launch {
            val sessionId = chatRepository.getCurrentSessionId()
            val demos = listOf(
                Triple("Hello",               MessageSource.SIGN,   0.95f),
                Triple("How are you?",        MessageSource.SPEECH, null),
                Triple("Yes",                 MessageSource.SIGN,   0.91f),
                Triple("Thank you very much", MessageSource.SPEECH, null),
                Triple("No",                  MessageSource.SIGN,   0.88f),
                Triple("Please help me",      MessageSource.SPEECH, null),
                Triple("Good morning",        MessageSource.SIGN,   0.97f),
                Triple("I love you",          MessageSource.SIGN,   0.93f)
            )
            demos.forEachIndexed { i, (text, src, conf) ->
                chatDao.insertMessage(
                    ChatMessage(
                        text        = text,
                        source      = src,
                        confidence  = conf,
                        sessionId   = sessionId,
                        timestampMs = System.currentTimeMillis() - (i * 45_000L)
                    )
                )
            }
        }
    }
}