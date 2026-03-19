package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class ClimaPrincipal(
    @SerializedName("temp") val temperatura: Double,
    @SerializedName("humidity") val humedad: Int)
