package com.example.tfg.ui.screen.user

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.components.FichaTecnicaSimple
import com.example.tfg.viewModel.HuertosViewModel
import com.example.tfg.viewModel.PlantaViewModel

// Pantalla de detalle de un cultivo concreto.
// Muestra datos básicos, permite registrar un riego y enseña guía técnica/consejos de cultivo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleCultivoScreen(
    navController: NavHostController,
    viewModelHuerto: HuertosViewModel,
    viewModelPlanta: PlantaViewModel,
    cultivoId: String
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val apiService = remember { RetrofitClient.getApiService(context) }
    val cultivos by viewModelHuerto.cultivosDelHuerto

    // Busca el cultivo actual dentro de la lista cargada en el ViewModel.
    val miCultivo = remember(cultivos, cultivoId) {
        cultivos.find { it.id == cultivoId }
    }

    // Información extendida del catálogo: icono, instrucciones y ficha técnica.
    val detalleEspecie = miCultivo?.infoCatalogo

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = miCultivo?.nombre?.replaceFirstChar { it.uppercase() } ?: "Detalle",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (miCultivo == null) {
            // Estado de carga mientras todavía no se encuentra el cultivo.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Cargando información del cultivo...", color = Color.Gray)
                }
            }
        } else {
            if (isLandscape) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CabeceraCultivo(
                            nombre = miCultivo.nombre,
                            estado = miCultivo.estado.toString(),
                            icono = detalleEspecie?.icono?.trim(),
                            compacta = true
                        )

                        BotonRegarCultivo(
                            onClick = {
                                viewModelPlanta.regarPlanta(
                                    miCultivo.id.toString(),
                                    apiService,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Planta regada con éxito",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { mensajeError ->
                                        Toast.makeText(
                                            context,
                                            "Error al regar: $mensajeError",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            }
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        ContenidoTecnicoCultivo(
                            nombreCultivo = miCultivo.nombre,
                            detalleEspecie = detalleEspecie
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    CabeceraCultivo(
                        nombre = miCultivo.nombre,
                        estado = miCultivo.estado.toString(),
                        icono = detalleEspecie?.icono?.trim()
                    )

                    Spacer(Modifier.height(24.dp))

                    BotonRegarCultivo(
                        onClick = {
                            viewModelPlanta.regarPlanta(
                                miCultivo.id.toString(),
                                apiService,
                                onSuccess = {
                                    Toast.makeText(
                                        context,
                                        "Planta regada con éxito",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onError = { mensajeError ->
                                    Toast.makeText(
                                        context,
                                        "Error al regar: $mensajeError",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    )

                    Spacer(Modifier.height(32.dp))

                    ContenidoTecnicoCultivo(
                        nombreCultivo = miCultivo.nombre,
                        detalleEspecie = detalleEspecie
                    )

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// Cabecera visual del cultivo.
// Enseña imagen, nombre y estado actual de la planta.
@Composable
private fun CabeceraCultivo(
    nombre: String,
    estado: String,
    icono: String?,
    compacta: Boolean = false
) {
    val imageSize = if (compacta) 72.dp else 100.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compacta) 18.dp else 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(if (compacta) 12.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = icono,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(imageSize)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            Spacer(Modifier.width(if (compacta) 14.dp else 20.dp))

            Column {
                Text(
                    text = nombre.replaceFirstChar { it.uppercase() },
                    style = if (compacta) {
                        MaterialTheme.typography.titleLarge
                    } else {
                        MaterialTheme.typography.headlineSmall
                    },
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Estado: $estado",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Botón principal para registrar que el cultivo se ha regado.
@Composable
private fun BotonRegarCultivo(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            imageVector = Icons.Default.WaterDrop,
            contentDescription = "Regar",
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "He regado esta planta",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Bloque con la ficha técnica y los consejos del cultivo.
@Composable
private fun ContenidoTecnicoCultivo(
    nombreCultivo: String,
    detalleEspecie: com.example.tfg.data.model.CatalogoDePlantas?
) {
    SectionTitle(title = "Guía técnica de la especie")

    if (detalleEspecie != null) {
        FichaTecnicaSimple(planta = detalleEspecie)
    }

    Spacer(Modifier.height(24.dp))

    SectionTitle(title = "Consejos de cultivo")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = detalleEspecie?.instrucciones
                    ?: "Estamos preparando los mejores consejos personalizados para tu $nombreCultivo...",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Título reutilizable para las secciones internas de la pantalla.
@Composable
fun SectionTitle(title: String) {
    Text(
        text = " $title",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}