package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

enum class Riego(val textoPantalla: String) {
    @SerializedName("Escaso")
    ESCASO("Escaso"),

    @SerializedName("Moderado")
    MODERADO("Moderado"),

    @SerializedName("Frecuente")
    FRECUENTE("Frecuente"),

    @SerializedName("Abundante")
    ABUNDANTE("Abundante"),

    @SerializedName("Constante")
    CONSTANTE("Constante")
}