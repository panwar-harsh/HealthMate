package com.example.HealthMateApplication.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
     val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // Database references
    val usersRef: DatabaseReference = database.getReference("users")
    val reportsRef: DatabaseReference = database.getReference("reports")
    val appointmentsRef: DatabaseReference = database.getReference("appointments")
    val testsRef: DatabaseReference = database.getReference("tests")
    val profileRef: DatabaseReference = database.getReference("profile")

    // Firebase Auth instance
    fun getAuth(): FirebaseAuth = auth
}
