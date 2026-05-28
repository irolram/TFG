package com.example.tfg.viewModel

import com.example.tfg.FakeApiService
import com.example.tfg.MainDispatcherRule
import com.example.tfg.cultivo
import com.example.tfg.huerto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class HuertosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun obtenerTodosLosHuertos_cargaListaYLimpiaCargando() = runTest {
        val api = FakeApiService().apply {
            huertosResponse = Response.success(
                listOf(
                    huerto(id = "1", nombre = "Terraza"),
                    huerto(id = "2", nombre = "Patio")
                )
            )
        }
        val viewModel = HuertosViewModel()

        viewModel.obtenerTodosLosHuertos(api)
        advanceUntilIdle()

        assertEquals(listOf("Terraza", "Patio"), viewModel.uiState.value.lista.map { it.nombre })
        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun obtenerTodosLosHuertos_guardaErrorCuandoFallaLaRed() = runTest {
        val api = FakeApiService().apply {
            obtenerHuertosException = RuntimeException("Sin conexion")
        }
        val viewModel = HuertosViewModel()

        viewModel.obtenerTodosLosHuertos(api)
        advanceUntilIdle()

        assertEquals(emptyList<com.example.tfg.data.model.Huerto>(), viewModel.uiState.value.lista)
        assertEquals("Error de conexión", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.cargando)
    }

    @Test
    fun crearNuevoHuerto_enviaDatosAlApiYMarcaOperacionExitosa() = runTest {
        val api = FakeApiService().apply {
            huertosResponse = Response.success(listOf(huerto(id = "nuevo", nombre = "Azotea")))
        }
        val viewModel = HuertosViewModel()

        viewModel.crearNuevoHuerto(
            apiService = api,
            nombre = "Azotea",
            descripcion = "Macetas grandes",
            lat = 37.38,
            lon = -5.99
        )
        advanceUntilIdle()

        val creado = api.huertosCreados.single()
        assertEquals("Azotea", creado.nombre)
        assertEquals("Macetas grandes", creado.descripcion)
        assertEquals(37.38, creado.latitud, 0.0)
        assertEquals(-5.99, creado.longitud, 0.0)
        assertTrue(viewModel.uiState.value.operacionExitosa)
        assertEquals("Azotea", viewModel.uiState.value.lista.single().nombre)
    }

    @Test
    fun eliminarCultivoDelHuerto_formateaBearerYRecargaCultivos() = runTest {
        val api = FakeApiService().apply {
            cultivosResponse = Response.success(listOf(cultivo(id = "cultivo-2", nombre = "Lechuga")))
        }
        val viewModel = HuertosViewModel()

        viewModel.eliminarCultivoDelHuerto(
            apiService = api,
            huertoId = "huerto-1",
            cultivoId = "cultivo-1",
            token = "abc123"
        )
        advanceUntilIdle()

        assertEquals("Bearer abc123", api.tokensEliminarCultivo.single())
        assertEquals(listOf("huerto-1"), api.idsHuertoRecargados)
        assertEquals("Lechuga", viewModel.cultivosDelHuerto.value.single().nombre)
    }
}
