package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

// Modelo de datos para la respuesta de la API de clima
data class RespuestaPrevision(
    @SerializedName("list") val lista: List<ElementoPrevision>,
    @SerializedName("city") val ciudad: DatosCiudad
)
