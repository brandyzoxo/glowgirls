package com.example.glowgirls.data.cycle

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowgirls.models.cycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CycleViewModel : ViewModel() {

    private val TAG = "CycleViewModel"
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // StateFlow for cycle data
    private val _cycleData = MutableStateFlow<CycleData?>(null)
    val cycleData: StateFlow<CycleData?> = _cycleData

    // LiveData for daily entries
    private val _dailyEntries = MutableLiveData<List<DailyEntry>>(emptyList())
    val dailyEntries: LiveData<List<DailyEntry>> = _dailyEntries

    // LiveData for insights
    private val _insights = MutableLiveData<List<CycleInsight>>(emptyList())
    val insights: LiveData<List<CycleInsight>> = _insights

    // StateFlow for current cycle phase
    private val _currentPhase = MutableStateFlow<PhaseData?>(null)
    val currentPhase: StateFlow<PhaseData?> = _currentPhase

    // Error handling
    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Save or update cycle data in Firebase
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveCycleData(
        lastPeriodDate: String,
        cycleLength: String,
        periodDuration: String,
        nextPeriodDate: String,
        ovulationDate: String,
        notes: String = "",
        symptoms: List<String> = emptyList(),
        mood: String = "",
        flow: com.example.glowgirls.models.cycle.Flow = com.example.glowgirls.models.cycle.Flow.MEDIUM,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                val userId = mAuth.currentUser?.uid ?: run {
                    _errorMessage.value = "User not logged in"
                    return@launch
                }

                // Create advanced cycle data
                val cycleData = CycleData(
                    id = FirebaseDatabase.getInstance().reference.push().key ?: "",
                    lastPeriodDate = lastPeriodDate,
                    cycleLength = cycleLength,
                    periodDuration = periodDuration,
                    nextPeriodDate = nextPeriodDate,
                    ovulationDate = ovulationDate,
                    createdAt = System.currentTimeMillis(),
                    notes = notes,
                    symptoms = symptoms,
                    mood = mood,
                    flow = flow,
                    predictions = generatePredictions(lastPeriodDate, cycleLength, periodDuration)
                )

                val cycleRef = FirebaseDatabase.getInstance()
                    .getReference("Users/$userId/CycleData")

                // Save to Firebase
                cycleRef.push().setValue(cycleData).await()

                // Update local state
                _cycleData.value = cycleData
                updateCurrentPhase()

                // Generate insights based on new data
                generateInsights()

                // Record this as a daily entry too
                saveDailyEntry(
                    date = lastPeriodDate,
                    mood = mood,
                    symptoms = symptoms,
                    notes = "Period started",
                    flow = flow
                )

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Cycle Data Saved Successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving cycle data", e)
                _errorMessage.value = "Failed to save: ${e.localizedMessage}"
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to Save Cycle Data", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Get the most recent cycle data
     */
    fun getCurrentCycleData(context: Context) {
        viewModelScope.launch {
            try {
                val userId = mAuth.currentUser?.uid ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }

                val cycleRef = FirebaseDatabase.getInstance()
                    .getReference("Users/$userId/CycleData")

                // Get latest cycle data
                cycleRef.orderByChild("createdAt").limitToLast(1)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        @RequiresApi(Build.VERSION_CODES.O)
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (childSnapshot in snapshot.children) {
                                    val cycleData = childSnapshot.getValue(CycleData::class.java)
                                    _cycleData.value = cycleData
                                    updateCurrentPhase()
                                    generateInsights()
                                    return
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            _errorMessage.value = "Failed to load cycle data: ${error.message}"
                            Toast.makeText(
                                context,
                                "Failed to load cycle data: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    })
            } catch (e: Exception) {
                Log.e(TAG, "Error getting cycle data", e)
                _errorMessage.value = "Error: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Save a daily entry with symptoms, mood, etc.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveDailyEntry(
        date: String,
        mood: String = "",
        symptoms: List<String> = emptyList(),
        notes: String = "",
        flow: Flow? = null,
        temperature: Float? = null,
        sleep: Float? = null,
        energy: Int? = null,
        exercise: Int? = null,
        stress: Int? = null,
        context: Context? = null
    ) {
        viewModelScope.launch {
            try {
                val userId = mAuth.currentUser?.uid ?: run {
                    _errorMessage.value = "User not logged in"
                    return@launch
                }

                // Calculate cycle day
                val cycleDay = _cycleData.value?.let {
                    val currentDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    it.getCycleDay(currentDate)
                }

                val entry = DailyEntry(
                    id = FirebaseDatabase.getInstance().reference.push().key ?: "",
                    date = date,
                    cycleDay = cycleDay,
                    mood = mood,
                    symptoms = symptoms,
                    notes = notes,
                    flow = flow,
                    temperature = temperature,
                    sleep = sleep,
                    energy = energy,
                    exercise = exercise,
                    stress = stress
                )

                val entriesRef = FirebaseDatabase.getInstance()
                    .getReference("Users/$userId/DailyEntries")

                // Save to Firebase
                entriesRef.child(entry.id).setValue(entry).await()

                // Refresh entries
                loadDailyEntries()

                // Update insights based on new symptoms
                if (symptoms.isNotEmpty()) {
                    generateInsights()
                }

                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(it, "Entry Saved!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving daily entry", e)
                _errorMessage.value = "Failed to save entry: ${e.localizedMessage}"
                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(it, "Failed to save entry", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Load all daily entries for the user
     */
    fun loadDailyEntries() {
        viewModelScope.launch {
            try {
                val userId = mAuth.currentUser?.uid ?: run {
                    _errorMessage.value = "User not logged in"
                    return@launch
                }

                val entriesRef = FirebaseDatabase.getInstance()
                    .getReference("Users/$userId/DailyEntries")

                entriesRef.orderByChild("date").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val entries = mutableListOf<DailyEntry>()
                        for (childSnapshot in snapshot.children) {
                            childSnapshot.getValue(DailyEntry::class.java)?.let {
                                entries.add(it)
                            }
                        }
                        _dailyEntries.value = entries
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _errorMessage.value = "Failed to load entries: ${error.message}"
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Error loading daily entries", e)
                _errorMessage.value = "Error loading entries: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Update current cycle phase based on today's date
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCurrentPhase() {
        val currentCycle = _cycleData.value ?: return
        val today = LocalDate.now()
        val phases = currentCycle.getCyclePhases()

        _currentPhase.value = when {
            isDateInRange(today, phases.menstrual.start, phases.menstrual.end) -> phases.menstrual
            isDateInRange(today, phases.follicular.start, phases.follicular.end) -> phases.follicular
            isDateInRange(today, phases.ovulatory.start, phases.ovulatory.end) -> phases.ovulatory
            isDateInRange(today, phases.luteal.start, phases.luteal.end) -> phases.luteal
            else -> null
        }
    }

    /**
     * Generate predictive data based on historical cycles
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun generatePredictions(
        lastPeriod: String,
        cycleLength: String,
        periodDuration: String
    ): PredictionData {
        // In a real app, we would use more sophisticated ML model
        // This is a simplified version for demo purposes

        val userId = mAuth.currentUser?.uid ?: return PredictionData()

        try {
            val cycleRef = FirebaseDatabase.getInstance()
                .getReference("Users/$userId/CycleData")

            // Get historical data
            val snapshot = withContext(Dispatchers.IO) {
                cycleRef.orderByChild("createdAt").limitToLast(6)
                    .get().await()
            }

            if (!snapshot.exists() || snapshot.childrenCount < 3) {
                // Not enough historical data, use provided values
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val lastPeriodDate = LocalDate.parse(lastPeriod, formatter)
                val cycleLengthInt = cycleLength.toIntOrNull() ?: 28
                val nextPeriod = lastPeriodDate.plusDays(cycleLengthInt.toLong())

                return PredictionData(
                    predictedNextPeriod = nextPeriod.format(formatter),
                    predictedCycleLength = cycleLengthInt,
                    predictedPeriodDuration = periodDuration.toIntOrNull() ?: 5,
                    certainty = 0.7f
                )
            }

            // With real data, calculate average cycle length and variation
            val cycles = mutableListOf<CycleData>()
            snapshot.children.forEach {
                it.getValue(CycleData::class.java)?.let { cycle ->
                    cycles.add(cycle)
                }
            }

            // Sort by creation date
            cycles.sortBy { it.createdAt }

            // Calculate average cycle length from historical data
            var totalCycleLength = 0
            var count = 0

            for (cycle in cycles) {
                val length = cycle.cycleLength.toIntOrNull()
                if (length != null) {
                    totalCycleLength += length
                    count++
                }
            }

            // Calculate averages
            val avgCycleLength = if (count > 0) totalCycleLength / count else cycleLength.toIntOrNull() ?: 28

            // Calculate most common symptoms
            val symptomFrequency = mutableMapOf<String, Int>()
            cycles.forEach { cycle ->
                cycle.symptoms.forEach { symptom ->
                    symptomFrequency[symptom] = (symptomFrequency[symptom] ?: 0) + 1
                }
            }

            // Convert to probability (0-1 scale)
            val symptomPredictions = symptomFrequency.mapValues { (_, count) ->
                count.toFloat() / cycles.size
            }.filter { it.value > 0.5f } // Only include symptoms that appear in >50% of cycles

            // Calculate next period date
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val lastPeriodDate = LocalDate.parse(lastPeriod, formatter)
            val nextPeriod = lastPeriodDate.plusDays(avgCycleLength.toLong())

            // Calculate certainty based on consistency of historical data
            val certainty = if (count > 3) 0.85f else 0.7f

            return PredictionData(
                predictedNextPeriod = nextPeriod.format(formatter),
                predictedCycleLength = avgCycleLength,
                predictedPeriodDuration = periodDuration.toIntOrNull() ?: 5,
                certainty = certainty,
                symptomPredictions = symptomPredictions
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error generating predictions", e)

            // Fallback to basic prediction
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val lastPeriodDate = LocalDate.parse(lastPeriod, formatter)
            val cycleLengthInt = cycleLength.toIntOrNull() ?: 28
            val nextPeriod = lastPeriodDate.plusDays(cycleLengthInt.toLong())

            return PredictionData(
                predictedNextPeriod = nextPeriod.format(formatter),
                predictedCycleLength = cycleLengthInt,
                predictedPeriodDuration = periodDuration.toIntOrNull() ?: 5,
                certainty = 0.6f
            )
        }
    }

    /**
     * Generate health insights based on cycle data and symptoms
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateInsights() {
        viewModelScope.launch {
            val cycleData = _cycleData.value ?: return@launch
            val entries = _dailyEntries.value ?: emptyList()

            val insights = mutableListOf<CycleInsight>()

            // 1. Cycle regularity insight
            val cycleLengthInt = cycleData.cycleLength.toIntOrNull() ?: 28
            when {
                cycleLengthInt < 21 -> {
                    insights.add(CycleInsight(
                        id = "short_cycle",
                        title = "Short Cycle Length",
                        description = "Your cycle length is shorter than average. Short cycles (less than 21 days) may indicate hormonal imbalances.",
                        category = InsightCategory.MEDICAL,
                        relevancyScore = 0.9f,
                        recommendations = listOf(
                            "Consider consulting with a healthcare provider",
                            "Track your cycle consistency over the next few months",
                            "Note any unusual symptoms that occur during your cycle"
                        )
                    ))
                }
                cycleLengthInt > 35 -> {
                    insights.add(CycleInsight(
                        id = "long_cycle",
                        title = "Long Cycle Length",
                        description = "Your cycle length is longer than average. Long cycles may be normal for some, but could indicate hormonal changes.",
                        category = InsightCategory.GENERAL,
                        relevancyScore = 0.7f,
                        recommendations = listOf(
                            "Track your cycle consistency over several months",
                            "Consider consulting a healthcare provider if this is a recent change",
                            "Maintain a balanced diet and regular exercise routine"
                        )
                    ))
                }
            }

            // 2. Phase-specific insights
            when (currentPhase.value?.name) {
                "Menstrual" -> {
                    insights.add(CycleInsight(
                        id = "menstrual_nutrition",
                        title = "Menstrual Phase Nutrition",
                        description = "During your period, focus on iron-rich foods to replenish what's lost through bleeding.",
                        category = InsightCategory.NUTRITION,
                        relevancyScore = 0.95f,
                        recommendations = listOf(
                            "Include iron-rich foods like spinach, legumes, and lean red meat",
                            "Stay hydrated to reduce bloating",
                            "Foods rich in omega-3 fatty acids may help reduce inflammation"
                        )
                    ))

                    insights.add(CycleInsight(
                        id = "menstrual_exercise",
                        title = "Exercise During Period",
                        description = "Light to moderate exercise can help alleviate cramps and boost your mood.",
                        category = InsightCategory.FITNESS,
                        relevancyScore = 0.85f,
                        recommendations = listOf(
                            "Try gentle yoga or walking",
                            "Light cardio can help relieve cramps",
                            "Listen to your body and rest if needed"
                        )
                    ))
                }
                "Follicular" -> {
                    insights.add(CycleInsight(
                        id = "follicular_energy",
                        title = "Follicular Phase Energy",
                        description = "Your energy levels tend to rise during this phase as estrogen increases.",
                        category = InsightCategory.FITNESS,
                        relevancyScore = 0.8f,
                        recommendations = listOf(
                            "Good time for high-intensity workouts",
                            "Try new fitness classes or activities",
                            "Focus on strength training for optimal results"
                        )
                    ))
                }
                "Ovulation" -> {
                    insights.add(CycleInsight(
                        id = "peak_fertility",
                        title = "Peak Fertility Window",
                        description = "You're in your most fertile phase. Egg survival is typically 24 hours after ovulation.",
                        category = InsightCategory.FERTILITY,
                        relevancyScore = 1.0f,
                        recommendations = listOf(
                            "If trying to conceive, this is your optimal window",
                            "If preventing pregnancy, use additional protection",
                            "You may notice increased energy and libido during this time"
                        )
                    ))
                }
                "Luteal" -> {
                    insights.add(CycleInsight(
                        id = "luteal_mood",
                        title = "Luteal Phase Mood Support",
                        description = "Progesterone rises and then falls during this phase, which can affect mood.",
                        category = InsightCategory.MOOD,
                        relevancyScore = 0.85f,
                        recommendations = listOf(
                            "Prioritize self-care and stress management",
                            "Foods rich in vitamin B6 and magnesium may help stabilize mood",
                            "Gentle exercise like yoga or walking can help manage PMS symptoms"
                        )
                    ))
                }
            }

            // 3. Symptom-based insights
            val recentEntries = entries.filter {
                try {
                    val entryDate = LocalDate.parse(it.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val today = LocalDate.now()
                    ChronoUnit.DAYS.between(entryDate, today) <= 90 // Last 3 months
                } catch (e: Exception) {
                    false
                }
            }

            // Count symptom frequencies
            val symptomCounts = mutableMapOf<String, Int>()
            recentEntries.forEach { entry ->
                entry.symptoms.forEach { symptom ->
                    symptomCounts[symptom] = (symptomCounts[symptom] ?: 0) + 1
                }
            }

            // Find most common symptoms
            val commonSymptoms = symptomCounts.entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }

            // Headache insights
            if (commonSymptoms.contains("Headache")) {
                insights.add(CycleInsight(
                    id = "headache_pattern",
                    title = "Headache Pattern Detected",
                    description = "You frequently experience headaches during your cycle. This could be hormone-related.",
                    category = InsightCategory.MEDICAL,
                    relevancyScore = 0.8f,
                    recommendations = listOf(
                        "Track when headaches occur in relation to your cycle phases",
                        "Stay hydrated throughout your cycle",
                        "Consider over-the-counter pain relief before your period starts",
                        "If severe, consult with a healthcare provider about hormone-related migraines"
                    )
                ))
            }

            // Mood patterns
            if (commonSymptoms.contains("Mood Swings") || commonSymptoms.contains("Irritability")) {
                insights.add(CycleInsight(
                    id = "mood_patterns",
                    title = "Mood Pattern Insights",
                    description = "Your tracking shows mood changes are common in your cycle, particularly in the luteal phase.",
                    category = InsightCategory.MOOD,
                    relevancyScore = 0.85f,
                    recommendations = listOf(
                        "Consider adding B vitamins and magnesium-rich foods to your diet",
                        "Practice mindfulness or meditation during the luteal phase",
                        "Plan self-care activities for the week before your period",
                        "Track if certain foods or activities worsen mood changes"
                    )
                ))
            }

            // Fatigue patterns
            if (commonSymptoms.contains("Fatigue") || commonSymptoms.contains("Low Energy")) {
                insights.add(CycleInsight(
                    id = "energy_patterns",
                    title = "Energy Fluctuation Pattern",
                    description = "Your energy levels appear to fluctuate with your cycle phases.",
                    category = InsightCategory.GENERAL,
                    relevancyScore = 0.7f,
                    recommendations = listOf(
                        "Plan high-energy activities during follicular and ovulatory phases",
                        "Focus on iron-rich foods if fatigue occurs during menstruation",
                        "Ensure adequate sleep during the luteal phase when fatigue may increase",
                        "Consider B-complex supplements (after consulting a healthcare provider)"
                    )
                ))
            }

            // PMS symptom cluster
            val pmsSymptoms = listOf("Bloating", "Breast Tenderness", "Mood Swings", "Irritability", "Cravings")
            val userHasPmsSymptoms = commonSymptoms.any { it in pmsSymptoms }

            if (userHasPmsSymptoms) {
                insights.add(CycleInsight(
                    id = "pms_management",
                    title = "PMS Symptom Management",
                    description = "Your tracking shows several common PMS symptoms regularly occurring before your period.",
                    category = InsightCategory.GENERAL,
                    relevancyScore = 0.9f,
                    recommendations = listOf(
                        "Reduce salt, caffeine, and alcohol in the week before your period",
                        "Regular exercise can help reduce PMS symptoms",
                        "Consider calcium and magnesium supplements (consult a healthcare provider first)",
                        "Track which symptoms are most disruptive to better manage them"
                    )
                ))
            }

            // 4. Exercise insights based on activity tracking
            val exerciseEntries = recentEntries.filter { it.exercise != null && it.exercise > 0 }
            if (exerciseEntries.isNotEmpty()) {
                val averageExercise = exerciseEntries.sumOf { it.exercise ?: 0 } / exerciseEntries.size

                when {
                    averageExercise < 15 -> {
                        insights.add(CycleInsight(
                            id = "exercise_recommendation",
                            title = "Exercise Benefits",
                            description = "Regular exercise can help reduce PMS symptoms and regulate your cycle.",
                            category = InsightCategory.FITNESS,
                            relevancyScore = 0.75f,
                            recommendations = listOf(
                                "Aim for at least 30 minutes of moderate activity most days",
                                "Even short walks can help with menstrual cramps",
                                "Consider activities that vary with your cycle phase - more intense during follicular, gentler during luteal"
                            )
                        ))
                    }
                    averageExercise > 60 -> {
                        insights.add(CycleInsight(
                            id = "high_exercise",
                            title = "Exercise and Your Cycle",
                            description = "Your exercise levels are high, which is great! Be aware that very intense exercise can sometimes affect cycle regularity.",
                            category = InsightCategory.FITNESS,
                            relevancyScore = 0.7f,
                            recommendations = listOf(
                                "Consider reducing intensity during your period if you experience discomfort",
                                "Ensure you're getting adequate nutrition to support your activity level",
                                "Track if heavy exercise days correlate with any cycle changes"
                            )
                        ))
                    }
                }
            }

            // 5. Sleep insights
            val sleepEntries = recentEntries.filter { it.sleep != null }
            if (sleepEntries.isNotEmpty()) {
                val sleepByPhase = mutableMapOf<String, MutableList<Float>>()

                sleepEntries.forEach { entry ->
                    try {
                        val entryDate = LocalDate.parse(entry.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        val phases = cycleData.getCyclePhases()

                        val phase = when {
                            isDateInRange(entryDate, phases.menstrual.start, phases.menstrual.end) -> "Menstrual"
                            isDateInRange(entryDate, phases.follicular.start, phases.follicular.end) -> "Follicular"
                            isDateInRange(entryDate, phases.ovulatory.start, phases.ovulatory.end) -> "Ovulatory"
                            isDateInRange(entryDate, phases.luteal.start, phases.luteal.end) -> "Luteal"
                            else -> null
                        }

                        phase?.let {
                            if (!sleepByPhase.containsKey(it)) {
                                sleepByPhase[it] = mutableListOf()
                            }
                            entry.sleep?.let { sleep -> sleepByPhase[it]?.add(sleep) }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error analyzing sleep data", e)
                    }
                }

                // Find phase with worst sleep
                var worstSleepPhase: String? = null
                var worstSleepAvg = Float.MAX_VALUE

                sleepByPhase.forEach { (phase, sleepValues) ->
                    if (sleepValues.isNotEmpty()) {
                        val average = sleepValues.sum() / sleepValues.size
                        if (average < worstSleepAvg) {
                            worstSleepAvg = average
                            worstSleepPhase = phase
                        }
                    }
                }

                // Add sleep insight if a clear pattern is found
                if (worstSleepPhase != null && sleepByPhase.size >= 2) {
                    insights.add(CycleInsight(
                        id = "sleep_patterns",
                        title = "Sleep Quality Patterns",
                        description = "Your tracking shows changes in sleep quality during different cycle phases, with the $worstSleepPhase phase showing the most disruption.",
                        category = InsightCategory.GENERAL,
                        relevancyScore = 0.8f,
                        recommendations = listOf(
                            "Create a consistent sleep routine regardless of cycle phase",
                            "Consider relaxation techniques before bed during the $worstSleepPhase phase",
                            "Limit caffeine and alcohol, especially during phases with disrupted sleep",
                            "Track bedroom temperature - hormonal changes can affect your temperature comfort"
                        )
                    ))
                }
            }

            // Update the insights LiveData
            _insights.value = insights
        }
    }

    /**
     * Utility method to check if a date is within a range
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDateInRange(date: LocalDate, start: LocalDate?, end: LocalDate?): Boolean {
        if (start == null || end == null) return false
        return !date.isBefore(start) && !date.isAfter(end)
    }

    /**
     * Calculate days between two dates
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.DAYS.between(start, end)
    }

    /**
     * Check if user is in fertile window
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isInFertileWindow(): Boolean {
        val cycleData = _cycleData.value ?: return false
        val today = LocalDate.now()

        val (fertileStart, fertileEnd) = cycleData.getFertileWindow()
        return isDateInRange(today, fertileStart, fertileEnd)
    }

    /**
     * Get days until next period
     */
    /**
     * Get days until next period
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getDaysUntilNextPeriod(): Int? {
        val cycleData = _cycleData.value ?: return null

        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val nextPeriod = LocalDate.parse(cycleData.nextPeriodDate, formatter)
            val today = LocalDate.now()

            return ChronoUnit.DAYS.between(today, nextPeriod).toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating days until next period", e)
            return null
        }
    }

    /**
     * Get the phase for a specific date
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getPhaseForDate(date: LocalDate): PhaseData? {
        val cycleData = _cycleData.value ?: return null
        val phases = cycleData.getCyclePhases()

        return when {
            isDateInRange(date, phases.menstrual.start, phases.menstrual.end) -> phases.menstrual
            isDateInRange(date, phases.follicular.start, phases.follicular.end) -> phases.follicular
            isDateInRange(date, phases.ovulatory.start, phases.ovulatory.end) -> phases.ovulatory
            isDateInRange(date, phases.luteal.start, phases.luteal.end) -> phases.luteal
            else -> null
        }
    }

    /**
     * Get entry for a specific date
     */
    fun getEntryForDate(date: String): DailyEntry? {
        return _dailyEntries.value?.find { it.date == date }
    }

    /**
     * Delete a daily entry
     */
    fun deleteDailyEntry(entryId: String, context: Context? = null) {
        viewModelScope.launch {
            try {
                val userId = mAuth.currentUser?.uid ?: run {
                    _errorMessage.value = "User not logged in"
                    return@launch
                }

                val entryRef = FirebaseDatabase.getInstance()
                    .getReference("Users/$userId/DailyEntries/$entryId")

                entryRef.removeValue().await()

                // Refresh entries
                loadDailyEntries()

                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(it, "Entry Deleted!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting daily entry", e)
                _errorMessage.value = "Failed to delete entry: ${e.localizedMessage}"
                context?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(it, "Failed to delete entry", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Update cycle data when period arrives
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updatePeriodArrival(
        actualPeriodDate: String,
        flow: Flow = Flow.MEDIUM,
        symptoms: List<String> = emptyList(),
        mood: String = "",
        notes: String = "",
        context: Context
    ) {
        viewModelScope.launch {
            try {
                val userId = mAuth.currentUser?.uid ?: run {
                    _errorMessage.value = "User not logged in"
                    return@launch
                }

                // Get most recent cycle data
                val currentCycle = _cycleData.value ?: run {
                    _errorMessage.value = "No cycle data available"
                    return@launch
                }

                // Format dates
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val actualStartDate = LocalDate.parse(actualPeriodDate, formatter)
                val previousStartDate = LocalDate.parse(currentCycle.lastPeriodDate, formatter)

                // Calculate actual cycle length
                val actualCycleLength = ChronoUnit.DAYS.between(previousStartDate, actualStartDate).toInt()

                // Predict next cycle based on recent history
                val calculatedNextPeriod = actualStartDate.plusDays(actualCycleLength.toLong())
                val calculatedOvulation = actualStartDate.plusDays((actualCycleLength / 2 - 2).toLong())

                // Create new cycle data with updated predictions
                val newCycleData = CycleData(
                    id = FirebaseDatabase.getInstance().reference.push().key ?: "",
                    // rest of your properties...

                    lastPeriodDate = actualPeriodDate,
                    cycleLength = actualCycleLength.toString(),
                    periodDuration = currentCycle.periodDuration, // Keep same duration unless specified
                    nextPeriodDate = calculatedNextPeriod.format(formatter),
                    ovulationDate = calculatedOvulation.format(formatter),
                    createdAt = System.currentTimeMillis(),
                    notes = notes,
                    symptoms = symptoms,
                    mood = mood,
                    flow = flow,
                    predictions = generatePredictions(
                        actualPeriodDate,
                        actualCycleLength.toString(),
                        currentCycle.periodDuration
                    )
                )

                // Save to Firebase
                val cycleRef = FirebaseDatabase.getInstance()
                    .getReference("Users/$userId/CycleData")

                cycleRef.push().setValue(newCycleData).await()

                // Update local state
                _cycleData.value = newCycleData
                updateCurrentPhase()

                // Also save as a daily entry
                saveDailyEntry(
                    date = actualPeriodDate,
                    mood = mood,
                    symptoms = symptoms,
                    notes = "Period started",
                    flow = flow
                )

                // Generate new insights
                generateInsights()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Period Tracked Successfully!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating period arrival", e)
                _errorMessage.value = "Failed to update: ${e.localizedMessage}"
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to track period", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Get entries for a specific date range
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getEntriesForDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyEntry> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return _dailyEntries.value?.filter {
            try {
                val entryDate = LocalDate.parse(it.date, formatter)
                !entryDate.isBefore(startDate) && !entryDate.isAfter(endDate)
            } catch (e: Exception) {
                false
            }
        } ?: emptyList()
    }

    /**
     * Export cycle data to CSV format
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun exportCycleDataToCsv(): String {
        val cycleData = _cycleData.value
        val entries = _dailyEntries.value

        if (cycleData == null || entries.isNullOrEmpty()) {
            return "No data available for export"
        }

        val csvBuilder = StringBuilder()

        // Header
        csvBuilder.append("Date,Cycle Day,Phase,Mood,Symptoms,Flow,Notes,Sleep,Energy,Exercise,Stress\n")

        // Sort entries by date
        val sortedEntries = entries.sortedBy { it.date }

        // Add each entry as a row
        for (entry in sortedEntries) {
            try {
                val entryDate = LocalDate.parse(entry.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val phase = getPhaseForDate(entryDate)?.name ?: "Unknown"

                csvBuilder.append("${entry.date},")
                csvBuilder.append("${entry.cycleDay ?: ""},")
                csvBuilder.append("$phase,")
                csvBuilder.append("${entry.mood},")
                csvBuilder.append("\"${entry.symptoms.joinToString("; ")}\",")
                csvBuilder.append("${entry.flow?.displayName ?: ""},")
                // Escape notes to avoid CSV issues
                csvBuilder.append("\"${entry.notes.replace("\"", "\"\"")}\",")
                csvBuilder.append("${entry.sleep ?: ""},")
                csvBuilder.append("${entry.energy ?: ""},")
                csvBuilder.append("${entry.exercise ?: ""},")
                csvBuilder.append("${entry.stress ?: ""}\n")
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting entry for CSV", e)
                // Skip problematic entries
            }
        }

        return csvBuilder.toString()
    }

    /**
     * Calculate average cycle length based on historical data
     */
    suspend fun calculateAverageCycleLength(): Double? {
        val userId = mAuth.currentUser?.uid ?: return null

        try {
            val cycleRef = FirebaseDatabase.getInstance()
                .getReference("Users/$userId/CycleData")

            val snapshot = withContext(Dispatchers.IO) {
                cycleRef.orderByChild("createdAt")
                    .get().await()
            }

            if (!snapshot.exists() || snapshot.childrenCount < 2) {
                // Not enough data
                return null
            }

            val cycles = mutableListOf<CycleData>()
            snapshot.children.forEach {
                it.getValue(CycleData::class.java)?.let { cycle ->
                    cycles.add(cycle)
                }
            }

            // Sort by creation date
            cycles.sortBy { it.createdAt }

            var totalLength = 0
            var count = 0

            // Calculate average from valid cycle lengths
            for (cycle in cycles) {
                val length = cycle.cycleLength.toIntOrNull()
                if (length != null && length in 21..45) { // Filter out potential outliers
                    totalLength += length
                    count++
                }
            }

            return if (count > 0) totalLength.toDouble() / count else null
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating average cycle length", e)
            return null
        }
    }

    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * Reset all data (for log out)
     */
    fun clearAllData() {
        _cycleData.value = null
        _dailyEntries.value = emptyList()
        _insights.value = emptyList()
        _currentPhase.value = null
        _errorMessage.value = null
    }
}