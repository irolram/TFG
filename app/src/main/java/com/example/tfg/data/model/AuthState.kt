package com.example.tfg.data.model

// Clase para representar el estado de autenticación
sealed class AuthState {
    object Cargando : AuthState()
    data class Autenticado(val usuario: Usuario) : AuthState()
    object NoAutenticado : AuthState()
}