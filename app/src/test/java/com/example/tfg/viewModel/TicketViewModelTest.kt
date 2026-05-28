package com.example.tfg.viewModel

import com.example.tfg.FakeApiService
import com.example.tfg.MainDispatcherRule
import com.example.tfg.data.model.EstadoTicket
import com.example.tfg.data.model.Rol
import com.example.tfg.data.model.TipoTicket
import com.example.tfg.data.model.Usuario
import com.example.tfg.ticket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class TicketViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun listarTickets_actualizaLaListaYFinalizaRefresh() = runTest {
        val api = FakeApiService().apply {
            ticketsResponse = Response.success(
                listOf(ticket(id = "1"), ticket(id = "2"))
            )
        }
        val viewModel = TicketViewModel(api)

        viewModel.listarTickets()
        advanceUntilIdle()

        assertEquals(listOf("1", "2"), viewModel.listaTickets.value.map { it.id })
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun resolverTicket_recargaLaListaSiLaOperacionTieneExito() = runTest {
        val api = FakeApiService().apply {
            resolverTicketResponse = Response.success(ticket(id = "1").copy(estado = EstadoTicket.CERRADO))
            ticketsResponse = Response.success(listOf(ticket(id = "2")))
        }
        val viewModel = TicketViewModel(api)

        viewModel.resolverTicket("1")
        advanceUntilIdle()

        assertEquals(1, api.resolverTicketCalls)
        assertEquals(1, api.listarTicketsCalls)
        assertEquals(listOf("2"), viewModel.listaTickets.value.map { it.id })
    }

    @Test
    fun enviarTicket_construyeElPayloadConElUsuarioActual() = runTest {
        val api = FakeApiService()
        val viewModel = TicketViewModel(api)
        val usuario = Usuario(
            id = "user-1",
            nombre = "Ana",
            apellidos = "Verde",
            email = "ana@example.com",
            rol = Rol.USER
        )

        viewModel.enviarTicket(
            asunto = "Mapa",
            descripcion = "No aparece mi huerto",
            usuario = usuario,
            tipo = TipoTicket.ERROR
        )
        advanceUntilIdle()

        val ticket = api.ticketsCreados.single()
        assertEquals("Mapa", ticket.asunto)
        assertEquals("No aparece mi huerto", ticket.descripcion)
        assertEquals("user-1", ticket.usuarioId)
        assertEquals("Ana Verde", ticket.usuarioNombre)
        assertEquals(TipoTicket.ERROR, ticket.tipo)
        assertEquals(EstadoTicket.ABIERTO, ticket.estado)
    }
}
