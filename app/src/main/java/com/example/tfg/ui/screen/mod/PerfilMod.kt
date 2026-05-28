package com.example.tfg.ui.screen.mod

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.Usuario
import com.example.tfg.ui.components.RiegoWorker
import kotlinx.coroutines.launch

// Pantalla de perfil del moderador.
// Muestra sus datos, permisos principales, selector de tema, escaneo de riego y cierre de sesión.
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PerfilModScreen(
    usuario: Usuario?,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    // Scope usado para lanzar la notificación de prueba sin bloquear la interfaz.
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val colorPrimario = MaterialTheme.colorScheme.primary
    val colorAcento = MaterialTheme.colorScheme.secondary

    // Detecta orientación horizontal para compactar cabecera y avatar.
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
        // Cabecera visual con avatar solapado, sin usar offset para que el scroll funcione bien.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(espacioCabeceraAvatar)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(alturaCabecera)
                    .background(colorPrimario)
                    .align(Alignment.TopCenter)
            )

            Surface(
                modifier = Modifier
                    .size(avatarSize)
                    .align(Alignment.BottomCenter),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(4.dp, MaterialTheme.colorScheme.background),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = usuario?.nombre?.take(1)?.uppercase() ?: "M",
                        style = if (isLandscape) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.displaySmall
                        },
                        fontWeight = FontWeight.Bold,
                        color = colorPrimario
                    )
                }
            }
        }

        // Datos principales del moderador: nombre, rol y email.
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
                    "Cargando perfil..."
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 24.dp))

        // Panel de herramientas y acciones del moderador.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
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
                    headlineContent = { Text("Gestión Tickets") },
                    supportingContent = { Text("Tienes permisos para editar tickets") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = colorPrimario
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                ListItem(
                    headlineContent = { Text("Tema Visual") },
                    supportingContent = { Text("Cambiar entre modo claro y oscuro") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = colorPrimario
                        )
                    },
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

            Spacer(modifier = Modifier.height(24.dp))

            // Lanza una notificación demo relacionada con el escaneo de riego.
            Button(
                onClick = {
                    scope.launch {
                        RiegoWorker.lanzarNotificacionDemoRealista(context)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("EJECUTAR ESCANEO DE RIEGO", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cierra la sesión del moderador.
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
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