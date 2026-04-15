package com.example.tfg.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.data.model.EstadoTicket
import com.example.tfg.data.model.Ticket
import com.example.tfg.data.model.TipoTicket
import com.example.tfg.data.model.Usuario
import com.example.tfg.data.network.IApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TicketViewModel(private val apiService: IApiService) : ViewModel() {
    private val _listaTickets = MutableStateFlow<List<Ticket>>(emptyList())
    val listaTickets: StateFlow<List<Ticket>> = _listaTickets

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun listarTickets() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = apiService.listarTickets()
                if (response.isSuccessful) {
                    _listaTickets.value = response.body() ?: emptyList()
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

// TicketViewModel.kt

    fun resolverTicket(id: String) {
        viewModelScope.launch {
            try {
                val response = apiService.resolverTicket(id)
                if (response.isSuccessful) {
                    listarTickets()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "No se pudo resolver el ticket")
            }
        }
    }

    fun enviarTicket(asunto: String, descripcion: String, usuario: Usuario?, tipo: TipoTicket)
    {
        val nuevoTicket = Ticket(
            id = null,
            asunto = asunto,
            descripcion = descripcion,
            usuarioId = usuario?.id ?: "",
            usuarioNombre = "${usuario?.nombre ?: "Sin"} ${usuario?.apellidos ?: "Nombre"}",
            tipo = tipo,
            estado = EstadoTicket.ABIERTO,
            fecha = ""
        )

        viewModelScope.launch {
            try {
                val response = apiService.crearTicket(nuevoTicket)
                if (response.isSuccessful) {
                    Log.d("API_SUCCESS", "¡Ticket subido! ID generado: ${response.body()?.id}")
                } else {
                    Log.e(
                        "API_ERROR",
                        "Error ${response.code()}: ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("API_FAILURE", "Fallo de red: ${e.message}")
            }
        }
    }
}
