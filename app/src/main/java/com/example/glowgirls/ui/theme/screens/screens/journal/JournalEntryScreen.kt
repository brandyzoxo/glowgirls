package com.example.glowgirls.ui.theme.screens.screens.journal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glowgirls.data.journal.JournalViewModel
import com.example.glowgirls.models.journal.Emotion
import com.example.glowgirls.models.journal.JournalEntry
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

// Define feminine color palette
private val roseGold = Color(0xFFbd8c7d)
private val softPink = Color(0xFFF8BBD0)
private val blushPink = Color(0xFFF8C8DC)
private val lavender = Color(0xFFE6E6FA)
private val mintGreen = Color(0xFFDCF8E6)
private val paleBlue = Color(0xFFD6EAFF)
private val creamyWhite = Color(0xFFFFFAF0)
private val softPurple = Color(0xFFD8BFD8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEntryScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit,
    existingEntryId: String? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedEmotion by remember { mutableStateOf(Emotion.NEUTRAL) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var isPrivate by remember { mutableStateOf(true) }
    var showEmotionPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Load existing entry if editing
    LaunchedEffect(existingEntryId) {
        existingEntryId?.let { id ->
            viewModel.getCurrentUserId()?.let { userId ->
                viewModel.getJournalEntry(userId, id)?.let { entry ->
                    title = entry.title
                    content = entry.content
                    selectedEmotion = entry.emotion
                    selectedTags = entry.tags.toSet()
                    isPrivate = entry.isPrivate
                }
            }
        }
    }

    // Create gradient background brush
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            creamyWhite,
            creamyWhite.copy(alpha = 0.8f),
            lavender.copy(alpha = 0.2f)
        )
    )

    Scaffold(
        containerColor = creamyWhite,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (existingEntryId == null) "My Journal" else "Edit Entry",
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = roseGold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                val userId = viewModel.getCurrentUserId() ?: return@launch

                                val entry = JournalEntry(
                                    id = existingEntryId ?: "",
                                    userId = userId,
                                    date = System.currentTimeMillis(),
                                    emotion = selectedEmotion,
                                    title = title,
                                    content = content,
                                    tags = selectedTags.toList(),
                                    isPrivate = isPrivate
                                )

                                val success = viewModel.saveJournalEntry(entry)
                                if (success) {
                                    onNavigateBack()
                                }
                                isSaving = false
                            }
                        },
                        enabled = !isSaving && title.isNotBlank() && content.isNotBlank()
                    ) {
                        Icon(
                            if (isSaving) Icons.Rounded.Pending else Icons.Rounded.Favorite,
                            contentDescription = "Save",
                            tint = if (!isSaving && title.isNotBlank() && content.isNotBlank())
                                roseGold else Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = creamyWhite,
                    titleContentColor = Color.Gray.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(scrollState)
            ) {
                // Date display with styled font
                Text(
                    text = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                        .format(Date()),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.Gray.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Current emotion display with picker
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Selected emotion display
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            selectedEmotion.color.copy(alpha = 0.4f),
                                            selectedEmotion.color.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, selectedEmotion.color.copy(alpha = 0.7f)),
                                    CircleShape
                                )
                                .clickable { showEmotionPicker = !showEmotionPicker },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (selectedEmotion) {
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
                                fontSize = 40.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "I'm feeling ${selectedEmotion.displayName}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Light,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.DarkGray
                        )

                        // Expand/collapse button
                        TextButton(
                            onClick = { showEmotionPicker = !showEmotionPicker },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = roseGold
                            )
                        ) {
                            Text(
                                if (showEmotionPicker) "Done" else "Change",
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Light
                            )
                            Icon(
                                if (showEmotionPicker) Icons.Rounded.KeyboardArrowUp
                                else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }
                    }
                }

                // Emotion picker with animated visibility
                AnimatedVisibility(
                    visible = showEmotionPicker,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 16.dp, horizontal = 4.dp)
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = creamyWhite
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "How are you feeling today?",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Light,
                                    letterSpacing = 0.5.sp
                                ),
                                color = roseGold,
                                modifier = Modifier.padding(bottom = 20.dp)
                            )

                            // Grid of emotions
                            EmotionPicker(
                                selectedEmotion = selectedEmotion,
                                onEmotionSelected = { selectedEmotion = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title field
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            "Title",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Light
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = roseGold,
                        unfocusedBorderColor = softPurple.copy(alpha = 0.5f),
                        focusedLabelColor = roseGold,
                        unfocusedLabelColor = softPurple.copy(alpha = 0.7f),
                        cursorColor = roseGold
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Content field
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    value = content,
                    onValueChange = { content = it },
                    label = {
                        Text(
                            "Journal Entry",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Light
                            )
                        )
                    },
                    placeholder = {
                        Text(
                            "What's on your mind today?",
                            color = Color.Gray.copy(alpha = 0.6f),
                            fontStyle = FontStyle.Italic
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = roseGold,
                        unfocusedBorderColor = softPurple.copy(alpha = 0.5f),
                        focusedLabelColor = roseGold,
                        unfocusedLabelColor = softPurple.copy(alpha = 0.7f),
                        cursorColor = roseGold
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tags section with elegant styling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { showTagPicker = !showTagPicker },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (showTagPicker)
                                    roseGold.copy(alpha = 0.2f)
                                else
                                    Color.Transparent
                            )
                    ) {
                        Icon(
                            if (showTagPicker) Icons.Rounded.Remove else Icons.Rounded.Add,
                            contentDescription = if (showTagPicker) "Hide Tags" else "Add Tags",
                            tint = roseGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (selectedTags.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedTags.toList()) { tag ->
                            SuggestionChip(
                                onClick = { selectedTags = selectedTags - tag },
                                label = {
                                    Text(
                                        tag,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Light
                                        )
                                    )
                                },
                                icon = {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = roseGold.copy(alpha = 0.5f)
                                ),
                                colors = SuggestionChipDefaults.suggestionChipColors( // Updated to SuggestionChipDefaults
                                    containerColor = blushPink.copy(alpha = 0.2f),
                                    labelColor = Color.DarkGray,
                                    iconContentColor = roseGold
                                )
                            )
                        }
                    }
                }
                // Tag picker with animated visibility
                AnimatedVisibility(
                    visible = showTagPicker,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    TagPicker(
                        selectedTags = selectedTags,
                        onTagsSelected = { selectedTags = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Privacy toggle with elegant styling
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPrivate)
                            softPink.copy(alpha = 0.2f)
                        else
                            paleBlue.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isPrivate) softPink.copy(alpha = 0.5f) else paleBlue.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isPrivate) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = null,
                            tint = if (isPrivate) roseGold else paleBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = if (isPrivate) "Private Entry" else "Shared Entry",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Light,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = roseGold,
                                checkedTrackColor = softPink.copy(alpha = 0.5f),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = paleBlue.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EmotionPicker(
    selectedEmotion: Emotion,
    onEmotionSelected: (Emotion) -> Unit
) {
    // Group emotions into positive and challenging
    val positiveEmotions = listOf(
        Emotion.JOY, Emotion.GRATITUDE, Emotion.SERENITY,
        Emotion.LOVE, Emotion.CONFIDENCE, Emotion.INSPIRED
    )

    val challengingEmotions = listOf(
        Emotion.ANXIOUS, Emotion.SAD, Emotion.ANGRY,
        Emotion.TIRED, Emotion.STRESSED, Emotion.NEUTRAL
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Positive emotions
        Text(
            text = "Positive",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            ),
            color = roseGold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // LazyRow for positive emotions
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(positiveEmotions) { emotion ->
                EmotionItem(
                    emotion = emotion,
                    isSelected = selectedEmotion == emotion,
                    onClick = { onEmotionSelected(emotion) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Challenging emotions
        Text(
            text = "Challenging",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Light,
                letterSpacing = 1.sp
            ),
            color = roseGold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // LazyRow for challenging emotions
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(challengingEmotions) { emotion ->
                EmotionItem(
                    emotion = emotion,
                    isSelected = selectedEmotion == emotion,
                    onClick = { onEmotionSelected(emotion) }
                )
            }
        }
    }
}
@Composable
fun EmotionItem(
    emotion: Emotion,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .shadow(if (isSelected) 8.dp else 4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            emotion.color.copy(alpha = if (isSelected) 0.4f else 0.2f),
                            emotion.color.copy(alpha = if (isSelected) 0.2f else 0.1f)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = emotion.color.copy(alpha = if (isSelected) 0.8f else 0.5f)
                    ),
                    CircleShape
                ),
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
                fontSize = 22.sp
            )
        }

        Text(
            text = emotion.displayName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Light,
                letterSpacing = 0.2.sp
            ),
            maxLines = 1,
            textAlign = TextAlign.Center,
            color = if (isSelected) roseGold else Color.DarkGray,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
