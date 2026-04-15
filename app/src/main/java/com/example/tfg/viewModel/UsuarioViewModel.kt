package com.example.tfg.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.StatsDashboard
import com.example.tfg.data.model.Usuario
import com.example.tfg.data.network.RetrofitClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsuarioViewModel(application: Application) : AndroidViewModel(application) {

    // --- 1. PROPIEDADES DE CONTEXTO Y API (PROFESIONAL) ---
    private val context = application.applicationContext
    // 🚩 Inicializamos el apiService aquí para que esté disponible en toda la clase
    private val apiService = RetrofitClient.getApiService(context)

    // --- 2. ESTADOS (Encapsulamiento profesional con asStateFlow) ---
    private val _listaUsuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val listaUsuarios: StateFlow<List<Usuario>> = _listaUsuarios.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _usuarioLogueado = MutableStateFlow<Usuario?>(null)
    val usuarioLogueado: StateFlow<Usuario?> = _usuarioLogueado.asStateFlow()

    private val _stats = MutableStateFlow<StatsDashboard?>(null)
    val stats: StateFlow<StatsDashboard?> = _stats.asStateFlow()

    var conteoProximidad by mutableLongStateOf(0L)
        private set

    // --- 3. INICIALIZACIÓN ---
    init {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            cargarPerfilActual(uid)
        }
        // Solo listamos usuarios si somos Admin/Mod (se suele llamar desde la pantalla)
    }

    // --- 4. FUNCIONES DE USUARIOS ---

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

    fun actualizarRol(id: String, nuevoRol: Rol) {
        viewModelScope.launch {
            try {
                val response = apiService.actualizarRol(id, nuevoRol)
                if (response.isSuccessful) {
                    Log.d("API_SUCCESS", "Rol cambiado a $nuevoRol")
                    listarUsuarios() // Refrescamos la lista automáticamente
                } else {
                    Log.e("API_ERROR", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fallo de red al actualizar rol: ${e.message}")
            }
        }
    }

    fun eliminarUsuario(id: String) {
        viewModelScope.launch {
            try {
                val response = apiService.eliminarUsuario(id)
                if (response.isSuccessful) {
                    _listaUsuarios.value = _listaUsuarios.value.filter { it.id != id }
                    Log.d("DELETE", "Usuario eliminado con éxito")
                }
            } catch (e: Exception) {
                Log.e("DELETE", "Fallo de red: ${e.message}")
            }
        }
    }

    fun cargarPerfilActual(uid: String) {
        viewModelScope.launch {
            try {
                // 🚩 YA NO creamos el apiService aquí, usamos el de la clase
                val response = apiService.obtenerUsuarioPorId(uid)

                if (response.isSuccessful) {
                    _usuarioLogueado.value = response.body()
                    Log.d("API_SUCCESS", "Perfil de ${response.body()?.nombre} cargado")
                }
            } catch (e: Exception) {
                Log.e("API_FAILURE", "Error de red: ${e.message}")
            }
        }
    }

    // --- 5. ESTADÍSTICAS Y MAPA ---

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
        viewModelScope.launch {
            try {
                // Log para ver qué llega de Railway
                val response = apiService.obtenerConteoProximidad(lat, lng, radio)

                if (response.isSuccessful) {

                    conteoProximidad = response.body() ?: 0L
                    Log.d("API_MAPA", "Huertos encontrados: $conteoProximidad")
                }
            } catch (e: Exception) {
                Log.e("API_MAPA", "Fallo: ${e.message}")
            }
        }
    }

    fun limpiarSesion() {
        _usuarioLogueado.value = null
        _listaUsuarios.value = emptyList()
        _stats.value = null
        Log.d("AUTH", "Estado del ViewModel limpiado")
    }
}