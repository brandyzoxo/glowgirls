package com.example.glowgirls.ui.theme.screens.screens.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowgirls.models.journal.Emotion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.MaterialTheme
import com.example.glowgirls.data.journal.JournalViewModel
import com.example.glowgirls.data.journal.TimePeriod
import com.example.glowgirls.models.journal.JournalEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(
    viewModel: JournalViewModel,
    onNavigateToEntry: (String?) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val entries by viewModel.journalEntries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val emotionStats by viewModel.emotionStats.collectAsState()
    var selectedPeriod by remember { mutableStateOf(TimePeriod.MONTH) }
    var showEmotionFilter by remember { mutableStateOf(false) }
    var selectedEmotion by remember { mutableStateOf<Emotion?>(null) }

    // Theme colors - soft, feminine palette
    val primaryColor = Color(0xFFF2B8C6)  // Soft pink
    val secondaryColor = Color(0xFFFCE4EC) // Lighter pink
    val accentColor = Color(0xFFE0BFE6)   // Soft lavender
    val gradientStart = Color(0xFFFCE4EC)
    val gradientEnd = Color(0xFFE0BFE6)

    // Load entries and stats
    LaunchedEffect(Unit) {
        viewModel.loadJournalEntries()
        viewModel.loadEmotionStats(selectedPeriod)
    }

    Scaffold(
        topBar = {
            // Custom feminine top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(gradientStart, gradientEnd)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Journal",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF5D4157) // Deep mauve for contrast
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { showEmotionFilter = !showEmotionFilter }
                    ) {
                        Icon(
                            Icons.Outlined.FilterAlt,
                            contentDescription = "Filter",
                            tint = Color(0xFF5D4157)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEntry(null) },
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Create, contentDescription = "New Journal Entry")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFFFFBFE)) // Soft white background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Emotion statistics card - redesigned
                EmotionStatsCard(
                    emotionStats = emotionStats,
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { period ->
                        selectedPeriod = period
                        coroutineScope.launch {
                            viewModel.loadEmotionStats(period)
                        }
                    },
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    accentColor = accentColor
                )

                // Emotion filter - with animation
                AnimatedVisibility(
                    visible = showEmotionFilter,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                ) {
                    EmotionFilterChips(
                        selectedEmotion = selectedEmotion,
                        onEmotionSelected = { emotion ->
                            selectedEmotion = if (emotion == selectedEmotion) null else emotion
                            if (emotion == null || emotion == selectedEmotion) {
                                viewModel.loadAllEntries()
                            } else {
                                viewModel.filterEntriesByEmotion(emotion)
                            }
                        },
                        accentColor = accentColor
                    )
                }

                // Journal entries list
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                } else if (entries.isEmpty()) {
                    EmptyJournalView(primaryColor = primaryColor)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(entries) { entry ->
                            JournalEntryCard(
                                entry = entry,
                                onClick = { onNavigateToEntry(entry.id) },
                                onDelete = {
                                    coroutineScope.launch {
                                        viewModel.deleteJournalEntry(entry.id)
                                    }
                                },
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyJournalView(primaryColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Custom illustrated icon
            Canvas(modifier = Modifier.size(80.dp)) {
                // Notebook outline
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.7f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                    size = size.copy(width = size.width * 0.8f),
                    topLeft = Offset(size.width * 0.1f, size.height * 0.15f)
                )

                // Spiral binding
                for (i in 0..8) {
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(size.width * 0.2f, size.height * (0.2f + i * 0.075f))
                    )
                }

                // Lines for text
                for (i in 1..4) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(size.width * 0.3f, size.height * (0.3f + i * 0.12f)),
                        end = Offset(size.width * 0.7f, size.height * (0.3f + i * 0.12f)),
                        strokeWidth = 2f
                    )
                }

                // Pen
                drawLine(
                    color = Color(0xFF5D4157),
                    start = Offset(size.width * 0.65f, size.height * 0.2f),
                    end = Offset(size.width * 0.9f, size.height * 0.45f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Your journal awaits",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center,
                color = Color(0xFF5D4157)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start your journey by adding your first entry",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionStatsCard(
    emotionStats: Map<Emotion, Int>,
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    primaryColor: Color,
    secondaryColor: Color,
    accentColor: Color
) {
    val totalEntries = emotionStats.values.sum()
    val mostFrequentEmotion = emotionStats.entries.maxByOrNull { it.value }?.key

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = primaryColor.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Title with period selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emotion Tracker",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5D4157)
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                // Period selection chips - redesigned
                Row {
                    PeriodChip(
                        text = "Week",
                        selected = selectedPeriod == TimePeriod.WEEK,
                        onClick = { onPeriodSelected(TimePeriod.WEEK) },
                        selectedColor = primaryColor,
                        unselectedColor = secondaryColor.copy(alpha = 0.5f)
                    )

                    PeriodChip(
                        text = "Month",
                        selected = selectedPeriod == TimePeriod.MONTH,
                        onClick = { onPeriodSelected(TimePeriod.MONTH) },
                        selectedColor = primaryColor,
                        unselectedColor = secondaryColor.copy(alpha = 0.5f)
                    )

                    PeriodChip(
                        text = "Year",
                        selected = selectedPeriod == TimePeriod.YEAR,
                        onClick = { onPeriodSelected(TimePeriod.YEAR) },
                        selectedColor = primaryColor,
                        unselectedColor = secondaryColor.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (totalEntries == 0) {
                // No data - redesigned empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.Timeline,
                            contentDescription = null,
                            tint = secondaryColor,
                            modifier = Modifier.size(32.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "No entries for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            } else {
                // Stats overview - redesigned
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total entries
                    StatItem(
                        value = "$totalEntries",
                        label = "Entries",
                        color = primaryColor
                    )

                    // Most frequent emotion
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(mostFrequentEmotion?.color?.copy(alpha = 0.2f) ?: Color.Gray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (mostFrequentEmotion) {
                                    Emotion.JOY -> "😊"
                                    Emotion.GRATITUDE -> "🙏"
                                    Emotion.SERENITY -> "😌"
                                    Emotion.LOVE -> "❤️"
                                    Emotion.CONFIDENCE -> "💪"
                                    Emotion.INSPIRED -> "✨"
                                    Emotion.ANXIOUS -> "😰"
                                    Emotion.SAD -> "😢"
                                    Emotion.ANGRY -> "😠"
                                    Emotion.TIRED -> "😴"
                                    Emotion.STRESSED -> "😩"
                                    Emotion.NEUTRAL, null -> "😐"
                                },
                                fontSize = 24.sp
                            )
                        }
                        Text(
                            text = "Most Common",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5D4157)
                        )
                    }

                    // Positive vs challenging ratio
                    val positiveEmotions = setOf(
                        Emotion.JOY, Emotion.GRATITUDE, Emotion.SERENITY,
                        Emotion.LOVE, Emotion.CONFIDENCE, Emotion.INSPIRED
                    )

                    val positiveCount = emotionStats.entries.filter {
                        it.key in positiveEmotions
                    }.sumOf { it.value }

                    val positiveRatio = if (totalEntries > 0) {
                        positiveCount.toFloat() / totalEntries
                    } else 0f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Draw progress circle
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Background circle
                                drawArc(
                                    color = secondaryColor.copy(alpha = 0.3f),
                                    startAngle = 0f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                                )

                                // Progress arc
                                drawArc(
                                    color = primaryColor,
                                    startAngle = -90f,
                                    sweepAngle = 360f * positiveRatio,
                                    useCenter = false,
                                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                                )
                            }

                            // Percentage text
                            Text(
                                text = "${(positiveRatio * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4157)
                            )
                        }
                        Text(
                            text = "Positive",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5D4157)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Emotion distribution - redesigned
                EmotionDistribution(
                    emotionStats = emotionStats,
                    primaryColor = primaryColor,
                    accentColor = accentColor
                )
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5D4157)
        )
    }
}

