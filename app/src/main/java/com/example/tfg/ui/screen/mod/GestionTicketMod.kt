package com.example.tfg.ui.screen.mod

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.data.model.Ticket
import com.example.tfg.data.model.TicketAction
import com.example.tfg.data.model.TipoTicket

// Pantalla de gestión de tickets del moderador.
// Muestra los tickets pendientes, permite refrescar la lista y marcar incidencias como resueltas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionTicketsScreen(
    listaTickets: List<Ticket>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onResolverTicket: (String) -> Unit
) {
    // Guarda la acción pendiente para mostrar el diálogo de confirmación antes de resolver un ticket.
    var actionPendiente by remember { mutableStateOf<TicketAction>(TicketAction.None) }

    // Detecta orientación horizontal para reducir espacios y que quepa más contenido.
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Si hay un ticket pendiente de resolver, se pide confirmación al usuario.
    if (actionPendiente is TicketAction.ConfirmResolve) {
        val ticket = (actionPendiente as TicketAction.ConfirmResolve).ticket

        AlertDialog(
            onDismissRequest = { actionPendiente = TicketAction.None },
            title = { Text("Resolver Ticket") },
            text = { Text("¿Confirmas que el problema '${ticket.asunto}' ha sido solucionado?") },
            confirmButton = {
                Button(
                    onClick = {
                        ticket.id?.let { onResolverTicket(it) }
                        actionPendiente = TicketAction.None
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { actionPendiente = TicketAction.None }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cabecera de la pantalla. En horizontal se hace más baja para dejar más espacio a la lista.
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 20.dp,
                        vertical = if (isLandscape) 10.dp else 20.dp
                    )
                    .statusBarsPadding()
            ) {
                Text(
                    text = "CENTRO DE SOPORTE",
                    style = if (isLandscape) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )

                if (!isLandscape) {
                    Text(
                        text = "Bandeja de entrada de incidencias",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Contenedor con pull-to-refresh para recargar tickets arrastrando hacia abajo.
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (listaTickets.isEmpty() && !isRefreshing) {
                // Estado vacío cuando no quedan incidencias pendientes.
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )

                        Text(
                            text = "No hay tickets pendientes",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // Lista con scroll de tickets pendientes.
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = if (isLandscape) 8.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(
                        if (isLandscape) 8.dp else 16.dp
                    )
                ) {
                    items(listaTickets, key = { it.id ?: "" }) { ticket ->
                        TicketItem(
                            ticket = ticket,
                            compacta = isLandscape,
                            onResolverClick = {
                                actionPendiente = TicketAction.ConfirmResolve(ticket)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Tarjeta visual de un ticket.
// Muestra tipo, identificador, fecha, usuario, descripción y botón para marcarlo como resuelto.
@Composable
fun TicketItem(
    ticket: Ticket,
    compacta: Boolean = false,
    onResolverClick: () -> Unit
) {
    // Color del ticket según su tipo para identificar rápido errores, sugerencias u otros casos.
    val colorTipo = when (ticket.tipo) {
        TipoTicket.ERROR -> MaterialTheme.colorScheme.error
        TipoTicket.SUGERENCIA -> Color(0xFF388E3C)
        TipoTicket.OTRO -> Color(0xFFF9A825)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = if (compacta) 10.dp else 16.dp
            )
        ) {
            // Fila superior con tipo, ID corto y fecha del ticket.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = colorTipo.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = ticket.tipo.name,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = colorTipo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "#${ticket.id?.take(5) ?: "---"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )

                Spacer(Modifier.weight(1f))

                Text(
                    text = ticket.fecha ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(if (compacta) 8.dp else 12.dp))

            // Información principal del ticket.
            Text(
                text = ticket.asunto ?: "Sin asunto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "De: ${ticket.usuarioNombre ?: "Anónimo"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(if (compacta) 6.dp else 8.dp))

            Text(
                text = ticket.descripcion ?: "No se proporcionó descripción.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(if (compacta) 10.dp else 16.dp))

            // Acción final del moderador: marca el ticket como resuelto tras confirmación.
            Button(
                onClick = onResolverClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorTipo),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = if (compacta) 8.dp else 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "MARCAR COMO RESUELTO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}