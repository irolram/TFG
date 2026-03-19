package com.example.tfg.ui.screen.user

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tfg.data.model.Huerto // Asegúrate de que este import es el tuyo
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapaHuertosScreen(huertos: List<Huerto>) {
    val context = LocalContext.current

    // Configuración obligatoria de OSMDroid (Pide un "User Agent" con el nombre de tu app)
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Usamos AndroidView para meter el mapa clásico dentro de Jetpack Compose
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                // Configuramos el estilo del mapa (Gratis y sin API Key)
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true) // Permitir hacer zoom con los dedos

                // Nivel de zoom inicial (12.0 es ideal para ver una ciudad entera)
                controller.setZoom(12.0)

                // Buscamos si hay algún huerto para centrar el mapa ahí
                // Si no tienes huertos, centramos el mapa en España por defecto
                val puntoCentral = if (huertos.isNotEmpty() && huertos[0].latitud != 0.0) {
                    GeoPoint(huertos[0].latitud, huertos[0].longitud)
                } else {
                    GeoPoint(40.4168, -3.7038) // Centro de Madrid
                }
                controller.setCenter(puntoCentral)

                // Recorremos tu lista de huertos y ponemos una chincheta en cada uno
                huertos.forEach { huerto ->
                    if (huerto.latitud != 0.0 && huerto.longitud != 0.0) {
                        val marker = Marker(this)
                        marker.position = GeoPoint(huerto.latitud, huerto.longitud)
                        marker.title = huerto.nombre       // Al tocarlo, sale el nombre
                        marker.snippet = huerto.descripcion // Y la descripción debajo

                        overlays.add(marker)
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}