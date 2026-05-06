package com.example.tfg.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.AuthState
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

    private val context = application.applicationContext
    private val apiService = RetrofitClient.getApiService(context)

    // --- 2. ESTADOS ---

    // 🌟 NUEVO 2: Estado de autenticación que empieza en "Cargando"
    private val _authState = MutableStateFlow<AuthState>(AuthState.Cargando)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

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
        } else {
            // 🌟 NUEVO 3: Si no hay UID en Firebase, no estamos logueados
            _authState.value = AuthState.NoAutenticado
        }
    }

    // --- 4. FUNCIONES DE USUARIOS ---

    // ... (listarUsuarios, actualizarRol, eliminarUsuario se quedan exactamente igual) ...
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
                    listarUsuarios()
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
            // 🌟 NUEVO 4: Avisamos a la UI de que estamos cargando datos
            _authState.value = AuthState.Cargando
            try {
                val response = apiService.obtenerUsuarioPorId(uid)

                if (response.isSuccessful && response.body() != null) {
                    val usuario = response.body()!!
                    _usuarioLogueado.value = usuario
                    // 🌟 NUEVO 5: ¡Datos listos! Pasamos a estado Autenticado
                    _authState.value = AuthState.Autenticado(usuario)
                    Log.d("API_SUCCESS", "Perfil de ${usuario.nombre} cargado")
                } else {
                    // Si el servidor falla (ej. usuario borrado), deslogueamos
                    _authState.value = AuthState.NoAutenticado
                }
            } catch (e: Exception) {
                Log.e("API_FAILURE", "Error de red: ${e.message}")
                _authState.value = AuthState.NoAutenticado
            }
        }
    }

    // --- 5. ESTADÍSTICAS Y MAPA ---
    // ... (cargarEstadisticas y cargarConteoProximidad se quedan igual) ...
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
        // 🌟 NUEVO 6: Pasamos a cargando un microsegundo para tapar la caída de datos
        _authState.value = AuthState.Cargando

        _usuarioLogueado.value = null
        _listaUsuarios.value = emptyList()
        _stats.value = null

        // Finalizamos el cierre de sesión
        _authState.value = AuthState.NoAutenticado
        Log.d("AUTH", "Estado del ViewModel limpiado")
    }
}