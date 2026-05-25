package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

// Modelo de datos para el clima principal
data class DescripcionClima(
    @SerializedName("description") val descripcion: String,
    @SerializedName("icon") val icono: String)
