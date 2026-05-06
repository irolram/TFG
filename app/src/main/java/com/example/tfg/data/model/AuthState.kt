package com.example.tfg.data.model


sealed class AuthState {
    object Cargando : AuthState()
    data class Autenticado(val usuario: Usuario) : AuthState()
    object NoAutenticado : AuthState()
}