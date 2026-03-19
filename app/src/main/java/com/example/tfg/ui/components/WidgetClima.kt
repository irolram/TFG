package com.example.tfg.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.data.model.RespuestaPrevision
import com.example.tfg.data.network.WeatherClient
import kotlinx.coroutines.launch

@Composable
fun WidgetClima(latitud: Double, longitud: Double) {
    val scope = rememberCoroutineScope()
    // Usamos la clase traducida
    var datosClima by remember { mutableStateOf<RespuestaPrevision?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf(false) }

    // 🔑 PON AQUÍ TU API KEY
    val apiKey = "8cbe46aee05331a0d8229a37daa15f61"

    LaunchedEffect(latitud, longitud) {
        if (latitud != 0.0 && longitud != 0.0) {
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
            .background(Color(0xFFE3F2FD), shape = RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        when {
            cargando -> CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.Center))
            error || datosClima == null || datosClima!!.lista.isEmpty() -> Text("Clima no disponible", fontSize = 12.sp, color = Color.Gray)
            else -> {
                // Navegamos usando las propiedades en español
                val prevision = datosClima!!.lista[0]

                val vientoKmh = (prevision.viento.velocidad * 3.6).toInt()
                val tempStr = "${prevision.principal.temperatura.toInt()}°C"
                val descripcionStr = prevision.clima.firstOrNull()?.descripcion?.replaceFirstChar { it.uppercase() } ?: "Despejado"

                // Multiplicamos por 100 para sacar el porcentaje real
                val lluviaPorcentaje = (prevision.probabilidadLluvia * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bloque Izquierdo
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.DeviceThermostat, contentDescription = "Temp", tint = Color(0xFFF57C00), modifier = Modifier.size(20.dp))
                            Text(text = tempStr, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                        Text(text = descripcionStr, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(start = 24.dp))
                    }

                    // Bloque Derecho
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "$lluviaPorcentaje% Lluvia", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (lluviaPorcentaje > 50) Color.Red else Color.DarkGray)
                            Icon(Icons.Filled.Umbrella, contentDescription = "Lluvia", tint = if (lluviaPorcentaje > 50) Color.Red else Color.Gray, modifier = Modifier.size(14.dp).padding(start = 4.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Text(text = "${prevision.principal.humedad}% Humedad", fontSize = 12.sp, color = Color.DarkGray)
                            Icon(Icons.Filled.WaterDrop, contentDescription = "Humedad", tint = Color(0xFF1E88E5), modifier = Modifier.size(12.dp).padding(start = 4.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Text(text = "$vientoKmh km/h", fontSize = 12.sp, color = Color.DarkGray)
                            Icon(Icons.Filled.Air, contentDescription = "Viento", tint = Color.Gray, modifier = Modifier.size(12.dp).padding(start = 4.dp))
                        }
                    }
                }
            }
        }
    }
}