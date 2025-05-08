package com.example.glowgirls.data.vision

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glowgirls.data.vision.network.RetrofitClient
import com.example.glowgirls.models.vision.VisionImage
import com.example.glowgirls.util.toRequestBody
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File

class VisionBoardViewModel : ViewModel() {
    private val firebaseHelper = FirebaseHelper()
    private val imgurClientId = "7c3f10d26d51e6d" // Replace with your Imgur Client ID

    private val _images = MutableStateFlow<List<VisionImage>>(emptyList())
    val images: StateFlow<List<VisionImage>> = _images

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        loadImages()
        if (imgurClientId == "YOUR_IMGUR_CLIENT_ID") {
            setError("Imgur Client ID is not set. Please update VisionBoardViewModel.kt with your Imgur Client ID.")
        }
    }

    private fun loadImages() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("VisionBoardViewModel", "Loading images from Firebase")
                _images.value = firebaseHelper.getImages()
                Log.d("VisionBoardViewModel", "Images loaded successfully: ${images.value.size}")
            } catch (e: Exception) {
                Log.e("VisionBoardViewModel", "Error loading images: ${e.message}")
                setError("Failed to load images: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadImage(uri: Uri, context: Context, category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("VisionBoardViewModel", "Starting image upload for URI: $uri")

                // Step 1: Create temp file
                Log.d("VisionBoardViewModel", "Creating temp file")
                val file = File.createTempFile("image", ".jpg", context.cacheDir)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                Log.d("VisionBoardViewModel", "Temp file created: ${file.absolutePath}")

                // Step 2: Prepare request body for Imgur
                Log.d("VisionBoardViewModel", "Preparing request body")
                val requestBody = MultipartBody.Part.createFormData("image", file.name, file.toRequestBody())
                Log.d("VisionBoardViewModel", "Request body prepared")

                // Step 3: Upload to Imgur
                Log.d("VisionBoardViewModel", "Uploading to Imgur with Client ID: $imgurClientId")
                val response = RetrofitClient.imgurApiService.uploadImage("Client-ID $imgurClientId", requestBody)
                Log.d("VisionBoardViewModel", "Imgur response: Success=${response.success}, Status=${response.status}, Link=${response.data.link}")

                // Step 4: Save to Firebase
                val visionImage = VisionImage(
                    id = response.data.id,
                    link = response.data.link,
                    timestamp = System.currentTimeMillis(),
                    category = category
                )
                Log.d("VisionBoardViewModel", "Saving to Firebase: $visionImage")
                firebaseHelper.saveImage(visionImage)
                Log.d("VisionBoardViewModel", "Image saved to Firebase")

                // Step 5: Update local state
                _images.value = _images.value + visionImage
                Log.d("VisionBoardViewModel", "Local state updated. Total images: ${images.value.size}")
            } catch (e: Exception) {
                Log.e("VisionBoardViewModel", "Error uploading image: ${e.message}", e)
                setError("Failed to upload image: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteImage(id: String) {
        viewModelScope.launch {
            try {
                Log.d("VisionBoardViewModel", "Deleting image with ID: $id")
                firebaseHelper.deleteImage(id)
                _images.value = _images.value.filter { it.id != id }
                Log.d("VisionBoardViewModel", "Image deleted successfully")
            } catch (e: Exception) {
                Log.e("VisionBoardViewModel", "Error deleting image: ${e.message}")
                setError("Failed to delete image: ${e.message}")
            }
        }
    }

    fun setError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }
}