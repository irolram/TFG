package com.example.tfg.data.network

import com.example.tfg.data.model.RespuestaPrevision
import retrofit2.http.GET
import retrofit2.http.Query

interface IWeatherApi {@GET("forecast")


suspend fun getPrevisionClima(
    @Query("lat") lat: Double,
    @Query("lon") lon: Double,
    @Query("appid") apiKey: String,
    @Query("units") units: String = "metric",
    @Query("lang") lang: String = "es"
): RespuestaPrevision
}