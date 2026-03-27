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
import coil.compose.AsyncImage // 📸 Librería Coil para iconos
import com.example.tfg.data.model.RespuestaPrevision
import com.example.tfg.data.network.WeatherClient
import kotlinx.coroutines.launch

@Composable
fun WidgetClima(latitud: Double, longitud: Double) {
    val scope = rememberCoroutineScope()
    var datosClima by remember { mutableStateOf<RespuestaPrevision?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    // Variable para forzar la recarga
    var version by remember { mutableIntStateOf(0) }

    val apiKey = "8cbe46aee05331a0d8229a37daa15f61"

    LaunchedEffect(latitud, longitud, version) {
        if (latitud != 0.0 && longitud != 0.0) {
            cargando = true
            error = false
            scope.launch {
                try {
                    val respuesta = WeatherClient.apiService.getPrevisionClima(
                        lat = latitud,
                        lon = longitud,
                        apiKey = apiKey
                    )
                    datosClima = respuesta
                } catch (e: Exception) {
                    error = true
                } finally {
                    cargando = false
                }
            }
        } else {
            cargando = false
            error = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        when {
            cargando -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp).align(Alignment.Center),
                color = Color(0xFF1976D2)
            )
            error || datosClima == null -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Clima no disponible", fontSize = 12.sp, color = Color.Gray)
                    IconButton(onClick = { version++ }) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
            else -> {
                val prevision = datosClima!!.lista[0]
                val nombreCiudad = if (!datosClima!!.ciudad.nombre.isNullOrBlank()) {
                    datosClima!!.ciudad.nombre
                } else {
                    "Zona Rural / Huerto"
                }

                val iconoCode = prevision.clima.firstOrNull()?.icono ?: "01d"
                val urlIcono = "https://openweathermap.org/img/wn/${iconoCode}@2x.png"

                Column {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationCity, null, tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                            Text(nombreCiudad, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2), modifier = Modifier.padding(start = 4.dp))
                        }

                        IconButton(onClick = { version++ }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Refresh, "Refrescar", tint = Color.Gray, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = urlIcono,
                                contentDescription = "Icono tiempo",
                                modifier = Modifier.size(50.dp),
                                contentScale = ContentScale.Fit
                            )

                            Column(modifier = Modifier.padding(start = 4.dp)) {
                                Text(
                                    text = "${prevision.principal.temperatura.toInt()}°C",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp
                                )
                                Text(
                                    text = prevision.clima.firstOrNull()?.descripcion?.replaceFirstChar { it.uppercase() } ?: "",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${(prevision.probabilidadLluvia * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Umbrella, null, modifier = Modifier.size(14.dp).padding(start = 2.dp), tint = Color(0xFF1976D2))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${prevision.principal.humedad}%", fontSize = 11.sp, color = Color.Gray)
                                Icon(Icons.Default.WaterDrop, null, modifier = Modifier.size(12.dp).padding(start = 2.dp), tint = Color(0xFF1E88E5))
                            }
                        }
                    }
                }
            }
        }
    }
}