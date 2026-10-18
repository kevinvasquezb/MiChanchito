package com.example.michanchito.data.notificaciones

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.michanchito.MainActivity
import com.example.michanchito.R
import com.example.michanchito.domain.usecase.Fechas
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/*
 * NOTIFICACIONES con WorkManager.
 *
 *  - Un canal ("avisos_diarios"): Android lo exige desde la versión 8.
 *  - Permiso POST_NOTIFICATIONS: lo pide el usuario desde Mi perfil (Android 13 o superior).
 *  - programarDiarios(): WorkManager corre AvisosWorker una vez al día, cerca de las 9:00,
 *    aunque la app esté cerrada. Android puede moverlo unos minutos para ahorrar batería.
 */
object NotificacionesHelper {

    private const val CANAL = "avisos_diarios"
    private const val TRABAJO_DIARIO = "avisos_diarios"
    private const val HORA_AVISO = 9   // 9 de la mañana

    /** En Android 13+ hace falta permiso; antes, no. */
    fun tienePermiso(contexto: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Muestra una notificación. Tocarla abre la app. */
    fun mostrar(contexto: Context, id: Int, titulo: String, texto: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        crearCanal(contexto)

        val abrirApp = PendingIntent.getActivity(
            contexto, 0, Intent(contexto, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))   // texto completo al desplegarla
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(contexto).notify(id, notificacion)
    }

    private fun crearCanal(contexto: Context) {
        val canal = NotificationChannel(CANAL, "Avisos de Mi Chanchito", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Límites de gasto del mes"
        }
        contexto.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    // =========================== PROGRAMACIÓN (WorkManager) ===========================

    /**
     * Programa el aviso diario. [reiniciar] = true vuelve a calcular la hora (al activar el switch);
     * false solo se asegura de que exista (al abrir la app).
     */
    fun programarDiarios(contexto: Context, reiniciar: Boolean) {
        val trabajo = PeriodicWorkRequestBuilder<AvisosWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(minutosHastaLaProximaHora(), TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
            TRABAJO_DIARIO,
            if (reiniciar) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
            trabajo
        )
    }

    fun cancelarDiarios(contexto: Context) {
        WorkManager.getInstance(contexto).cancelUniqueWork(TRABAJO_DIARIO)
    }

    /** Botón "Probar avisos ahora" de Mi perfil: corre el Worker una sola vez, enseguida. */
    fun probarAhora(contexto: Context) {
        WorkManager.getInstance(contexto).enqueue(OneTimeWorkRequestBuilder<AvisosWorker>().build())
    }

    /** Minutos que faltan para las próximas 9:00 de Bolivia (hoy si todavía no pasaron; si no, mañana). */
    private fun minutosHastaLaProximaHora(): Long {
        val ahora = ZonedDateTime.now(Fechas.ZONA)
        var proxima = ahora.toLocalDate().atTime(LocalTime.of(HORA_AVISO, 0)).atZone(Fechas.ZONA)
        if (!proxima.isAfter(ahora)) proxima = proxima.plusDays(1)
        return Duration.between(ahora, proxima).toMinutes().coerceAtLeast(1)
    }
}
