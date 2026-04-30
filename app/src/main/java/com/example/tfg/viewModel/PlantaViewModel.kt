package com.example.tfg.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.CatalogoDePlantas // 🚩 Tu nuevo modelo
import com.example.tfg.data.network.IApiService
import com.example.tfg.data.network.WeatherClient.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlantaViewModel(private val apiService: IApiService) : ViewModel() {

    // Variable para almacenar los resultados de la búsqueda
    var resultadosBusqueda = mutableStateOf<List<CatalogoDePlantas>>(emptyList())

    var buscando = mutableStateOf(false)
    var errorBusqueda = mutableStateOf<String?>(null)

    init {
        cargarCatalogoInicial()
    }

    // Función para Buscar plantas en el catálogo de Railway
    fun buscarPlantas(nombre: String) {
        if (nombre.length < 2) {
            resultadosBusqueda.value = emptyList()
            return
        }

        viewModelScope.launch {
            buscando.value = true
            errorBusqueda.value = null
            try {
                // Llamamos al endpoint de Spring Boot
                val respuesta = apiService.buscarEnCatalogo(nombre)

                if (respuesta.isSuccessful) {
                    // Sacamos la lista del "sobre" con .body()
                    // Si el cuerpo es nulo, le pasamos una lista vacía para que no pete
                    resultadosBusqueda.value = respuesta.body() ?: emptyList()
                } else {
                    // Aquí puedes gestionar si el servidor falla (opcional)
                    println("Error en la búsqueda: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                errorBusqueda.value = "Error al conectar con el catálogo"
                e.printStackTrace()
            } finally {
                buscando.value = false
            }
        }
    }

    fun guardarPlantaEnHuerto(huertoId: String, planta: CatalogoDePlantas, apodo: String, onExito: () -> Unit) {
        viewModelScope.launch {
            try {
                val nuevoCultivo = Cultivo(
                    nombre = planta.nombre,
                    estado = "PLANTADO",
                    fechaPlantacion = System.currentTimeMillis(),
                    huertoId = huertoId,
                    apodo = apodo.ifBlank { planta.nombre },

                    infoCatalogo = planta
                )

                val response = apiService.aniadirCultivo(huertoId, nuevoCultivo)
                if (response.isSuccessful) onExito()
            } catch (e: Exception) {
                Log.e("ERROR", "No se pudo guardar: ${e.message}")
            }
        }
    }

    val cargando = mutableStateOf(false)

    init {
        cargarCatalogoInicial()
    }

    fun cargarCatalogoInicial() {
        viewModelScope.launch(Dispatchers.IO) {
            cargando.value = true
            try {
                val respuesta = apiService.obtenerTodoElCatalogo()
                withContext(Dispatchers.Main) {
                    if (respuesta.isSuccessful) {
                        // Sacamos la lista del "sobre" con .body()
                        // Si el cuerpo es nulo, le pasamos una lista vacía para que no pete
                        resultadosBusqueda.value = respuesta.body() ?: emptyList()
                    } else {
                        // Aquí puedes gestionar si el servidor falla (opcional)
                        println("Error en la búsqueda: ${respuesta.code()}")
                    }                }
            } catch (e: Exception) {
                Log.e("API", "Error al cargar catálogo inicial: ${e.message}")
            } finally {
                cargando.value = false
            }
        }
    }
}