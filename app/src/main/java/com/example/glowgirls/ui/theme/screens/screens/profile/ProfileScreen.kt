package com.example.glowgirls.ui.theme.screens.screens.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Settings
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
import coil.compose.rememberImagePainter
import com.example.glowgirls.data.profile.FirebaseProfileManager
import com.example.glowgirls.models.profile.UserProfile
import com.example.glowgirls.network.ImgurHelper
import com.google.firebase.database.ValueEventListener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userId: String, onNavigateToSettings: () -> Unit = {}) {
    val context = LocalContext.current
    val firebaseProfileManager = FirebaseProfileManager()
    val imgurHelper = ImgurHelper()
    var userProfile by remember { mutableStateOf(UserProfile()) }
    var isLoading by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    var profileListener: ValueEventListener? by remember { mutableStateOf(null) }

    // Set up listener for real-time profile updates
    DisposableEffect(userId) {
        isLoading = true

        // Check if profile exists, create default if needed
        firebaseProfileManager.checkProfileExists(userId) { exists ->
            if (!exists) {
                // Create a default profile if none exists
                val defaultProfile = UserProfile(
                    username = "Glow Girl",
                    bio = "Ready to glow and grow!",
                    createdAt = System.currentTimeMillis(),
                    lastUpdatedAt = System.currentTimeMillis()
                )

                firebaseProfileManager.saveUserProfile(userId, defaultProfile) { success, error ->
                    if (!success) {
                        errorMessage = error ?: "Failed to create default profile"
                    }
                    isLoading = false
                }
            } else {
                isLoading = false
            }
        }

        // Set up real-time listener
        val listener = firebaseProfileManager.listenForProfileChanges(userId) { profile ->
            profile?.let { userProfile = it }
        }
        profileListener = listener

        // Clean up listener when component is disposed
        onDispose {
            profileListener?.let {
                firebaseProfileManager.removeProfileListener(userId, it)
            }
        }
    }

    // Image picker
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                imgurHelper.uploadImage(bytes) { imageUrl ->
                    imageUrl?.let { url ->
                        val updatedProfile = userProfile.copy(
                            profilePictureUrl = url,
                            lastUpdatedAt = System.currentTimeMillis()
                        )

                        firebaseProfileManager.saveUserProfile(userId, updatedProfile) { success, error ->
                            isLoading = false
                            if (success) {
                                Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to update profile picture: ${error ?: "Unknown error"}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } ?: run {
                        isLoading = false
                        Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFC2E2)) // Light pink background matching app theme
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Top App Bar with settings icon
            TopAppBar(
                title = {
                    Text(
                        "My Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color(0xFFFF4081),
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFFFF4081)
                        )
                    }
                }
            )

            // Profile header section with background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(Color.White)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Profile Picture with edit button overlay
                    Box(contentAlignment = Alignment.BottomEnd) {
                        // Profile picture
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color(0xFFFF4081), CircleShape)
                                .background(Color.White)
                        ) {
                            Image(
                                painter = rememberImagePainter(
                                    data = userProfile.profilePictureUrl.takeIf { it.isNotEmpty() }
                                        ?: "https://via.placeholder.com/150",
                                    builder = {
                                        crossfade(true)
                                        placeholder(drawableResId = android.R.drawable.ic_menu_gallery)
                                    }
                                ),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Edit button overlay
                        FloatingActionButton(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.size(36.dp),
                            containerColor = Color(0xFFFF4081),
                            contentColor = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit profile picture",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username display
                    Text(
                        text = userProfile.username.ifEmpty { "Glow Girl" },
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                    )

                    // Bio display
                    Text(
                        text = userProfile.bio.ifEmpty { "Ready to glow and grow!" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )

                    // Stats row (followers, posts, etc.)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStat(count = userProfile.posts, label = "Posts")
                        ProfileStat(count = userProfile.streakDays, label = "Streak Days")
                        ProfileStat(count = userProfile.achievements, label = "Achievements")
                    }

                    // Edit Profile button
                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4081)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Edit Profile")
                    }
                }
            }

            // Profile content sections
            ProfileSection(
                title = "My Glow Journey",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatCard(
                        icon = Icons.Outlined.Favorite,
                        title = "Cycle Days",
                        value = "${userProfile.cycleDays}",
                        color = Color(0xFFF06292),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    StatCard(
                        icon = Icons.Default.Star,
                        title = "Glow Points",
                        value = "${userProfile.glowPoints}",
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            ProfileSection(
                title = "My Achievements",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AchievementBadge(
                        title = "7-Day Streak",
                        isAchieved = userProfile.streakDays >= 7,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AchievementBadge(
                        title = "Budget Master",
                        isAchieved = userProfile.budgetMaster,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AchievementBadge(
                        title = "Vision Set",
                        isAchieved = userProfile.visionSet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            ProfileSection(
                title = "My Activity",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                ActivityItem(
                    title = "Budget created",
                    time = "2 days ago",
                    icon = Icons.Default.AttachMoney
                )
                ActivityItem(
                    title = "Vision board updated",
                    time = "1 week ago",
                    icon = Icons.Default.Visibility
                )
                ActivityItem(
                    title = "Journal entry added",
                    time = "3 days ago",
                    icon = Icons.Default.Book
                )
            }

            Spacer(modifier = Modifier.height(80.dp)) // Bottom spacing
        }

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x80000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFFF4081)
                )
            }
        }

        // Error message handling
        errorMessage?.let { message ->
            LaunchedEffect(message) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                errorMessage = null
            }
        }

        // Edit Profile Dialog
        if (isEditing) {
            EditProfileDialog(
                userProfile = userProfile,
                onDismiss = { isEditing = false },
                onSave = { updatedProfile ->
                    isLoading = true
                    val finalUpdatedProfile = updatedProfile.copy(
                        lastUpdatedAt = System.currentTimeMillis()
                    )
                    firebaseProfileManager.saveUserProfile(userId, finalUpdatedProfile) { success, error ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to update profile: ${error ?: "Unknown error"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        isEditing = false
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileStat(count: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF4081)
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF666666)
            )
        )
    }
}

@Composable
fun ProfileSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF4081)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF666666)
                )
            )
        }
    }
}

@Composable
fun AchievementBadge(
    title: String,
    isAchieved: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isAchieved) Color(0xFFFFD700).copy(alpha = 0.2f)
                    else Color(0xFFEEEEEE)
                )
                .border(
                    width = 2.dp,
                    color = if (isAchieved) Color(0xFFFFD700) else Color(0xFFCCCCCC),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (isAchieved) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (isAchieved) Color(0xFFFFD700) else Color(0xFFCCCCCC),
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                textAlign = TextAlign.Center,
                color = if (isAchieved) Color(0xFF333333) else Color(0xFF999999)
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ActivityItem(
    title: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFC2E2))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFF4081),
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF999999)
                )
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    userProfile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var updatedProfile by remember { mutableStateOf(userProfile) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Profile",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFFFF4081),
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = updatedProfile.username,
                    onValueChange = { updatedProfile = updatedProfile.copy(username = it) },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = updatedProfile.bio,
                    onValueChange = { updatedProfile = updatedProfile.copy(bio = it) },
                    label = { Text("Bio") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = updatedProfile.cycleDays.toString(),
                    onValueChange = {
                        val days = it.toIntOrNull() ?: updatedProfile.cycleDays
                        updatedProfile = updatedProfile.copy(cycleDays = days)
                    },
                    label = { Text("Cycle Days") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // Add more fields for other profile attributes if needed
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(updatedProfile) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFFF4081)
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}