package com.example.tfg.data.network

import com.example.tfg.data.network.IWeatherApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val apiService: IWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IWeatherApi::class.java)
    }
}