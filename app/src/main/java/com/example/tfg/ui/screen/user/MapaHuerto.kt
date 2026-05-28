package com.example.tfg.ui.screen.user

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tfg.data.model.Huerto
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Pantalla que muestra un mapa global con todos los huertos disponibles.
// Usa OSMDroid para pintar el mapa y coloca un marcador por cada huerto con coordenadas válidas.
@Composable
fun MapaHuertosScreen(
    huertos: List<Huerto>
) {
    val context = LocalContext.current

    // OSMDroid necesita un userAgent para cargar correctamente los mapas.
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Filtra huertos que tienen coordenadas reales.
    val huertosConUbicacion = remember(huertos) {
        huertos.filter { huerto ->
            huerto.latitud != 0.0 && huerto.longitud != 0.0
        }
    }

    // Punto inicial del mapa. Si no hay huertos, se centra en Madrid.
    val puntoCentral = remember(huertosConUbicacion) {
        if (huertosConUbicacion.isNotEmpty()) {
            GeoPoint(
                huertosConUbicacion.first().latitud,
                huertosConUbicacion.first().longitud
            )
        } else {
            GeoPoint(40.4168, -3.7038)
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(12.0)
                controller.setCenter(puntoCentral)
            }
        },
        update = { mapView ->
            // Limpia marcadores anteriores para evitar duplicados al recomponer la pantalla.
            mapView.overlays.clear()

            // Recorre los huertos con ubicación válida y añade una chincheta por cada uno.
            huertosConUbicacion.forEach { huerto ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(huerto.latitud, huerto.longitud)
                    title = huerto.nombre
                    snippet = huerto.descripcion
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }

                mapView.overlays.add(marker)
            }

            // Si hay huertos, centra el mapa en el primero. Si no, muestra Madrid.
            mapView.controller.setCenter(puntoCentral)

            // Fuerza el repintado del mapa tras actualizar marcadores.
            mapView.invalidate()
        }
    )
}