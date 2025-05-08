package com.example.glowgirls.util

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.io.File

fun File.toRequestBody(): RequestBody {
    return RequestBody.create("image/*".toMediaTypeOrNull(), this)
}