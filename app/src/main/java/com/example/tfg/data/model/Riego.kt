package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

enum class Riego(val textoPantalla: String) {
    @SerializedName("ESCASO")
    ESCASO("Escaso"),

    @SerializedName("MODERADO")
    MODERADO("Moderado"),

    @SerializedName("FRECUENTE")
    FRECUENTE("Frecuente"),

    @SerializedName("ABUNDANTE")
    ABUNDANTE("Abundante"),

    @SerializedName("CONSTANTE")
    CONSTANTE("Constante")
}