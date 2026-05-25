package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

// Información necesaria para el viento
data class Viento(@SerializedName("speed") val velocidad: Double)
