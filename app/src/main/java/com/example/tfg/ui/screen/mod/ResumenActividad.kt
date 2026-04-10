package com.example.tfg.ui.screen.mod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.ui.screen.user.verdeEco

@Composable
fun ResumenActividadContent() {
    val colorMod = Color(0xFF1976D2) // Azul para identificar al Moderador

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. BIENVENIDA ---
        item {
            Column {
                Text(
                    text = "Estado de la Comunidad",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black
                )
                Text(
                    text = "Resumen de las últimas 24 horas",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        // --- 2. TARJETAS DE ESTADO RÁPIDO ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResumenCard(
                    titulo = "Nuevos Huertos",
                    valor = "+12",
                    icono = Icons.Default.Eco,
                    color = verdeEco,
                    modifier = Modifier.weight(1f)
                )
                ResumenCard(
                    titulo = "Nuevos Users",
                    valor = "+5",
                    icono = Icons.Default.People,
                    color = colorMod,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- 3. SECCIÓN DE ALERTAS/PENDIENTES ---
        item {
            Text(
                text = "Acciones Pendientes",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            AlertaOperativaCard(
                mensaje = "Hay 3 plantas sin descripción en el catálogo.",
                botonTexto = "Revisar",
                color = Color(0xFFFFA000) // Ámbar
            )
        }

        // --- 4. LISTA DE ACTIVIDAD RECIENTE ---
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Actividad Reciente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Simulamos una lista de actividad
        items(5) { index ->
            ItemActividadReciente(
                usuario = "Usuario ${index + 20}",
                accion = "ha plantado un nuevo cultivo de Tomates.",
                hora = "${index + 1}h ago"
            )
        }
    }
}

// --- COMPONENTES INTERNOS ---

@Composable
fun ResumenCard(titulo: String, valor: String, icono: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icono, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(text = valor, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = titulo, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}

@Composable
fun AlertaOperativaCard(mensaje: String, botonTexto: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.NotificationsActive, null, tint = color)
            Spacer(Modifier.width(12.dp))
            Text(text = mensaje, modifier = Modifier.weight(1f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            TextButton(onClick = { /* Ir a catálogo */ }) {
                Text(botonTexto, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ItemActividadReciente(usuario: String, accion: String, hora: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(verdeEco, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    // Parte en negrita (el nombre del usuario)
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(usuario)
                    }
                    // Parte normal (la acción)
                    append(" $accion")
                },
                fontSize = 14.sp
            )
            Text(text = hora, fontSize = 12.sp, color = Color.Gray)
        }
    }
}