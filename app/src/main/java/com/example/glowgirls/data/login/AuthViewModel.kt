package com.example.glowgirls.data.login

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.glowgirls.models.login.UserModel
import com.example.glowgirls.navigation.ROUTE_HOME
import com.example.glowgirls.navigation.ROUTE_LOGIN
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow

class AuthViewModel : ViewModel() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    fun signup(
        fulllname: String,
        email: String,
        password: String,
        comfirmpassword: String,
        navController: NavController,
        context: Context

    ){
        if (fulllname.isBlank() ||
            email.isBlank() ||
            password.isBlank() ||
            comfirmpassword.isBlank()) {
            Toast.makeText(context,
                "Please fill all the fields",
                Toast.LENGTH_LONG).show()
            return
        }
        _isLoading.value = true
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task->
                _isLoading.value = false
                if (task.isSuccessful) {
                    Toast.makeText(context, "Signup Successful", Toast.LENGTH_LONG).show()
                    val userId=mAuth.currentUser?.uid?:""
                    val userData= UserModel(
                        fullname = fulllname,
                        email = email,
                        password = password,
                        userId = userId,
                        comfirmpassword = comfirmpassword
                    )
                    saveUserDataToDatabase(userId,userData,navController,context)
                    navController.navigate(ROUTE_LOGIN)
                } else {
                    _errorMessage.value = task.exception?.message.toString()
                    Toast.makeText(context, "Signup Failed", Toast.LENGTH_LONG).show()

    }}}
    fun saveUserDataToDatabase(userId: String, userData: UserModel, navController: NavController, context: Context) {
        val regRef= FirebaseDatabase.getInstance().getReference("Users/${userId}")
        regRef.setValue(userData)
            .addOnCompleteListener {  regRef->
                if (regRef.isSuccessful) {
                    Toast.makeText(context, "User Data Saved Successfully", Toast.LENGTH_LONG).show()
                    navController.navigate(ROUTE_LOGIN)
                } else {

                    Toast.makeText(context, "Failed to Save User Data", Toast.LENGTH_LONG).show()
                }
            }

    }
       fun login(email: String, password: String, navController: NavController, context: Context) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_LONG).show()
            return
        }

        _isLoading.value = true
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show()
                    navController.navigate(ROUTE_HOME)
                } else {
                    _errorMessage.value = task.exception?.message.toString()
                    Toast.makeText(context, "Login Failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun logout(navController: NavController, context: Context) {
        mAuth.signOut()
        Toast.makeText(context, "Logout Successful", Toast.LENGTH_LONG).show()
        navController.navigate(ROUTE_LOGIN)
    }
}