package com.example.tfg.ui.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.tfg.data.TokenManager
import com.example.tfg.data.model.LoginRequest
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.ui.components.HuertoTextField
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Pantalla de inicio de sesión.
// Autentica con Firebase, pide token/rol al servidor y redirige según el rol del usuario.
@Composable
fun LoginEcoDropScreen(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Gestor encargado de guardar el token JWT y el userId localmente.
    val tokenManager = remember { TokenManager(context) }

    // Campos del formulario. rememberSaveable evita perderlos al girar la pantalla.
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = if (isLandscape) 48.dp else 32.dp)
                .padding(vertical = if (isLandscape) 20.dp else 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (isLandscape) {
                Arrangement.Top
            } else {
                Arrangement.Center
            }
        ) {
            // Marca y subtítulo de la app.
            Text(
                text = "ECO DROP",
                color = MaterialTheme.colorScheme.primary,
                fontSize = if (isLandscape) 36.sp else 48.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Tu huerto en la palma de tu mano",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontSize = if (isLandscape) 14.sp else 16.sp,
                modifier = Modifier.padding(bottom = if (isLandscape) 24.dp else 48.dp)
            )

            // Formulario de email y contraseña.
            HuertoTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                placeholder = "Email de cultivador",
                icon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            HuertoTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = "Contraseña",
                icon = Icons.Default.Lock,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 16.dp else 24.dp))

            // Mensaje de error si falla validación, Firebase o backend.
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                                // 1. Login contra Firebase.
                                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                                val firebaseUser = authResult.user

                                if (firebaseUser != null) {
                                    val uid = firebaseUser.uid
                                    val correo = firebaseUser.email ?: ""

                                    // 2. Login contra el servidor para recibir token y rol.
                                    val loginRequest = LoginRequest(userId = uid, email = correo)
                                    val response = RetrofitClient
                                        .getApiService(context)
                                        .loginConServidor(loginRequest)

                                    if (response.isSuccessful) {
                                        val authData = response.body()

                                        if (authData != null) {
                                            // 3. Guarda token JWT y userId.
                                            tokenManager.saveToken(
                                                authData.accessToken,
                                                authData.userId
                                            )

                                            // 4. Decide pantalla inicial según rol.
                                            val rutaDestino = when (authData.rol) {
                                                "ADMIN" -> "main_menuAdmin"
                                                "MOD" -> "main_menuMod"
                                                else -> "main_menuUser"
                                            }

                                            // 5. Navega limpiando login del back stack.
                                            navController.navigate(rutaDestino) {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                    } else {
                                        errorMessage = "Error en el servidor EcoDrop. Verifica tu cuenta."
                                        Log.e(
                                            "LOGIN",
                                            "Error API: ${response.code()} - ${response.errorBody()?.string()}"
                                        )
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
                        .height(if (isLandscape) 52.dp else 60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "Entrar al Huerto",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = if (isLandscape) 18.sp else 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isLandscape) 24.dp else 40.dp))

            // Enlace a registro para usuarios nuevos.
            Row {
                Text(
                    text = "¿No tienes cuenta? ",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                Text(
                    text = "Regístrate aquí",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("register")
                    }
                )
            }
        }
    }
}