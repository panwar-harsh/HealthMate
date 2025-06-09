package com.example.HealthMateApplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.HealthMateApplication.databinding.ActivityReportAccessBinding
import com.squareup.picasso.Picasso

class ReportAccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReportAccessBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportAccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        // When the button is clicked, fetch the user's email, then check the user report
        binding.btnFetchAadhaar.setOnClickListener {
            fetchUserEmail { userEmail ->
                fetchUserAdhaar(userEmail) { userAadhaar ->
                    fetchReport(userAadhaar, false) // Fetch user report
                }
            }
        }

        binding.btnNavHome.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        // When the second button is clicked, fetch the parental report
        binding.btnFetchParentalAadhaar.setOnClickListener {
            fetchUserEmail { userEmail ->
                fetchUserAdhaar(userEmail) { userAadhaar ->
                    fetchReport(userAadhaar, true) // Fetch parental report
                }
            }
        }
    }

    // Fetch the current user's email from Firebase Auth
    private fun fetchUserEmail(callback: (String) -> Unit) {
        val user = auth.currentUser
        user?.let {
            val userEmail = it.email
            if (!userEmail.isNullOrEmpty()) {
                callback(userEmail)
            } else {
                Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserAdhaar(userEmail: String, callback: (String) -> Unit) {
        // Search for the user by email in the users collection
        database.orderByChild("user_email").equalTo(userEmail).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                for (userSnapshot in snapshot.children) {
                    val userAdhaar = userSnapshot.child("user_adhaar").value as String?
                    userAdhaar?.let {
                        callback(it) // Pass the Adhaar back to the callback
                        return@addOnSuccessListener
                    }
                }
                Toast.makeText(this, "Adhaar not found for the user", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No matching user found for this email", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error fetching user Aadhaar: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Fetch the report based on the Aadhaar
    private fun fetchReport(userAadhaar: String, isParental: Boolean) {
        val reportsRef = FirebaseDatabase.getInstance().getReference("reports")
        val queryField = if (isParental) "user_report_parent_aadhaar" else "user_report_aadhaar"

        // Search the reports collection for a matching Aadhaar
        reportsRef.orderByChild(queryField).equalTo(userAadhaar)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    for (reportSnapshot in snapshot.children) {
                        val reportUrl = reportSnapshot.child("user_report_url").value as String?
                        reportUrl?.let {
                            Toast.makeText(this, if (isParental) "Parental Report Found!" else "User Report Found!", Toast.LENGTH_SHORT).show()
                            displayReportImage(it) // Display the report image
                            return@addOnSuccessListener
                        }
                    }
                } else {
                    Toast.makeText(this, if (isParental) "No parental report found" else "No user report found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error fetching report: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Display the report image using Picasso
    private fun displayReportImage(imageUrl: String) {
        val imageView: ImageView = binding.reportImageView
        Picasso.get().load(imageUrl).into(imageView)
    }
}
