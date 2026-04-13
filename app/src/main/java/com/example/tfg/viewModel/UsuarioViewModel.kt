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

    // --- ESTADOS ---
    private val _listaUsuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val listaUsuarios: StateFlow<List<Usuario>> = _listaUsuarios

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _usuarioLogueado = MutableStateFlow<Usuario?>(null)
    val usuarioLogueado: StateFlow<Usuario?> = _usuarioLogueado

    private val _stats = MutableStateFlow<StatsDashboard?>(null)
    val stats: StateFlow<StatsDashboard?> = _stats

    var conteoProximidad by mutableLongStateOf(0L)
        private set

    // --- INICIALIZACIÓN ---
    init {
        listarUsuarios()
    }

    // --- FUNCIONES DE USUARIOS ---

    // 1. Cargar todos los usuarios (Reemplaza a obtenerUsuarios y listarUsuarios)
    fun listarUsuarios() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = apiService.listarUsuarios()
                if (response.isSuccessful) {
                    _listaUsuarios.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error al cargar los usuarios: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // 2. Actualizar Rol (Consolidado: Admin y Mod usan esta)
    fun actualizarRol(id: String, nuevoRol: Rol) {
        viewModelScope.launch {
            try {
                // Enviamos el .name para que el Backend reciba el String ("ADMIN", "MOD", etc)
                val response = apiService.actualizarRol(id, nuevoRol)
                if (response.isSuccessful) {
                    Log.d("API_SUCCESS", "Rol cambiado a $nuevoRol")
                    listarUsuarios() // Refrescamos la lista para ver el cambio
                } else {
                    Log.e("API_ERROR", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fallo de red al actualizar rol: ${e.message}")
            }
        }
    }

    // 3. Eliminar Usuario
    fun eliminarUsuario(id: String) {
        viewModelScope.launch {
            try {
                val response = apiService.eliminarUsuario(id)
                if (response.isSuccessful) {
                    // Quitamos al usuario de la lista local para que desaparezca al instante
                    _listaUsuarios.value = _listaUsuarios.value.filter { it.id != id }
                    Log.d("DELETE", "Usuario eliminado con éxito")
                } else {
                    Log.e("DELETE", "Error del servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DELETE", "Fallo de red: ${e.message}")
            }
        }
    }

    // 4. Cargar Perfil propio
    fun cargarPerfilActual(userId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.obtenerUsuarioPorId(userId)
                if (response.isSuccessful) {
                    _usuarioLogueado.value = response.body()
                }
            } catch (e: Exception) {
                Log.e("PROFILE_ERROR", "Error al cargar perfil: ${e.message}")
            }
        }
    }

    // --- OTRAS FUNCIONES (Stats y Mapa) ---

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

    fun cargarConteoProximidad(lat: Double, lng: Double, radio: Double) {
        if (radio < 0.1) {
            conteoProximidad = 0L
            return
        }
        viewModelScope.launch {
            try {
                val response = apiService.obtenerConteoProximidad(lat, lng, radio)
                if (response.isSuccessful) {
                    conteoProximidad = response.body() ?: 0L
                }
            } catch (e: Exception) {
                Log.e("MAPA_ERROR", "Error: ${e.message}")
            }
        }
    }
}