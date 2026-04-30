package com.example.tfg.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tfg.MainActivity
import com.example.tfg.data.network.RetrofitClient
import com.example.tfg.data.model.Huerto
import com.example.tfg.data.model.Cultivo
import com.example.tfg.data.model.Riego // 🚩 IMPORTANTE: Cambia esto a la ruta real de tu Enum si es diferente
import kotlinx.coroutines.*

class RiegoWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        ejecutarEscaneo(applicationContext)
    }

    companion object {
        private const val CHANNEL_ID = "CANAL_RIEGO_TF"
        private const val GROUP_KEY = "com.example.tfg.RIEGO_GROUP"
        private const val SUMMARY_ID = 999

        suspend fun lanzarNotificacionDemoRealista(context: Context) {
            withContext(Dispatchers.IO) {
                ejecutarEscaneo(context)
            }
        }

        private suspend fun ejecutarEscaneo(context: Context): Result = coroutineScope {
            val apiService = RetrofitClient.getApiService(context)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            crearCanalSiNoExiste(notificationManager)

            try {
                val huertosRes = apiService.obtenerHuertos()
                if (!huertosRes.isSuccessful) return@coroutineScope Result.retry()

                val listaHuertos = huertosRes.body() ?: emptyList()

                val resultados = listaHuertos.map { huerto ->
                    async {
                        val res = apiService.obtenerCultivosDelHuerto(huerto.id.toString())
                        if (res.isSuccessful) {
                            val cultivos = res.body() ?: emptyList()
                            huerto to cultivos
                        } else {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()

                var totalAlertas = 0

                resultados.forEach { (huerto, cultivos) ->
                    cultivos.forEach { cultivo ->

                        val tipoRiego = cultivo.infoCatalogo?.riego

                        if (tipoRiego == Riego.FRECUENTE || tipoRiego == Riego.MODERADO) {
                            enviarAlerta(
                                context,
                                notificationManager,
                                cultivo.id.hashCode(),
                                "Riego en ${huerto.nombre}",
                                "${cultivo.nombre} necesita agua"
                            )
                            totalAlertas++
                        }
                    }
                }

                if (totalAlertas == 0) {
                    enviarAlerta(context, notificationManager, 888, "Eco Drop", "Todo hidratado ✨")
                }

                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }

        private fun enviarAlerta(ctx: Context, mgr: NotificationManager, id: Int, tit: String, msg: String) {
            val intent = Intent(ctx, MainActivity::class.java)
            val pi = PendingIntent.getActivity(ctx, id, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(tit)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(GROUP_KEY)
                .setContentIntent(pi)
                .setAutoCancel(true)

            mgr.notify(id, builder.build())

            val summary = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build()
            mgr.notify(SUMMARY_ID, summary)
        }

        private fun crearCanalSiNoExiste(mgr: NotificationManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mgr.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Alertas Riego", NotificationManager.IMPORTANCE_HIGH)
                )
            }
        }
    }
}