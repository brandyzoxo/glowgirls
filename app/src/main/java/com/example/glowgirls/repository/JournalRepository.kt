package com.example.glowgirls.repository

import com.example.glowgirls.models.journal.Emotion
import com.example.glowgirls.models.journal.JournalEntry
import com.example.glowgirls.models.journal.JournalEntryConverter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JournalRepository {
    private val database = FirebaseDatabase.getInstance()
    private val journalsRef = database.getReference("journals")

    // Save or update a journal entry
    suspend fun saveJournalEntry(entry: JournalEntry): Boolean {
        return try {
            val entryId = entry.id.ifEmpty { journalsRef.push().key ?: return false }
            val journalToSave = entry.copy(id = entryId)

            journalsRef.child(entry.userId)
                .child(entryId)
                .updateChildren(JournalEntryConverter.toMap(journalToSave))
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Delete a journal entry
    suspend fun deleteJournalEntry(userId: String, entryId: String): Boolean {
        return try {
            journalsRef.child(userId)
                .child(entryId)
                .removeValue()
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    // Get a specific journal entry
    suspend fun getJournalEntry(userId: String, entryId: String): JournalEntry? {
        return try {
            val snapshot = journalsRef.child(userId)
                .child(entryId)
                .get()
                .await()

            if (snapshot.exists()) {
                val data = snapshot.value as? Map<String, Any?> ?: return null
                JournalEntryConverter.fromMap(data)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Get all journal entries for a user as a Flow
    fun getUserJournalEntriesFlow(userId: String): Flow<List<JournalEntry>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = mutableListOf<JournalEntry>()
                for (childSnapshot in snapshot.children) {
                    val data = childSnapshot.value as? Map<String, Any?> ?: continue
                    entries.add(JournalEntryConverter.fromMap(data))
                }
                trySend(entries.sortedByDescending { it.date })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        journalsRef.child(userId).addValueEventListener(listener)

        awaitClose {
            journalsRef.child(userId).removeEventListener(listener)
        }
    }

    // Filter journal entries by emotion
    fun getJournalEntriesByEmotion(userId: String, emotion: Emotion): Flow<List<JournalEntry>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = mutableListOf<JournalEntry>()
                for (childSnapshot in snapshot.children) {
                    val data = childSnapshot.value as? Map<String, Any?> ?: continue
                    val entry = JournalEntryConverter.fromMap(data)
                    if (entry.emotion == emotion) {
                        entries.add(entry)
                    }
                }
                trySend(entries.sortedByDescending { it.date })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        journalsRef.child(userId).addValueEventListener(listener)

        awaitClose {
            journalsRef.child(userId).removeEventListener(listener)
        }
    }

    // Get journal statistics
    suspend fun getEmotionStatistics(userId: String, startDate: Long, endDate: Long): Map<Emotion, Int> {
        val stats = Emotion.values().associateWith { 0 }.toMutableMap()

        try {
            val snapshot = journalsRef.child(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                for (childSnapshot in snapshot.children) {
                    val data = childSnapshot.value as? Map<String, Any?> ?: continue
                    val entry = JournalEntryConverter.fromMap(data)

                    if (entry.date in startDate..endDate) {
                        stats[entry.emotion] = stats[entry.emotion]!! + 1
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error
        }

        return stats
    }
}