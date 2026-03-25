package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class RespuestaPrevision(
    @SerializedName("list") val lista: List<ElementoPrevision>,
    @SerializedName("city") val ciudad: DatosCiudad
)
