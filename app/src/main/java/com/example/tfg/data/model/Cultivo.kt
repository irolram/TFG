package com.example.tfg.data.model

data class Cultivo(
    val id: String,
    val nombre: String,     // Ej: "Tomate Cherry", "Lechuga"
    val variedad: String,     // Opcional: para dar más detalle
    val estado: Estado , //
    val fechaPlantacion: Long = System.currentTimeMillis(),
    val icono: String // Icono de la verdura / fruta
)