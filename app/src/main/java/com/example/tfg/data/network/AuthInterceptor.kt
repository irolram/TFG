package com.example.tfg.data.network

import com.example.tfg.data.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log
import kotlinx.coroutines.flow.first

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    // Función que recibe una cadena de texto y devuelve una cadena de texto
    override fun intercept(chain: Interceptor.Chain): Response {
        // Variable para obtener el token
        val token = runBlocking {
            tokenManager.accessToken.first()
        }

        Log.d("INTERCEPTOR_DEBUG", "Token a enviar: [$token]")

        // Construimos la petición con el token
        val requestBuilder = chain.request().newBuilder()

        // Si el token es distinto de nulo, lo añadimos a la petición
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.e("INTERCEPTOR_DEBUG", "¡OJO! El token está vacío o es nulo")
        }

        // Devolvemos la petición modificada
        return chain.proceed(requestBuilder.build())
    }
}