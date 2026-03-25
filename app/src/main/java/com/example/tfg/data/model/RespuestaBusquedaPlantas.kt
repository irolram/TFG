package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class RespuestaBusquedaPlantas(
    @SerializedName("data") val datos: List<InfoBasicaPlanta>
)