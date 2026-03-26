package com.example.tfg.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg.ui.screens.VerdePrimario
import com.example.tfg.ui.screens.VerdeSecundario
import com.example.tfg.data.model.CatalogoDePlantas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HuertoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.Gray) },
            leadingIcon = { Icon(imageVector = icon, contentDescription = null, tint = VerdePrimario) },
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VerdeSecundario,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color.Black
            )
        )
    }
}

@Composable
fun FichaTecnicaSimple(planta: PlantaCatalogo) { // 🚩 Asegúrate que el tipo coincide (PlantaCatalogo)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📏 Especificaciones de siembra",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🚩 Ahora sacamos todo de 'planta'
            InfoItem(label = "Riego:", valor = planta.riego ?: "N/A", icon = Icons.Default.WaterDrop)
            InfoItem(label = "Luz:", valor = planta.luzSolar ?: "N/A", icon = Icons.Default.LightMode)
            InfoItem(label = "Profundidad:", valor = planta.profundidadSiembra ?: "N/A", icon = Icons.Default.Straighten)
            InfoItem(label = "Crecimiento:", valor = "${planta.diasCrecimiento ?: "--"} días", icon = Icons.Default.Timer)
        }
    }
}

@Composable
fun SeccionInstrucciones(instrucciones: String?) { // para mas adelante para personalizar las instrucciones

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MenuBook, null, tint = Color(0xFF1B5E20), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Manual de cultivo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1B5E20)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = instrucciones ?: "Cargando consejos de expertos para esta planta...",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun InfoItem(label: String, valor: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color(0xFF4CAF50))
        Spacer(Modifier.width(8.dp))
        Text(text = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Text(text = valor, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}