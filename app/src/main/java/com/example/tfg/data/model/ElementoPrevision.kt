package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class ElementoPrevision(
    @SerializedName("main") val principal: ClimaPrincipal,
    @SerializedName("wind") val viento: Viento,
    @SerializedName("weather") val clima: List<DescripcionClima>,
    @SerializedName("pop") val probabilidadLluvia: Double)
