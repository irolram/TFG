package com.example.tfg.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun MapaSelectorUbicacion(
    onUbicacionSeleccionada: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var marcadorActual by remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clipToBounds()
    ) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    // Centramos el mapa por defecto al abrirlo
                    controller.setZoom(15.0)
                    controller.setCenter(GeoPoint(36.5283, -6.1901))

                    // 🛑 EL ARREGLO PARA EL BUG DEL ZOOM Y DESPLAZAMIENTO
                    // Esto le dice a la pantalla: "No te muevas mientras el usuario toca el mapa"
                    setOnTouchListener { view, event ->
                        when (event.action) {
                            MotionEvent.ACTION_DOWN,
                            MotionEvent.ACTION_MOVE -> {
                                // Bloqueamos el scroll de la columna padre
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                            }

                            MotionEvent.ACTION_UP,
                            MotionEvent.ACTION_CANCEL -> {
                                // Devolvemos el control del scroll a la pantalla normal
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                        false // Devolvemos false para que el mapa siga calculando el zoom internamente
                    }


                    // Creamos el "escuchador" de toques en el mapa
                    val receptorToques = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            // 1. Si ya había una chincheta antes, la borramos
                            marcadorActual?.let { overlays.remove(it) }

                            // 2. Creamos la nueva chincheta donde el usuario ha tocado
                            val nuevoMarcador = Marker(this@apply).apply {
                                position = p
                                title = "Ubicación elegida"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }

                            overlays.add(nuevoMarcador)
                            marcadorActual = nuevoMarcador
                            invalidate() // Refrescamos el mapa para que se vea el cambio

                            // 3. Le pasamos las coordenadas a tu pantalla
                            onUbicacionSeleccionada(p.latitude, p.longitude)

                            return true
                        }

                        override fun longPressHelper(p: GeoPoint): Boolean {
                            return false // No hacemos nada si mantiene pulsado
                        }
                    }

                    // Añadimos la capa invisible que detecta los toques
                    overlays.add(MapEventsOverlay(receptorToques))
                }
            },
            // Le damos una altura fija para que no ocupe toda la pantalla de crear huerto
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )
    }
}