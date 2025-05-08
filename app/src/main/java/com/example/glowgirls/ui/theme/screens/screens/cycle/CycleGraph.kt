package com.example.glowgirls.ui.theme.screens.screens.cycle

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glowgirls.data.cycle.CycleViewModel
import com.example.glowgirls.models.cycle.CycleData
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class CyclePhase(
    val name: String,
    val startDay: Int,
    val endDay: Int,
    val color: Color,
    val description: String
)

// Color scheme matching CycleScreen
val gradientColors = listOf(
    Color(0xFFFEE3EC), // Light pink
    Color(0xFFF5E1F7)  // Light lavender
)
val accentColor = Color(0xFFD76BA2)  // Rich pink
val secondaryAccent = Color(0xFF9C6BC5) // Purple accent
val cardColor = Color.White
val textColor = Color(0xFF4A4A6A) // Dark purple-grey
val lightTextColor = Color(0xFF8A8AA0) // Lighter text
val buttonGradient = listOf(accentColor, secondaryAccent)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CycleGraphScreen(navController: NavController) {
    val cycleViewModel = CycleViewModel()
    val context = LocalContext.current
    var cycleData by remember { mutableStateOf<CycleData?>(null) }
    var currentDay by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Define phase colors matching CycleScreen aesthetic
    val menstruationColor = accentColor
    val follicularColor = Color(0xFF81C784) // Matches OVULATION from CycleScreen
    val ovulatoryColor = Color(0xFFB2DFDB) // Matches FERTILE_WINDOW from CycleScreen
    val lutealColor = secondaryAccent

    LaunchedEffect(key1 = Unit) {
        cycleViewModel.getCycleData(context) { data ->
            isLoading = false

            if (data != null) {
                cycleData = data

                // Calculate current day in cycle
                if (data.lastPeriodDate.isNotEmpty()) {
                    try {
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val lastPeriod = LocalDate.parse(data.lastPeriodDate, formatter)
                        val today = LocalDate.now()
                        val daysSinceLastPeriod = ChronoUnit.DAYS.between(lastPeriod, today).toInt()
                        currentDay = (daysSinceLastPeriod % data.cycleLength.toInt()) + 1
                    } catch (e: Exception) {
                        errorMessage = "Error calculating cycle day: ${e.message}"
                    }
                }
            } else {
                errorMessage = "No cycle data found. Please add your cycle information first."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cycle Tracker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            fontSize = 22.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = accentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardColor
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(gradientColors))
                .padding(paddingValues)
        ) {
            if (isLoading) {
                LoadingState()
            } else if (errorMessage.isNotEmpty()) {
                ErrorState(errorMessage)
            } else {
                cycleData?.let { data ->
                    val periodDuration = data.periodDuration.toInt()
                    val cycleLength = data.cycleLength.toInt()

                    val phases = listOf(
                        CyclePhase(
                            "Menstruation",
                            1,
                            periodDuration,
                            menstruationColor,
                            "Your period is active. Prioritize rest and self-care."
                        ),
                        CyclePhase(
                            "Follicular",
                            periodDuration + 1,
                            7,
                            follicularColor,
                            "Estrogen rises as your body prepares for ovulation."
                        ),
                        CyclePhase(
                            "Ovulatory",
                            8,
                            14,
                            ovulatoryColor,
                            "Peak fertility phase. Estrogen peaks."
                        ),
                        CyclePhase(
                            "Luteal",
                            15,
                            cycleLength,
                            lutealColor,
                            "Progesterone dominates. PMS symptoms may appear."
                        )
                    )

                    val currentPhase = phases.find {
                        currentDay in it.startDay..it.endDay
                    } ?: phases[0]

                    val daysToNextPeriod = cycleLength - currentDay + 1

                    CycleContent(
                        modifier = Modifier.padding(24.dp),
                        currentDay = currentDay,
                        cycleLength = cycleLength,
                        currentPhase = currentPhase,
                        phases = phases,
                        daysToNextPeriod = daysToNextPeriod,
                        cycleData = data
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = accentColor,
                strokeWidth = 4.dp
            )
            Text(
                "Loading your cycle data...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun ErrorState(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}

@Composable
fun CycleContent(
    modifier: Modifier = Modifier,
    currentDay: Int,
    cycleLength: Int,
    currentPhase: CyclePhase,
    phases: List<CyclePhase>,
    daysToNextPeriod: Int,
    cycleData: CycleData
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DayCounter(currentDay, cycleLength, currentPhase.color)
        CurrentPhaseCard(currentPhase)
        EnhancedCycleGraph(cycleLength, phases, currentDay)
        ImportantDatesCard(cycleData, daysToNextPeriod)
        PhaseLegends(phases)
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DayCounter(currentDay: Int, cycleLength: Int, phaseColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(phaseColor.copy(alpha = 0.2f))
                    .border(1.dp, phaseColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentDay.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = phaseColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    "DAY OF CYCLE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = lightTextColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    "Day $currentDay of $cycleLength",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
fun CurrentPhaseCard(currentPhase: CyclePhase) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = currentPhase.color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(currentPhase.color)
                        .border(1.dp, Color.White, CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${currentPhase.name} Phase",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Text(
                text = currentPhase.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = lightTextColor
                )
            )
        }
    }
}

@Composable
fun EnhancedCycleGraph(
    cycleLength: Int,
    phases: List<CyclePhase>,
    currentDay: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val segmentWidth = width / cycleLength

                // Draw phase sections
                phases.forEach { phase ->
                    val startX = (phase.startDay - 1) * segmentWidth
                    val phaseWidth = (phase.endDay - phase.startDay + 1) * segmentWidth

                    drawRect(
                        color = phase.color.copy(alpha = 0.2f),
                        topLeft = Offset(startX, 0f),
                        size = Size(phaseWidth, height)
                    )

                    drawRect(
                        color = phase.color.copy(alpha = 0.4f),
                        topLeft = Offset(startX, 0f),
                        size = Size(phaseWidth, height),
                        style = Stroke(width = 2f)
                    )
                }

                // Draw current day indicator
                val currentX = (currentDay - 1) * segmentWidth + (segmentWidth / 2)

                drawCircle(
                    color = Color.Black.copy(alpha = 0.1f),
                    radius = 10f,
                    center = Offset(currentX, height / 2 + 1)
                )

                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = Offset(currentX, height / 2)
                )

                drawCircle(
                    color = phases.find { currentDay in it.startDay..it.endDay }?.color ?: accentColor,
                    radius = 6f,
                    center = Offset(currentX, height / 2)
                )
            }

            Text(
                text = "1",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.align(Alignment.BottomStart)
            )

            Text(
                text = cycleLength.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = textColor,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun ImportantDatesCard(data: CycleData, daysToNextPeriod: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Key Dates",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            DateItem("Last Period", data.lastPeriodDate)
            DateItem("Next Period", data.nextPeriodDate)
            DateItem("Ovulation", data.ovulationDate)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(buttonGradient))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Days until next period: $daysToNextPeriod",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun DateItem(label: String, date: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = lightTextColor
            )
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        )
    }
}

@Composable
fun PhaseLegends(phases: List<CyclePhase>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Cycle Phases",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                phases.forEach { phase ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(phase.color)
                                .border(1.dp, Color.White, CircleShape)
                        )
                        Text(
                            text = phase.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textColor,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val horizontalGapPx = 0
        val verticalGapPx = 0

        val rows = mutableListOf<MeasuredRow>()
        var rowConstraints = constraints
        var rowPlaceables = mutableListOf<Placeable>()
        var rowWidth = 0
        var rowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(rowConstraints)

            if (rowWidth + placeable.width > constraints.maxWidth) {
                rows.add(
                    MeasuredRow(
                        placeables = rowPlaceables,
                        width = rowWidth - horizontalGapPx,
                        height = rowHeight
                    )
                )

                rowPlaceables = mutableListOf()
                rowWidth = 0
                rowHeight = 0
            }

            rowPlaceables.add(placeable)
            rowWidth += placeable.width + horizontalGapPx
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        if (rowPlaceables.isNotEmpty()) {
            rows.add(
                MeasuredRow(
                    placeables = rowPlaceables,
                    width = rowWidth - horizontalGapPx,
                    height = rowHeight
                )
            )
        }

        val layoutHeight = rows.sumOf { row -> row.height } + maxOf(0, rows.size - 1) * verticalGapPx
        val layoutWidth = constraints.maxWidth

        layout(layoutWidth, layoutHeight) {
            var y = 0

            rows.forEach { row ->
                var x = when (horizontalArrangement) {
                    Arrangement.Start -> 0
                    Arrangement.Center -> (layoutWidth - row.width) / 2
                    Arrangement.End -> layoutWidth - row.width
                    else -> 0
                }

                row.placeables.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + horizontalGapPx
                }
                y += row.height + verticalGapPx
            }
        }
    }
}

private data class MeasuredRow(
    val placeables: List<Placeable>,
    val width: Int,
    val height: Int
)