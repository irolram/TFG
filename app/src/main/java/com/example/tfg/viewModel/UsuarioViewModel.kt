package com.example.tfg.viewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.StatsDashboard
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
    // En UsuarioViewModel.kt

    private val _usuarioLogueado = MutableStateFlow<Usuario?>(null)
    val usuarioLogueado: StateFlow<Usuario?> = _usuarioLogueado

    // Y asegúrate de tener esta función para cargar los datos
    fun cargarPerfilActual(userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.obtenerUsuarioPorId(userId)
                if (response.isSuccessful) {
                    _usuarioLogueado.value = response.body()
                }
            } catch (e: Exception) {
                println("Error al cargar perfil: ${e.message}")
            }
        }
    }

    private val _stats = MutableStateFlow<StatsDashboard?>(null)
    val stats: StateFlow<StatsDashboard?> = _stats

    fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                val response = apiService.obtenerEstadisticas()
                if (response.isSuccessful) {
                    _stats.value = response.body()
                }
            } catch (e: Exception) {
                Log.e("STATS_ERROR", "Error al conectar: ${e.message}")
            }
        }
    }

    var conteoProximidad by mutableLongStateOf(0L)
        private set

    fun cargarConteoProximidad(lat: Double, lng: Double, radio: Double) {
        viewModelScope.launch {
            try {
                val response = apiService.obtenerConteoProximidad(lat, lng, radio)
                if (response.isSuccessful) {
                    conteoProximidad = response.body() ?: 0L
                }
            } catch (e: Exception) {
                Log.e("MAPA_ERROR", "Error en Haversine: ${e.message}")
            }
        }
    }
}

