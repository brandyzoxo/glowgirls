package com.example.glowgirls.ui.theme.screens.screens.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.glowgirls.data.chat.ChatViewModel
import com.example.glowgirls.models.chat.ChatMessage
import com.example.glowgirls.models.chat.ChatRoom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Enhanced feminine color scheme
val RosePink = Color(0xFFF2C4CE)
val DeepRose = Color(0xFFE6A4B4)
val LavenderMist = Color(0xFFD8B4E2)
val PeachBlush = Color(0xFFFFD9C0)
val SoftLilac = Color(0xFFBCB6FF)
val MintGreen = Color(0xFFBDEDC3)
val SoftCream = Color(0xFFFFF7F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val chatRooms by viewModel.chatRooms.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentChatRoomId by viewModel.currentChatRoomId.collectAsState()
    val currentUserId = viewModel.currentUserId

    // For create chat room dialog
    var showCreateRoomDialog by remember { mutableStateOf(false) }

    // Find the current chat room
    val currentChatRoom = chatRooms.find { it.id == currentChatRoomId }

    // Enhanced gradient background brush
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            SoftCream,
            RosePink.copy(alpha = 0.12f),
            LavenderMist.copy(alpha = 0.15f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentChatRoom?.name ?: "GlowChat",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = DeepRose
                        )
                        if (currentChatRoom?.description?.isNotEmpty() == true) {
                            Text(
                                text = currentChatRoom.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = DeepRose.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = DeepRose
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Chat room selector
                ChatRoomSelector(
                    chatRooms = chatRooms,
                    currentChatRoomId = currentChatRoomId,
                    onChatRoomSelected = { viewModel.selectChatRoom(it.id) },
                    onAddChatRoom = { showCreateRoomDialog = true }
                )

                // Messages
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(
                            items = messages,
                            key = { message -> message.messageId }
                        ) { message ->
                            ChatMessageItem(
                                message = message,
                                isOwnMessage = message.senderId == currentUserId
                            )
                        }
                    }
                }

                // Message input field
                ChatInputField(
                    onSendMessage = { messageText ->
                        viewModel.sendMessage(messageText)
                    }
                )
            }
        }

        // Create chat room dialog
        if (showCreateRoomDialog) {
            CreateChatRoomDialog(
                onDismiss = { showCreateRoomDialog = false },
                onCreateRoom = { name, description ->
                    viewModel.createChatRoom(name, description)
                    showCreateRoomDialog = false
                }
            )
        }
    }
}

@Composable
fun ChatRoomSelector(
    chatRooms: List<ChatRoom>,
    currentChatRoomId: String?,
    onChatRoomSelected: (ChatRoom) -> Unit,
    onAddChatRoom: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.5f))
            .padding(vertical = 12.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Add room button
            item {
                AddChatRoomChip(onClick = onAddChatRoom)
            }

            // Chat rooms
            items(chatRooms) { room ->
                ChatRoomChip(
                    room = room,
                    isSelected = room.id == currentChatRoomId,
                    onClick = { onChatRoomSelected(room) }
                )
            }
        }
    }
}

@Composable
fun AddChatRoomChip(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        DeepRose.copy(alpha = 0.8f),
                        SoftLilac.copy(alpha = 0.8f)
                    )
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .wrapContentSize()
            .heightIn(min = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Room",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = "New Space",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun ChatRoomChip(
    room: ChatRoom,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            DeepRose
        } else {
            LavenderMist.copy(alpha = 0.5f)
        },
        label = "chipBackgroundColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 2.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "chipElevation"
    )

    Box(
        modifier = Modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .wrapContentSize()
            .heightIn(min = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = room.name,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) Color.White else Color.DarkGray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatRoomDialog(
    onDismiss: () -> Unit,
    onCreateRoom: (name: String, description: String) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var roomDescription by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SoftCream,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Create New Chat Space",
                color = DeepRose,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Space Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepRose,
                        focusedLabelColor = DeepRose,
                        cursorColor = DeepRose,
                        unfocusedBorderColor = LavenderMist
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = roomDescription,
                    onValueChange = { roomDescription = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepRose,
                        focusedLabelColor = DeepRose,
                        cursorColor = DeepRose,
                        unfocusedBorderColor = LavenderMist
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roomName.isNotBlank()) {
                        onCreateRoom(roomName, roomDescription)
                    }
                },
                enabled = roomName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepRose
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = DeepRose
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChatMessageItem(message: ChatMessage, isOwnMessage: Boolean) {
    val messageBubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isOwnMessage) 20.dp else 5.dp,
        bottomEnd = if (isOwnMessage) 5.dp else 20.dp
    )

    val bubbleGradient = if (isOwnMessage) {
        Brush.linearGradient(
            colors = listOf(
                DeepRose,
                SoftLilac
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color.White.copy(alpha = 0.95f)
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isOwnMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        // Message bubble without profile picture or username
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 2.dp,
                    shape = messageBubbleShape
                )
                .clip(messageBubbleShape)
                .background(bubbleGradient)
                .padding(14.dp)
                .widthIn(max = 260.dp)
        ) {
            Column {
                Text(
                    text = message.message,
                    color = if (isOwnMessage) Color.White else Color.DarkGray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTimestamp(message.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontStyle = FontStyle.Italic
                    ),
                    color = if (isOwnMessage) Color.White.copy(alpha = 0.7f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputField(onSendMessage: (String) -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            RosePink.copy(alpha = 0.3f),
                            LavenderMist.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = DeepRose
                ),
                singleLine = false,
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                DeepRose,
                                SoftLilac
                            )
                        )
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = messageText.isNotBlank()
                    ) {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText)
                            messageText = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                )
            }
        }
    }
}

// Helper function to format timestamp
private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(date)
}