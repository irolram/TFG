package com.example.tfg.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tfg.viewModel.UsuarioViewModel
import kotlinx.coroutines.delay
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

@Composable
fun MapaAdminScreen(viewModel: UsuarioViewModel) {
    // 1. Estados locales para la UI del Mapa
    var puntoClickado by remember { mutableStateOf(GeoPoint(36.5297, -6.1465)) }
    var radioKm by remember { mutableFloatStateOf(50f) }

    // 🚩 ESTRATEGIA PRO: Debouncing
    // Este efecto se dispara cada vez que cambias el punto o el radio
    LaunchedEffect(puntoClickado, radioKm) {
        // Esperamos medio segundo. Si el usuario sigue moviendo el slider,
        // este bloque se cancela y vuelve a empezar, evitando saturar la API.
        delay(500)


        android.util.Log.d("MAPA_DEBUG", "Llamando API: Lat=${puntoClickado.latitude}, Radio=${radioKm * 1000}m")

        viewModel.cargarConteoProximidad(
            puntoClickado.latitude,
            puntoClickado.longitude,
            radioKm.toDouble() * 1000.0 // Convertimos a metros para el servidor

        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- MAPA ---
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
                mapView.overlays.clear()

                // Capa del círculo (Radio visual)
                val circulo = Polygon(mapView).apply {
                    points = Polygon.pointsAsCircle(puntoClickado, radioKm.toDouble() * 1000.0)
                    fillColor = android.graphics.Color.argb(40, 0, 121, 107) // Teal transparente
                    strokeColor = android.graphics.Color.rgb(0, 121, 107)
                    strokeWidth = 2f
                    setOnClickListener { _, _, _ -> false }
                }
                mapView.overlays.add(circulo)

                // Capa del marcador (Centro)
                val marcador = Marker(mapView).apply {
                    position = puntoClickado
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Centro de búsqueda"
                }
                mapView.overlays.add(marcador)

                // Capa de eventos (Clic para mover centro)
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

        // --- PANEL DE CONTROL (Card inferior) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Explorador de Proximidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Resultado que viene del ViewModel
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${viewModel.conteoProximidad}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Huertos encontrados",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Slider para el radio
                Text(
                    text = "Radio de búsqueda: ${radioKm.toInt()} km",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )

                Slider(
                    value = radioKm,
                    onValueChange = { radioKm = it },
                    valueRange = 1f..300f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    text = "Toca el mapa para reubicar el radar de búsqueda.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}