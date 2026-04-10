package com.example.tfg.ui.screen.admin


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.data.model.Usuario

@Composable
fun PerfilAdminScreen(
    usuario: Usuario?,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val verdeEco = Color(0xFF4CAF50)
    val colorAdmin = Color(0xFFD32F2F) // Rojo para destacar el poder de Admin

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        // Franja superior
        Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(verdeEco))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(100.dp).offset(y = (-50).dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.background),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario?.nombre?.take(1)?.uppercase() ?: "A",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = verdeEco
                    )
                }
            }

            // Nombre
            Text(
                text = if (usuario != null) "${usuario.nombre} ${usuario.apellidos ?: ""}" else "Cargando...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-40).dp)
            )

            // BADGE ADMIN
            Surface(
                modifier = Modifier.offset(y = (-35).dp),
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

            Text(
                text = usuario?.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.offset(y = (-30).dp)
            )
        }

        // Ajustes
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).offset(y = (-15).dp)) {
            Text("Panel de Control", style = MaterialTheme.typography.titleSmall, color = Color.Gray)

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                ListItem(
                    headlineContent = { Text("Privilegios de Admin") },
                    supportingContent = { Text("Acceso total a la base de datos") },
                    leadingContent = { Icon(Icons.Default.Shield, null, tint = colorAdmin) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Modo Oscuro") },
                    supportingContent = { Text("Cambiar el tema visual") },
                    leadingContent = { Icon(Icons.Default.Person, null, tint = verdeEco) },
                    trailingContent = {
                        Switch(checked = isDarkMode, onCheckedChange = onDarkModeChange)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

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