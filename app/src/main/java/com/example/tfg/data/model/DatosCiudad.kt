package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

// Modelo de datos para el clima principal
data class DatosCiudad(
    @SerializedName("name") val nombre: String,
    @SerializedName("country") val pais: String
)