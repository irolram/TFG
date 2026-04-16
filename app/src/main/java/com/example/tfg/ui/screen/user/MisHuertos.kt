package com.example.tfg.ui.screen.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.components.ItemHuerto
import com.example.tfg.viewModel.HuertosViewModel

@Composable
fun MisHuertosScreen(navController: NavHostController, viewModel: HuertosViewModel) {
    val context = LocalContext.current
    val apiService = remember { RetrofitClient.getApiService(context) }

    // 🚩 OPTIMIZACIÓN 1: Acceso correcto al estado unificado
    val state by viewModel.uiState

    var huertoABorrar by remember { mutableStateOf<Huerto?>(null) }

    // Carga inicial
    LaunchedEffect(Unit) {
        if (state.lista.isEmpty()) {
            viewModel.obtenerTodosLosHuertos(apiService)
        }
    }

    // --- DIÁLOGO DE ELIMINACIÓN ---
    if (huertoABorrar != null) {
        AlertDialog(
            onDismissRequest = { huertoABorrar = null },
            title = { Text("¿Eliminar huerto?") },
            text = { Text("Se perderán todos los cultivos asociados a '${huertoABorrar?.nombre}'.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        huertoABorrar?.id?.let { id ->
                            viewModel.borrarHuerto(apiService, id)
                        }
                        huertoABorrar = null
                    }
                ) {
                    Text("Borrar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { huertoABorrar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    // --- CONTENIDO PRINCIPAL ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 🚩 OPTIMIZACIÓN 2: Lógica de estados basada en el objeto 'state'
            state.cargando && state.lista.isEmpty() -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            state.error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Ups! Algo ha fallado", fontWeight = FontWeight.Bold)
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(
                        onClick = { viewModel.obtenerTodosLosHuertos(apiService) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            state.lista.isEmpty() -> {
                Text(
                    text = "Aún no tienes huertos registrados.\n¡Crea el primero usando el botón +!",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Tus Parcelas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(state.lista, key = { it.id ?: "" }) { huerto ->
                        ItemHuerto(
                            huerto = huerto,
                            onClick = { navController.navigate("detalle_huerto/${huerto.id}") },
                            onDeleteClick = { huertoABorrar = huerto }
                        )
                    }
                }
            }
        }
    }
}