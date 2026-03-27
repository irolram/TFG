package com.example.tfg.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.example.tfg.ui.components.WidgetClima
import com.example.tfg.viewModel.HuertosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHuertoScreen(
    navController: NavHostController,
    viewModel: HuertosViewModel,
    tokenManager: com.example.tfg.data.TokenManager,
    huertoId: String
) {
    val context = LocalContext.current
    val apiService = remember { RetrofitClient.getApiService(context) }

    // Observamos el token del DataStore
    val token by tokenManager.accessToken.collectAsState(initial = null)

    // Observamos los datos del ViewModel
    val cultivos by viewModel.cultivosDelHuerto
    val cargando by viewModel.cargandoCultivos

    // Buscamos el huerto actual en la lista para obtener latitud/longitud
    val huertoActual = viewModel.huertos.value.find { it.id == huertoId }

    // Disparamos la carga de cultivos al entrar
    LaunchedEffect(huertoId) {
        viewModel.cargarCultivosDeUnHuerto(apiService, huertoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(huertoActual?.nombre ?: "Detalle del Huerto", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF4CAF50)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            // Botón para añadir cultivo al huerto
            FloatingActionButton(
                onClick = { navController.navigate("buscar_cultivo/$huertoId") },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Añadir Planta")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Se muestra el clima del huerto
            if (huertoActual != null) {
                WidgetClima(
                    latitud = huertoActual.latitud,
                    longitud = huertoActual.longitud
                )
            } else {
                // Estado de carga del clima
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lista de cultivos
            Text(
                text = "Tus cultivos",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else if (cultivos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Aún no tienes nada plantado aquí.\n¡Pulsa el botón + para empezar!",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(cultivos) { cultivo ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("detalle_planta/${cultivo.id}")
                                },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                AsyncImage(
                                    model = cultivo.infoCatalogo?.icono?.trim(),
                                    contentDescription = null,
                                    error = rememberVectorPainter(Icons.Default.Warning),
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1F8E9)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                // Info de la cultivo
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cultivo.nombre.replaceFirstChar { it.uppercase() },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "Estado: ${cultivo.estado}",
                                        color = Color.Gray,
                                        fontSize = 13.sp
                                    )
                                }

                                // Botón de Borrar
                                IconButton(onClick = {
                                    cultivo.id?.let { idSeguro ->
                                        viewModel.eliminarCultivoDelHuerto(
                                            apiService = apiService,
                                            huertoId = huertoId,
                                            cultivoId = idSeguro,
                                            token = token ?: ""
                                        )
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color(0xFFE57373)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}