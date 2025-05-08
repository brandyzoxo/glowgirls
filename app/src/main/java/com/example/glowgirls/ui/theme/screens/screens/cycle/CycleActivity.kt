package com.example.glowgirls.ui.theme.screens.screens.cycle

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glowgirls.data.cycle.CycleViewModel
import com.example.glowgirls.navigation.ROUTE_CYCLE_GRAPH
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Feminine color palette
val FemininePink = Color(0xFFF5A8C7)
val SoftPurple = Color(0xFFD4A5F5)
val PastelPeach = Color(0xFFFFE4E1)
val GradientColors = listOf(FemininePink, SoftPurple, PastelPeach)
val AccentColor = Color(0xFF8B5CF6) // Vibrant purple for highlights
val TextColor = Color(0xFF4A3466) // Deep purple for text

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleScreen(navController: NavController) {
    var lastPeriodDate by remember { mutableStateOf("") }
    var cycleLength by remember { mutableStateOf("") }
    var nextPeriodDate by remember { mutableStateOf("") }
    var ovulationDate by remember { mutableStateOf("") }
    var periodDuration by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val cycleViewModel = CycleViewModel()

    // Scroll state for making the screen scrollable
    val scrollState = rememberScrollState()

    // Parse the calculated dates to LocalDate objects when available
    val nextPeriodLocalDate = remember(nextPeriodDate) {
        if (nextPeriodDate.isNotEmpty()) {
            try {
                LocalDate.parse(nextPeriodDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val ovulationLocalDate = remember(ovulationDate) {
        if (ovulationDate.isNotEmpty()) {
            try {
                LocalDate.parse(ovulationDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            } catch (e: Exception) {
                null
            }
        } else null
    }

    var lastPeriodLocalDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Current calendar view date
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    // Define gradient colors
    val gradientColors = listOf(
        Color(0xFFFEE3EC), // Light pink
        Color(0xFFF5E1F7)  // Light lavender
    )

    val accentColor = Color(0xFFD76BA2)  // Rich pink
    val secondaryAccent = Color(0xFF9C6BC5) // Purple accent
    val cardColor = Color.White
    val textColor = Color(0xFF4A4A6A) // Dark purple-grey
    val lightTextColor = Color(0xFF8A8AA0) // Lighter text

    // Main content wrapped in a Box with gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        // Main column with verticalScroll modifier
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App header with title and icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bloom",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = secondaryAccent,
                        fontSize = 32.sp
                    )
                )

                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Page title
            Text(
                text = "Track Your Cycle",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    fontSize = 24.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Main card for inputs
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date selector
                    OutlinedTextField(
                        value = lastPeriodDate,
                        onValueChange = { /* Read-only, handled by date picker */ },
                        label = { Text("Last Period Date", color = lightTextColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { showDatePicker = true },
                        readOnly = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFBF6FF),
                            unfocusedContainerColor = Color(0xFFFBF6FF),
                            focusedIndicatorColor = accentColor,
                            unfocusedIndicatorColor = Color(0xFFE5D9EB),
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = lightTextColor,
                            cursorColor = accentColor
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = accentColor,
                                modifier = Modifier.clickable { showDatePicker = true }
                            )
                        },
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Cycle length input
                    OutlinedTextField(
                        value = cycleLength,
                        onValueChange = { cycleLength = it },
                        label = { Text("Average Cycle Length (days)", color = lightTextColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFBF6FF),
                            unfocusedContainerColor = Color(0xFFFBF6FF),
                            focusedIndicatorColor = accentColor,
                            unfocusedIndicatorColor = Color(0xFFE5D9EB),
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = lightTextColor,
                            cursorColor = accentColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Period duration input
                    OutlinedTextField(
                        value = periodDuration,
                        onValueChange = { periodDuration = it },
                        label = { Text("Period Duration (days)", color = lightTextColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFBF6FF),
                            unfocusedContainerColor = Color(0xFFFBF6FF),
                            focusedIndicatorColor = accentColor,
                            unfocusedIndicatorColor = Color(0xFFE5D9EB),
                            focusedLabelColor = accentColor,
                            unfocusedLabelColor = lightTextColor,
                            cursorColor = accentColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    // Calculate button
                    Button(
                        onClick = {
                            if (lastPeriodDate.isNotEmpty() && cycleLength.isNotEmpty()) {
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                val lastDate = LocalDate.parse(lastPeriodDate, formatter)
                                val length = cycleLength.toInt()

                                val nextDate = lastDate.plusDays(length.toLong())
                                val ovulation = lastDate.plusDays((length / 2).toLong() - 2)

                                nextPeriodDate = nextDate.format(formatter)
                                ovulationDate = ovulation.format(formatter)
                                showResults = true

                                if (nextDate.month == lastDate.month) {
                                    currentYearMonth = YearMonth.from(lastDate)
                                } else {
                                    currentYearMonth = YearMonth.from(nextDate)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Calculate", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }

            // Results card
            AnimatedVisibility(
                visible = showResults,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Your Cycle Results",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        )

                        Divider(
                            color = Color(0xFFEDE0F5),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Next period row with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFEE3EC), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Next Period",
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = "Next Period",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = lightTextColor
                                    )
                                )
                                Text(
                                    text = nextPeriodDate,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                )
                            }
                        }

                        // Ovulation row with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFF3E5FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Ovulation",
                                    tint = secondaryAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    text = "Ovulation Date",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = lightTextColor
                                    )
                                )
                                Text(
                                    text = ovulationDate,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Action buttons card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Save to Firebase button
                    Button(
                        onClick = {
                            if (lastPeriodDate.isNotEmpty() && cycleLength.isNotEmpty() && periodDuration.isNotEmpty()) {
                                cycleViewModel.saveCycleData(
                                    lastPeriodDate,
                                    cycleLength,
                                    periodDuration,
                                    nextPeriodDate,
                                    ovulationDate,
                                    context
                                )
                            } else {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = secondaryAccent,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = "Save",
                                modifier = Modifier.size(20.dp).padding(end = 4.dp)
                            )
                            Text("Save to Firebase", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }

                    // View Cycle Graph button
                    OutlinedButton(
                        onClick = {
                            navController.navigate(route = ROUTE_CYCLE_GRAPH)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, secondaryAccent),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = secondaryAccent
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Graph",
                                modifier = Modifier.size(20.dp).padding(end = 4.dp)
                            )
                            Text("View Cycle Graph", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }
                    }
                }
            }

            // Calendar card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Calendar title row with month navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            currentYearMonth = currentYearMonth.minusMonths(1)
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = accentColor
                            )
                        }

                        Text(
                            text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        )

                        IconButton(onClick = {
                            currentYearMonth = currentYearMonth.plusMonths(1)
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = accentColor
                            )
                        }
                    }

                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = lightTextColor,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom calendar implementation
                    CustomCalendar(
                        currentYearMonth = currentYearMonth,
                        onMonthChanged = { currentYearMonth = it },
                        highlightedDates = buildMap {
                            if (lastPeriodLocalDate != null) {
                                put(lastPeriodLocalDate!!, CalendarHighlight.LAST_PERIOD)
                            }
                            if (nextPeriodLocalDate != null) {
                                put(nextPeriodLocalDate, CalendarHighlight.NEXT_PERIOD)
                                if (periodDuration.isNotEmpty()) {
                                    val duration = periodDuration.toIntOrNull() ?: 0
                                    for (i in 0 until duration) {
                                        put(nextPeriodLocalDate.plusDays(i.toLong()), CalendarHighlight.PERIOD_DURATION)
                                    }
                                }
                            }
                            if (ovulationLocalDate != null) {
                                put(ovulationLocalDate, CalendarHighlight.OVULATION)
                                for (i in -2..2) {
                                    if (i != 0) {
                                        put(ovulationLocalDate.plusDays(i.toLong()), CalendarHighlight.FERTILE_WINDOW)
                                    }
                                }
                            }
                        }
                    )

                    // Calendar legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = accentColor, text = "Period")
                        LegendItem(color = secondaryAccent, text = "Ovulation")
                        LegendItem(color = Color(0xFFE0B7FF), text = "Fertile")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Date picker dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            lastPeriodDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            lastPeriodLocalDate = localDate
                            currentYearMonth = YearMonth.from(localDate)
                        }
                        showDatePicker = false
                    }) {
                        Text("Confirm", color = accentColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = accentColor)
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White,
                    titleContentColor = textColor,
                    headlineContentColor = textColor,
                    selectedDayContainerColor = accentColor
                )
            ) {
                DatePicker(
                    state = datePickerState,
                    title = { Text("Select Last Period Date", color = textColor) },
                    headline = {
                        Text(
                            "Please select the first day of your last period",
                            color = textColor,
                            fontSize = 14.sp
                        )
                    },
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = accentColor,
                        todayContentColor = secondaryAccent,
                        todayDateBorderColor = secondaryAccent
                    )
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF8A8AA0)
        )
    }
}

