package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(

    @SerializedName("token")
    val accessToken: String,

    @SerializedName("refresh")
    val refreshToken: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("rol")
    val rol: String
)