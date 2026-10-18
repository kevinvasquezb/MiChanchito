package com.example.michanchito.domain.usecase

import com.example.michanchito.domain.model.Categorias
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.Presupuesto
import java.time.LocalDate
import java.time.YearMonth

/** Un aviso: el título va en negrita (y como título de la notificación), el texto debajo. */
data class Aviso(
    val titulo: String,
    val texto: String,
    val urgente: Boolean      // rojo en Inicio; en notificaciones no cambia nada
)

/*
 * LOS MISMOS AVISOS SE USAN EN DOS LUGARES:
 *   - en Inicio (recuadros rojos arriba del saldo)
 *   - en las notificaciones diarias (AvisosWorker)
 * Por eso están aquí, en domain, sin nada de Android.
 */
object Avisos {

    /** Avisos de límites: al 80 % y al pasarse, con cuánto queda y cuántos días faltan para fin de mes. */
    fun limites(presupuestos: List<Presupuesto>, movimientos: List<Movimiento>, hoy: LocalDate): List<Aviso> {
        val resumen = CalculosFinanzas.resumenDelMes(movimientos, YearMonth.from(hoy))
        val diasQueFaltan = hoy.lengthOfMonth() - hoy.dayOfMonth
        val cierre = when (diasQueFaltan) {
            0 -> "Hoy termina el mes."
            1 -> "Falta 1 día para fin de mes."
            else -> "Faltan $diasQueFaltan días para fin de mes."
        }

        return presupuestos.mapNotNull { limite ->
            val gastado = resumen.gastosPorCategoria[limite.categoriaId] ?: 0L
            val nombre = Categorias.buscar(limite.categoriaId).nombre
            when {
                gastado >= limite.limiteCentavos -> Aviso(
                    titulo = "Te pasaste del límite de $nombre.",
                    texto = "Llevas ${Dinero.formatear(gastado)} de ${Dinero.formatear(limite.limiteCentavos)}. $cierre",
                    urgente = true
                )
                gastado * 100 >= limite.limiteCentavos * 80 -> Aviso(
                    titulo = "Ya usaste más del 80 % del límite de $nombre.",
                    texto = "Te quedan ${Dinero.formatear(limite.limiteCentavos - gastado)}. $cierre",
                    urgente = true
                )
                else -> null
            }
        }
    }
}
