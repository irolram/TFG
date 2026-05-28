package com.example.tfg.viewModel

import com.example.tfg.FakeApiService
import com.example.tfg.MainDispatcherRule
import com.example.tfg.cultivo
import com.example.tfg.planta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PlantaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun buscarPlantas_actualizaResultadosCuandoElServidorResponde() = runTest {
        val resultados = listOf(planta("Tomate"), planta("Tomillo"))
        val api = FakeApiService().apply {
            catalogoResponse = Response.success(resultados)
            buscarCatalogoResponse = Response.success(resultados)
        }
        val viewModel = PlantaViewModel(api)

        viewModel.buscarPlantas("tom")
        advanceUntilIdle()

        assertEquals(listOf("Tomate", "Tomillo"), viewModel.resultadosBusqueda.value.map { it.nombre })
        assertFalse(viewModel.buscando.value)
        assertEquals(null, viewModel.errorBusqueda.value)
    }

    @Test
    fun buscarPlantas_conTextoCortoLimpiaResultados() = runTest {
        val api = FakeApiService()
        val viewModel = PlantaViewModel(api)
        viewModel.resultadosBusqueda.value = listOf(planta("Tomate"))

        viewModel.buscarPlantas("t")
        advanceUntilIdle()

        assertTrue(viewModel.resultadosBusqueda.value.isEmpty())
    }

    @Test
    fun buscarPlantas_muestraErrorSiFallaLaConexion() = runTest {
        val api = FakeApiService().apply {
            buscarCatalogoException = RuntimeException("Servidor caido")
        }
        val viewModel = PlantaViewModel(api)

        viewModel.buscarPlantas("tomate")
        advanceUntilIdle()

        assertEquals("Error al conectar con el catálogo", viewModel.errorBusqueda.value)
        assertFalse(viewModel.buscando.value)
    }

    @Test
    fun regarPlanta_invocaExitoCuandoLaApiConfirmaElRiego() = runTest {
        val api = FakeApiService().apply {
            registrarRiegoResponse = Response.success(cultivo(nombre = "Albahaca"))
        }
        val viewModel = PlantaViewModel(api)
        var exito = false
        var error: String? = null

        viewModel.regarPlanta(
            cultivoId = "cultivo-1",
            apiService = api,
            onSuccess = { exito = true },
            onError = { error = it }
        )
        advanceUntilIdle()

        assertTrue(exito)
        assertEquals(null, error)
    }

    @Test
    fun guardarPlantaEnHuerto_usaElNombreComoApodoSiNoSeIndicaUno() = runTest {
        val api = FakeApiService()
        val viewModel = PlantaViewModel(api)
        var guardado = false

        viewModel.guardarPlantaEnHuerto(
            huertoId = "huerto-1",
            planta = planta("Perejil"),
            apodo = "",
            onExito = { guardado = true }
        )
        advanceUntilIdle()

        val cultivo = api.cultivosAniadidos.single()
        assertEquals("Perejil", cultivo.nombre)
        assertEquals("Perejil", cultivo.apodo)
        assertEquals("PLANTADO", cultivo.estado)
        assertEquals("huerto-1", cultivo.huertoId)
        assertTrue(guardado)
    }
}
