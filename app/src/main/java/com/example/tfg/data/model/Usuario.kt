package com.example.tfg.data.model


// Información del usuario
data class Usuario(
    val id: String,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val rol: Rol
    )