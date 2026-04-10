package com.example.tfg.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.Usuario
import com.example.tfg.data.network.IApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UsuarioViewModel(private val apiService: IApiService) : ViewModel() {

    private val _listaUsuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val listaUsuarios: StateFlow<List<Usuario>> = _listaUsuarios
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing
    // Función para cargar todos los usuarios (Solo la llamará el Admin/Mod)
    fun listarUsuarios() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = apiService.listarUsuarios()
                if (response.isSuccessful) {
                    _listaUsuarios.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
               println("error al cargar los usuarios ${e.message}")
            }
            finally {
                _isRefreshing.value = false
            }
        }
    }



    fun cambiarRolEnServidor(id: String, rol: Rol) {
        viewModelScope.launch {
            val respuesta = apiService.actualizarRol(id, rol)

            if (respuesta.isSuccessful) {

                Log.d("API_SUCCESS", "Rol cambiado a $rol")
                apiService.listarUsuarios()
            } else {
                Log.e("API_ERROR", "Error: ${respuesta.code()} - ${respuesta.errorBody()?.string()}")
            }
        }
    }
    fun actualizarRol(id: String, nuevoRol: Rol) {
        viewModelScope.launch {
            try {
                val response = apiService.actualizarRol(id, nuevoRol)
                if (response.isSuccessful) {
                    listarUsuarios()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al actualizar rol: ${e.message}")

            }
        }
    }

    fun eliminarUsuario(id: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = apiService.eliminarUsuario(id)
                if (response.isSuccessful) {
                    listarUsuarios()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al eliminar: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

