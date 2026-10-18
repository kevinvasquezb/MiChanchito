package com.example.michanchito.data.notificaciones

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.michanchito.data.database.aMovimiento
import com.example.michanchito.data.database.aPresupuesto
import com.example.michanchito.domain.usecase.Avisos
import com.example.michanchito.domain.usecase.Fechas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/*
 * EL TRABAJO DIARIO: lo ejecuta WorkManager aunque la app esté cerrada.
 *
 *   1. ¿Hay sesión? Si no, no hay nada que avisar.
 *   2. Lee de Firestore los límites y los movimientos del usuario
 *      (si no hay internet, Firestore responde con la copia del celular).
 *   3. Calcula los avisos con la MISMA regla que usa Inicio (domain/usecase/Avisos.kt).
 *   4. Muestra una notificación por aviso.
 *
 * CoroutineWorker = un Worker que puede usar suspend/await.
 */
class AvisosWorker(contexto: Context, parametros: WorkerParameters) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
        if (!NotificacionesHelper.tienePermiso(applicationContext)) return Result.success()

        return try {
            val usuario = FirebaseFirestore.getInstance().collection("usuarios").document(uid)
            val movimientos = usuario.collection("movimientos").get().await().documents.map { it.aMovimiento() }
            val presupuestos = usuario.collection("presupuestos").get().await().documents.map { it.aPresupuesto() }

            val avisos = Avisos.limites(presupuestos, movimientos, Fechas.hoy())
            avisos.take(MAXIMO_NOTIFICACIONES).forEachIndexed { indice, aviso ->
                NotificacionesHelper.mostrar(applicationContext, PRIMER_ID + indice, aviso.titulo, aviso.texto)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()   // sin datos todavía: WorkManager lo intenta de nuevo más tarde
        }
    }

    private companion object {
        const val PRIMER_ID = 100
        const val MAXIMO_NOTIFICACIONES = 6
    }
}
