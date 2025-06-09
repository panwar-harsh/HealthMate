package com.example.HealthMateApplication.models

data class User(
    val user_name: String = "",
    val user_email: String = "",
    val user_aadhar: String = "",
    val user_password: String = "",
    val user_adhaar_img_url: String = "",
    var user_account_status: String = "" ,// null = underVerification, true = verified, false = rejected
    var user_phone: String = "" // null = underVerification, true = verified, false = rejected
)
