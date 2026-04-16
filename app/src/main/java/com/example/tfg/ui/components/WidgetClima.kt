package com.example.tfg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tfg.data.model.RespuestaPrevision
import com.example.tfg.data.network.WeatherClient

@Composable
fun WidgetClima(latitud: Double, longitud: Double) {
    // 🚩 OPTIMIZACIÓN 1: Estados más limpios
    var datosClima by remember { mutableStateOf<RespuestaPrevision?>(null) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var version by remember { mutableIntStateOf(0) }

    val apiKey = "8cbe46aee05331a0d8229a37daa15f61"

    // 🚩 OPTIMIZACIÓN 2: Corregir el uso de Corrutinas
    // LaunchedEffect ya es un scope de corrutina. No necesitas scope.launch dentro.
    LaunchedEffect(latitud, longitud, version) {
        if (latitud != 0.0 && longitud != 0.0) {
            cargando = true
            error = false
            try {
                // Llamada directa suspendida
                datosClima = WeatherClient.apiService.getPrevisionClima(
                    lat = latitud,
                    lon = longitud,
                    apiKey = apiKey
                )
            } catch (e: Exception) {
                error = true
            } finally {
                cargando = false
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Box(
            modifier = Modifier.padding(12.dp).fillMaxWidth().heightIn(min = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                cargando -> CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)

                error || datosClima == null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Clima no disponible", style = MaterialTheme.typography.labelSmall)
                        IconButton(onClick = { version++ }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                else -> {
                    val prevision = datosClima!!.lista[0]
                    val iconoCode = prevision.clima.firstOrNull()?.icono ?: "01d"

                    Column {
                        // Fila superior: Ciudad y Refrescar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp))
                                Text(
                                    text = datosClima!!.ciudad.nombre.ifBlank { "Zona de cultivo" },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { version++ }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Refresh, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Fila inferior: Info Clima
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = "https://openweathermap.org/img/wn/${iconoCode}@2x.png",
                                    contentDescription = null,
                                    modifier = Modifier.size(45.dp)
                                )
                                Column {
                                    Text(
                                        text = "${prevision.principal.temperatura.toInt()}°C",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = prevision.clima.firstOrNull()?.descripcion?.replaceFirstChar { it.uppercase() } ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Info extra (Lluvia y Humedad)
                            Column(horizontalAlignment = Alignment.End) {
                                WeatherInfoMini(
                                    value = "${(prevision.probabilidadLluvia * 100).toInt()}%",
                                    icon = Icons.Default.Umbrella
                                )
                                WeatherInfoMini(
                                    value = "${prevision.principal.humedad}%",
                                    icon = Icons.Default.WaterDrop
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherInfoMini(value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(12.dp).padding(start = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}