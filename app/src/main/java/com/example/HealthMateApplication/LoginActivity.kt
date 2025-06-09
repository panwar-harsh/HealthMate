package com.example.HealthMateApplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.example.HealthMateApplication.databinding.ActivityLoginBinding
import com.example.HealthMateApplication.models.User
import com.example.HealthMateApplication.utils.FirebaseHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 🛠️ Check if user session already exists
        if (isUserLoggedIn()) {
            val userEmail = getUserEmailFromSession()
            userEmail?.let { fetchUserData(it) }
        }

        binding.signin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val dialog = showLoadingDialog("Logging In...")
                loginUser(email, password) {
                    dialog.dismiss() // Close loading dialog ✅
                }
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.signup.setOnClickListener {
            startActivity(Intent(this, EmailSignupActivity::class.java))
        }

        binding.forgetPassword.setOnClickListener {
            startActivity(Intent(this, ForgetPasswordActivity::class.java))
        }
    }

    // ✅ Function to log in user
    private fun loginUser(email: String, password: String, onComplete: () -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                onComplete()

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        saveSession(email) // 🛠️ Save session
                        fetchUserData(email)
                    } else {
                        Toast.makeText(this, "Please verify your email address.", Toast.LENGTH_SHORT).show()
                        resendVerificationEmail()
                    }
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                onComplete()
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch user data using Aadhaar
    private fun fetchUserData(email: String) {
        FirebaseHelper.usersRef.orderByChild("user_email").equalTo(email).get()
            .addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.children.first().getValue(User::class.java)
                    userData?.let {
                        navigateBasedOnUserData(it)
                    }
                } else {
                    // If user data doesn't exist, start Aadhaar submission
                    Toast.makeText(this, "User not found in database.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AadharSubmissionActivity::class.java))
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Failed to retrieve user data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 🛠️ Updated logic to use Aadhaar-based flow
    private fun navigateBasedOnUserData(userData: User) {
        when {
            userData.user_aadhar.isNullOrEmpty() -> {
                startActivity(Intent(this, AadharSubmissionActivity::class.java))
            }
            userData.user_account_status == "P" -> {
                startActivity(Intent(this, UnderVerificationActivity::class.java))
            }
            userData.user_account_status == "F" -> {
                Toast.makeText(this, "Account Verification Rejected", Toast.LENGTH_LONG).show()
            }
            userData.user_account_status == "T" -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    // 🛠️ Resend verification email if not verified
    private fun resendVerificationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
            if (verifyTask.isSuccessful) {
                Toast.makeText(this, "Verification email sent!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Save user session in SharedPreferences
    private fun saveSession(email: String) {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("user_email", email)
            apply()
        }
    }

    //  Check if session exists
    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.contains("user_email")
    }

    //  Get stored email from session
    private fun getUserEmailFromSession(): String? {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPref.getString("user_email", null)
    }

    private fun clearSession() {
        val sharedPref = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
    }

    private fun showLoadingDialog(message: String): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        val lottieAnimation = dialogView.findViewById<LottieAnimationView>(R.id.lottie_animation)
        val textViewMessage = dialogView.findViewById<TextView>(R.id.text_message)

        lottieAnimation.setAnimation("Animation_1732726453432.json")
        lottieAnimation.playAnimation()
        textViewMessage.text = message

        dialog.show()
        return dialog
    }
}
