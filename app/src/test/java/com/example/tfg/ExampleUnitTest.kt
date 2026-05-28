package com.example.tfg

import com.example.tfg.data.model.HuertoUiState
import com.example.tfg.data.model.LuzSolar
import com.example.tfg.data.model.Riego
import org.junit.Assert.*
import org.junit.Test

class DomainModelTest {

    @Test
    fun huertoUiState_empiezaSinDatosNiErrores() {
        val state = HuertoUiState()

        assertTrue(state.lista.isEmpty())
        assertFalse(state.cargando)
        assertNull(state.error)
        assertFalse(state.operacionExitosa)
    }

    @Test
    fun enums_muestranTextosAmigablesEnPantalla() {
        assertEquals("Frecuente", Riego.FRECUENTE.textoPantalla)
        assertEquals("Pleno sol", LuzSolar.PLENO_SOL.textoPantalla)
    }
}
