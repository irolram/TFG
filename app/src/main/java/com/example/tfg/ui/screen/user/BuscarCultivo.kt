package com.example.tfg.ui.screen.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tfg.viewModel.PlantaViewModel
import coil.compose.AsyncImage
import com.example.tfg.data.network.RetrofitClient

@Composable
fun BuscarCultivoScreen(
    huertoId: String,
    onCultivoGuardado: () -> Unit,
    viewModel: PlantaViewModel = viewModel()
) {
    var textoBusqueda by remember { mutableStateOf("") }
    val resultados by viewModel.resultadosBusqueda
    val buscando by viewModel.buscando
    val error by viewModel.errorBusqueda
    val context = LocalContext.current

    // 🔌 Ahora solo necesitamos TU API de Railway
    val apiService = remember { RetrofitClient.getApiService(context) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Añadir nuevo cultivo", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Búsqueda en catálogo propio", fontSize = 12.sp, color = Color(0xFF4CAF50))

        // 🔍 BARRA DE BÚSQUEDA
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
                // Buscamos directamente en tu Spring Boot
                viewModel.buscarPlantas(apiService, it)
            },
            label = { Text("¿Qué quieres plantar? (ej: Tomate)") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        if (buscando) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }

        error?.let {
            Text(text = it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        // 🌿 LISTA DE RESULTADOS DEL CATÁLOGO
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(resultados) { planta ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.guardarPlantaEnHuerto(
                                apiService,
                                huertoId,
                                planta
                            ) {
                                onCultivoGuardado()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        //  Imagen directa de tu base de datos (Unsplash, etc.)
                        AsyncImage(
                            model = planta.iconoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(65.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )

                        Column(modifier = Modifier.padding(start = 16.dp)) {
                            Text(
                                text = planta.nombre.replaceFirstChar { it.uppercase() },
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )

                            // Información de cultivo sacada de tu propia DB
                            Text(
                                text = "Sol: ${planta.luzSolar ?: "Pleno sol"}",
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )

                            Text(
                                text = "Riego: ${planta.riego ?: "Moderado"}",
                                fontSize = 13.sp,
                                color = Color(0xFF388E3C)
                            )
                        }
                    }
                }
            }
        }
    }
}