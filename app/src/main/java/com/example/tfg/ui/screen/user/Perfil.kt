package com.example.tfg.ui.screen.user

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.Usuario

// Pantalla de perfil del usuario normal.
// Muestra datos personales, ajustes básicos, acceso a soporte y cierre de sesión.
@Composable
fun PerfilScreen(
    usuario: Usuario?,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onNavigateToSupport: () -> Unit
) {
    val verdeEco = Color(0xFF4CAF50)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val alturaCabecera = if (isLandscape) 72.dp else 120.dp
    val avatarSize = if (isLandscape) 64.dp else 96.dp
    val espacioCabeceraAvatar = alturaCabecera + (avatarSize / 2)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera con franja verde y avatar integrado sin offsets negativos.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(espacioCabeceraAvatar)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(alturaCabecera)
                    .background(verdeEco)
                    .align(Alignment.TopCenter)
            )

            Surface(
                modifier = Modifier
                    .size(avatarSize)
                    .align(Alignment.BottomCenter),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.background),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario?.nombre?.take(1)?.uppercase() ?: "U",
                        style = if (isLandscape) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.displaySmall
                        },
                        fontWeight = FontWeight.Bold,
                        color = verdeEco
                    )
                }
            }
        }

        // Información principal del usuario.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (usuario != null) {
                    "${usuario.nombre} ${usuario.apellidos ?: ""}"
                } else {
                    "Cargando..."
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (usuario != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = usuario.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 24.dp))

        // Opciones y acciones de la cuenta.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Ajustes de cuenta",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                ListItem(
                    headlineContent = { Text("Modo Oscuro") },
                    supportingContent = { Text("Cambiar el tema visual") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = verdeEco
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onDarkModeChange
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSupport() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = verdeEco
                    )

                    Spacer(Modifier.width(16.dp))

                    Text(
                        text = "Contactar con Soporte",
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.weight(1f))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cierra la sesión del usuario.
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text("Cerrar Sesión")
            }
        }
    }
}