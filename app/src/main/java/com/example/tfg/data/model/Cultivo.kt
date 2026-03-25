package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class Cultivo(
    @SerializedName("id") val id: String? = null,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("variedad") val variedad: String? = null,
    @SerializedName("estado") val estado: String,
    @SerializedName("fechaPlantacion") val fechaPlantacion: Long,
    @SerializedName("icono") val icono: String,
    @SerializedName("huertoId") val huertoId: String,
    @SerializedName("riego") val riego: String? = null,
    @SerializedName("luzSolar") val luzSolar: String? = null
)