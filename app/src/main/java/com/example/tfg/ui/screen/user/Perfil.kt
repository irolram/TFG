package com.example.tfg.ui.screen.user
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController


@Composable
fun PerfilScreen(
    usuario: com.example.tfg.data.model.Usuario?,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigateToSupport: () -> Unit) {
    // Usamos el verde de tu marca EcoDrop
    val verdeEco = Color(0xFF4CAF50)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1️⃣ LA FRANJA VERDE (Solo el borde de arriba)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Altura de la franja
                .background(verdeEco)
        )

        // 2️⃣ INFO DEL USUARIO (Solapada y fuera del verde)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar con borde para que destaque sobre el verde
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .offset(y = (-50).dp), // Lo subimos la mitad de su tamaño
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.background),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario?.nombre?.take(1)?.uppercase() ?: "U",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = verdeEco
                    )
                }
            }

            // Nombre y Apellido (Ya sobre el fondo normal de la app)
            Text(
                text = if (usuario != null) "${usuario.nombre} ${usuario.apellidos ?: ""}" else "Cargando...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-40).dp) // Ajustamos posición por el offset anterior
            )

            if (usuario != null) {
                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.offset(y = (-35).dp)
                )
            }
        }

        // 3️⃣ LISTA DE OPCIONES (Cuerpo de la pantalla)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp) // Subimos un poco para compensar huecos
        ) {
            Text(
                text = "Ajustes de cuenta",
                style = MaterialTheme.typography.titleSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                ListItem(
                    headlineContent = { Text("Modo Oscuro") },
                    supportingContent = { Text("Cambiar el tema visual") },
                    leadingContent = { Icon(Icons.Default.Person, null, tint = verdeEco) },
                    trailingContent = {
                        Switch(checked = isDarkMode, onCheckedChange = onDarkModeChange)
                    }
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToSupport() },
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = verdeEco)
                    Spacer(Modifier.width(16.dp))
                    Text("Contactar con Soporte", fontWeight = FontWeight.Medium)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Logout
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}