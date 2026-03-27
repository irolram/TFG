package com.example.tfg.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import com.example.tfg.ui.components.ItemHuerto
import com.example.tfg.ui.components.WidgetClima

// Pantalla donde se visualizan los huertos del usuario
@Composable
fun MisHuertosScreen(navController: NavHostController, viewModel: HuertosViewModel) {
    val context = LocalContext.current
    val apiService = remember { RetrofitClient.getApiService(context) }

    val huertos = viewModel.huertos.value
    val cargando = viewModel.cargando.value
    val error = viewModel.error.value

    // Variable para controlar si se muestra el aviso de confirmación
    var huertoABorrar by remember { mutableStateOf<Huerto?>(null) }

    // Cargamos los huertos al iniciar la pantalla
    LaunchedEffect(Unit) {
        viewModel.obtenerTodosLosHuertos(apiService)
    }


    if (huertoABorrar != null) {
        AlertDialog(
            onDismissRequest = { huertoABorrar = null },
            title = { Text("Borrar huerto") },
            text = { Text("¿Estás seguro de que quieres eliminar '${huertoABorrar?.nombre}'? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        huertoABorrar?.id?.let { id ->
                            viewModel.borrarHuerto(apiService, id)
                        }
                        huertoABorrar = null // Cerramos el diálogo
                    }
                ) {
                    Text("Borrar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { huertoABorrar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            cargando -> {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            }
            error != null -> {
                Text(text = error, color = Color.Red, modifier = Modifier.padding(16.dp))
            }
            huertos.isEmpty() -> {
                Text("Aún no tienes huertos. ¡Crea el primero!", color = Color.Gray)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(huertos) { huerto ->
                        ItemHuerto(
                            huerto = huerto,
                            onClick = { navController.navigate("detalle_huerto/${huerto.id}") },
                            // Le pasamos la acción de borrar al componente
                            onDeleteClick = { huertoABorrar = huerto }
                        )
                    }
                }
            }
        }
    }
}