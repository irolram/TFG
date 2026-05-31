package com.example.tfg.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tfg.data.model.CatalogoDePlantas
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.model.Usuario
import java.io.BufferedReader
import java.io.InputStreamReader

// Función reutilizable para los campos de texto
@Composable
fun HuertoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

// Función reutilizable para la ficha tecnica de cada planta
@Composable
fun FichaTecnicaSimple(planta: CatalogoDePlantas) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📏 Especificaciones de siembra",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de items reutilizando el componente InfoItem
            val specs = listOf(
                Triple("Riego", planta.riego?.textoPantalla?: "N/A", Icons.Default.WaterDrop),
                Triple("Luz", planta.luzSolar?.textoPantalla ?: "N/A", Icons.Default.LightMode),
                Triple("Profundidad", planta.profundidadSiembra ?: "N/A", Icons.Default.Straighten),
                Triple("Crecimiento", "${planta.diasCrecimiento ?: "--"} días", Icons.Default.Timer),
                Triple("Separación entre plantas", "${planta.distanciaEntrePlantas ?: "--"} ", Icons.Default.WaterDrop),
                Triple("Estación ideal", planta.temporadaIdeal ?: "N/A", Icons.Default.CalendarMonth)
            )

            specs.forEach { (label, valor, icon) ->
                InfoItem(label = label, valor = valor, icon = icon)
            }
        }
    }
}

// Función reutilizable para los items de la ficha tecnica
@Composable
fun InfoItem(label: String, valor: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Función reutilizable para el huerto
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemHuerto(huerto: Huerto, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDeleteClick()
                false
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false, // Solo permitimos borrar deslizando a la izquierda
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
                    .background(color, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 16.dp).size(28.dp)
                )
            }
        },
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = huerto.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (huerto.descripcion.isNotEmpty()) {
                        Text(
                            text = huerto.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // El widget de clima se mantiene porque es funcional
                    WidgetClima(latitud = huerto.latitud, longitud = huerto.longitud)
                }
            }
        }
    )
}

// Función reutilizable para formatear la fecha
fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return "Sin fecha"

    return try {
        // Definimos el formato: dd/MM/yyyy
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Creamos el objeto fecha directamente con el Long
        val date = Date(timestamp)
        sdf.format(date)
    } catch (e: Exception) {
        "Fecha inválida"
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

// Función encargada de transformar los datos de los usuarios a formato tabla Excel (CSV)
fun generarCsvUsuarios(listaUsuarios: List<Usuario>): String {
    val builder = StringBuilder()

    // Cabeceras de las columnas (ajustado para que Excel lo lea bien)
    builder.append("ID,Nombre,Apellidos,Email,Rol\n")

    // Recorremos la lista de la base de datos fila por fila
    listaUsuarios.forEach { user ->

        val nombreLimpio = user.nombre.replace(",", "")
        val apellidosLimpios = user.apellidos?.replace(",", "") ?: ""

        // Añadimos los datos reales separados por coma y un salto de línea al final
        builder.append("${user.id},$nombreLimpio,$apellidosLimpios,${user.email},${user.rol}\n")
    }

    return builder.toString()
}