package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName
// Enum class para representar la luz solar de un cultivo
enum class LuzSolar(val textoPantalla: String) {
    @SerializedName("PLENO_SOL")
    PLENO_SOL("Pleno sol"),

    @SerializedName("SEMISOMBRA")
    SEMISOMBRA("Sombra parcial"),

    @SerializedName("SOMBRA")
    SOMBRA("Sombra")
}