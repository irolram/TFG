package com.example.tfg.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tfg.viewModel.UsuarioViewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Composable
fun MapaAdminScreen(viewModel: UsuarioViewModel) {
    val context = LocalContext.current
    var puntoClickado by remember { mutableStateOf(GeoPoint(36.5297, -6.1465)) }
    var radioKm by remember { mutableFloatStateOf(50f) }

    LaunchedEffect(puntoClickado, radioKm) {
        viewModel.cargarConteoProximidad(
            puntoClickado.latitude,
            puntoClickado.longitude,
            radioKm.toDouble()
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { ctx ->
                MapView(ctx).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(10.0)
                    controller.setCenter(puntoClickado)
                }
            },
            update = { mapView ->
                // 1. Limpiamos todas las capas para reordenar
                mapView.overlays.clear()

                // 2. Dibujamos el círculo del radio
                val circulo = Polygon(mapView).apply {
                    points = Polygon.pointsAsCircle(puntoClickado, radioKm.toDouble() * 1000.0)
                    fillColor = android.graphics.Color.argb(45, 76, 175, 80)
                    strokeColor = android.graphics.Color.rgb(76, 175, 80)
                    strokeWidth = 3f
                    // 🚩 TRUCO: Desactivamos el clic en el círculo para que sea "transparente"
                    setOnClickListener { _, _, _ -> false }
                }
                mapView.overlays.add(circulo)

                // 3. Añadimos el Marcador
                val marcador = Marker(mapView).apply {
                    position = puntoClickado
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Centro"
                }
                mapView.overlays.add(marcador)

                // 4. 🚩 CAPA SUPERIOR: El detector de clics debe ir el ÚLTIMO
                // De esta forma, siempre recibe el toque primero, incluso dentro del círculo
                val receptorEventos = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        puntoClickado = p
                        return true
                    }
                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }
                mapView.overlays.add(MapEventsOverlay(receptorEventos))

                mapView.invalidate()
            }
        )

        // --- PANEL DE CONTROL DEL ADMIN ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Explorador de Proximidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Resultado de la API
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(
                        text = "${viewModel.conteoProximidad}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Huertos en esta zona", color = Color.Gray)
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Selector de Radio
                Text(
                    text = "Ajustar radio de acción: ${radioKm.toInt()} km",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = radioKm,
                    onValueChange = { radioKm = it },
                    valueRange = 5f..300f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF4CAF50),
                        activeTrackColor = Color(0xFF4CAF50)
                    )
                )

                Text(
                    text = "Toca el mapa para cambiar la ubicación de origen.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}