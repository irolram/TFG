package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

data class ImagenPlanta(
    @SerializedName("thumbnail") val miniatura: String?,
    @SerializedName("original_url") val urlOriginal: String?
)