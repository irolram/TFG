package com.example.tfg.ui.screen.admin

import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.Usuario
import com.example.tfg.ui.components.generarCsvUsuarios
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Funcion que gestiona el perfil del administrador
@Composable
fun PerfilAdminScreen(
    usuario: Usuario?,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    listaUsuarios: List<Usuario>
) {
    val colorPrimario = MaterialTheme.colorScheme.primary
    val colorAdmin = MaterialTheme.colorScheme.error

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val alturaCabecera = if (isLandscape) 72.dp else 120.dp
    val avatarSize = if (isLandscape) 64.dp else 96.dp
    val espacioCabeceraAvatar = alturaCabecera + (avatarSize / 2)

    val tablaUsuarios = generarCsvUsuarios(listaUsuarios)

    val fechaActual = SimpleDateFormat(
        "dd/MM/yyyy HH:mm:ss",
        Locale.getDefault()
    ).format(Date())

    val informeCompleto = """
        === INFORME ECO DROP ===
        Rol Activo: ADMINISTRADOR
        Estado de la base de datos: ONLINE
        Fecha de exportación: $fechaActual
        
        $tablaUsuarios
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
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
                        text = usuario?.nombre?.take(1)?.uppercase() ?: "A",
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (usuario != null) "${usuario.nombre} ${usuario.apellidos ?: ""}" else "Cargando...",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

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

        Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
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
                ListItem(
                    headlineContent = { Text("Privilegios de Root") },
                    supportingContent = { Text("Acceso total de lectura/escritura") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = colorAdmin
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                ListItem(
                    headlineContent = { Text("Modo Oscuro") },
                    supportingContent = { Text("Ajustar interfaz para la noche") },
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

            PanelGestionFicheros(textoAExportar = informeCompleto)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}
// Funcion que gestiona la exportación de datos locales
@Composable
fun PanelGestionFicheros(
    textoAExportar: String
) {
    val context = LocalContext.current

    val exportarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(textoAExportar.toByteArray(Charsets.UTF_8))
                outputStream.flush()
            }

            Toast.makeText(context, "Archivo exportado correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    val importarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }

            Toast.makeText(context, "Archivo leído correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al leer: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Exportación de Datos Locales",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { exportarLauncher.launch("Log_Sistema_EcoDrop.txt") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Exportar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar")
                }

                FilledTonalButton(
                    onClick = { importarLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Leer",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Leer")
                }
            }
        }
    }
}