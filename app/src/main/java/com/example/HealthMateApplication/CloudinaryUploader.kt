package com.example.HealthMateApplication

import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object CloudinaryUploader {

    private const val CLOUD_NAME = "drrnzfcir"
    private const val UPLOAD_PRESET = "HealthifyPreset"

    fun uploadImage(
        context: Context,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)

        if (inputStream == null) {
            onFailure("Unable to open image stream.")
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "upload.jpg",
                RequestBody.create("image/*".toMediaTypeOrNull(), inputStream.readBytes())
            )
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            // No public_id because use filename = false
            // No overwrite parameter because overwrite is false by default
            .build()

        val request = Request.Builder()
            .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFailure("Upload failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onFailure("Upload failed: ${response.message}")
                    return
                }

                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    val json = JSONObject(responseBody)
                    val url = json.getString("secure_url")
                    onSuccess(url)
                } else {
                    onFailure("No response body.")
                }
            }
        })
    }
}
