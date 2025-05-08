package com.example.glowgirls.data.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.glowgirls.models.chat.ChatMessage
import com.example.glowgirls.models.chat.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ViewModel() {

    // Current user info
    val currentUserId: String
        get() = firebaseAuth.currentUser?.uid ?: ""

    private val currentUserName: String
        get() = firebaseAuth.currentUser?.displayName ?: "Anonymous"

    // References to Firebase nodes
    private val chatRoomsRef = database.getReference("chat_rooms")
    private val messagesRef = database.getReference("chat_messages")

    // StateFlow for chat rooms and messages
    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    // Track the currently selected chat room
    private val _currentChatRoomId = MutableStateFlow<String?>(null)
    val currentChatRoomId: StateFlow<String?> = _currentChatRoomId

    init {
        // Load available chat rooms
        loadChatRooms()
    }

    private fun loadChatRooms() {
        chatRoomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val roomsList = mutableListOf<ChatRoom>()

                for (roomSnapshot in snapshot.children) {
                    val room = roomSnapshot.getValue(ChatRoom::class.java)
                    room?.let { roomsList.add(it) }
                }

                _chatRooms.value = roomsList

                // If no room is selected and we have rooms, select the first one
                if (_currentChatRoomId.value == null && roomsList.isNotEmpty()) {
                    selectChatRoom(roomsList[0].id)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Failed to load chat rooms: ${error.message}")
            }
        })
    }

    fun selectChatRoom(roomId: String) {
        _currentChatRoomId.value = roomId
        loadMessagesForRoom(roomId)
    }

    private fun loadMessagesForRoom(roomId: String) {
        messagesRef.child(roomId)
            .orderByChild("timestamp")
            .limitToLast(100)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messagesList = mutableListOf<ChatMessage>()

                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(ChatMessage::class.java)
                        message?.let { messagesList.add(it) }
                    }

                    // Sort messages by timestamp (newest last)
                    messagesList.sortBy { it.timestamp }

                    _messages.value = messagesList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "Failed to load messages: ${error.message}")
                }
            })
    }

    fun sendMessage(messageText: String) {
        val roomId = _currentChatRoomId.value ?: return
        if (messageText.isBlank() || currentUserId.isEmpty()) return

        // Create a new message ID
        val messageId = messagesRef.child(roomId).push().key ?: return

        // Create message object
        val newMessage = ChatMessage(
            messageId = messageId,
            chatRoomId = roomId,
            senderId = currentUserId,
            senderName = currentUserName,
            message = messageText,
            timestamp = System.currentTimeMillis()
        )

        // Save the message to Firebase
        messagesRef.child(roomId).child(messageId).setValue(newMessage)
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Failed to send message: ${e.message}")
            }
    }

    fun createChatRoom(name: String, description: String) {
        if (name.isBlank() || currentUserId.isEmpty()) return

        // Create a new chat room ID
        val roomId = chatRoomsRef.push().key ?: return

        // Create chat room object
        val newChatRoom = ChatRoom(
            id = roomId,
            name = name,
            description = description,
            createdBy = currentUserId,
            createdAt = System.currentTimeMillis(),
            memberCount = 1 // Start with the creator
        )

        // Save the chat room to Firebase
        chatRoomsRef.child(roomId).setValue(newChatRoom)
            .addOnSuccessListener {
                // Select the newly created room
                selectChatRoom(roomId)
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Failed to create chat room: ${e.message}")
            }
    }
}