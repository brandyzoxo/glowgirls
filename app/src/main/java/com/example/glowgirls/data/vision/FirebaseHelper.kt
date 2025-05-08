package com.example.glowgirls.data.vision

import com.example.glowgirls.models.vision.VisionImage
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseHelper {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("vision_images")

    suspend fun saveImage(visionImage: VisionImage) {
        database.child(visionImage.id).setValue(visionImage).await()
    }

    suspend fun getImages(): List<VisionImage> {
        val snapshot = database.get().await()
        return snapshot.children.mapNotNull { it.getValue(VisionImage::class.java) }
    }

    suspend fun deleteImage(id: String) {
        database.child(id).removeValue().await()
    }
}