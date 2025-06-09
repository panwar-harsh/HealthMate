package com.example.HealthMateApplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.HealthMateApplication.databinding.ActivityAadharsubmissionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.HealthMateApplication.models.User



import org.mindrot.jbcrypt.BCrypt

class AadharSubmissionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAadharsubmissionBinding
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var auth: FirebaseAuth

    // Firebase Database reference
    private val database = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAadharsubmissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val username = intent.getStringExtra("username")
        val email = intent.getStringExtra("email")
        val simplepassword = intent.getStringExtra("password")
        val phone = intent.getStringExtra("phone")
        val password = BCrypt.hashpw(simplepassword, BCrypt.gensalt())

        binding.buttonSelectImage.setOnClickListener {
            openFileChooser()
        }

        binding.buttonUploadAdhaar.setOnClickListener {
            val aadhaar = binding.editTextAdhaar.text.toString().trim()
            if (aadhaar.isNotEmpty() && imageUri != null) {
                uploadAadhaarData(aadhaar, imageUri!!, username, email, password, phone)
            } else {
                Toast.makeText(this, "Please fill all fields and select an image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            binding.imageViewAdhaar.setImageURI(imageUri)
        }
    }

    private fun uploadAadhaarData(
        aadhaar: String,
        imageUri: Uri,
        username: String?,
        email: String?,
        password: String?,
        phone: String?
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        CloudinaryUploader.uploadImage(
            context = this,
            imageUri = imageUri,
            onSuccess = { uploadedUrl ->
                val user = User(
                    user_name = username ?: "",
                    user_email = email ?: "",
                    user_password = password ?: "",
                    user_aadhar = aadhaar,
                    user_adhaar_img_url = uploadedUrl,
                    user_phone = phone ?: "",
                    user_account_status = "underVerification"
                )

                database.child(userId).setValue(user)
                    .addOnSuccessListener {
                        runOnUiThread {
                            Toast.makeText(this@AadharSubmissionActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, UserProfileActivity::class.java))
                        }
                        finish()
                    }
                    .addOnFailureListener { exception ->
                        runOnUiThread {
                            Toast.makeText(this@AadharSubmissionActivity, "Error saving user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            },
            onFailure = { error ->
                runOnUiThread {
                    Log.e("CloudinaryUpload", "Upload failed: $error")
                    Toast.makeText(this, "Upload failed: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
