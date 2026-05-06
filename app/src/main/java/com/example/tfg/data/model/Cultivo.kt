package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName


// Modelo de datos para Cultivo necesario para la representacion de un cultivo en la app
data class Cultivo(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("fechaPlantacion")
    val fechaPlantacion: Long,

    @SerializedName("huertoId")
    val huertoId: String,

    @SerializedName("apodo")
    val apodo: String,

    @SerializedName("catalogo_info")
    val infoCatalogo: CatalogoDePlantas? = null,

    @SerializedName("fechaUltimoRiego")
    val fechaUltimoRiego: String? = null
)