package com.example.tfg.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.text.get

// Creamos la instancia de DataStore
private val Context.dataStore by preferencesDataStore(name = "auth_prefs")
// Esta clase sirve para guardar el token de acceso (JWT)
class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val USER_ID = stringPreferencesKey("user_id")
    }

    // Guardar el token cuando el login tiene éxito
    suspend fun saveToken(token: String, userId: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = token
            prefs[USER_ID] = userId
        }
    }

    // Recuperar el token
    val accessToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[ACCESS_TOKEN]
    }

    // Borrar al cerrar sesión (Logout)
    suspend fun clearAuth() {
        context.dataStore.edit { it.clear() }
    }
}