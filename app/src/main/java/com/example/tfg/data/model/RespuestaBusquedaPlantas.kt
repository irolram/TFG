package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

// Recibe las plantas de la busqueda
data class RespuestaBusquedaPlantas(
    @SerializedName("data") val datos: List<InfoBasicaPlanta>
)