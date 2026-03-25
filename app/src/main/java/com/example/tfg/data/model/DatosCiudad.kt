package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class DatosCiudad(
    @SerializedName("name") val nombre: String,
    @SerializedName("country") val pais: String
)