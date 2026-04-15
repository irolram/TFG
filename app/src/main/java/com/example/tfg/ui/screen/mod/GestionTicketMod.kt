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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionTicketsScreen(
    listaTickets: List<Ticket>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onResolverTicket: (String) -> Unit
) {
    val colorMod = Color(0xFF00796B)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF4F7F6))) {

        // Cabecera
        Box(modifier = Modifier.fillMaxWidth().background(colorMod).padding(20.dp)) {
            Column {
                Text("SOPORTE TÉCNICO", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Gestiona fallos y sugerencias de los usuarios", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            if (listaTickets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Bandeja de entrada vacía. ¡Buen trabajo!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(listaTickets) { ticket ->
                        TicketItem(ticket = ticket, onResolver = { ticket.id?.let { onResolverTicket(it) } })
                    }
                }
            }
        }
    }
}

@Composable
fun TicketItem(ticket: Ticket, onResolver: () -> Unit) {
    // 🚩 Definimos el color aquí para que no de error de compilación
    val colorMod = Color(0xFF00796B)

    val colorTipo = when(ticket.tipo) {
        TipoTicket.ERROR -> Color(0xFFD32F2F)
        TipoTicket.SUGERENCIA -> Color(0xFF388E3C)
        TipoTicket.OTRO -> Color(0xFFF9A825)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = colorTipo.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = ticket.tipo.name, // El Enum no suele ser nulo, pero ojo
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = colorTipo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))

                // 🚩 SEGURIDAD: Si el id es nulo, ponemos "00000"
                Text(
                    text = "Ticket #${ticket.id?.take(5) ?: "00000"}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(Modifier.weight(1f))

                // 🚩 SEGURIDAD: Si la fecha es nula, ponemos texto vacío
                Text(
                    text = ticket.fecha ?: "",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // 🚩 SEGURIDAD: Asunto y UsuarioNombre
            Text(
                text = ticket.asunto ?: "Sin asunto",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Enviado por: ${ticket.usuarioNombre ?: "Usuario desconocido"}",
                fontSize = 13.sp,
                color = colorMod // Corregido el nombre de la variable
            )

            Spacer(Modifier.height(8.dp))

            // 🚩 SEGURIDAD: Descripción
            Text(
                text = ticket.descripcion ?: "Sin descripción adicional",
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onResolver,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = colorTipo),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("MARCAR COMO RESUELTO")
            }
        }
    }
}