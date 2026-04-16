package com.example.tfg.ui.screen.mod

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.Usuario
import com.example.tfg.ui.components.RiegoWorker
import kotlinx.coroutines.launch

@Composable
fun PerfilModScreen(
    usuario: Usuario?,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // 🚩 OPTIMIZACIÓN: Usamos la paleta de colores del Tema actual (Mod = Teal)
    val colorPrimario = MaterialTheme.colorScheme.primary
    val colorAcento = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- CABECERA DINÁMICA ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(colorPrimario)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar solapado
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .offset(y = (-50).dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.background),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario?.nombre?.take(1)?.uppercase() ?: "M",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = colorPrimario
                    )
                }
            }

            // Nombre y Rol
            Column(
                modifier = Modifier.offset(y = (-40).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (usuario != null) "${usuario.nombre} ${usuario.apellidos ?: ""}" else "Cargando perfil...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badge de Moderador
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorAcento.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "MODERADOR",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorAcento,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = usuario?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- PANEL DE HERRAMIENTAS ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp)
        ) {
            Text(
                text = "Gestión de Moderación",
                style = MaterialTheme.typography.titleSmall,
                color = colorPrimario,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                ListItem(
                    headlineContent = { Text("Catálogo de Plantas") },
                    supportingContent = { Text("Tienes permisos para editar fichas técnicas") },
                    leadingContent = { Icon(Icons.Default.EditNote, null, tint = colorPrimario) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

                ListItem(
                    headlineContent = { Text("Tema Visual") },
                    supportingContent = { Text("Cambiar entre modo claro y oscuro") },
                    leadingContent = { Icon(Icons.Default.Palette, null, tint = colorPrimario) },
                    trailingContent = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onDarkModeChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = colorPrimario)
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- BOTÓN DE ESCANEO (DEMO) ---
            Button(
                onClick = {
                    scope.launch {
                        RiegoWorker.lanzarNotificacionDemoRealista(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.NotificationsActive, null)
                Spacer(Modifier.width(8.dp))
                Text("EJECUTAR ESCANEO DE RIEGO", fontWeight = FontWeight.Bold)
            }

            // --- CERRAR SESIÓN ---
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión")
            }
        }
    }
}