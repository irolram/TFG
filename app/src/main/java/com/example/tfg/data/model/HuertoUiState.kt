package com.example.tfg.data.model

data class HuertoUiState(
    val lista: List<Huerto> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
    val operacionExitosa: Boolean = false
)