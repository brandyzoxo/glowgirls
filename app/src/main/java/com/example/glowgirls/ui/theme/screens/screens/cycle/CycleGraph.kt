package com.example.glowgirls.ui.theme.screens.screens.cycle

import androidx.lifecycle.Observer
import androidx.lifecycle.LiveData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.example.glowgirls.models.cycle.CycleData
import com.example.glowgirls.models.cycle.Flow
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.glowgirls.data.cycle.CycleViewModel
import com.example.glowgirls.models.cycle.DailyEntry
import com.example.glowgirls.models.cycle.PhaseData
import com.example.glowgirls.ui.theme.*

// Added imports for charting library
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.startAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
// PROBLEM:
// LocalContext.current can only be used inside a @Composable function or
// within a composable scope (like LaunchedEffect, remember, etc.)

// SOLUTION:
// Move the context retrieval inside a composable scope

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CycleGraphScreen(
    navController: NavController,
    cycleViewModel: CycleViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val cycleData by cycleViewModel.cycleData.collectAsState()
    val entries by cycleViewModel.dailyEntries.observeAsState(emptyList())
    val currentPhase by cycleViewModel.currentPhase.collectAsState()

    val context = LocalContext.current // Get context here, inside the composable
    val scrollState = rememberScrollState()

    // Load data if not already loaded
    LaunchedEffect(Unit) {
        if (cycleData == null) {
            cycleViewModel.getCurrentCycleData(context) // Use the local context variable
        }
        if (entries.isEmpty()) {
            cycleViewModel.loadDailyEntries()
        }
    }

    // Rest of your code...
   // State for selected graph type
    var selectedGraphType by remember { mutableStateOf("Cycle") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Bar with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Cycle Analytics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Graph type selection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GraphTypeButton(
                text = "Cycle",
                isSelected = selectedGraphType == "Cycle",
                onClick = { selectedGraphType = "Cycle" }
            )

            GraphTypeButton(
                text = "Symptoms",
                isSelected = selectedGraphType == "Symptoms",
                onClick = { selectedGraphType = "Symptoms" }
            )

            GraphTypeButton(
                text = "Mood",
                isSelected = selectedGraphType == "Mood",
                onClick = { selectedGraphType = "Mood" }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show loading indicator if data is not available
        if (cycleData == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentColor)
            }
        } else {
            // Show selected graph
            when (selectedGraphType) {
                "Cycle" -> CycleGraph(entries, currentPhase)
                "Symptoms" -> SymptomsBarGraph(entries)
                "Mood" -> MoodGraph(entries)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Additional stats section
        cycleData?.let { cycle ->
            CycleStatsSection(cycle, entries)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CycleGraph(entries: List<DailyEntry>, currentPhase: PhaseData?) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    // Get entries from the last 60 days
    val startDay = today.minusDays(60)
    val endDay = today.plusDays(14)  // Include some future days for predictions

    // Process entries into chart data
    val dataPoints = mutableListOf<com.patrykandpatrick.vico.core.entry.ChartEntry>()
    val dayLabels = mutableListOf<String>()

    var currentDay = startDay
    while (!currentDay.isAfter(endDay)) {
        val dayStr = currentDay.format(formatter)
        val entry = entries.find { it.date == dayStr }

        // Add point for cycle day (if available) or based on calculation
        val cycleDay = entry?.cycleDay ?: calculateCycleDay(currentDay, entries)

        if (cycleDay != null) {
            dataPoints.add(entryOf(dataPoints.size.toFloat(), cycleDay.toFloat()))

            // Add label for every 7th day
            if (ChronoUnit.DAYS.between(startDay, currentDay) % 7 == 0L) {
                dayLabels.add(currentDay.format(DateTimeFormatter.ofPattern("MM/dd")))
            } else {
                dayLabels.add("")
            }
        }

        currentDay = currentDay.plusDays(1)
    }

    // Create chart model
    val chartEntryModel = ChartEntryModelProducer(dataPoints).getModel()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your Cycle Pattern",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display the phase indicator
            currentPhase?.let { phase ->
                // Assumes PhaseData has a 'name' property
                Text(
                    text = "Current Phase: ${phase.name}",
                    fontSize = 14.sp,
                    color = getPhaseStyling(phase.name).color
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (dataPoints.isNotEmpty()) {
                ProvideChartStyle {
                    Chart(
                        chart = lineChart(),
                        model = chartEntryModel,
                        startAxis = startAxis(),
                        bottomAxis = bottomAxis(
                            valueFormatter = { value, _ ->
                                val index = value.toInt()
                                if (index >= 0 && index < dayLabels.size) {
                                    dayLabels[index]
                                } else {
                                    ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Not enough cycle data to generate graph")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SymptomsBarGraph(entries: List<DailyEntry>) {
    // Count frequency of each symptom
    val symptomCounts = mutableMapOf<String, Int>()

    entries.forEach { entry ->
        entry.symptoms.forEach { symptom ->
            symptomCounts[symptom] = (symptomCounts[symptom] ?: 0) + 1
        }
    }

    // Sort symptoms by frequency and take top 8
    val topSymptoms = symptomCounts.entries
        .sortedByDescending { it.value }
        .take(8)

    // Create bar chart data
    val dataPoints = topSymptoms.mapIndexed { index, entry ->
        entryOf(index.toFloat(), entry.value.toFloat())
    }

    val chartEntryModel = ChartEntryModelProducer(dataPoints).getModel()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Common Symptoms",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (topSymptoms.isNotEmpty()) {
                ProvideChartStyle {
                    Chart(
                        chart = columnChart(),
                        model = chartEntryModel,
                        startAxis = startAxis(),
                        bottomAxis = bottomAxis(
                            valueFormatter = { value, _ ->
                                val index = value.toInt()
                                if (index >= 0 && index < topSymptoms.size) {
                                    val symptom = topSymptoms[index].key
                                    if (symptom.length > 10) {
                                        symptom.substring(0, 7) + "..."
                                    } else {
                                        symptom
                                    }
                                } else {
                                    ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                // Legend for longer symptom names
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    topSymptoms.forEachIndexed { index, entry ->
                        if (entry.key.length > 10) {
                            Text(
                                text = "${index + 1}: ${entry.key}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No symptom data recorded yet")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoodGraph(entries: List<DailyEntry>) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    // Get entries from the last 30 days with mood data
    val startDay = today.minusDays(30)

    // Filter entries with mood data in the date range
    val moodEntries = entries.filter { entry ->
        try {
            val entryDate = LocalDate.parse(entry.date, formatter)
            !entryDate.isBefore(startDay) && !entryDate.isAfter(today) && entry.mood.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }.sortedBy { it.date }

    // Map moods to numerical values for the graph
    val moodValues = mapOf(
        "Happy" to 5f,
        "Good" to 4f,
        "Neutral" to 3f,
        "Sad" to 2f,
        "Irritable" to 2f,
        "Anxious" to 2f,
        "Depressed" to 1f
    )

    // Process entries into chart data
    val dataPoints = moodEntries.mapIndexed { index, entry ->
        val moodValue = moodValues[entry.mood] ?: 3f
        entryOf(index.toFloat(), moodValue)
    }

    val chartEntryModel = ChartEntryModelProducer(dataPoints).getModel()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Mood Tracking",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (moodEntries.isNotEmpty()) {
                ProvideChartStyle {
                    Chart(
                        chart = lineChart(),
                        model = chartEntryModel,
                        startAxis = startAxis(
                            valueFormatter = { value, _ ->
                                when (value.toInt()) {
                                    5 -> "Happy"
                                    4 -> "Good"
                                    3 -> "Neutral"
                                    2 -> "Low"
                                    1 -> "Very Low"
                                    else -> ""
                                }
                            }
                        ),
                        bottomAxis = bottomAxis(
                            valueFormatter = { value, _ ->
                                val index = value.toInt()
                                if (index >= 0 && index < moodEntries.size) {
                                    try {
                                        val date = LocalDate.parse(moodEntries[index].date, formatter)
                                        date.format(DateTimeFormatter.ofPattern("MM/dd"))
                                    } catch (e: Exception) {
                                        ""
                                    }
                                } else {
                                    ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No mood data recorded in the last 30 days")
                }
            }
        }
    }
}

@Composable
fun GraphTypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AccentColor else Color.LightGray.copy(alpha = 0.5f),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(text)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CycleStatsSection(cycle: CycleData, entries: List<DailyEntry>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cycle Statistics",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            StatRow("Cycle Length", "${cycle.cycleLength} days")
            StatRow("Period Duration", "${cycle.periodDuration} days")

            // Calculate most common symptoms
            val topSymptoms = entries
                .flatMap { it.symptoms }
                .groupingBy { it }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }
                .joinToString(", ")

            StatRow("Common Symptoms", if (topSymptoms.isBlank()) "None recorded" else topSymptoms)

            // Calculate average mood
            val moodCounts = entries
                .filter { it.mood.isNotBlank() }
                .groupingBy { it.mood }
                .eachCount()

            val mostCommonMood = moodCounts.entries
                .maxByOrNull { it.value }
                ?.key ?: "No data"

            StatRow("Most Common Mood", mostCommonMood)
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateCycleDay(date: LocalDate, entries: List<DailyEntry>): Int? {
    // Find the most recent period start before this date
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val periodEntries = entries
        .filter { entry ->
            // Assumes DailyEntry.flow is of type Flow
            entry.flow != null && entry.flow != Flow.MEDIUM
        }
        .sortedByDescending { it.date }

    // Find the most recent period start date before or on the given date
    val mostRecentPeriod = periodEntries.firstOrNull { entry ->
        try {
            val entryDate = LocalDate.parse(entry.date, formatter)
            !entryDate.isAfter(date)
        } catch (e: Exception) {
            false
        }
    }

    return mostRecentPeriod?.let {
        try {
            val periodStart = LocalDate.parse(it.date, formatter)
            ChronoUnit.DAYS.between(periodStart, date).toInt() + 1
        } catch (e: Exception) {
            null
        }
    }
}

// Helper function to get styling based on cycle phase
private fun getPhaseStyling(phaseName: String): PhaseStyling {
    return when (phaseName) {
        "Menstrual" -> PhaseStyling(Color(0xFFE57373)) // Reddish
        "Follicular" -> PhaseStyling(Color(0xFF81C784)) // Greenish
        "Ovulatory" -> PhaseStyling(Color(0xFF64B5F6)) // Blueish
        "Luteal" -> PhaseStyling(Color(0xFFFFB74D)) // Orangish
        else -> PhaseStyling(Color.Gray)
    }
}

data class PhaseStyling(
    val color: Color
)

// Extensions for observeAsState
@Composable
fun <T> LiveData<T>.observeAsState(initial: T): State<T> {
    val state = remember { mutableStateOf(initial) }

    // Capture the lifecycle owner from composition
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(this) {
        val observer = Observer<T> { value ->
            if (value != null) {
                state.value = value
            }
        }

        // Use the captured lifecycleOwner variable
        observe(lifecycleOwner, observer)

        onDispose {
            removeObserver(observer)
        }
    }

    return state
}