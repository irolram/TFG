package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName


// Modelo de datos para Cultivo necesario para la representacion de un cultivo en la app
data class Cultivo(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("variedad")
    val variedad: String? = null,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("fechaPlantacion")
    val fechaPlantacion: Long,

    @SerializedName("huertoId")
    val huertoId: String,

    @SerializedName("infoCatalogo")
    val infoCatalogo: CatalogoDePlantas? = null
)