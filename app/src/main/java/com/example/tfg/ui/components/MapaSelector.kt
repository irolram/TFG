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

// Función para mostrar el mapa con el punto seleccionado
@Composable
fun MapaSelectorUbicacion(
    latitudActual: Double,
    longitudActual: Double,
    modoGpsActivo: Boolean,
    onUbicacionSeleccionada: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var marcadorActual by remember { mutableStateOf<Marker?>(null) }

    // Configuramos la librería OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = Modifier.fillMaxWidth().height(300.dp).clipToBounds()) {

        //  AndroidView sirve para mostrar un componente de Android en Compose (en este caso un mapa)
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    minZoomLevel = 4.0
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(36.5283, -6.1901))

                    setOnTouchListener { view, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> view.parent?.requestDisallowInterceptTouchEvent(true)
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                        false
                    }

                    overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            marcadorActual?.let { overlays.remove(it) }
                            marcadorActual = Marker(this@apply).apply {
                                position = p
                                title = "Punto seleccionado"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            overlays.add(marcadorActual)
                            invalidate()
                            onUbicacionSeleccionada(p.latitude, p.longitude)
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint) = false
                    }))
                }
            },
            update = { vistaMapa ->
                if (latitudActual != 0.0 && longitudActual != 0.0) {
                    val punto = GeoPoint(latitudActual, longitudActual)
                    vistaMapa.controller.animateTo(punto)
                    marcadorActual?.let { vistaMapa.overlays.remove(it) }
                    marcadorActual = Marker(vistaMapa).apply {
                        position = punto
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        if (modoGpsActivo) {
                            icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
                            title = "📍 Mi GPS"
                        }
                    }
                    vistaMapa.overlays.add(marcadorActual)
                    vistaMapa.invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}