package com.example.tfg.ui.screen.user

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.text.style.TextOverflow


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisHuertosScreen(navController: NavHostController, viewModel: HuertosViewModel) {
    val context = LocalContext.current
    val apiService = remember { RetrofitClient.getApiService(context) }

    val huertos = viewModel.huertos.value
    val cargando = viewModel.cargando.value
    val error = viewModel.error.value

    LaunchedEffect(Unit) {
        viewModel.obtenerTodosLosHuertos(apiService)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Huertos", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50)) // Tu VerdePrenda
            )
        },
        // 3. 🔌 Botón flotante para ir a tu pantalla de "Crear Huerto"
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Asegúrate de poner la ruta exacta que usas en tu NavHost
                    navController.navigate("crear_huerto")
                },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Huerto")
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Manejo de estados visuales
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
                    // 4. 🔌 El "RecyclerView" de Compose
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(huertos) { huerto ->
                            ItemHuerto(
                                huerto = huerto,
                                onClick = {

                                    navController.navigate("detalle_huerto/${huerto.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ItemHuerto(huerto: Huerto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        // Usamos Row para dividir la tarjeta en Izquierda (Datos) y Derecha (Clima)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically // Centramos verticalmente ambos bloques
        ) {

            // ==========================================
            // IZQUIERDA: Textos del Huerto (weight = 1f)
            // ==========================================
            Column(
                modifier = Modifier.weight(1f) // Esto hace que ocupe todo el espacio a la izquierda
            ) {
                Text(
                    text = huerto.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis // Si el nombre es muy largo, pone "..."
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = huerto.descripcion,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ==========================================
            // DERECHA: Bloque del Clima (MOCK para el futuro)
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 16.dp) // Separación con el texto
            ) {
                // 🔌 Cuando tengas la API, cambiarás este Icono dinámicamente
                Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = "Tiempo actual",
                    tint = Color(0xFFFFA000), // Color naranja para el sol
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 🔌 Cuando tengas la API, enlazarás aquí la variable de temperatura
                Text(
                    text = "24°C",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }
        }
    }
}