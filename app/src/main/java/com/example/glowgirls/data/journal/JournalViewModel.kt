package com.example.glowgirls.data.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowgirls.models.journal.Emotion
import com.example.glowgirls.models.journal.JournalEntry
import com.example.glowgirls.repository.JournalRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class JournalViewModel : ViewModel() {
    private val repository = JournalRepository()
    private val auth = FirebaseAuth.getInstance()

    // UI state
    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _emotionStats = MutableStateFlow<Map<Emotion, Int>>(emptyMap())
    val emotionStats: StateFlow<Map<Emotion, Int>> = _emotionStats.asStateFlow()

    // Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Load journal entries for current user
    fun loadJournalEntries() {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            repository.getUserJournalEntriesFlow(userId).collect { entries ->
                _journalEntries.value = entries
            }
        }
    }

    // Save journal entry
    suspend fun saveJournalEntry(entry: JournalEntry): Boolean {
        _isLoading.value = true
        val result = repository.saveJournalEntry(entry)
        _isLoading.value = false
        return result
    }

    // Delete journal entry
    suspend fun deleteJournalEntry(entryId: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        _isLoading.value = true
        val result = repository.deleteJournalEntry(userId, entryId)
        _isLoading.value = false
        return result
    }

    // Get specific journal entry
    suspend fun getJournalEntry(userId: String, entryId: String): JournalEntry? {
        _isLoading.value = true
        val entry = repository.getJournalEntry(userId, entryId)
        _isLoading.value = false
        return entry
    }

    // Filter entries by emotion
    fun filterEntriesByEmotion(emotion: Emotion) {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            repository.getJournalEntriesByEmotion(userId, emotion).collect { entries ->
                _journalEntries.value = entries
            }
        }
    }

    // Load all entries
    fun loadAllEntries() {
        loadJournalEntries()
    }

    // Load emotion statistics for a specific time period
    fun loadEmotionStats(period: TimePeriod = TimePeriod.MONTH) {
        val userId = getCurrentUserId() ?: return

        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        when (period) {
            TimePeriod.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
            }
            TimePeriod.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
            }
            TimePeriod.YEAR -> {
                calendar.add(Calendar.YEAR, -1)
            }
        }

        val startDate = calendar.timeInMillis

        viewModelScope.launch {
            _isLoading.value = true
            val stats = repository.getEmotionStatistics(userId, startDate, endDate)
            _emotionStats.value = stats
            _isLoading.value = false
        }
    }
}

enum class TimePeriod {
    WEEK, MONTH, YEAR
}