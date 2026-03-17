package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("role")
    val rol: String
)