

package com.example.tfg.data.network

import android.content.Context
import com.example.tfg.data.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



object RetrofitClient {
    private const val BASE_URL = "https://tfgapispringboot-production.up.railway.app/"

    fun getApiService(context: Context): IApiService {
        val tokenManager = TokenManager(context)

        // 1. Creamos un interceptor de Log para ver los errores en el Logcat (Super útil)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // 2. Configuramos el OkHttpClient con nuestro AuthInterceptor
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenManager))
            .build()

        // 3. Construimos Retrofit
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IApiService::class.java)
    }
}