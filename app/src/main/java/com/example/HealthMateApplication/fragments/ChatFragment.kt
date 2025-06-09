package com.example.HealthMateApplication.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.HealthMateApplication.databinding.FragmentChatBinding
import com.example.HealthMateApplication.helper.ChatbotHelper
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.submitButton.setOnClickListener {
            val prompt = binding.promptInput.text.toString()
            if (prompt.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your query", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.submitButton.isEnabled = false
            binding.responseText.text = "Loading..."

            lifecycleScope.launch {
                val response = ChatbotHelper.getHealthGuidance(prompt)
                binding.responseText.text = response
                binding.promptInput.text?.clear()
                binding.submitButton.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

