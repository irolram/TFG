package com.example.tfg.data.model

// Estados de acción para el Admin
sealed class AdminAction {
    data object None : AdminAction()
    data class ChangeRole(val usuario: Usuario, val nuevoRol: Rol) : AdminAction()
    data class DeleteUser(val usuario: Usuario) : AdminAction()
}