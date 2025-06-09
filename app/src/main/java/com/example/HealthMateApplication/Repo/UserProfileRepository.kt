package com.example.HealthMateApplication.Repo

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.example.HealthMateApplication.R
import com.example.HealthMateApplication.models.Profile
import com.example.HealthMateApplication.utils.FirebaseHelper

class UserProfileRepository {

    fun fetchUserProfile(
        includeName: Boolean = true,
        includePhone: Boolean = true,
        includeEmail: Boolean = true,
        includeImage: Boolean = true,
        imageView: ImageView? = null, // Pass ImageView for loading image
        callback: (Map<String, Any?>) -> Unit
    ) {
        val currentUserEmail = FirebaseHelper.getAuth().currentUser?.email

        if (currentUserEmail != null) {
            FirebaseHelper.profileRef.orderByChild("user_email").equalTo(currentUserEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val profileData = mutableMapOf<String, Any?>()

                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val profile = child.getValue(Profile::class.java)
                                profile?.let {
                                    // Conditionally add values based on user request
                                    if (includeName) profileData["user_name"] = it.user_name
                                    if (includePhone && it.user_phone.isNotEmpty()) profileData["user_phone"] = it.user_phone
                                    if (includeEmail) profileData["user_email"] = it.user_email

                                    // Load image directly from Cloudinary URL
                                    if (includeImage && !it.user_profile_pic.isNullOrEmpty()) {
                                        loadImage(it.user_profile_pic, imageView)
                                    }
                                }
                            }
                        }

                        callback(profileData)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        callback(emptyMap()) // Return empty data on error
                    }
                })
        } else {
            callback(emptyMap()) // Return empty data if user not authenticated
        }
    }

    // Load image directly using Glide from Cloudinary URL
    private fun loadImage(imageUrl: String?, imageView: ImageView?) {
        if (imageView == null || imageUrl.isNullOrEmpty()) {
            imageView?.setImageResource(R.drawable.profile_image)
            return
        }

        Glide.with(imageView.context)
            .load(imageUrl)
            .placeholder(R.drawable.profile_image)
            .error(R.drawable.profile_image)
            .into(imageView)
    }
}
