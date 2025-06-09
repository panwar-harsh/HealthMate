package com.example.HealthMateApplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.HealthMateApplication.databinding.ActivityChatbotBinding
import com.example.HealthMateApplication.fragments.TestsAvailableFragment
import com.example.HealthMateApplication.MainActivity


class ChatbotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatbotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        // Load the ChatFragment directly
        loadFragment(TestsAvailableFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_chat, fragment) // Replace `fragment_container` with the ID of your container in the layout
            .commit()
    }
}
