package com.example.tfg.data.model

enum class TipoTicket { ERROR, SUGERENCIA, OTRO }
enum class EstadoTicket { ABIERTO, CERRADO }

data class Ticket(
    val id: String? = null,
    val usuarioNombre: String= "",
    val usuarioId: String= "",
    val asunto: String = "",
    val descripcion: String = "",
    val tipo: TipoTicket,
    @Transient val fecha: String = "",
    val estado: EstadoTicket = EstadoTicket.ABIERTO
)