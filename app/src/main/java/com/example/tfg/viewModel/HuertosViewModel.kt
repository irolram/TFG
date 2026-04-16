package com.example.tfg.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.model.HuertoUiState
import com.example.tfg.data.network.IApiService
import kotlinx.coroutines.launch

// 🚩 Esta clase es la que permite que 'state' funcione en las pantallas


class HuertosViewModel : ViewModel() {

    // El estado unificado que esperan tus pantallas
    private val _uiState = mutableStateOf(HuertoUiState())
    val uiState: State<HuertoUiState> = _uiState

    // Estados para cultivos (los mantenemos como los tenías)
    val cultivosDelHuerto = mutableStateOf<List<Cultivo>>(emptyList())
    val cargandoCultivos = mutableStateOf(false)

    /**
     * Obtener todos los huertos
     */
    fun obtenerTodosLosHuertos(apiService: IApiService) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null)
            try {
                val response = apiService.obtenerHuertos()
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(lista = response.body() ?: emptyList())
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error de conexión")
            } finally {
                _uiState.value = _uiState.value.copy(cargando = false)
            }
        }
    }

    /**
     * Crear un nuevo huerto
     * 🚩 RECUPERAMOS EL NOMBRE: crearNuevoHuerto
     */
    fun crearNuevoHuerto(apiService: IApiService, nombre: String, descripcion: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, error = null, operacionExitosa = false)
            try {
                val nuevo = Huerto(nombre = nombre, descripcion = descripcion, latitud = lat, longitud = lon)
                val response = apiService.crearHuerto(nuevo)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(operacionExitosa = true)
                    obtenerTodosLosHuertos(apiService)
                } else {
                    _uiState.value = _uiState.value.copy(error = "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Fallo de red")
            } finally {
                _uiState.value = _uiState.value.copy(cargando = false)
            }
        }
    }

    /**
     * Borrar huerto
     */
    fun borrarHuerto(apiService: IApiService, idHuerto: String) {
        viewModelScope.launch {
            try {
                val response = apiService.borrarHuerto(idHuerto)
                if (response.isSuccessful) {
                    // Actualizamos la lista quitando el borrado
                    _uiState.value = _uiState.value.copy(
                        lista = _uiState.value.lista.filter { it.id != idHuerto }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al borrar")
            }
        }
    }

    /**
     * Cargar cultivos de un huerto
     */
    fun cargarCultivosDeUnHuerto(apiService: IApiService, huertoId: String) {
        viewModelScope.launch {
            cargandoCultivos.value = true
            try {
                val respuesta = apiService.obtenerCultivosDelHuerto(huertoId)
                cultivosDelHuerto.value = if (respuesta.isSuccessful) respuesta.body() ?: emptyList() else emptyList()
            } catch (e: Exception) {
                cultivosDelHuerto.value = emptyList()
            } finally {
                cargandoCultivos.value = false
            }
        }
    }

    /**
     * Eliminar cultivo
     */
    fun eliminarCultivoDelHuerto(apiService: IApiService, huertoId: String, cultivoId: String, token: String) {
        viewModelScope.launch {
            try {
                val tokenConFormato = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = apiService.eliminarCultivo(tokenConFormato, huertoId, cultivoId)
                if (response.isSuccessful) {
                    cargarCultivosDeUnHuerto(apiService, huertoId)
                }
            } catch (e: Exception) {
                Log.e("DEBUG", "Error: ${e.message}")
            }
        }
    }

    /**
     * Reset de señales para la UI
     */
    fun resetEstado() {
        _uiState.value = _uiState.value.copy(operacionExitosa = false, error = null)
    }

    /**
     * Limpieza al cerrar sesión
     */
    fun limpiarDatos() {
        _uiState.value = HuertoUiState()
        cultivosDelHuerto.value = emptyList()
    }
}