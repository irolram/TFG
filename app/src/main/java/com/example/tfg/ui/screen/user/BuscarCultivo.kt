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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.tfg.viewModel.PlantaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscarCultivoScreen(
    huertoId: String,
    viewModel: PlantaViewModel,
    onBack: () -> Unit,
    onCultivoGuardado: () -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }
    val resultados by viewModel.resultadosBusqueda
    val buscando by viewModel.buscando
    val error by viewModel.errorBusqueda

    // Estados para el diálogo de apodo
    var mostrarDialogo by remember { mutableStateOf(false) }
    var plantaSeleccionada by remember { mutableStateOf<com.example.tfg.data.model.CatalogoDePlantas?>(null) }
    var apodoTexto by remember { mutableStateOf("") }

    // --- DIÁLOGO DE PERSONALIZACIÓN ---
    if (mostrarDialogo && plantaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Personalizar cultivo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("¿Qué nombre le quieres poner a tu ${plantaSeleccionada?.nombre}?")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = apodoTexto,
                        onValueChange = { apodoTexto = it },
                        label = { Text("Apodo (ej: La de la ventana)") },
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
                        // 🚩 Llamamos al ViewModel pasando el apodo
                        viewModel.guardarPlantaEnHuerto(huertoId, plantaSeleccionada!!, apodoTexto) {
                            onCultivoGuardado()
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
                title = { Text("Añadir Cultivo", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = {
                    textoBusqueda = it
                    viewModel.buscarPlantas(it)
                },
                label = { Text("Filtrar catálogo...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (buscando) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
            ) {
                if (resultados.isEmpty() && !buscando) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No se han encontrado plantas", color = Color.Gray)
                        }
                    }
                }

                items(resultados) { planta ->
                    ItemPlantaCatalogo(
                        planta = planta,
                        onClick = {
                            // 🚩 CAMBIO: En lugar de guardar, preparamos el diálogo
                            plantaSeleccionada = planta
                            apodoTexto = "" // Limpiar el campo para la nueva planta
                            mostrarDialogo = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemPlantaCatalogo(planta: com.example.tfg.data.model.CatalogoDePlantas, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = planta.icono,
                contentDescription = planta.nombre,
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(
                    text = planta.nombre.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
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