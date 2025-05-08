package com.example.glowgirls.ui.theme.screens.screens.vision

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.glowgirls.data.vision.VisionBoardViewModel
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionBoardScreen(navController: NavController) {
    val viewModel = remember { VisionBoardViewModel() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val images by viewModel.images.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Custom color palette for feminine design
    val pinkGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFCE4EC), // Light pink
            Color(0xFFF8BBD0)  // Slightly darker pink
        )
    )

    // Track selected category with debouncing
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Goals", "Motivation", "Quotes", "Ideas")

    // Permission handling
    var hasStoragePermission by remember { mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    ) }
    var shouldShowRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePermission = isGranted
        if (!isGranted) {
            shouldShowRationale = shouldShowRequestPermissionRationale(context)
            if (!shouldShowRationale) {
                viewModel.setError("Storage permission denied. Please enable it in settings to add images.")
            } else {
                viewModel.setError("Storage permission is required to add images. Please grant permission.")
            }
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                viewModel.uploadImage(it, context, selectedCategory)
            }
        }
    }

    // Dialog visibility
    var showErrorDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Vision Board",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(0xFF9C27B0) // Purple for feminine touch
                    )
                },
                actions = {
                    // Add share button
//                    IconButton(onClick = { shareVisionBoard(context, images) }) {
//                        Icon(
//                            Icons.Default.Share,
//                            contentDescription = "Share vision board",
//                            tint = Color(0xFF9C27B0)
//                        )
//                    }

                    // Add image button
                    IconButton(
                        onClick = {
                            if (hasStoragePermission) {
                                imagePickerLauncher.launch("image/*")
                            } else {
                                shouldShowRationale = shouldShowRequestPermissionRationale(context)
                                if (!shouldShowRationale && !hasStoragePermission) {
                                    showPermissionDeniedDialog = true
                                } else {
                                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add new image",
                            tint = Color(0xFF9C27B0)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFCE4EC),
                    titleContentColor = Color(0xFF9C27B0)
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .background(pinkGradient)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    // Categories chips with feminine design
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategory),
                        edgePadding = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        divider = {},
                        containerColor = Color.Transparent
                    ) {
                        categories.forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = {
                                    scope.launch {
                                        delay(100)
                                        selectedCategory = category
                                    }
                                },
                                label = {
                                    Text(
                                        text = category,
                                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFCE93D8), // Light purple
                                    selectedLabelColor = Color.White
                                ),
                                elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    // Motivational quote with improved design
                    AnimatedVisibility(
                        visible = selectedCategory == "All" || selectedCategory == "Quotes",
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        QuoteSection()
                    }

                    // Main content area
                    Box(modifier = Modifier.weight(1f)) {
                        when {
                            isLoading -> ShimmerLoadingGrid()
                            images.isEmpty() -> EmptyStateMessage(onAddClick = {
                                if (hasStoragePermission) {
                                    imagePickerLauncher.launch("image/*")
                                } else {
                                    shouldShowRationale = shouldShowRequestPermissionRationale(context)
                                    if (!shouldShowRationale && !hasStoragePermission) {
                                        showPermissionDeniedDialog = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                    }
                                }
                            })
                            else -> {
                                val filteredImages = if (selectedCategory == "All") {
                                    images
                                } else {
                                    images.filter { it.category == selectedCategory }
                                }

                                if (filteredImages.isEmpty() && selectedCategory != "All") {
                                    EmptyCategoryMessage(category = selectedCategory, onAddClick = {
                                        if (hasStoragePermission) {
                                            imagePickerLauncher.launch("image/*")
                                        } else {
                                            shouldShowRationale = shouldShowRequestPermissionRationale(context)
                                            if (!shouldShowRationale && !hasStoragePermission) {
                                                showPermissionDeniedDialog = true
                                            } else {
                                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                            }
                                        }
                                    })
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(filteredImages, key = { it.id }) { image ->
                                            VisionBoardItem(
                                                imageUrl = image.link,
                                                onDelete = { scope.launch { viewModel.deleteImage(image.id) } },
                                                onShare = { shareImage(context, image.link) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Error dialog with improved design
                errorMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        showErrorDialog = true
                        delay(5000)
                        viewModel.clearError()
                        showErrorDialog = false
                        if (msg.contains("permission denied", ignoreCase = true) && !hasStoragePermission) {
                            showPermissionDeniedDialog = true
                        }
                    }
                    if (showErrorDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.clearError(); showErrorDialog = false },
                            title = { Text("Error", color = Color(0xFF9C27B0)) },
                            text = { Text(msg) },
                            confirmButton = {
                                TextButton(onClick = { viewModel.clearError(); showErrorDialog = false }) {
                                    Text("OK", color = Color(0xFF9C27B0))
                                }
                            },
                            containerColor = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                // Permission denied dialog with improved design
                if (showPermissionDeniedDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDeniedDialog = false },
                        title = { Text("Permission Required", color = Color(0xFF9C27B0)) },
                        text = { Text("Storage permission is needed to add images. Please enable it in settings.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showPermissionDeniedDialog = false
                                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Open Settings",
                                    modifier = Modifier.padding(end = 8.dp),
                                    tint = Color(0xFF9C27B0)
                                )
                                Text("Go to Settings", color = Color(0xFF9C27B0))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPermissionDeniedDialog = false }) {
                                Text("Cancel", color = Color(0xFF9C27B0))
                            }
                        },
                        containerColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    )
}

private fun shouldShowRequestPermissionRationale(context: android.content.Context): Boolean {
    return if (context is ComponentActivity) {
        ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        false
    }
}

// Function to share the entire vision board
//private fun shareVisionBoard(context: android.content.Context, images: List<com.example.glowgirls.models.VisionImage>) {
//    if (images.isEmpty()) {
//        // Show toast or message that there are no images to share
//        return
//    }
//
//    val shareIntent = Intent().apply {
//        action = Intent.ACTION_SEND_MULTIPLE
//        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(images.map { Uri.parse(it.link) }))
//        type = "image/*"
//        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//    }
//
//    val shareChooser = Intent.createChooser(shareIntent, "Share Your Vision Board")
//    context.startActivity(shareChooser)
//}

// Function to share a single image
private fun shareImage(context: android.content.Context, imageUrl: String) {
    try {
        val uri = Uri.parse(imageUrl)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val shareChooser = Intent.createChooser(shareIntent, "Share This Vision")
        context.startActivity(shareChooser)
    } catch (e: Exception) {
        // Handle errors
    }
}

@Composable
fun QuoteSection() {
    val quotes = listOf(
        "Visualize your highest self, then start showing up as that person.",
        "What you focus on expands. Focus on your dreams.",
        "The only limit to your impact is your imagination and commitment.",
        "Dream big, work hard, stay focused."
    )

    val randomQuote = remember { quotes.random() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3E5F5) // Very light purple
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "\"$randomQuote\"",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = Color(0xFF7B1FA2), // Deep purple
            lineHeight = 24.sp
        )
    }
}

@Composable
fun ShimmerLoadingGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF3E5F5),
                                Color(0xFFE1BEE7),
                                Color(0xFFF3E5F5)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun EmptyStateMessage(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Empty vision board",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFBA68C8) // Medium purple
        )

        Text(
            text = "Your vision board is empty",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF7B1FA2) // Deep purple
        )

        Text(
            text = "Add images to visualize your goals, dreams, and aspirations",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color(0xFF9C27B0).copy(alpha = 0.7f),
            lineHeight = 20.sp
        )

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCE93D8) // Light purple
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Add Your First Image")
        }
    }
}

@Composable
fun EmptyCategoryMessage(category: String, onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = "Empty category",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFBA68C8) // Medium purple
        )

        Text(
            text = "No images in $category",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF7B1FA2) // Deep purple
        )

        Text(
            text = "Add images to this category to inspire your journey",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color(0xFF9C27B0).copy(alpha = 0.7f),
            lineHeight = 20.sp
        )

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFCE93D8) // Light purple
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Add Image")
        }
    }
}

@Composable
fun VisionBoardItem(imageUrl: String, onDelete: () -> Unit, onShare: () -> Unit) {
    var showActionDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { showActionDialog = true },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                ),
                contentDescription = "Vision board image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            if (showActionDialog) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF9C27B0).copy(alpha = 0.3f))
                        .clickable { showActionDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Delete button
                        FloatingActionButton(
                            onClick = { onDelete() },
                            containerColor = Color.White,
                            contentColor = Color(0xFFF44336), // Red
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete image",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Share button
                        FloatingActionButton(
                            onClick = { onShare() },
                            containerColor = Color.White,
                            contentColor = Color(0xFF9C27B0), // Purple
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share image",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}