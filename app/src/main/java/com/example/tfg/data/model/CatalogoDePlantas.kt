package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

// Información completa de una planta
data class CatalogoDePlantas(
    val id: Long?,
    val nombre: String,
    @SerializedName("nombre_cientifico")
    val nombreCientifico: String?,
    val riego: Riego?,
    @SerializedName("luz_solar")
    val luzSolar: LuzSolar?,
    @SerializedName("icono_url")
    val icono: String?,
    val instrucciones: String?,
    @SerializedName("dias_crecimiento")
    val diasCrecimiento: Int?,
    @SerializedName("temporada_ideal")
    val temporadaIdeal: String?,
    @SerializedName("profundidad_siembra")
    val profundidadSiembra: String?,
    @SerializedName("distancia_entre_plantas")
    val distanciaEntrePlantas: String?
)