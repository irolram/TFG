package com.example.tfg.data.network

import android.content.Context
import com.example.tfg.data.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Conexión con Railway
    private const val BASE_URL_RAILWAY = "https://tfgapispringboot-production-fbf5.up.railway.app"


    // Variable privada para el interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Función para crear el servicio de API
    fun getApiService(context: Context): IApiService {
        val tokenManager = TokenManager(context)

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL_RAILWAY)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IApiService::class.java)
    }
}