fun TagPicker(
    selectedTags: Set<String>,
    onTagsSelected: (Set<String>) -> Unit
) {
    val commonTags = listOf(
        "Self-care", "Work", "Family", "Growth",
        "Challenge", "Achievement", "Reflection", "Health",
        "Relationship", "Goals", "Learning", "Creativity"
    )

    var customTag by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = creamyWhite
        ),
        border = BorderStroke(
            width = 1.dp,
            color = softPurple.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Common tag chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 12.dp
            ) {
                commonTags.forEach { tag ->
                    val isSelected = selectedTags.contains(tag)

                    SuggestionChip(
                        onClick = {
                            onTagsSelected(
                                if (isSelected) selectedTags - tag
                                else selectedTags + tag
                            )
                        },
                        label = {
                            Text(
                                tag,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Light
                                )
                            )
                        },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected)
                                roseGold.copy(alpha = 0.7f)
                            else
                                softPurple.copy(alpha = 0.3f)
                        ),
                        colors = SuggestionChipDefaults.suggestionChipColors( // Updated to SuggestionChipDefaults
                            containerColor = if (isSelected)
                                blushPink.copy(alpha = 0.2f)
                            else
                                softPurple.copy(alpha = 0.1f),
                            labelColor = if (isSelected) roseGold else Color.Gray
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // Custom tag input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = customTag,
                    onValueChange = { customTag = it },
                    label = {
                        Text(
                            "Add custom tag",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Light
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = roseGold,
                        unfocusedBorderColor = softPurple.copy(alpha = 0.5f),
                        focusedLabelColor = roseGold,
                        unfocusedLabelColor = softPurple.copy(alpha = 0.7f),
                        cursorColor = roseGold
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (customTag.isNotBlank()) {
                            onTagsSelected(selectedTags + customTag.trim())
                            customTag = ""
                        }
                    },
                    enabled = customTag.isNotBlank(),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (customTag.isNotBlank())
                                roseGold.copy(alpha = 0.2f)
                            else Color.Gray.copy(alpha = 0.1f)
                        )
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add tag",
                        tint = if (customTag.isNotBlank()) roseGold else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val mainAxisSpacingPx = mainAxisSpacing.roundToPx()
        val crossAxisSpacingPx = crossAxisSpacing.roundToPx()

        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            val nextMainAxisSize = if (currentSequence.isEmpty())
                placeable.width
            else
                currentMainAxisSize + mainAxisSpacingPx + placeable.width

            if (nextMainAxisSize > constraints.maxWidth && currentSequence.isNotEmpty()) {
                sequences += currentSequence.toList()
                crossAxisSizes += currentCrossAxisSize
                crossAxisPositions += crossAxisSpace

                crossAxisSpace += currentCrossAxisSize + crossAxisSpacingPx
                mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

                currentSequence.clear()
                currentMainAxisSize = 0
                currentCrossAxisSize = 0
            }

            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacingPx
            }

            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) {
            sequences += currentSequence
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)
        }

        val width = constraints.maxWidth
        val height = max(constraints.minHeight, crossAxisSpace)

        layout(width, height) {
            sequences.forEachIndexed { index, placeables ->
                val y = crossAxisPositions[index]
                var x = 0

                placeables.forEachIndexed { i, placeable ->
                    placeable.place(x, y)
                    x += placeable.width
                    if (i < placeables.lastIndex) {
                        x += mainAxisSpacingPx
                    }
                }
            }
        }
    }
}