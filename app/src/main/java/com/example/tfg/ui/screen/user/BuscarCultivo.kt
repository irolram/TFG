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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tfg.data.model.CatalogoDePlantas
import com.example.tfg.viewModel.PlantaViewModel

// Pantalla para buscar plantas en el catálogo y añadir una al huerto seleccionado.
// Permite filtrar por texto, elegir una planta y personalizarla con un apodo antes de guardarla.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarCultivoScreen(
    huertoId: String,
    viewModel: PlantaViewModel,
    onBack: () -> Unit,
    onCultivoGuardado: () -> Unit
) {
    var textoBusqueda by rememberSaveable { mutableStateOf("") }

    val resultados by viewModel.resultadosBusqueda
    val buscando by viewModel.buscando
    val error by viewModel.errorBusqueda

    var mostrarDialogo by remember { mutableStateOf(false) }
    var plantaSeleccionada by remember { mutableStateOf<CatalogoDePlantas?>(null) }
    var apodoTexto by rememberSaveable { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Diálogo para ponerle un nombre personalizado al cultivo antes de guardarlo.
    if (mostrarDialogo && plantaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = {
                Text(
                    text = "Personalizar cultivo",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("¿Qué nombre le quieres poner a tu ${plantaSeleccionada?.nombre}?")

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apodoTexto,
                        onValueChange = { apodoTexto = it },
                        label = { Text("Apodo") },
                        placeholder = { Text(plantaSeleccionada?.nombre ?: "") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        plantaSeleccionada?.let { planta ->
                            viewModel.guardarPlantaEnHuerto(
                                huertoId,
                                planta,
                                apodoTexto
                            ) {
                                onCultivoGuardado()
                            }
                        }

                        mostrarDialogo = false
                    }
                ) {
                    Text("Plantar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Añadir Cultivo",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))

            // Campo de búsqueda. Cada cambio llama al ViewModel para filtrar el catálogo.
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = {
                    textoBusqueda = it
                    viewModel.buscarPlantas(it)
                },
                label = { Text("Filtrar catálogo...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Indicador de carga mientras se buscan plantas.
            if (buscando) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Mensaje de error si la búsqueda falla.
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))

            // Lista con scroll de plantas encontradas.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 12.dp),
                contentPadding = PaddingValues(
                    top = if (isLandscape) 4.dp else 8.dp,
                    bottom = 16.dp
                )
            ) {
                if (resultados.isEmpty() && !buscando) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(if (isLandscape) 16.dp else 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No se han encontrado plantas", color = Color.Gray)
                        }
                    }
                }

                items(resultados) { planta ->
                    ItemPlantaCatalogo(
                        planta = planta,
                        compacta = isLandscape,
                        onClick = {
                            plantaSeleccionada = planta
                            apodoTexto = ""
                            mostrarDialogo = true
                        }
                    )
                }
            }
        }
    }
}

// Tarjeta de una planta del catálogo.
// Muestra imagen, nombre, luz solar y riego; al pulsarla abre el diálogo para plantarla.
@Composable
fun ItemPlantaCatalogo(
    planta: CatalogoDePlantas,
    compacta: Boolean = false,
    onClick: () -> Unit
) {
    val imageSize = if (compacta) 52.dp else 65.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(if (compacta) 12.dp else 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(if (compacta) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = planta.icono,
                contentDescription = planta.nombre,
                modifier = Modifier
                    .size(imageSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = planta.nombre.replaceFirstChar { it.uppercase() },
                    style = if (compacta) {
                        MaterialTheme.typography.titleSmall
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sol: ${planta.luzSolar?.textoPantalla ?: "No Data"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(" • ", color = Color.Gray)

                    Text(
                        text = "Riego: ${planta.riego?.textoPantalla ?: "No Data"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}