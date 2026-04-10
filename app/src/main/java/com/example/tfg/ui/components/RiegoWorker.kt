package com.example.tfg.ui.components


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tfg.MainActivity
import com.example.tfg.R
import com.example.tfg.data.network.RetrofitClient

class RiegoWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private fun necesitaNotificacion(tipoRiego: String): Boolean {
        val riego = tipoRiego.lowercase()
        return riego.contains("frecuente") || riego.contains("moderado")
    }
    override suspend fun doWork(): Result {
        val apiService = RetrofitClient.getApiService(applicationContext)

        return try {
            // 1. Usamos tu método real para traer la lista de huertos
            val huertosResponse = apiService.obtenerHuertos()

            if (huertosResponse.isSuccessful) {
                val listaHuertos = huertosResponse.body() ?: emptyList()

                for (huerto in listaHuertos) {
                    // 2. Usamos tu método para traer los cultivos de ESTE huerto concreto
                    // Pasamos el huerto.id que sacamos del bucle anterior
                    val cultivosResponse = apiService.obtenerCultivosDelHuerto(huerto.id.toString())

                    if (cultivosResponse.isSuccessful) {
                        val listaCultivos = cultivosResponse.body() ?: emptyList()

                        for (cultivo in listaCultivos) {

                            val tipoRiego = cultivo.infoCatalogo?.riego ?: "desconocido"

                            if (necesitaNotificacion(tipoRiego)) {
                                lanzarNotificacionIndividual(
                                    cultivo.id.hashCode(),
                                    "Riego en ${huerto.nombre} ",
                                    "${cultivo.nombre} necesita agua (Riego: ${tipoRiego})"
                                )
                            }
                        }
                    }
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    // Función auxiliar para filtrar (ajústala según lo que tengas en tu DB)


    @RequiresApi(Build.VERSION_CODES.O)
    private fun lanzarNotificacion(titulo: String, mensaje: String) {
        val channelId = "CANAL_RIEGO_TF"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        // 1. Forzamos importancia ALTA para que suene y salga el banner
        val channel = NotificationChannel(
            channelId,
            "Recordatorios de Riego",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Canal para avisos de riego"
        }
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            // 2. USA UN ICONO DEL SISTEMA (esto no falla nunca)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            // 3. PRIORIDAD ALTA
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent) // Al tocarla, abre la App
            .setAutoCancel(true) // La notificación desaparece al tocarla

        notificationManager.notify(101, builder.build())
    }
    private fun lanzarNotificacionIndividual(notificationId: Int, titulo: String, mensaje: String) {
        val channelId = "CANAL_RIEGO_TF"
        val groupKey = "com.example.tfg.RIEGO_GROUP" // 🚩 La llave del grupo
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Intent para abrir la App
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, notificationId, intent, PendingIntent.FLAG_IMMUTABLE)

        // 2. La notificación individual (ahora con setGroup)
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupKey) // 🚩 IMPORTANTE: Todas las de riego llevan la misma llave
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())

        // 3. Crear la NOTIFICACIÓN DE SUMARIO (La que agrupa a todas)
        val summaryNotification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Eco Drop: Alertas de Riego")
            .setContentText("Tienes varios cultivos que necesitan agua")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(999, summaryNotification) // ID fijo para el sumario
    }
}