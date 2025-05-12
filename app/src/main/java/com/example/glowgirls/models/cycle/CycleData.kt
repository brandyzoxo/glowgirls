package com.example.glowgirls.models.cycle

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CycleData(
    val id: String = "",
    val lastPeriodDate: String = "",
    val cycleLength: String = "",
    val periodDuration: String = "",
    val nextPeriodDate: String = "",
    val ovulationDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val symptoms: List<String> = emptyList(),
    val mood: String = "",
    val flow: Flow = Flow.MEDIUM,
    val temperature: Float? = null,
    val metrics: Map<String, Float> = emptyMap(),
    val predictions: PredictionData? = null
) {
    // Helper extension function to convert string dates to LocalDate
    @RequiresApi(Build.VERSION_CODES.O)
    fun String.toLocalDate(): LocalDate? {
        return try {
            if (this.isNotEmpty()) {
                LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Calculate fertile window
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFertileWindow(): Pair<LocalDate?, LocalDate?> {
        val ovulationLocalDate = ovulationDate.toLocalDate() ?: return Pair(null, null)
        return Pair(
            ovulationLocalDate.minusDays(5),
            ovulationLocalDate.plusDays(1)
        )
    }

    // Calculate estimated cycle phases
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCyclePhases(): CyclePhases {
        val lastPeriodLocalDate = lastPeriodDate.toLocalDate() ?: return CyclePhases()
        val cycleLengthDays = cycleLength.toIntOrNull() ?: 28
        val periodDurationDays = periodDuration.toIntOrNull() ?: 5

        val follicularStart = lastPeriodLocalDate.plusDays(periodDurationDays.toLong())
        val ovulationLocalDate = lastPeriodDate.toLocalDate()?.plusDays((cycleLengthDays / 2 - 2).toLong())
        val lutealStart = ovulationLocalDate?.plusDays(1)
        val nextPeriodLocalDate = lastPeriodLocalDate.plusDays(cycleLengthDays.toLong())

        return CyclePhases(
            menstrual = PhaseData(
                start = lastPeriodLocalDate,
                end = lastPeriodLocalDate.plusDays(periodDurationDays.toLong() - 1),
                name = "Menstrual",
                description = "Your period phase"
            ),
            follicular = PhaseData(
                start = follicularStart,
                end = ovulationLocalDate?.minusDays(1) ?: follicularStart.plusDays(7),
                name = "Follicular",
                description = "Pre-ovulation phase"
            ),
            ovulatory = PhaseData(
                start = ovulationLocalDate,
                end = ovulationLocalDate,
                name = "Ovulation",
                description = "Peak fertility window"
            ),
            luteal = PhaseData(
                start = lutealStart,
                end = nextPeriodLocalDate.minusDays(1),
                name = "Luteal",
                description = "Post-ovulation phase"
            )
        )
    }

    // Calculate if currently in period
    @RequiresApi(Build.VERSION_CODES.O)
    fun isInPeriod(currentDate: LocalDate): Boolean {
        val lastPeriodLocalDate = lastPeriodDate.toLocalDate() ?: return false
        val cycleLengthDays = cycleLength.toIntOrNull() ?: 28
        val periodDurationDays = periodDuration.toIntOrNull() ?: 5

        // Check if within current period
        if (currentDate.isAfter(lastPeriodLocalDate.minusDays(1)) &&
            currentDate.isBefore(lastPeriodLocalDate.plusDays(periodDurationDays.toLong()))) {
            return true
        }

        // Check for previous cycles
        var cycleStartDate = lastPeriodLocalDate
        while (cycleStartDate.isBefore(currentDate.plusDays(1))) {
            val periodEndDate = cycleStartDate.plusDays(periodDurationDays.toLong())
            if (currentDate.isAfter(cycleStartDate.minusDays(1)) &&
                currentDate.isBefore(periodEndDate.plusDays(1))) {
                return true
            }
            cycleStartDate = cycleStartDate.plusDays(cycleLengthDays.toLong())
        }

        return false
    }

    // Get cycle day number (1 = first day of period)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCycleDay(currentDate: LocalDate): Int? {
        val lastPeriodLocalDate = lastPeriodDate.toLocalDate() ?: return null
        val cycleLengthDays = cycleLength.toIntOrNull() ?: return null

        // Calculate days since last period started
        val daysSinceStart = currentDate.toEpochDay() - lastPeriodLocalDate.toEpochDay()
        if (daysSinceStart < 0) return null

        // Calculate cycle day accounting for cycle length
        return (daysSinceStart % cycleLengthDays).toInt() + 1
    }
}

// Flow intensity enum



// Mood tracking enum

// Prediction data class for ML-based predictions

// Data class for representing cycle phases

// Data for a specific phase of the menstrual cycle

// Daily entry for tracking symptoms, mood and other metrics

// Health insights based on cycle data

