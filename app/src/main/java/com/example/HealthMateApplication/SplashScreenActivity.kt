package com.example.HealthMateApplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    companion object {
        private const val PREF_NAME = "UserSession" //  Used constant for SharedPreferences name
        private const val KEY_USER_EMAIL = "user_email" //  Used constant for session key
        private const val SPLASH_DELAY: Long = 2000 // ⏱ Splash delay time (in ms)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // ⏳ Use main looper to delay splash screen transition safely on main thread ✅
        Handler(Looper.getMainLooper()).postDelayed({
            if (isUserLoggedIn()) {
                // 🟢 User is logged in, navigate to MainActivity
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // 🔴 User is not logged in, navigate to LoginActivity
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish() // ✅ Close SplashScreenActivity after navigation
        }, SPLASH_DELAY)
    }

    // 🔍 Check if user session exists based on email
    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.contains(KEY_USER_EMAIL)
    }

    // 📧 Get user email from session (null if not found)
    private fun getUserEmailFromSession(): String? {
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPref.getString(KEY_USER_EMAIL, null)
    }

    // 🧹 Clear user session
    private fun clearSession() {
        val sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
    }
}
