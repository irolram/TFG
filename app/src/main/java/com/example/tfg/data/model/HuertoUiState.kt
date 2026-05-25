package com.example.tfg.data.model

// Modelo de datos para los estados de la pantalla principal
data class HuertoUiState(
    val lista: List<Huerto> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val operacionExitosa: Boolean = false
)