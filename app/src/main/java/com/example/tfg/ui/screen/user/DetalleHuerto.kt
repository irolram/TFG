package com.example.tfg.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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

    // 🚩 OPTIMIZACIÓN 1: Acceso al nuevo UI State
    val state by viewModel.uiState
    val token by tokenManager.accessToken.collectAsState(initial = null)
    val cultivos by viewModel.cultivosDelHuerto
    val cargandoCultivos by viewModel.cargandoCultivos

    // 🚩 CLAVE: Buscamos el huerto específico en la lista del estado
    val huertoActual = remember(state.lista, huertoId) {
        state.lista.find { it.id == huertoId }
    }

    LaunchedEffect(huertoId) {
        // Si por algún motivo la lista de huertos está vacía (ej. recarga), los pedimos
        if (state.lista.isEmpty()) {
            viewModel.obtenerTodosLosHuertos(apiService)
        }
        viewModel.cargarCultivosDeUnHuerto(apiService, huertoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = huertoActual?.nombre ?: "Cargando...",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("buscar_cultivo/$huertoId") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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

            // --- SECCIÓN CLIMA ---
            if (huertoActual != null) {
                WidgetClima(
                    latitud = huertoActual.latitud,
                    longitud = huertoActual.longitud
                )
            } else {
                // Shimmer o carga simple
                Card(
                    modifier = Modifier.fillMaxWidth().height(110.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- LISTA DE CULTIVOS ---
            Text(
                text = "Tus cultivos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (cargandoCultivos) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (cultivos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Este huerto está vacío.\n¡Añade tu primera planta!",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(cultivos, key = { it.id ?: "" }) { cultivo ->
                        ItemCultivo(
                            cultivo = cultivo,
                            onDelete = {
                                cultivo.id?.let { idSeguro ->
                                    viewModel.eliminarCultivoDelHuerto(
                                        apiService = apiService,
                                        huertoId = huertoId,
                                        cultivoId = idSeguro,
                                        token = token ?: ""
                                    )
                                }
                            },
                            onClick = { navController.navigate("detalle_planta/${cultivo.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCultivo(
    cultivo: com.example.tfg.data.model.Cultivo,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cultivo.nombre.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Estado: ${cultivo.estado}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}