package com.example.tfg.ui.screen.user

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.components.ItemHuerto
import com.example.tfg.viewModel.HuertosViewModel

// Pantalla de "Mis Huertos".
// Carga los huertos del usuario, muestra estados de carga/error/vacío y permite abrir o borrar cada huerto.
@Composable
fun MisHuertosScreen(
    navController: NavHostController,
    viewModel: HuertosViewModel
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val apiService = remember { RetrofitClient.getApiService(context) }
    val state by viewModel.uiState

    // Guarda el id y nombre del huerto seleccionado para borrar.
    var huertoIdABorrar by rememberSaveable { mutableStateOf<String?>(null) }
    var huertoNombreABorrar by rememberSaveable { mutableStateOf<String?>(null) }

    // Carga inicial de huertos si todavía no hay datos en memoria.
    LaunchedEffect(Unit) {
        if (state.lista.isEmpty()) {
            viewModel.obtenerTodosLosHuertos(apiService)
        }
    }

    // Diálogo de confirmación antes de eliminar un huerto.
    if (huertoIdABorrar != null) {
        AlertDialog(
            onDismissRequest = {
                huertoIdABorrar = null
                huertoNombreABorrar = null
            },
            title = { Text("¿Eliminar huerto?") },
            text = {
                Text("Se perderán todos los cultivos asociados a '${huertoNombreABorrar ?: "este huerto"}'.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        huertoIdABorrar?.let { id ->
                            viewModel.borrarHuerto(apiService, id)
                        }

                        huertoIdABorrar = null
                        huertoNombreABorrar = null
                    }
                ) {
                    Text(
                        text = "Borrar",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        huertoIdABorrar = null
                        huertoNombreABorrar = null
                    }
                ) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    // Contenedor principal. Los estados se centran, la lista usa LazyColumn con scroll.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = if (isLandscape) 16.dp else 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            state.cargando && state.lista.isEmpty() -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            state.error != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Ups! Algo ha fallado",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

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
                    contentPadding = PaddingValues(
                        horizontal = if (isLandscape) 8.dp else 16.dp,
                        vertical = if (isLandscape) 8.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        if (isLandscape) 10.dp else 16.dp
                    )
                ) {
                    item {
                        Text(
                            text = "Tus Parcelas",
                            style = if (isLandscape) {
                                MaterialTheme.typography.titleMedium
                            } else {
                                MaterialTheme.typography.titleLarge
                            },
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(
                        items = state.lista,
                        key = { huerto -> huerto.id ?: huerto.nombre }
                    ) { huerto ->
                        ItemHuerto(
                            huerto = huerto,
                            onClick = {
                                navController.navigate("detalle_huerto/${huerto.id}")
                            },
                            onDeleteClick = {
                                huertoIdABorrar = huerto.id
                                huertoNombreABorrar = huerto.nombre
                            }
                        )
                    }
                }
            }
        }
    }
}