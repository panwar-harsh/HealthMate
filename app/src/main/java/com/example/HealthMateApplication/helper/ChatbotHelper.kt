package com.example.HealthMateApplication.helper

import android.util.Log
import com.example.HealthMateApplication.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object ChatbotHelper {
    private val client = OkHttpClient()

    // OpenRouter API endpoint (ensure it's correct)
    private const val OPENROUTER_API_URL = "https://openrouter.ai/api/v1/chat/completions"

    private val JSON = "application/json; charset=utf-8".toMediaType()

    // Function to get health guidance from OpenRouter's chatbot
    suspend fun getHealthGuidance(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            // Create JSON body for the request
            val jsonBody = JSONObject()

            // Ensure "gpt-3.5-turbo" or another model is supported by OpenRouter
            jsonBody.put("model", "gpt-3.5-turbo")

            // Structure the messages (check OpenRouter documentation for correct format)
            val messages = JSONObject()
            messages.put("role", "system")
            messages.put("content", "You are a helpful health guidance assistant.")

            val userMessage = JSONObject()
            userMessage.put("role", "user")
            userMessage.put("content", prompt)  // User's input message

            // Create an array of messages
            val messageList = arrayListOf<JSONObject>()
            messageList.add(messages)
            messageList.add(userMessage)

            // Add message array to request body
            jsonBody.put("messages", messageList)

            // Convert JSON body to request body
            val body = jsonBody.toString().toRequestBody(JSON)

            // Build request
            val request = Request.Builder()
                .url(OPENROUTER_API_URL)
                .addHeader("Authorization", "Bearer ${Constant.OPENAI_API_KEY}")  // Replace with OpenRouter API key
                .post(body)
                .build()

            // Make the API call
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("API_ERROR", "OpenRouter API call failed: ${response.code} - ${response.message}")
                    return@withContext "Failed to get response from API: ${response.message}"
                }

                // Parse the response body
                val respString = response.body?.string() ?: return@withContext "Empty response from API"
                Log.d("API_RESPONSE", respString)

                // Parse the response (ensure the format matches OpenRouter's response structure)
                val jsonResponse = JSONObject(respString)

                // Check if 'choices' array exists and extract content
                if (jsonResponse.has("choices")) {
                    val choices = jsonResponse.getJSONArray("choices")
                    if (choices.length() > 0) {
                        // Retrieve and return the chatbot's response
                        val message = choices.getJSONObject(0).getString("message")
                        return@withContext message.trim()
                    }
                }

                // In case there is no response
                "No response from API"
            }

        } catch (e: Exception) {
            Log.e("API_EXCEPTION", e.toString())
            "Error: ${e.localizedMessage}"
        }
    }
}
