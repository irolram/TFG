package com.example.tfg.ui.screen.user

import android.content.res.Configuration
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.components.WidgetClima
import com.example.tfg.ui.components.formatTimestamp
import com.example.tfg.viewModel.HuertosViewModel

// Pantalla de detalle de un huerto.
// Muestra el clima del huerto, su lista de cultivos y permite añadir o borrar plantas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleHuertoScreen(
    navController: NavHostController,
    viewModel: HuertosViewModel,
    tokenManager: TokenManager,
    huertoId: String
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val apiService = remember { RetrofitClient.getApiService(context) }

    // Guarda el id del cultivo que se quiere borrar para mostrar el diálogo de confirmación.
    var idCultivoABorrar by rememberSaveable { mutableStateOf<String?>(null) }

    val state by viewModel.uiState
    val token by tokenManager.accessToken.collectAsState(initial = null)
    val cultivos by viewModel.cultivosDelHuerto
    val cargandoCultivos by viewModel.cargandoCultivos

    // Busca el huerto actual dentro de la lista cargada en el ViewModel.
    val huertoActual = remember(state.lista, huertoId) {
        state.lista.find { it.id == huertoId }
    }

    // Al abrir la pantalla, carga los datos necesarios del huerto y sus cultivos.
    LaunchedEffect(huertoId) {
        viewModel.iniciarDetalleHuerto(apiService, huertoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = huertoActual?.nombre ?: "Cargando...",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
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
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Añadir Planta"
                )
            }
        }
    ) { paddingValues ->
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PanelResumenHuerto(
                    huertoActual = huertoActual,
                    totalCultivos = cultivos.size,
                    modifier = Modifier
                        .widthIn(min = 260.dp, max = 340.dp)
                        .fillMaxHeight()
                )

                PanelListaCultivos(
                    cultivos = cultivos,
                    cargandoCultivos = cargandoCultivos,
                    compacta = true,
                    onDelete = { idCultivoABorrar = it },
                    onClickCultivo = { cultivoId ->
                        navController.navigate("detalle_planta/$cultivoId")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                PanelResumenHuerto(
                    huertoActual = huertoActual,
                    totalCultivos = cultivos.size,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                PanelListaCultivos(
                    cultivos = cultivos,
                    cargandoCultivos = cargandoCultivos,
                    compacta = false,
                    onDelete = { idCultivoABorrar = it },
                    onClickCultivo = { cultivoId ->
                        navController.navigate("detalle_planta/$cultivoId")
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Diálogo de confirmación antes de borrar una planta del huerto.
    if (idCultivoABorrar != null) {
        AlertDialog(
            onDismissRequest = { idCultivoABorrar = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red
                )
            },
            title = { Text("¿Eliminar planta?") },
            text = { Text("Se borrarán todos los datos de esta planta. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        if (token != null) {
                            viewModel.eliminarCultivoDelHuerto(
                                apiService = apiService,
                                huertoId = huertoId,
                                cultivoId = idCultivoABorrar!!,
                                token = token!!
                            )
                        }

                        idCultivoABorrar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Borrar definitivamente")
                }
            },
            dismissButton = {
                TextButton(onClick = { idCultivoABorrar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
}

// Panel superior/lateral del huerto.
// Muestra el widget del clima si el huerto ya está cargado y un pequeño resumen de cultivos.
@Composable
private fun PanelResumenHuerto(
    huertoActual: com.example.tfg.data.model.Huerto?,
    totalCultivos: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (huertoActual != null) {
            WidgetClima(
                latitud = huertoActual.latitud,
                longitud = huertoActual.longitud
            )
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tus cultivos",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "$totalCultivos plantas registradas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Panel que muestra el estado de carga, el estado vacío o la lista de cultivos.
// LazyColumn gestiona el scroll de la lista.
@Composable
private fun PanelListaCultivos(
    cultivos: List<Cultivo>,
    cargandoCultivos: Boolean,
    compacta: Boolean,
    onDelete: (String) -> Unit,
    onClickCultivo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        cargandoCultivos -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        cultivos.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Este huerto está vacío.\n¡Añade tu primera planta!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(if (compacta) 8.dp else 12.dp),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(
                    items = cultivos,
                    key = { cultivo -> cultivo.id ?: "${cultivo.nombre}-${cultivo.fechaPlantacion}" }
                ) { cultivo ->
                    ItemCultivo(
                        cultivo = cultivo,
                        compacta = compacta,
                        onDelete = {
                            cultivo.id?.let { onDelete(it) }
                        },
                        onClick = {
                            cultivo.id?.let { onClickCultivo(it) }
                        }
                    )
                }
            }
        }
    }
}

// Tarjeta visual de un cultivo.
// Muestra imagen, nombre, apodo, fecha de siembra, estado y botón de borrado.
@Composable
fun ItemCultivo(
    cultivo: Cultivo,
    compacta: Boolean = false,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val imageSize = if (compacta) 48.dp else 60.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compacta) 12.dp else 16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(if (compacta) 10.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = cultivo.infoCatalogo?.icono?.trim(),
                contentDescription = null,
                modifier = Modifier
                    .size(imageSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cultivo.nombre.replaceFirstChar { it.uppercase() },
                    style = if (compacta) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Apodo: ${
                        if (cultivo.apodo.isNotBlank()) {
                            cultivo.apodo.replaceFirstChar { it.uppercase() }
                        } else {
                            "Sin nombre"
                        }
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                if (!compacta) {
                    Text(
                        text = "Fecha de siembra: ${formatTimestamp(cultivo.fechaPlantacion)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "Estado: ${cultivo.estado}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}