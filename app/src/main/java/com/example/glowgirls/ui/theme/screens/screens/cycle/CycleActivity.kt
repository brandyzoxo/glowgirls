package com.example.glowgirls.ui.theme.screens.screens.cycle

import android.R.attr.contentDescription
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.collectAsState
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.glowgirls.data.cycle.CycleViewModel
import com.example.glowgirls.models.cycle.*
import com.example.glowgirls.navigation.ROUTE_CYCLE_GRAPH
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Feminine color palette
val FemininePink = Color(0xFFF5A8C7)
val SoftPurple = Color(0xFFD4A5F5)
val PastelPeach = Color(0xFFFFE4E1)
val GradientColors = listOf(FemininePink, SoftPurple, PastelPeach)
val AccentColor = Color(0xFFD76BA2) // Rich pink
val SecondaryAccent = Color(0xFF9C6BC5) // Purple accent
val TextColor = Color(0xFF4A3466) // Deep purple for text
val LightTextColor = Color(0xFF8A8AA0) // Lighter text
val CardColor = Color.White

// Missing enums implementation

enum class CalendarHighlight(val color: Color, val description: String) {
    LAST_PERIOD(FemininePink, "Last Period Start"),
    NEXT_PERIOD(AccentColor, "Next Period Start"),
    PERIOD_DURATION(PastelPeach, "Period Duration"),
    OVULATION(SecondaryAccent, "Ovulation Day"),
    FERTILE_WINDOW(Color(0xFFB2DFDB), "Fertile Window")
}

