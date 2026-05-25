package com.example.tfg.data.model

sealed class ModAction {
    data object None : ModAction()
    data class ConfirmPromote(val usuario: Usuario) : ModAction()
}