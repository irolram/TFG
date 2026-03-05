package com.example.tfg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tfg.ui.theme.TFGTheme
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TFGTheme {

                val db = Firebase.firestore // Esta línea "enciende" la conexión con la nube

                db.collection("huertos")
                    .get()
                    .addOnSuccessListener { resultado ->
                        for (documento in resultado) {
                            // Esto imprimirá en la consola de Android Studio lo que pusiste en la web
                            Log.d("FIREBASE_TEST", "Huerto leído: ${documento.data["nombre"]}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("FIREBASE_TEST", "Error al conectar: ", exception)
                    }
            }
        }
    }
}