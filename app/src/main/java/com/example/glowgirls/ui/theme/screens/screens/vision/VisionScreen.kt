package com.example.glowgirls.ui.theme.screens.screens.vision

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.glowgirls.data.vision.VisionViewModel
import com.example.glowgirls.models.vision.Vision
import com.example.glowgirls.navigation.ROUTE_VISION_BOARD
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VisionScreen(navController: NavController, viewModel: VisionViewModel = viewModel()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var editingVision by remember { mutableStateOf<Vision?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showCalendar by remember { mutableStateOf(false) }
    var showDashboard by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var showInputForm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Define feminine colors
    val primaryColor = Color(0xFFF48FB1) // Pink
    val secondaryColor = Color(0xFFFCE4EC) // Light Pink
    val accentColor = Color(0xFFAD1457) // Deep Pink
    val surfaceColor = Color(0xFFFFFAFD) // Very Light Pink
    val errorColor = Color(0xFFE91E63) // Pink-Red

    // Custom color scheme
    val colorScheme = MaterialTheme.colorScheme.copy(
        primary = primaryColor,
        primaryContainer = secondaryColor,
        secondary = accentColor,
        secondaryContainer = Color(0xFFF8BBD0),
        surface = surfaceColor,
        error = errorColor
    )

    // Safely handle empty categories list
    LaunchedEffect(Unit) {
        if (viewModel.categories.isNotEmpty() && category.isEmpty()) {
            category = viewModel.categories[0]
        }
    }

    // Safe filtering - handle case when visions might be null initially
    val visions by remember {
        derivedStateOf {
            viewModel.visions.filter {
                searchQuery.isEmpty() || it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Safe grouping - prevent NullPointerException
    val groupedVisions = visions.groupBy { it.category }

    // Image picker launcher
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        if (uri != null) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Image selected successfully")
            }
        }
    }

    // Authentication Check - Add error handling
    LaunchedEffect(Unit) {
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                FirebaseAuth.getInstance().signInAnonymously()
                    .addOnFailureListener { e ->
                        Log.e("VisionScreen", "Authentication failed", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Authentication failed. Please try again later.")
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("VisionScreen", "Firebase initialization error", e)
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Dream Visions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.primaryContainer,
                        titleContentColor = colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = { navController.navigate(ROUTE_VISION_BOARD) }) {
                            Icon(
                                Icons.Filled.Dashboard,
                                contentDescription = "Vision Board",
                                tint = colorScheme.primary
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showInputForm = !showInputForm },
                    containerColor = colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        if (showInputForm) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = if (showInputForm) "Close" else "Add Vision",
                        tint = Color.White
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Motivational Quote with Animation
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        elevation = CardDefaults.cardElevation(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            colorScheme.secondaryContainer,
                                            colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Outlined.AutoAwesome,
                                    contentDescription = "Inspiration",
                                    tint = colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(1000)),
                                    exit = fadeOut(animationSpec = tween(1000))
                                ) {
                                    Text(
                                        text = "\"${viewModel.motivationalQuote.value}\"",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontStyle = FontStyle.Italic,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 28.sp
                                        ),
                                        color = colorScheme.primary,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }

                                TextButton(
                                    onClick = { viewModel.refreshQuote() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = colorScheme.primary
                                    )
                                ) {
                                    Text("✨ New Quote")
                                }
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Your Visions") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.primary.copy(alpha = 0.5f),
                            focusedLabelColor = colorScheme.primary
                        )
                    )
                }

                // Navigation Buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDashboard = !showDashboard },
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(if (showDashboard) colorScheme.primary else colorScheme.primary.copy(alpha = 0.5f))
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (showDashboard) colorScheme.primaryContainer else Color.Transparent,
                                contentColor = colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Outlined.InsertChart,
                                contentDescription = "Dashboard",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Progress")
                        }

                        OutlinedButton(
                            onClick = { showCalendar = !showCalendar },
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = SolidColor(if (showCalendar) colorScheme.primary else colorScheme.primary.copy(alpha = 0.5f))
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (showCalendar) colorScheme.primaryContainer else Color.Transparent,
                                contentColor = colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Outlined.CalendarToday,
                                contentDescription = "Calendar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Timeline")
                        }
                    }
                }

                // Dashboard View
                if (showDashboard) {
                    item {
                        DashboardView(viewModel.completionStats.value, colorScheme)
                    }
                }

                //  // Calendar View
                if (showCalendar) {
                    item {
                        CalendarView(visions, colorScheme)
                    }
                }

                // Input Form - Only show when necessary
                if (showInputForm) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            elevation = CardDefaults.cardElevation(6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = surfaceColor
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text(
                                    text = if (editingVision == null) "Create Your Vision" else "Edit Your Vision",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Vision Title") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorScheme.primary,
                                        unfocusedBorderColor = colorScheme.primary.copy(alpha = 0.5f),
                                        focusedLabelColor = colorScheme.primary
                                    )
                                )

                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Description") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .height(120.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colorScheme.primary,
                                        unfocusedBorderColor = colorScheme.primary.copy(alpha = 0.5f),
                                        focusedLabelColor = colorScheme.primary
                                    )
                                )

                                // Safe category handling
                                if (viewModel.categories.isNotEmpty()) {
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded }
                                    ) {
                                        OutlinedTextField(
                                            value = category,
                                            onValueChange = { },
                                            label = { Text("Category") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor()
                                                .padding(vertical = 6.dp),
                                            readOnly = true,
                                            shape = RoundedCornerShape(12.dp),
                                            trailingIcon = {
                                                Icon(
                                                    if (expanded) Icons.Filled.KeyboardArrowUp
                                                    else Icons.Filled.KeyboardArrowDown,
                                                    contentDescription = "Show categories",
                                                    tint = colorScheme.primary
                                                )
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = colorScheme.primary,
                                                unfocusedBorderColor = colorScheme.primary.copy(alpha = 0.5f),
                                                focusedLabelColor = colorScheme.primary
                                            )
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            viewModel.categories.forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text(cat) },
                                                    onClick = {
                                                        category = cat
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = { launcher.launch("image/*") },
                                    modifier = Modifier
                                        .padding(vertical = 12.dp)
                                        .align(Alignment.Start),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.AddPhotoAlternate,
                                        contentDescription = "Upload",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upload Image")
                                }

                                // Display the selected image preview
                                imageUri?.let {
                                    AsyncImage(
                                        model = it,
                                        contentDescription = "Selected Image",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .padding(vertical = 8.dp)
                                    )
                                }

                                // Template section - display horizontally scrollable
                                if (viewModel.templates.isNotEmpty()) {
                                    Text(
                                        "Quick Templates",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = colorScheme.primary
                                        ),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    Row(
                                        modifier = Modifier
                                            .horizontalScroll(rememberScrollState())
                                            .padding(bottom = 12.dp)
                                    ) {
                                        viewModel.templates.forEach { template ->
                                            OutlinedButton(
                                                onClick = {
                                                    title = template.title
                                                    description = template.description
                                                    category = template.category
                                                },
                                                modifier = Modifier.padding(end = 8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                    contentColor = colorScheme.primary
                                                ),
                                                shape = RoundedCornerShape(20.dp),
                                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                                    brush = SolidColor(colorScheme.primary.copy(alpha = 0.3f))
                                                )
                                            ) {
                                                Text(template.title)
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        try {
                                            // Validate required fields
                                            if (title.isBlank()) {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Title cannot be empty")
                                                }
                                                return@Button
                                            }

                                            val vision = Vision(
                                                id = editingVision?.id ?: "",
                                                title = title,
                                                description = description,
                                                category = category
                                            )

                                            if (editingVision == null) {
                                                viewModel.addVision(vision, imageUri, context)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Vision added successfully!")
                                                }
                                            } else {
                                                viewModel.updateVision(vision.copy(id = editingVision!!.id), imageUri, context)
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Vision updated successfully!")
                                                }
                                            }

                                            // Reset form
                                            title = ""
                                            description = ""
                                            if (viewModel.categories.isNotEmpty()) {
                                                category = viewModel.categories[0]
                                            } else {
                                                category = ""
                                            }
                                            imageUri = null
                                            editingVision = null
                                            showInputForm = false
                                        } catch (e: Exception) {
                                            Log.e("VisionScreen", "Error saving vision", e)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Error: ${e.message ?: "Unknown error"}")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        if (editingVision == null) "Save Vision" else "Update Vision",
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Vision List by Category
                groupedVisions.forEach { (cat, visionsList) ->
                    stickyHeader {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        )
                    }

                    items(visionsList, key = { it.id }) { vision ->
                        var offsetX by remember { mutableStateOf(0f) }
                        var isSwiped by remember { mutableStateOf(false) }

                        VisionItem(
                            vision = vision,
                            colorScheme = colorScheme,
                            onEdit = {
                                title = vision.title
                                description = vision.description
                                category = vision.category
                                editingVision = vision
                                showInputForm = true
                            },
                            onDelete = {
                                try {
                                    viewModel.deleteVision(vision.id)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("'${vision.title}' deleted")
                                    }
                                } catch (e: Exception) {
                                    Log.e("VisionScreen", "Error deleting vision", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error deleting vision")
                                    }
                                }
                            },
                            onProgressUpdate = { progress ->
                                try {
                                    viewModel.updateProgress(vision, progress)
                                } catch (e: Exception) {
                                    Log.e("VisionScreen", "Error updating progress", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error updating progress")
                                    }
                                }
                            },
                            onShare = {
                                try {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "My Vision: ${vision.title}")
                                        putExtra(Intent.EXTRA_TEXT, "My Vision: ${vision.title}\n\n${vision.description}\n\nProgress: ${vision.progress}%")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Vision"))
                                } catch (e: Exception) {
                                    Log.e("VisionScreen", "Error sharing vision", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error sharing vision")
                                    }
                                }
                            },
                            modifier = Modifier
                                .offset(x = offsetX.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            if (!isSwiped) {
                                                if (offsetX > 150f) {
                                                    Log.d("VisionScreen", "Swipe right: Edit vision ${vision.id}")
                                                    title = vision.title
                                                    description = vision.description
                                                    category = vision.category
                                                    editingVision = vision
                                                    showInputForm = true
                                                    isSwiped = true
                                                } else if (offsetX < -150f) {
                                                    Log.d("VisionScreen", "Swipe left: Delete vision ${vision.id}")
                                                    try {
                                                        viewModel.deleteVision(vision.id)
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("'${vision.title}' deleted")
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("VisionScreen", "Error deleting vision", e)
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("Error deleting vision")
                                                        }
                                                    }
                                                    isSwiped = true
                                                }
                                            }
                                            offsetX = 0f
                                            isSwiped = false
                                        }
                                    ) { _, dragAmount ->
                                        if (!isSwiped) {
                                            offsetX += dragAmount.x
                                            offsetX = offsetX.coerceIn(-200f, 200f)
                                        }
                                    }
                                }
                        )
                    }
                }

                // Empty state
                if (visions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.Lightbulb,
                                contentDescription = "No visions",
                                tint = colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Your vision board is empty",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = colorScheme.primary
                                )
                            )
                            Text(
                                text = "Add your first vision to begin manifesting your dreams!",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VisionItem(
    vision: Vision,
    colorScheme: ColorScheme,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onProgressUpdate: (Int) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMilestone by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(vision.progress.toFloat()) }

    LaunchedEffect(vision.id, vision.progress) {
        if (vision.progress in 50..54 || vision.progress >= 95) {
            showMilestone = true
            delay(3000)
            showMilestone = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vision.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (vision.streak > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "✨ ${vision.streak}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (!vision.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = vision.imageUrl,
                        contentDescription = vision.title,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                        onError = {
                            Log.e("VisionItem", "Image load failed for ${vision.imageUrl}")
                        }
                    )
                }
            }

            Text(
                text = vision.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colorScheme.onSurface
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Category: ${vision.category}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
                Text(
                    text = "Added: ${vision.date}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Progress: ${vision.progress}%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = colorScheme.primary
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            LinearProgressIndicator(
                progress = vision.progress / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    vision.progress >= 75 -> Color(0xFF4CAF50)
                    vision.progress >= 50 -> Color(0xFFFFC107)
                    else -> colorScheme.primary
                },
                trackColor = colorScheme.primary.copy(alpha = 0.2f)
            )

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = {
                    onProgressUpdate(sliderValue.toInt())
                },
                valueRange = 0f..100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = SliderDefaults.colors(
                    thumbColor = colorScheme.primary,
                    activeTrackColor = colorScheme.primary,
                    inactiveTrackColor = colorScheme.primary.copy(alpha = 0.2f)
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            colorScheme.secondary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = colorScheme.secondary
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            colorScheme.error.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = colorScheme.error
                    )
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(
                visible = showMilestone,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when {
                            vision.progress >= 95 -> "Vision Achieved! 🎉"
                            else -> "Milestone Reached! ✨"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardView(stats: Map<String, Int>, colorScheme: ColorScheme) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Progress Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (stats.isEmpty()) {
                Text(
                    "No progress data available yet",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            } else {
                stats.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            key,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colorScheme.primary
                            )
                        )
                        Text(
                            "$value%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarView(visions: List<Vision>, colorScheme: ColorScheme) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Vision Timeline",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (visions.isEmpty()) {
                Text(
                    "No visions to display",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
            } else {
                visions.forEach { vision ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    vision.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                )
                                Text(
                                    vision.category,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = colorScheme.primary.copy(alpha = 0.7f)
                                    )
                                )
                            }
                            Text(
                                vision.date,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = colorScheme.primary.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}