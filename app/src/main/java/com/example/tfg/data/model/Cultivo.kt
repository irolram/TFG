package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

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