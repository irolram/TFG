package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

enum class LuzSolar(val textoPantalla: String) {
    @SerializedName("PLENO_SOL")
    PLENO_SOL("Pleno sol"),

    @SerializedName("SEMISOMBRA")
    SEMISOMBRA("Sombra parcial"),

    @SerializedName("SOMBRA")
    SOMBRA("Sombra")
}