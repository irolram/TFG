package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName
// Modelo de datos para los tokens de autenticacion
data class RespuestaAuth(

    @SerializedName("token")
    val accessToken: String,

    @SerializedName("refresh")
    val refreshToken: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("rol")
    val rol: String
)