// Extension function for String to LocalDate conversion
@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalDate(): LocalDate? {
    return if (this.isNotEmpty()) {
        try {
            LocalDate.parse(this, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}

// Composable functions moved outside the main function
@Composable
fun ResultRow(icon: ImageVector, label: String, value: String, iconTint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PastelPeach, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(color = LightTextColor)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextColor
                )
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = LightTextColor
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarDay(
    date: LocalDate,
    currentMonth: Month,
    highlight: CalendarHighlight?
) {
    val isCurrentMonth = date.month == currentMonth
    val today = LocalDate.now()
    val isToday = date == today

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(6.dp)
            .clip(CircleShape)
            .background(
                when {
                    highlight != null -> highlight.color
                    isToday -> AccentColor.copy(alpha = 0.2f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday) 1.5.dp else 0.dp,
                color = if (isToday) AccentColor else Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
                color = when {
                    !isCurrentMonth -> TextColor.copy(alpha = 0.3f)
                    else -> TextColor
                },
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CustomCalendar(
    currentYearMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    highlightedDates: Map<LocalDate, CalendarHighlight>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardColor.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(currentYearMonth.minusMonths(1)) }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month", tint = AccentColor)
            }
            Text(
                text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
            IconButton(onClick = { onMonthChanged(currentYearMonth.plusMonths(1)) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month", tint = AccentColor)
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            for (day in DayOfWeek.values()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).first().toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextColor,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
        val days = buildCalendarDays(currentYearMonth)
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            content = {
                items(days) { date ->
                    CalendarDay(
                        date = date,
                        currentMonth = currentYearMonth.month,
                        highlight = highlightedDates[date]
                    )
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildCalendarDays(yearMonth: YearMonth): List<LocalDate> {
    val firstOfMonth = yearMonth.atDay(1)
    val firstDayOfGrid = firstOfMonth.minusDays(firstOfMonth.dayOfWeek.value.toLong() - 1)
    return (0 until 42).map { firstDayOfGrid.plusDays(it.toLong()) }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleScreen(navController: NavController, cycleViewModel: CycleViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var lastPeriodDate by remember { mutableStateOf("") }
    var cycleLength by remember { mutableStateOf("") }
    var periodDuration by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedSymptoms by remember { mutableStateOf(listOf<String>()) }
    var selectedMood by remember { mutableStateOf("") }
    var selectedFlow by remember { mutableStateOf(Flow.MEDIUM) }
    var showResults by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSymptomsDialog by remember { mutableStateOf(false) }
    var showMoodDialog by remember { mutableStateOf(false) }
    var expandedFlow by remember { mutableStateOf(false) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var lastPeriodLocalDate by remember { mutableStateOf<LocalDate?>(null) }

    // ViewModel state
    val cycleData by cycleViewModel.cycleData.collectAsState(initial = null)
    val dailyEntries by cycleViewModel.dailyEntries.observeAsState(emptyList())
    val insights by cycleViewModel.insights.observeAsState(emptyList())
    val currentPhase by cycleViewModel.currentPhase.collectAsState(initial = null)
    val errorMessage by cycleViewModel.errorMessage.observeAsState(null)

    // Date picker state
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = lastPeriodLocalDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            ?: System.currentTimeMillis()
    )

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }

    // Load current cycle data on composition
    LaunchedEffect(Unit) {
        cycleViewModel.getCurrentCycleData(context)
    }

    // Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            cycleViewModel.clearErrorMessage()
        }
    }

    // Sync UI state with ViewModel data
    LaunchedEffect(cycleData) {
        cycleData?.let { data ->
            lastPeriodDate = data.lastPeriodDate
            cycleLength = data.cycleLength
            periodDuration = data.periodDuration
            lastPeriodLocalDate = data.lastPeriodDate.toLocalDate()
            showResults = true
        }
    }

    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(GradientColors))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bloom",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SecondaryAccent,
                        fontSize = 32.sp
                    )
                )
                IconButton(onClick = { /* TODO: Open menu */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = AccentColor)
                }
            }

            // Title
            Text(
                text = "Track Your Cycle",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextColor,
                    fontSize = 24.sp
                )
            )

            // Current Phase Card
            currentPhase?.let { phase ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Current Phase: ${phase.name}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextColor
                            )
                        )
                        Text(
                            text = phase.description,
                            style = MaterialTheme.typography.bodyMedium.copy(color = LightTextColor)
                        )
                    }
                }
            }

            // Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Last Period Date
                    OutlinedTextField(
                        value = lastPeriodDate,
                        onValueChange = { /* Read-only */ },
                        label = { Text("Last Period Date", color = LightTextColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showDatePicker = true },
                        readOnly = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFBF6FF),
                            unfocusedContainerColor = Color(0xFFFBF6FF),
                            focusedIndicatorColor = AccentColor,
                            unfocusedIndicatorColor = Color(0xFFE5D9EB),
                            focusedLabelColor = AccentColor,
                            unfocusedLabelColor = LightTextColor,
                            cursorColor = AccentColor
                        ),
                        trailingIcon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = AccentColor,
                                modifier = Modifier.clickable { showDatePicker = true }
                            )
                        }
                    )

                    // Cycle Length
                    OutlinedTextField(
                        value = cycleLength,
                        onValueChange = { cycleLength = it },
                        label = { Text("Cycle Length (days)", color = LightTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFBF6FF),
                            unfocusedContainerColor = Color(0xFFFBF6FF),
                            focusedIndicatorColor = AccentColor,
                            unfocusedIndicatorColor = Color(0xFFE5D9EB),
                            focusedLabelColor = AccentColor,
                            unfocusedLabelColor = LightTextColor,
                            cursorColor = AccentColor
                        )
                    )

                    // Period Duration
                    OutlinedTextField(
                        value = periodDuration,
                        onValueChange = { periodDuration = it },
                        label = { Text("Period Duration (days)", color = LightTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFBF6FF),
                            unfocusedContainerColor = Color(0xFFFBF6FF),
                            focusedIndicatorColor = AccentColor,
                            unfocusedIndicatorColor = Color(0xFFE5D9EB),
                            focusedLabelColor = AccentColor,
                            unfocusedLabelColor = LightTextColor,
                            cursorColor = AccentColor
                        )
                    )

                    // Symptoms
                    OutlinedButton(
                        onClick = { showSymptomsDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AccentColor)
                    ) {
                        Text(
                            text = if (selectedSymptoms.isEmpty()) "Select Symptoms" else selectedSymptoms.joinToString(),
                            color = AccentColor
                        )
                    }

                    // Mood
                    OutlinedButton(
                        onClick = { showMoodDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AccentColor)
                    ) {
                        Text(
                            text = selectedMood.ifEmpty { "Select Mood" },
                            color = AccentColor
                        )
                    }

                    // Flow
                    ExposedDropdownMenuBox(
                        expanded = expandedFlow,
                        onExpandedChange = { expandedFlow = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedFlow.displayName,
                            onValueChange = {},
                            label = { Text("Flow", color = LightTextColor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFBF6FF),
                                unfocusedContainerColor = Color(0xFFFBF6FF),
                                focusedIndicatorColor = AccentColor,
                                unfocusedIndicatorColor = Color(0xFFE5D9EB),
                                focusedLabelColor = AccentColor,
                                unfocusedLabelColor = LightTextColor
                            ),
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFlow)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFlow,
                            onDismissRequest = { expandedFlow = false }
                        ) {
                            Flow.values().forEach { flow ->
                                DropdownMenuItem(
                                    text = { Text(flow.displayName) },
                                    onClick = {
                                        selectedFlow = flow
                                        expandedFlow = false
                                    }
                                )
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes", color = LightTextColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFBF6FF),
                            unfocusedContainerColor = Color(0xFFFBF6FF),
                            focusedIndicatorColor = AccentColor,
                            unfocusedIndicatorColor = Color(0xFFE5D9EB),
                            focusedLabelColor = AccentColor,
                            unfocusedLabelColor = LightTextColor,
                            cursorColor = AccentColor
                        )
                    )

                    // Save Cycle Data
                    Button(
                        onClick = {
                            if (lastPeriodDate.isNotEmpty() && cycleLength.isNotEmpty() && periodDuration.isNotEmpty()) {
                                cycleViewModel.saveCycleData(
                                    lastPeriodDate = lastPeriodDate,
                                    cycleLength = cycleLength,
                                    periodDuration = periodDuration,
                                    nextPeriodDate = cycleData?.nextPeriodDate ?: "",
                                    ovulationDate = cycleData?.ovulationDate ?: "",
                                    notes = notes,
                                    symptoms = selectedSymptoms,
                                    mood = selectedMood,
                                    flow = selectedFlow,
                                    context = context
                                )
                            } else {
                                Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                    ) {
                        Text("Save Cycle", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Results Card
            AnimatedVisibility(
                visible = showResults && cycleData != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                cycleData?.let { data ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Cycle Overview",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextColor
                                )
                            )
                            Divider(color = Color(0xFFEDE0F5), thickness = 1.dp)
                            ResultRow(Icons.Default.DateRange, "Next Period", data.nextPeriodDate, AccentColor)
                            ResultRow(Icons.Default.Star, "Ovulation Date", data.ovulationDate, SecondaryAccent)
                            cycleViewModel.getDaysUntilNextPeriod()?.let {
                                ResultRow(Icons.Default.Timer, "Days Until Next Period", it.toString(), AccentColor)
                            }
                            if (cycleViewModel.isInFertileWindow()) {
                                ResultRow(Icons.Default.Favorite, "Fertile Window", "Active Now", SecondaryAccent)
                            }
                        }
                    }
                }
            }

            // Calendar Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Month", tint = AccentColor)
                        }
                        Text(
                            text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextColor
                            )
                        )
                        IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Month", tint = AccentColor)
                        }
                    }
                    CustomCalendar(
                        currentYearMonth = currentYearMonth,
                        onMonthChanged = { currentYearMonth = it },
                        highlightedDates = buildMap {
                            lastPeriodLocalDate?.let { put(it, CalendarHighlight.LAST_PERIOD) }
                            cycleData?.nextPeriodDate?.toLocalDate()?.let { nextPeriod ->
                                put(nextPeriod, CalendarHighlight.NEXT_PERIOD)
                                periodDuration.toIntOrNull()?.let { duration ->
                                    for (i in 0 until duration) {
                                        put(nextPeriod.plusDays(i.toLong()), CalendarHighlight.PERIOD_DURATION)
                                    }
                                }
                            }
                            cycleData?.ovulationDate?.toLocalDate()?.let { ovulation ->
                                put(ovulation, CalendarHighlight.OVULATION)
                                for (i in -2..2) {
                                    if (i != 0) {
                                        put(ovulation.plusDays(i.toLong()), CalendarHighlight.FERTILE_WINDOW)
                                    }
                                }
                            }
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem(FemininePink, "Period")
                        LegendItem(SecondaryAccent, "Ovulation")
                        LegendItem(Color(0xFFB2DFDB), "Fertile")
                    }
                }
            }

            // Insights Card
            if (insights.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Health Insights",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextColor
                            )
                        )
                        insights.take(3).forEach { insight ->
                            Column {
                                Text(
                                    text = insight.title,
                                    style = MaterialTheme.typography.titleMedium.copy(color = TextColor)
                                )
                                Text(
                                    text = insight.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = LightTextColor)
                                )
                                insight.recommendations.forEach { recommendation ->
                                    Text(
                                        text = "• $recommendation",
                                        style = MaterialTheme.typography.bodySmall.copy(color = LightTextColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
//                    OutlinedButton(
//                        onClick = { /* Navigate to detailed insights */ },
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
//                        border = BorderStroke(1.dp, AccentColor)
//                    ) {
//                        Text("View Insights")
//                    }
//                    Button(
//                        onClick = { /* Navigate to add entry */ },
//                        shape = RoundedCornerShape(16.dp),
//                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
//                    ) {
//                        Text("Add Entry")
//                    }
                    Button(
                        onClick = { navController.navigate(ROUTE_CYCLE_GRAPH) },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor), // Consistent with app's theme
//                        modifier = Modifier.semantics { contentDescription = 0 }
                    ) {
                        Text("Cycle Graph")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                lastPeriodDate = selectedDate.toString()
                                lastPeriodLocalDate = selectedDate
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDatePicker = false }
                    ) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Symptoms Dialog
        if (showSymptomsDialog) {
            AlertDialog(
                onDismissRequest = { showSymptomsDialog = false },
                title = { Text("Select Symptoms") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CycleSymptom.values().forEach { symptom ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        selectedSymptoms = if (selectedSymptoms.contains(symptom.displayName)) {
                                            selectedSymptoms - symptom.displayName
                                        } else {
                                            selectedSymptoms + symptom.displayName
                                        }
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedSymptoms.contains(symptom.displayName),
                                    onCheckedChange = { checked ->
                                        selectedSymptoms = if (checked) {
                                            selectedSymptoms + symptom.displayName
                                        } else {
                                            selectedSymptoms - symptom.displayName
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AccentColor,
                                        uncheckedColor = LightTextColor
                                    )
                                )
                                Text(
                                    text = symptom.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showSymptomsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                    ) {
                        Text("Done")
                    }
                }
            )
        }

        // Mood Dialog
        if (showMoodDialog) {
            AlertDialog(
                onDismissRequest = { showMoodDialog = false },
                title = { Text("Select Mood") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CycleMood.values().forEach { mood ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        selectedMood = mood.displayName
                                        showMoodDialog = false
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mood.emoji,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Text(
                                    text = mood.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showMoodDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}