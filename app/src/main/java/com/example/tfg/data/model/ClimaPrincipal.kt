package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

// Modelo de datos para el clima principal
data class ClimaPrincipal(
    @SerializedName("temp") val temperatura: Double,
    @SerializedName("humidity") val humedad: Int)
