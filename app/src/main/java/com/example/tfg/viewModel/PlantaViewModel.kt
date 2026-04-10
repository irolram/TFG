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
                val respuesta = apiService.buscarEnCatalogo(nombre.trim())
                resultadosBusqueda.value = respuesta
            } catch (e: Exception) {
                errorBusqueda.value = "Error al conectar con el catálogo"
                e.printStackTrace()
            } finally {
                buscando.value = false
            }
        }
    }

    fun guardarPlantaEnHuerto(
        huertoId: String,
        plantaSeleccionada: CatalogoDePlantas,
        onExito: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val cultivoParaEnviar = Cultivo(
                    nombre = plantaSeleccionada.nombre,
                    estado = "PLANTADO",
                    fechaPlantacion = System.currentTimeMillis(),
                    huertoId = huertoId,
                    infoCatalogo = plantaSeleccionada
                )

                val respuesta = apiService.aniadirCultivo(huertoId, cultivoParaEnviar)

                if (respuesta.isSuccessful) {
                    onExito()
                } else {
                    errorBusqueda.value = "Error al guardar: ${respuesta.code()}"
                }
            } catch (e: Exception) {
                errorBusqueda.value = "Fallo de conexión con el servidor"
                e.printStackTrace()
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
                    resultadosBusqueda.value = respuesta
                }
            } catch (e: Exception) {
                Log.e("API", "Error al cargar catálogo inicial: ${e.message}")
            } finally {
                cargando.value = false
            }
        }
    }
}