package com.example.tfg

import com.google.firebase.firestore.FirebaseFirestore

fun probarConexionFirestore() {
    val db = FirebaseFirestore.getInstance()

    db.collection("huertos")
        .get()
        .addOnSuccessListener { documentos ->
            for (documento in documentos) {
                // Si esto sale en el Logcat, ¡estás conectado!
                println("Huerto encontrado: ${documento.data["nombre"]}")
            }
        }
        .addOnFailureListener { exception ->
            println("Error al conectar: $exception")
        }
}