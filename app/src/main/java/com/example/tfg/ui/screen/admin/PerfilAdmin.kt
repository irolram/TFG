package com.example.tfg.ui.screen.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.Usuario

@Composable
fun PerfilAdminScreen(
    usuario: Usuario?,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    // 🚩 OPTIMIZACIÓN 1: Uso de tokens del sistema en lugar de Hardcoded Colors
    // Esto asegura que si cambias el color primario en el Theme, todo cambie aquí también.
    val colorPrimario = MaterialTheme.colorScheme.primary
    val colorAdmin = MaterialTheme.colorScheme.error // El rojo de error suele ser el mejor para Admin por contraste

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Franja superior dinámica
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
            // Avatar con solapamiento corregido
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
                        text = usuario?.nombre?.take(1)?.uppercase() ?: "A",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = colorPrimario
                    )
                }
            }

            // Información de Usuario
            Column(
                modifier = Modifier.offset(y = (-40).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (usuario != null) "${usuario.nombre} ${usuario.apellidos ?: ""}" else "Cargando...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badge Admin optimizado
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorAdmin.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, colorAdmin.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "ADMINISTRADOR",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = colorAdmin,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = usuario?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Panel de Ajustes
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp)
        ) {
            Text(
                text = "Configuración del Sistema",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                // Item: Privilegios
                ListItem(
                    headlineContent = { Text("Privilegios de Root") },
                    supportingContent = { Text("Acceso total de lectura/escritura") },
                    leadingContent = { Icon(Icons.Default.Shield, null, tint = colorAdmin) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Item: Modo Oscuro
                ListItem(
                    headlineContent = { Text("Modo Oscuro") },
                    supportingContent = { Text("Ajustar interfaz para la noche") },
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

            // Botón Logout mejorado
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}