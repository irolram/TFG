package com.example.tfg.ui.screen.admin

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
    var puntoClickado by remember { mutableStateOf(GeoPoint(36.5297, -6.1465)) }
    var radioKm by remember { mutableFloatStateOf(50f) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(puntoClickado, radioKm) {
        delay(500)

        android.util.Log.d(
            "MAPA_DEBUG",
            "Llamando API: Lat=${puntoClickado.latitude}, Radio=${radioKm * 1000}m"
        )

        viewModel.cargarConteoProximidad(
            puntoClickado.latitude,
            puntoClickado.longitude,
            radioKm.toDouble() * 1000.0
        )
    }

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            MapaProximidad(
                puntoClickado = puntoClickado,
                radioKm = radioKm,
                onPuntoClickadoChange = { puntoClickado = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            PanelControlMapa(
                conteoProximidad = viewModel.conteoProximidad,
                radioKm = radioKm,
                onRadioKmChange = { radioKm = it },
                modifier = Modifier
                    .widthIn(min = 280.dp, max = 380.dp)
                    .fillMaxHeight()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            MapaProximidad(
                puntoClickado = puntoClickado,
                radioKm = radioKm,
                onPuntoClickadoChange = { puntoClickado = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            PanelControlMapa(
                conteoProximidad = viewModel.conteoProximidad,
                radioKm = radioKm,
                onRadioKmChange = { radioKm = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun MapaProximidad(
    puntoClickado: GeoPoint,
    radioKm: Float,
    onPuntoClickadoChange: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setMultiTouchControls(true)
                controller.setZoom(10.0)
                controller.setCenter(puntoClickado)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val circulo = Polygon(mapView).apply {
                points = Polygon.pointsAsCircle(puntoClickado, radioKm.toDouble() * 1000.0)
                fillColor = android.graphics.Color.argb(40, 0, 121, 107)
                strokeColor = android.graphics.Color.rgb(0, 121, 107)
                strokeWidth = 2f
                setOnClickListener { _, _, _ -> false }
            }

            mapView.overlays.add(circulo)

            val marcador = Marker(mapView).apply {
                position = puntoClickado
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Centro de búsqueda"
            }

            mapView.overlays.add(marcador)

            val receptorEventos = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    onPuntoClickadoChange(p)
                    return true
                }

                override fun longPressHelper(p: GeoPoint): Boolean = false
            }

            mapView.overlays.add(MapEventsOverlay(receptorEventos))

            mapView.controller.setCenter(puntoClickado)
            mapView.invalidate()
        }
    )
}

@Composable
private fun PanelControlMapa(
    conteoProximidad: Long,
    radioKm: Float,
    onRadioKmChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Explorador de Proximidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$conteoProximidad",
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

            Text(
                text = "Radio de búsqueda: ${radioKm.toInt()} km",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )

            Slider(
                value = radioKm,
                onValueChange = onRadioKmChange,
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