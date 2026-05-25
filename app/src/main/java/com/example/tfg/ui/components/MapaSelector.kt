package com.example.tfg.ui.screen.user

import android.view.MotionEvent
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

// Función para seleccionar la ubicación en el mapa
@Composable
fun MapaSelectorUbicacion(
    latitudActual: Double,
    longitudActual: Double,
    modoGpsActivo: Boolean,
    onUbicacionSeleccionada: (Double, Double) -> Unit
) {
    val context = LocalContext.current


    Box(modifier = Modifier.fillMaxWidth().height(300.dp).clipToBounds()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    minZoomLevel = 4.0
                    controller.setZoom(15.0)

                    // Centro inicial (Puerto Real/Cádiz)
                    val puntoInicial = GeoPoint(36.5283, -6.1901)
                    controller.setCenter(puntoInicial)


                    setOnTouchListener { view, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN -> view.parent?.requestDisallowInterceptTouchEvent(true)
                            MotionEvent.ACTION_UP -> view.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                        false
                    }

                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            onUbicacionSeleccionada(p.latitude, p.longitude)
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint) = false
                    })
                    overlays.add(eventsOverlay)
                }
            },
            update = { vistaMapa ->
                if (latitudActual != 0.0 && longitudActual != 0.0) {
                    val punto = GeoPoint(latitudActual, longitudActual)

                    // Solo animamos si el mapa no está ya centrado ahí
                    if (vistaMapa.mapCenter.latitude != punto.latitude) {
                        vistaMapa.controller.animateTo(punto)
                    }

                    // Limpiamos solo los marcadores previos para no borrar la capa de eventos
                    vistaMapa.overlays.removeAll { it is Marker }

                    val nuevoMarcador = Marker(vistaMapa).apply {
                        position = punto
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                        if (modoGpsActivo) {
                            icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
                            title = "📍 Ubicación GPS"
                        } else {
                            title = "Punto seleccionado"
                        }
                    }

                    vistaMapa.overlays.add(nuevoMarcador)
                    vistaMapa.invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}