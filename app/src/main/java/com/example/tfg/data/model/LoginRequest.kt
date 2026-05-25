package com.example.tfg.data.model

// Modelo de datos para la solicitud de inicio de sesión
data class LoginRequest(
    val userId: String,   // El UID que te da Firebase
    val email: String     // El email que te da Firebase
)