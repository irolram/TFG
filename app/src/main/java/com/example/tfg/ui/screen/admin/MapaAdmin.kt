package com.example.tfg.ui.screen.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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

    // 1. Estados de posición (Empezamos en Puerto Real/Cádiz por defecto)
    var puntoSeleccionado by remember { mutableStateOf(GeoPoint(36.5297, -6.1465)) }
    var radioSeleccionado by remember { mutableFloatStateOf(50f) }

    // 2. Cargamos datos cuando cambie el punto o el radio
    LaunchedEffect(puntoSeleccionado, radioSeleccionado) {
        viewModel.cargarConteoProximidad(
            puntoSeleccionado.latitude,
            puntoSeleccionado.longitude,
            radioSeleccionado.toDouble()
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- PARTE SUPERIOR: EL MAPA (OSMDroid) ---
        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { ctx ->
                MapView(ctx).apply {
                    setMultiTouchControls(true)
                    controller.setZoom(9.0)
                    controller.setCenter(puntoSeleccionado)

                    // 🚩 DETECTAR CLIC: Receptor de eventos
                    val mReceive = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            puntoSeleccionado = p // Actualizamos el punto al clickar
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(mReceive))
                }
            },
            update = { mapView ->
                mapView.overlays.removeIf { it is Marker || it is Polygon }

                // Dibujar el Marcador
                val marker = Marker(mapView).apply {
                    position = puntoSeleccionado
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "Punto de búsqueda"
                }
                mapView.overlays.add(marker)

                // Dibujar el Círculo de Radio (Polygon)
                val circle = Polygon().apply {
                    points = Polygon.pointsAsCircle(puntoSeleccionado, radioSeleccionado.toDouble() * 1000.0)
                    fillColor = android.graphics.Color.argb(50, 76, 175, 80) // VerdeEco transparente
                    strokeColor = android.graphics.Color.rgb(76, 175, 80)
                    strokeWidth = 2f
                }
                mapView.overlays.add(circle)

                mapView.invalidate() // Refrescar mapa
            }
        )

        // --- PARTE INFERIOR: CONTROLES ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Análisis por Proximidad", style = MaterialTheme.typography.titleMedium)

                Text(
                    text = "${viewModel.conteoProximidad} huertos en esta zona",
                    fontSize = 20.sp,
                    color = Color(0xFF4CAF50), // VerdeEco
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Radio de búsqueda: ${radioSeleccionado.toInt()} km", fontSize = 12.sp)
                Slider(
                    value = radioSeleccionado,
                    onValueChange = { radioSeleccionado = it },
                    valueRange = 5f..300f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF4CAF50))
                )

                Text(
                    text = "Toca cualquier punto del mapa para mover el centro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}