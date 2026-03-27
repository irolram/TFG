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

    // Función para obtener todos los huertos
    fun obtenerTodosLosHuertos(apiService: IApiService) {

        //Lanzamos una corrutina
        viewModelScope.launch {
            cargando.value = true
            error.value = null
            try {
                // Intentamos obtener los huertos
                val response = apiService.obtenerHuertos()
                // Si es exitosa actualizamos la lista
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

    // Función para crear un nuevo huerto
    fun crearNuevoHuerto(apiService: IApiService, nombre: String, descripcion: String,latitud: Double, longitud: Double) {
        viewModelScope.launch {
            // Lanzamos la corrutina y establecemos las variables de estado
            guardando.value = true
            errorGuardar.value = null

            try {
                // Creamos el nuevo huerto
                val nuevo = Huerto(nombre = nombre, descripcion = descripcion, latitud = latitud, longitud = longitud)
                //Mandamos la petición a la api
                val response = apiService.crearHuerto(nuevo)
                // Si es exitosa actualizamos la lista
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

    //Función para borrar un huerto
    fun borrarHuerto(apiService: IApiService, idHuerto: String) {
        //Lanzamos una corrutina
        viewModelScope.launch {
            try {
                // Intentamos borrar en el servidor
                val response = apiService.borrarHuerto(idHuerto)
                //SI el borrado es exitoso actualizamos la lista
                if (response.isSuccessful) {
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
    //Función auxiliar para limpiar la lista de huertos
    fun limpiarDatos() {
        huertos.value = emptyList()
    }



    var cultivosDelHuerto = mutableStateOf<List<Cultivo>>(emptyList())
    var cargandoCultivos = mutableStateOf(false)


    // Cargamos los cultivos de un huerto
    fun cargarCultivosDeUnHuerto(apiService: IApiService, huertoId: String) {
        // Lanzamos una corrutina
        viewModelScope.launch {
            cargandoCultivos.value = true

            try {
                // Variable que almacena la respuesta de la API
                val respuesta = apiService.obtenerCultivosDelHuerto(huertoId)

                // Si la respuesta es exitosa y el cuerpo no es nulo, actualizamos la lista
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

    // Borramos un cultivo de un huerto
    fun eliminarCultivoDelHuerto(apiService: IApiService, huertoId: String, cultivoId: String, token: String) {

        // Lanzamos una corrutina
        viewModelScope.launch {
            try {
                // Si el token no empieza por "Bearer ", lo añadimos
                val tokenConFormato = if (token.startsWith("Bearer ")) token else "Bearer $token"
                // Variable que almacena la respuesta de la API
                val response = apiService.eliminarCultivo(tokenConFormato, huertoId, cultivoId)

                // Si es exitosa actualizamos la lista
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
