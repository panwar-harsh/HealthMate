package com.example.HealthMateApplication

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import android.view.View
import com.example.HealthMateApplication.databinding.ActivityUserProfileBinding
import com.example.HealthMateApplication.models.Profile
import com.example.HealthMateApplication.utils.FirebaseHelper

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var personalRef: DatabaseReference
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private var userEmail: String? = null
    private var personalKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        personalRef = FirebaseHelper.profileRef
        userEmail = FirebaseHelper.getAuth().currentUser?.email
        binding.etEmail.text = userEmail

        loadUserProfile()

        binding.changeProfilePicture.setOnClickListener {
            openImagePicker()
        }

        binding.iconDown.setOnClickListener {
            finish()
        }

        binding.save.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun loadUserProfile() {
        userEmail?.let { email ->
            binding.progressBar.visibility = View.VISIBLE
            personalRef.orderByChild("user_email").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.progressBar.visibility = View.GONE
                        if (snapshot.exists()) {
                            personalKey = snapshot.children.first().key
                            val profile = snapshot.children.first().getValue(Profile::class.java)
                            profile?.let {
                                binding.profileName.setText(it.user_name)
                                binding.userPhone.setText(it.user_phone)
                                it.user_profile_pic?.let { url ->
                                    Glide.with(this@UserProfileActivity)
                                        .load(url)
                                        .into(binding.profile)
                                }
                            }
                        } else {
                            Toast.makeText(this@UserProfileActivity, "Profile not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@UserProfileActivity, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        } ?: run {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this@UserProfileActivity, "User email not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserProfile() {
        val name = binding.profileName.text.toString().trim()
        val phone = binding.userPhone.text.toString().trim()
        val address = binding.profileLocation.text.toString().trim()

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        if (imageUri != null) {
            CloudinaryUploader.uploadImage(
                context = this,
                imageUri = imageUri!!,
                onSuccess = { imageUrl ->
                    saveProfileToDatabase(name, phone, address, imageUrl)
                },
                onFailure = { error ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            loadExistingProfilePicUrl { existingUrl ->
                saveProfileToDatabase(name, phone, address, existingUrl)
            }
        }
    }

    private fun loadExistingProfilePicUrl(onSuccess: (String) -> Unit) {
        personalKey?.let { key ->
            personalRef.child(key).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val existingProfilePicUrl = snapshot.child("user_profile_pic").getValue(String::class.java)
                    onSuccess(existingProfilePicUrl ?: "")
                }

                override fun onCancelled(error: DatabaseError) {
                    onSuccess("") // fallback if error
                }
            })
        } ?: onSuccess("") // fallback if no key
    }

    private fun saveProfileToDatabase(name: String, phone: String, address: String, profilePicUrl: String?) {
        userEmail?.let { email ->
            val profile = Profile(
                user_email = email,
                user_name = name,
                user_profile_pic = profilePicUrl ?: "",
                user_address = address,
                user_phone = phone
            )

            val isNewUser = personalKey == null
            val key = personalKey ?: personalRef.push().key ?: email.replace(".", "_")

            personalRef.child(key).setValue(profile)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    if (isNewUser) {
                        startActivity(Intent(this, UnderVerificationActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.profile.setImageURI(imageUri)
        }
    }
}
