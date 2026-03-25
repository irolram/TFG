package com.example.tfg.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.network.IApiService
import kotlinx.coroutines.launch


class HuertosViewModel : ViewModel() {

    val huertos = mutableStateOf<List<Huerto>>(emptyList())
    val cargando = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    fun cargarMisHuertos(apiService: IApiService, nombre: String,descripcion: String) {
        viewModelScope.launch {

            cargando.value = true
            error.value = null

            try {
                val nuevoHuerto = Huerto(nombre = nombre, descripcion = descripcion)
                // Hacemos la llamada a Spring Boot en Railway
                val response = apiService.crearHuerto(nuevoHuerto)

                if (response.isSuccessful) {
                    // 1. Encendemos la señal para que la pantalla vuelva hacia atrás
                    guardadoExitoso.value = true

                    obtenerTodosLosHuertos(apiService)

                } else {
                    val errorReal = response.errorBody()?.string()
                    // El servidor respondió, pero con un error (ej. 403 o 500)
                    error.value = "Error del servidor: ${response.code()}"
                    Log.e("API_HUERTOS", "Fallo HTTP: ${response.code()}")
                }
            } catch (e: Exception) {
                // Falló antes de llegar al servidor (sin internet, timeout, etc.)
                error.value = "Fallo de conexión al servidor"
                Log.e("API_HUERTOS", "Excepción: ${e.message}")
            } finally {
                // Pase lo que pase, apagamos la ruedita de carga al terminar
                cargando.value = false
            }
        }
    }

    val guardando = mutableStateOf(false)
    val errorGuardar = mutableStateOf<String?>(null)

    val guardadoExitoso = mutableStateOf(false)


    fun resetEstado() {
        guardadoExitoso.value = false
        errorGuardar.value = null
    }

    fun obtenerTodosLosHuertos(apiService: IApiService) {
        viewModelScope.launch {
            cargando.value = true
            error.value = null
            try {
                val response = apiService.obtenerHuertos() // Tu GET de siempre
                if (response.isSuccessful) {
                    huertos.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                error.value = "Error de conexión"
            } finally {
                cargando.value = false
            }
        }
    }

    fun crearNuevoHuerto(apiService: IApiService, nombre: String, descripcion: String,latitud: Double, longitud: Double) {
        viewModelScope.launch {
            guardando.value = true // 1. Enciende la rueda
            errorGuardar.value = null

            try {
                val nuevo = Huerto(nombre = nombre, descripcion = descripcion, latitud = latitud, longitud = longitud)
                val response = apiService.crearHuerto(nuevo)

                if (response.isSuccessful) {
                    guardadoExitoso.value = true
                    Log.d("DEBUG_HUERTO", "¡Huerto guardado con éxito!")
                } else {
                    errorGuardar.value = "Error del servidor: ${response.code()}"
                    Log.e("DEBUG_HUERTO", "Fallo API: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Si no hay internet o el servidor está apagado
                errorGuardar.value = "Fallo de conexión"
                Log.e("DEBUG_HUERTO", "Excepción: ${e.message}")
            } finally {
                guardando.value = false
            }
        }
    }
    fun borrarHuerto(apiService: IApiService, idHuerto: String) {
        viewModelScope.launch {
            try {
                val response = apiService.borrarHuerto(idHuerto)
                if (response.isSuccessful) {
                    // Si se borra en el servidor, actualizamos la lista local
                    val listaActualizada = huertos.value.toMutableList()
                    listaActualizada.removeAll { it.id == idHuerto }
                    huertos.value = listaActualizada
                } else {
                    error.value = "Error al borrar: ${response.code()}"
                }
            } catch (e: Exception) {
                error.value = "Fallo de conexión al intentar borrar"
            }
        }

    }
    fun limpiarDatos() {
        huertos.value = emptyList()
    }



    var cultivosDelHuerto = mutableStateOf<List<Cultivo>>(emptyList())
    var cargandoCultivos = mutableStateOf(false)


    fun cargarCultivosDeUnHuerto(apiService: IApiService, huertoId: String) {
        viewModelScope.launch {
            cargandoCultivos.value = true

            try {
                val respuesta = apiService.obtenerCultivosDelHuerto(huertoId)

                if (respuesta.isSuccessful) {
                    cultivosDelHuerto.value = respuesta.body() ?: emptyList()
                } else {
                    cultivosDelHuerto.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                cultivosDelHuerto.value = emptyList()
            } finally {
                cargandoCultivos.value = false
            }
        }
    }
    private val _cultivosDelHuerto = mutableStateOf<List<Cultivo>>(emptyList())


    fun eliminarCultivoDelHuerto(apiService: IApiService, huertoId: String, cultivoId: String, token: String) {
        viewModelScope.launch {
            try {
                val tokenConFormato = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = apiService.eliminarCultivo(tokenConFormato, huertoId, cultivoId)

                if (response.isSuccessful) {
                    cargarCultivosDeUnHuerto(apiService, huertoId)

                    println("DEBUG: Lista refrescada tras borrar")
                }
            } catch (e: Exception) {
                println("DEBUG: Error al refrescar: ${e.message}")
            }
        }
    }
}
