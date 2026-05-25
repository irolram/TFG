package com.example.tfg.data.model

sealed class TicketAction {
    data object None : TicketAction()
    data class ConfirmResolve(val ticket: Ticket) : TicketAction()
}