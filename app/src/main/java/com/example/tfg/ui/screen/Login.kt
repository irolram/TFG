package com.example.tfg.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.model.Rol
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.components.HuertoTextField
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Reutilizamos la paleta de colores del huerto
val VerdeFondoInicio = Color(0xFFE8F5E9)
val VerdeFondoFin = Color(0xFFC8E6C9)
val VerdePrimario = Color(0xFF2E7D32)
val VerdeSecundario = Color(0xFF43A047)
val ColorTierra = Color(0xFF795548)

@Composable
fun LoginEcoDropScreen(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Instanciamos nuestro gestor de tokens
    val tokenManager = remember { TokenManager(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VerdeFondoInicio, VerdeFondoFin)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título estilizado
            Text(
                text = "ECO DROP",
                color = VerdePrimario,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Tu huerto en la palma de tu mano",
                color = ColorTierra,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Campos con el estilo del huerto
            HuertoTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                placeholder = "Email de cultivador",
                icon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            HuertoTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                placeholder = "Contraseña",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(color = VerdePrimario)
            } else {
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            errorMessage = "¡Faltan semillas! Rellena los campos"
                            return@Button
                        }

                        scope.launch {
                            isLoading = true
                            errorMessage = null

                            try {
                                // 1. Login en Firebase
                                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                                val firebaseUser = authResult.user

                                if (firebaseUser != null) {
                                    val uid = firebaseUser.uid
                                    val correo = firebaseUser.email ?: ""

                                    // 2. Pedir el Token y Rol a Railway
                                    val loginRequest = LoginRequest(userId = uid, email = correo)
                                    val response = RetrofitClient.getApiService(context).loginConServidor(loginRequest)

                                    if (response.isSuccessful) {
                                        val authData = response.body()

                                        if (authData != null) {
                                            // 3. GUARDAMOS EL TOKEN (Crucial para las siguientes peticiones)
                                            tokenManager.saveToken(authData.accessToken, authData.userId)

                                            // 4. Decidimos la ruta según el Rol
                                            val rutaDestino = if (authData.rol == "ADMIN") {
                                                "main_menuAdmin"
                                            } else {
                                                "main_menuUser"
                                            }

                                            // 5. Navegamos
                                            navController.navigate(rutaDestino) {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        errorMessage = "Error en el servidor EcoDrop. Verifica tu cuenta."
                                        Log.e("LOGIN", "Error API: ${response.code()} - ${response.errorBody()?.string()}")
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error al entrar al huerto: Credenciales incorrectas o fallo de red."
                                Log.e("LOGIN", "Excepción: ${e.localizedMessage}")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VerdePrimario),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Entrar al Huerto", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row {
                Text(text = "¿No tienes cuenta? ", color = ColorTierra, fontSize = 14.sp)
                Text(
                    text = "Regístrate aquí",
                    color = VerdeSecundario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { navController.navigate("register") }
                )
            }
        }
    }
}