@Composable
fun EmotionDistribution(
    emotionStats: Map<Emotion, Int>,
    primaryColor: Color,
    accentColor: Color
) {
    val totalEntries = emotionStats.values.sum().toFloat().coerceAtLeast(1f)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Emotion Distribution",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = Color(0xFF5D4157)
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Only show top 5 emotions
        val topEmotions = emotionStats.entries
            .sortedByDescending { it.value }
            .take(5)
            .filter { it.value > 0 }

        if (topEmotions.isEmpty()) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E)
            )
        } else {
            topEmotions.forEach { (emotion, count) ->
                val percentage = count / totalEntries

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emotion indicator
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(emotion.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (emotion) {
                                Emotion.JOY -> "😊"
                                Emotion.GRATITUDE -> "🙏"
                                Emotion.SERENITY -> "😌"
                                Emotion.LOVE -> "❤️"
                                Emotion.CONFIDENCE -> "💪"
                                Emotion.INSPIRED -> "✨"
                                Emotion.ANXIOUS -> "😰"
                                Emotion.SAD -> "😢"
                                Emotion.ANGRY -> "😠"
                                Emotion.TIRED -> "😴"
                                Emotion.STRESSED -> "😩"
                                Emotion.NEUTRAL -> "😐"
                            },
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = emotion.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF5D4157)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "$count (${(percentage * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )
                }

                // Progress bar - redesigned with rounded corners
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(emotion.color, emotion.color.copy(alpha = 0.6f))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun PeriodChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) selectedColor else unselectedColor,
        contentColor = if (selected) Color.White else Color(0xFF5D4157),
        modifier = Modifier
            .padding(end = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotionFilterChips(
    selectedEmotion: Emotion?,
    onEmotionSelected: (Emotion?) -> Unit,
    accentColor: Color
) {
    val emotions = Emotion.values()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Filter by emotion",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF5D4157),
            modifier = Modifier.padding(bottom = 10.dp, top = 4.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedEmotion == null,
                    onClick = { onEmotionSelected(null) },
                    label = { Text("All") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }

            items(emotions) { emotion ->
                FilterChip(
                    selected = selectedEmotion == emotion,
                    onClick = { onEmotionSelected(emotion) },
                    label = { Text(emotion.displayName) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(emotion.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (emotion) {
                                    Emotion.JOY -> "😊"
                                    Emotion.GRATITUDE -> "🙏"
                                    Emotion.SERENITY -> "😌"
                                    Emotion.LOVE -> "❤️"
                                    Emotion.CONFIDENCE -> "💪"
                                    Emotion.INSPIRED -> "✨"
                                    Emotion.ANXIOUS -> "😰"
                                    Emotion.SAD -> "😢"
                                    Emotion.ANGRY -> "😠"
                                    Emotion.TIRED -> "😴"
                                    Emotion.STRESSED -> "😩"
                                    Emotion.NEUTRAL -> "😐"
                                },
                                fontSize = 14.sp
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = emotion.color.copy(alpha = 0.3f),
                        selectedLabelColor = Color(0xFF5D4157)
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    primaryColor: Color,
    secondaryColor: Color
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(entry.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = primaryColor.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header - date and emotion
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date with small icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF9E9E9E)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9E9E9E)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Emotion - redesigned with soft background
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(entry.emotion.color.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (entry.emotion) {
                                Emotion.JOY -> "😊"
                                Emotion.GRATITUDE -> "🙏"
                                Emotion.SERENITY -> "😌"
                                Emotion.LOVE -> "❤️"
                                Emotion.CONFIDENCE -> "💪"
                                Emotion.INSPIRED -> "✨"
                                Emotion.ANXIOUS -> "😰"
                                Emotion.SAD -> "😢"
                                Emotion.ANGRY -> "😠"
                                Emotion.TIRED -> "😴"
                                Emotion.STRESSED -> "😩"
                                Emotion.NEUTRAL -> "😐"
                            },
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = entry.emotion.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4157)
                        )
                    }
                }

                // More options
                Box {
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { expanded = true }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = Color(0xFF9E9E9E)
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFF5D4157)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF5D4157)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Content preview
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B6B6B),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Tags if any
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(entry.tags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = secondaryColor.copy(alpha = 0.5f),
                            contentColor = Color(0xFF5D4157)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}