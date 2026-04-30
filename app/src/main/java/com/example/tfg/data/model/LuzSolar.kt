package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

enum class LuzSolar(val textoPantalla: String) {
    @SerializedName("Pleno sol")
    PLENO_SOL("Pleno sol"),

    @SerializedName("Semisombra")
    SEMISOMBRA("Sombra parcial"),

    @SerializedName("Sombra")
    SOMBRA("Sombra")
}