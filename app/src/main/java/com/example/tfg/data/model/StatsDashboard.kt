package com.example.tfg.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para recibir las estadísticas globales del sistema.
 * Debe coincidir exactamente con los nombres de los campos del Backend.
 */
data class StatsDashboard(
    @SerializedName("totalUsuarios")
    val totalUsuarios: Long,

    @SerializedName("totalHuertos")
    val totalHuertos: Long,

    @SerializedName("totalPlantas")
    val totalPlantas: Long,

    @SerializedName("usuariosPorRol")
    val usuariosPorRol: Map<String, Long>
)