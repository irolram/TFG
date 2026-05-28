package com.example.tfg.data.network

import com.example.tfg.data.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import android.util.Log
import kotlinx.coroutines.flow.first

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    // Función que intercepta las peticiones para inyectar el token
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        // 1. Excluir rutas públicas (Login y Registro)
        // Ajusta los textos según cómo se llamen tus endpoints en Spring Boot
        if (path.contains("login") || path.contains("registro") || path.contains("auth")) {
            return chain.proceed(request)
        }

        // 2. Variable para obtener el token para el resto de peticiones
        val token = runBlocking {
            tokenManager.accessToken.first()
        }

        // Construimos la petición preparándola para modificaciones
        val requestBuilder = request.newBuilder()

        // Si el token es distinto de nulo, lo añadimos a la petición
        if (!token.isNullOrBlank()) {
            Log.d("INTERCEPTOR_DEBUG", "Token a enviar: [$token]")
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.e("INTERCEPTOR_DEBUG", "¡OJO! El token está vacío o es nulo")
        }

        // Devolvemos la petición modificada
        return chain.proceed(requestBuilder.build())
    }
}