package com.example.glowgirls.network

import android.util.Base64
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ImgurHelper {
    private val client = OkHttpClient()
    private val clientId = "7c3f10d26d51e6d" // Replace with your Imgur Client ID

    fun uploadImage(imageByteArray: ByteArray, onResult: (String?) -> Unit) {
        val base64Image = Base64.encodeToString(imageByteArray, Base64.DEFAULT)
        val requestBody = FormBody.Builder()
            .add("image", base64Image)
            .build()

        val request = Request.Builder()
            .url("https://api.imgur.com/3/image")
            .header("Authorization", "Client-ID 7c3f10d26d51e6d")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string())
                    val imageUrl = json.getJSONObject("data").getString("link")
                    onResult(imageUrl)
                } else {
                    onResult(null)
                }
            }
        })
    }
}