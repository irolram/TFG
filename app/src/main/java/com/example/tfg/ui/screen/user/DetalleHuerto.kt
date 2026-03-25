package com.example.tfg.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.viewModel.HuertosViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHuertoScreen(
    navController: NavHostController,
    viewModel: HuertosViewModel,
    tokenManager: com.example.tfg.data.TokenManager, // 🚩 1. Ahora recibimos el tokenManager
    huertoId: String
) {
    val context = LocalContext.current
    val apiService = remember { RetrofitClient.getApiService(context) }

    // 🚩 2. Usamos 'accessToken' (que es como se llama en tu clase) y añadimos el valor inicial
    val token by tokenManager.accessToken.collectAsState(initial = null)

    val cultivos by viewModel.cultivosDelHuerto
    val cargando by viewModel.cargandoCultivos
    val huertoActual = viewModel.huertos.value.find { it.id == huertoId }

    LaunchedEffect(huertoId) {
        viewModel.cargarCultivosDeUnHuerto(apiService, huertoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(huertoActual?.nombre ?: "Detalle", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("buscar_cultivo/$huertoId") },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Añadir Cultivo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- 🌤️ SECCIÓN DE INFO DEL HUERTO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = huertoActual?.nombre ?: "Mi Huerto",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), Color.Gray)
                            Text(
                                text = "Lat: ${huertoActual?.latitud?.toString()?.take(5) ?: "0.0"}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "22°C", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                        Text(text = "Despejado ☀️", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Cultivos en este huerto",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else if (cultivos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay plantas registradas.\nPulsa el botón + para añadir una.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(cultivos) { planta ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = planta.icono?.trim(),
                                    contentDescription = null,
                                    error = rememberVectorPainter(Icons.Default.Warning),
                                    modifier = Modifier
                                        .size(55.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F8E9)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = planta.nombre.replaceFirstChar { it.uppercase() },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(text = planta.estado, color = Color.Gray, fontSize = 12.sp)
                                }

                                IconButton(onClick = {
                                    planta.id?.let { idSeguro ->
                                        viewModel.eliminarCultivoDelHuerto(
                                            apiService = apiService,
                                            huertoId = huertoId,
                                            cultivoId = idSeguro,
                                            token = token ?: ""
                                        )
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, "Borrar", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}