package com.example.HealthMateApplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.HealthMateApplication.databinding.ActivityEmailVerificationBinding
import com.example.HealthMateApplication.utils.FirebaseHelper

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailVerificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnChk.setOnClickListener {
            checkEmailVerification()
        }

    }
    private fun checkEmailVerification() {
        val user = FirebaseHelper.getAuth().currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    // If email is verified, pass data to PhoneVerificationActivity
                    val username = intent.getStringExtra("username")
                    val email = intent.getStringExtra("email")
                    val password = intent.getStringExtra("password")
                    val phone = intent.getStringExtra("phone")

                    val intent = Intent(this, AadharSubmissionActivity::class.java)
                    intent.putExtra("username", username)
                    intent.putExtra("email", email)
                    intent.putExtra("password", password)
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to refresh user data. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}