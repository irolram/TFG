package com.example.tfg.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.CatalogoDePlantas // 🚩 Tu nuevo modelo
import com.example.tfg.data.network.IApiService
import kotlinx.coroutines.launch

class PlantaViewModel : ViewModel() {

    // Ahora guardamos la lista de tu propio catálogo
    var resultadosBusqueda = mutableStateOf<List<CatalogoDePlantas>>(emptyList())

    var buscando = mutableStateOf(false)
    var errorBusqueda = mutableStateOf<String?>(null)

    // 🔍 1. Buscar plantas en TU catálogo de Railway
    fun buscarPlantas(apiService: IApiService, nombre: String) {
        if (nombre.length < 2) {
            resultadosBusqueda.value = emptyList()
            return
        }

        viewModelScope.launch {
            buscando.value = true
            errorBusqueda.value = null
            try {
                // Llamamos a tu endpoint de Spring Boot
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
        apiService: IApiService,
        huertoId: String,
        plantaSeleccionada: CatalogoDePlantas,
        onExito: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val cultivoParaEnviar = Cultivo(
                    nombre = plantaSeleccionada.nombre,
                    variedad = plantaSeleccionada.nombreCientifico ?: "Especie común",
                    estado = "PLANTADO",
                    fechaPlantacion = System.currentTimeMillis(),
                    icono = plantaSeleccionada.iconoUrl ?: "",
                    riego = plantaSeleccionada.riego ?: "Moderado",
                    luzSolar = plantaSeleccionada.luzSolar ?: "Pleno sol",
                    huertoId = huertoId
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
}