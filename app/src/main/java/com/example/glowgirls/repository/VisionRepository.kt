package com.example.glowgirls.repository

import android.net.Uri
import android.util.Log
import com.example.glowgirls.models.vision.Vision
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.util.UUID

class VisionRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("visions")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val TAG = "VisionRepository"

    suspend fun addVision(vision: Vision, imageUri: Uri? = null): String? {
        val userId = auth.currentUser?.uid ?: run {
            Log.e(TAG, "AddVision: User not authenticated")
            return null
        }
        val id = database.child(userId).push().key ?: run {
            Log.e(TAG, "AddVision: Failed to generate database key")
            return null
        }
        var imageUrl: String? = null

        if (imageUri != null) {
            try {
                val ref = storage.reference.child("visions/$userId/$id/${UUID.randomUUID()}")
                ref.putFile(imageUri).await()
                imageUrl = ref.downloadUrl.await().toString()
                Log.d(TAG, "AddVision: Image uploaded successfully, URL: $imageUrl")
            } catch (e: StorageException) {
                Log.e(TAG, "AddVision: Storage error - Code: ${e.errorCode}, Message: ${e.message}")
                when (e.errorCode) {
                    StorageException.ERROR_NOT_AUTHENTICATED -> Log.e(TAG, "User not authenticated for storage access")
                    StorageException.ERROR_OBJECT_NOT_FOUND -> Log.e(TAG, "Storage path does not exist")
                    StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> Log.e(TAG, "Network issue, retry limit exceeded")
                    StorageException.ERROR_UNKNOWN -> Log.e(TAG, "Unknown storage error, check network or file integrity")
                }
                return null // Fail silently or handle as needed
            } catch (e: Exception) {
                Log.e(TAG, "AddVision: Unexpected error during upload: ${e.message}")
                return null
            }
        }

        try {
            val newVision = vision.copy(id = id, imageUrl = imageUrl)
            database.child(userId).child(id).setValue(newVision).await()
            Log.d(TAG, "AddVision: Vision added to database with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e(TAG, "AddVision: Database error: ${e.message}")
            return null
        }
    }

    suspend fun getVisions(): List<Vision> {
        val userId = auth.currentUser?.uid ?: run {
            Log.e(TAG, "GetVisions: User not authenticated")
            return emptyList()
        }
        try {
            val snapshot = database.child(userId).get().await()
            return snapshot.children.mapNotNull { it.getValue(Vision::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "GetVisions: Database error: ${e.message}")
            return emptyList()
        }
    }

    suspend fun updateVision(vision: Vision, imageUri: Uri? = null) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e(TAG, "UpdateVision: User not authenticated")
            return
        }
        var updatedVision = vision

        if (imageUri != null) {
            try {
                val ref = storage.reference.child("visions/$userId/${vision.id}/${UUID.randomUUID()}")
                ref.putFile(imageUri).await()
                val imageUrl = ref.downloadUrl.await().toString()
                updatedVision = vision.copy(imageUrl = imageUrl)
                Log.d(TAG, "UpdateVision: Image uploaded successfully, URL: $imageUrl")
            } catch (e: StorageException) {
                Log.e(TAG, "UpdateVision: Storage error - Code: ${e.errorCode}, Message: ${e.message}")
                when (e.errorCode) {
                    StorageException.ERROR_NOT_AUTHENTICATED -> Log.e(TAG, "User not authenticated for storage access")
                    StorageException.ERROR_OBJECT_NOT_FOUND -> Log.e(TAG, "Storage path does not exist")
                    StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> Log.e(TAG, "Network issue, retry limit exceeded")
                    StorageException.ERROR_UNKNOWN -> Log.e(TAG, "Unknown storage error, check network or file integrity")
                }
                return // Fail silently or handle as needed
            } catch (e: Exception) {
                Log.e(TAG, "UpdateVision: Unexpected error during upload: ${e.message}")
                return
            }
        }

        try {
            database.child(userId).child(vision.id).setValue(updatedVision).await()
            Log.d(TAG, "UpdateVision: Vision updated with ID: ${vision.id}")
        } catch (e: Exception) {
            Log.e(TAG, "UpdateVision: Database error: ${e.message}")
        }
    }

    suspend fun deleteVision(id: String) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e(TAG, "DeleteVision: User not authenticated")
            return
        }
        try {
            database.child(userId).child(id).removeValue().await()
            try {
                storage.reference.child("visions/$userId/$id").delete().await()
                Log.d(TAG, "DeleteVision: Image folder deleted for ID: $id")
            } catch (e: StorageException) {
                if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                    Log.e(TAG, "DeleteVision: Storage error - Code: ${e.errorCode}, Message: ${e.message}")
                } else {
                    Log.d(TAG, "DeleteVision: No images to delete for ID: $id")
                }
            }
            Log.d(TAG, "DeleteVision: Vision deleted with ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, "DeleteVision: Database error: ${e.message}")
        }
    } suspend fun getPosts(): List<Vision> {
        return try {
            val snapshot = database.get().await()
            snapshot.children.mapNotNull { it.getValue(Vision::class.java) }
        } catch (e: Exception) {
            Log.e("VisionRepository", "Error fetching posts", e)
            emptyList()
        }
    }

    suspend fun addPost(post: Vision) {
        try {
            val id = database.push().key ?: return
            val postWithId = post.copy(id = id)
            database.child(id).setValue(postWithId).await()
        } catch (e: Exception) {
            Log.e("VisionRepository", "Error adding post", e)
        }
    }

    suspend fun updatePost(post: Vision) {
        try {
            database.child(post.id).setValue(post).await()
        } catch (e: Exception) {
            Log.e("VisionRepository", "Error updating post", e)
        }
    }

}