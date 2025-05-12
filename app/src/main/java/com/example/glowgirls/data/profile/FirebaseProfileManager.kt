package com.example.glowgirls.data.profile

import com.example.glowgirls.models.profile.UserProfile
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseError

class FirebaseProfileManager {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val profilesRef: DatabaseReference = database.child("profiles")

    // Save a user profile with proper error handling
    fun saveUserProfile(userId: String, userProfile: UserProfile, onComplete: (Boolean, String?) -> Unit) {
        profilesRef.child(userId).setValue(userProfile)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    // Get a user profile with enhanced error handling
    fun getUserProfile(userId: String, onResult: (UserProfile?, String?) -> Unit) {
        profilesRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                val userProfile = snapshot.getValue(UserProfile::class.java)
                onResult(userProfile, null)
            }
            .addOnFailureListener { exception ->
                onResult(null, exception.message)
            }
    }

    // Delete a user profile
    fun deleteUserProfile(userId: String, onComplete: (Boolean, String?) -> Unit) {
        profilesRef.child(userId).removeValue()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    // Update specific fields of a user profile
    fun updateUserProfileFields(userId: String, updates: Map<String, Any>, onComplete: (Boolean, String?) -> Unit) {
        profilesRef.child(userId).updateChildren(updates)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { exception ->
                onComplete(false, exception.message)
            }
    }

    // Listen for real-time updates to a user profile
    fun listenForProfileChanges(userId: String, onProfileChange: (UserProfile?) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val profile = snapshot.getValue(UserProfile::class.java)
                onProfileChange(profile)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
                onProfileChange(null)
            }
        }

        profilesRef.child(userId).addValueEventListener(listener)
        return listener
    }

    // Remove a profile listener when no longer needed
    fun removeProfileListener(userId: String, listener: ValueEventListener) {
        profilesRef.child(userId).removeEventListener(listener)
    }

    // Check if a profile exists
    fun checkProfileExists(userId: String, onResult: (Boolean) -> Unit) {
        profilesRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.exists())
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}