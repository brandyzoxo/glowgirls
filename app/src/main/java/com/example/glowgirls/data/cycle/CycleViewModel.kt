package com.example.glowgirls.data.cycle

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.glowgirls.models.cycle.CycleData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CycleViewModel : ViewModel() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    fun saveCycleData(
        lastPeriodDate: String,
        cycleLength: String,
        periodDuration: String,
        nextPeriodDate: String,
        ovulationDate: String,
        context: Context
    ) {
        val userId = mAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
            return
        }

        val cycleData = CycleData(
            lastPeriodDate = lastPeriodDate,
            cycleLength = cycleLength,
            periodDuration = periodDuration,
            nextPeriodDate = nextPeriodDate,
            ovulationDate = ovulationDate
        )

        val cycleRef = FirebaseDatabase.getInstance()
            .getReference("Users/$userId/CycleData")  // Saving under each user

        cycleRef.push().setValue(cycleData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Cycle Data Saved Successfully", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to Save Cycle Data", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun getCycleData(context: Context, callback: (CycleData?) -> Unit) {
        val userId = mAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show()
            callback(null)
            return
        }

        val cycleRef = FirebaseDatabase.getInstance()
            .getReference("Users/$userId/CycleData")

        cycleRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Get the most recent entry
                    for (childSnapshot in snapshot.children) {
                        val cycleData = childSnapshot.getValue(CycleData::class.java)
                        callback(cycleData)
                        return
                    }
                }
                // No data found
                callback(null)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load cycle data: ${error.message}", Toast.LENGTH_LONG).show()
                callback(null)
            }
        })
    }
}