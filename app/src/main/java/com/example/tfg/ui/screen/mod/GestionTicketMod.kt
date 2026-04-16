package com.example.tfg.ui.screen.mod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.data.model.Ticket
import com.example.tfg.data.model.TipoTicket

// 🚩 OPTIMIZACIÓN 1: Estado sellado para acciones del moderador
sealed class TicketAction {
    data object None : TicketAction()
    data class ConfirmResolve(val ticket: Ticket) : TicketAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionTicketsScreen(
    listaTickets: List<Ticket>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onResolverTicket: (String) -> Unit
) {
    var actionPendiente by remember { mutableStateOf<TicketAction>(TicketAction.None) }

    // 🚩 OPTIMIZACIÓN 2: Diálogo de confirmación (Seguridad de estado)
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
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { actionPendiente = TicketAction.None }) { Text("Cancelar") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Cabecera que hereda el color Teal del Tema Mod
        Surface(color = MaterialTheme.colorScheme.primary, shadowElevation = 4.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp).statusBarsPadding()) {
                Text(
                    text = "CENTRO DE SOPORTE",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Bandeja de entrada de incidencias",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (listaTickets.isEmpty() && !isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text("No hay tickets pendientes", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 🚩 OPTIMIZACIÓN 3: Key única para mejorar el rendimiento del scroll
                    items(listaTickets, key = { it.id ?: "" }) { ticket ->
                        TicketItem(
                            ticket = ticket,
                            onResolverClick = { actionPendiente = TicketAction.ConfirmResolve(ticket) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicketItem(ticket: Ticket, onResolverClick: () -> Unit) {
    // Colores semánticos según el tipo
    val colorTipo = when(ticket.tipo) {
        TipoTicket.ERROR -> MaterialTheme.colorScheme.error
        TipoTicket.SUGERENCIA -> Color(0xFF388E3C) // Verde éxito
        TipoTicket.OTRO -> Color(0xFFF9A825) // Ámbar
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: Tipo, ID y Fecha
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

            Spacer(Modifier.height(12.dp))

            // Cuerpo del Ticket
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

            Spacer(Modifier.height(8.dp))

            Text(
                text = ticket.descripcion ?: "No se proporcionó descripción.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(16.dp))

            // Botón de Acción
            Button(
                onClick = onResolverClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorTipo),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("MARCAR COMO RESUELTO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}