// Color Scheme
//val FemininePink = Color(0xFFD76BA2)
//val PastelPeach = Color(0xFFFFF5F5)
//val AccentColor = Color(0xFF9C6BC5)
//val TextColor = Color(0xFF4A4A6A)



// Updated CalendarHighlight with feminine colors
enum class CalendarHighlight(val color: Color, val description: String) {
    LAST_PERIOD(FemininePink, "Last Period Start"),
    NEXT_PERIOD(AccentColor, "Next Period Start"),
    PERIOD_DURATION(PastelPeach, "Period Duration"),
    OVULATION(Color(0xFF81C784), "Ovulation Day"), // Soft green
    FERTILE_WINDOW(Color(0xFFB2DFDB), "Fertile Window") // Light teal
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
            .background(Color.White.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        // Month navigation row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChanged(currentYearMonth.minusMonths(1)) }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = AccentColor
                )
            }

            Text(
                text = "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextColor,
                    fontWeight = FontWeight.SemiBold
                )
            )

            IconButton(onClick = { onMonthChanged(currentYearMonth.plusMonths(1)) }) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Next Month",
                    tint = AccentColor
                )
            }
        }

        // Days of week header
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

        // Calendar days grid
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

        // Legend for highlighted dates
        if (highlightedDates.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Legend:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val uniqueHighlights = highlightedDates.values.toSet()
                    for (highlight in uniqueHighlights) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .background(highlight.color, CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                            Text(
                                highlight.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextColor
                                )
                            )
                        }
                    }
                }
            }
        }
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
    val isToday = date.equals(today)

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
private fun buildCalendarDays(yearMonth: YearMonth): List<LocalDate> {
    val firstOfMonth = yearMonth.atDay(1)
    val firstDayOfGrid = firstOfMonth.minusDays(firstOfMonth.dayOfWeek.value.toLong() - 1)

    return (0 until 42).map { firstDayOfGrid.plusDays(it.toLong()) }
}