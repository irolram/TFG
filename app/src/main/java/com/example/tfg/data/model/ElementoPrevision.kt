package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

// Modelo de datos para el clima principal
data class ElementoPrevision(
    @SerializedName("main") val principal: ClimaPrincipal,
    @SerializedName("wind") val viento: Viento,
    @SerializedName("weather") val clima: List<DescripcionClima>,
    @SerializedName("pop") val probabilidadLluvia: Double)
