package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class DescripcionClima(
    @SerializedName("description") val descripcion: String,
    @SerializedName("icon") val icono: String)
