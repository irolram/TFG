package com.example.tfg.data.network

import com.example.tfg.data.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log
import androidx.compose.remote.creation.first
import kotlinx.coroutines.flow.first

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking {
            tokenManager.accessToken.first()
        }

        // 🔍 AÑADE ESTE LOG:
        Log.d("INTERCEPTOR_DEBUG", "Token a enviar: [$token]")

        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.e("INTERCEPTOR_DEBUG", "¡OJO! El token está vacío o es nulo")
        }

        return chain.proceed(requestBuilder.build())
    }
}