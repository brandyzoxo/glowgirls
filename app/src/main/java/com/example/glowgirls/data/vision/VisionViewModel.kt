package com.example.glowgirls.data.vision

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowgirls.models.vision.Vision
import com.example.glowgirls.repository.VisionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisionViewModel : ViewModel() {
    private val repository = VisionRepository()
    val visions = mutableStateListOf<Vision>()
    val motivationalQuote = mutableStateOf(getRandomQuote())
    val categories = listOf("General", "Career", "Personal", "Health", "Education")
    val templates = listOf(
        Vision(
            title = "Career Growth",
            description = "Achieve a promotion within a year",
            category = "Career"
        ),
        Vision(
            title = "Fitness Goal",
            description = "Run a marathon in 6 months",
            category = "Health"
        )
    )
    val completionStats = mutableStateOf<Map<String, Int>>(emptyMap())
    private var quoteIndex = 0

    init {
        loadVisions()
        rotateQuote()
    }

    private fun loadVisions() {
        viewModelScope.launch {
            visions.clear()
            visions.addAll(repository.getVisions().sortedBy { it.priority })
            updateStats()
        }
    }

    fun addVision(vision: Vision, imageUri: Uri?, context: Context) {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val newVision = vision.copy(date = date)
            repository.addVision(newVision, imageUri)
            loadVisions()
        }
    }

    fun updateVision(vision: Vision, imageUri: Uri?, context: Context) {
        viewModelScope.launch {
            repository.updateVision(vision, imageUri)
            loadVisions()
        }
    }

    fun deleteVision(id: String) {
        viewModelScope.launch {
            repository.deleteVision(id)
            loadVisions()
        }
    }

    fun updateProgress(vision: Vision, newProgress: Int) {
        viewModelScope.launch {
            try {
                val updatedVision = vision.copy(
                    progress = newProgress.coerceIn(0, 100), // Ensure progress is within bounds
                    streak = if (newProgress > vision.progress) vision.streak + 1 else vision.streak
                )
                // Update the backend (e.g., Firebase)
                repository.updateVision(updatedVision)
                // Update the local visions list directly
                val index = visions.indexOfFirst { it.id == vision.id }
                if (index != -1) {
                    visions[index] = updatedVision
                }
                // Update completion stats
                updateStats()
            } catch (e: Exception) {
                Log.e("VisionViewModel", "Error updating progress", e)
                // Optionally notify UI of error
            }
        }
    }
    fun reorderVisions(newOrder: List<Vision>) {
        viewModelScope.launch {
            newOrder.forEachIndexed { index, vision ->
                repository.updateVision(vision.copy(priority = index))
            }
            loadVisions()
        }
    }

    fun refreshQuote() {
        motivationalQuote.value = getRandomQuote()
    }

    private fun rotateQuote() {
        viewModelScope.launch {
            while (true) {
                delay(30_000) // Rotate every 30 seconds
                motivationalQuote.value = getRandomQuote()
            }
        }
    }

    private fun getRandomQuote(): String {
        val quotes = listOf(
            "The future belongs to those who believe in their dreams.",
            "Your goals are the road maps to success.",
            "Dream big, work hard, stay focused.",
            "Every step brings you closer to your vision."
        )
        quoteIndex = (quoteIndex + 1) % quotes.size
        return quotes[quoteIndex]
    }

    private fun updateStats() {
        val total = visions.size
        val completed = visions.count { it.progress == 100 }
        completionStats.value = mapOf(
            "Total" to total,
            "Completed" to completed,
            "In Progress" to (total - completed)
        )
    }
}