package com.example.tfg.data.model


data class Huerto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val imagenUrl: String = "",
    val fechaCreacion: Long = System.currentTimeMillis(),
    val creadorId: String = ""
)