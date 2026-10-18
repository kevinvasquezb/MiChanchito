package com.example.michanchito.domain.usecase

import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/*
 * TODA LA MATEMÁTICA DE LA APP ESTÁ AQUÍ.
 * Son funciones puras: reciben listas y devuelven números.
 * No saben nada de Firebase ni de pantallas, por eso se pueden probar con JUnit.
 *
 *   saldo total  = saldo inicial + ingresos − gastos
 *   reservado    = lo ahorrado en todas las metas
 *   disponible   = saldo total − reservado
 *
 * Nada de esto se guarda en Firestore: se calcula siempre, así nunca queda desactualizado.
 */

/** Resumen de un mes (para Inicio, Límites y Reportes). */
data class ResumenMes(
    val ingresosCentavos: Long,
    val gastosCentavos: Long,
    val gastosPorCategoria: Map<String, Long>   // categoriaId -> centavos
) {
    val resultadoCentavos: Long get() = ingresosCentavos - gastosCentavos
}

object CalculosFinanzas {

    fun saldoTotal(saldoInicial: Long, movimientos: List<Movimiento>): Long =
        saldoInicial + movimientos.sumOf { if (it.esGasto) -it.montoCentavos else it.montoCentavos }

    fun reservado(metas: List<Meta>): Long = metas.sumOf { it.ahorradoCentavos }

    fun disponible(saldoInicial: Long, movimientos: List<Movimiento>, metas: List<Meta>): Long =
        saldoTotal(saldoInicial, movimientos) - reservado(metas)

    /** Suma ingresos y gastos de un mes. La fecha es "yyyy-MM-dd", así que basta con ver si empieza con "yyyy-MM". */
    fun resumenDelMes(movimientos: List<Movimiento>, mes: YearMonth): ResumenMes {
        val delMes = movimientos.filter { it.fecha.startsWith(mes.toString()) }
        val gastos = delMes.filter { it.esGasto }
        return ResumenMes(
            ingresosCentavos = delMes.filter { !it.esGasto }.sumOf { it.montoCentavos },
            gastosCentavos = gastos.sumOf { it.montoCentavos },
            gastosPorCategoria = gastos
                .groupBy { it.categoriaId }
                .mapValues { (_, lista) -> lista.sumOf { it.montoCentavos } }
        )
    }

    /**
     * Revisa si se puede aportar a una meta. Devuelve null si está bien,
     * o el mensaje de error para mostrar.
     */
    fun errorAlAportar(monto: Long, meta: Meta, disponible: Long): String? = when {
        monto > disponible -> "No tienes suficiente dinero disponible para este aporte."
        monto > meta.faltaCentavos -> "El aporte supera lo que falta para la meta."
        else -> null
    }

    fun errorAlRetirar(monto: Long, meta: Meta): String? =
        if (monto > meta.ahorradoCentavos) "No puedes retirar más de lo ahorrado en esta meta." else null

    /** Ordena del más nuevo al más viejo. */
    fun ordenarRecientes(movimientos: List<Movimiento>): List<Movimiento> =
        movimientos.sortedWith(compareByDescending<Movimiento> { it.fecha }.thenByDescending { it.id })

    /** Resumen de los últimos [cantidad] meses terminando en [hasta], del más viejo al más nuevo (para el gráfico). */
    fun ultimosMeses(movimientos: List<Movimiento>, hasta: YearMonth, cantidad: Int = 6): List<Pair<YearMonth, ResumenMes>> =
        (cantidad - 1 downTo 0).map { atras ->
            val mes = hasta.minusMonths(atras.toLong())
            mes to resumenDelMes(movimientos, mes)
        }

    /**
     * Cuánto habría que ahorrar POR MES para llegar a la meta en su fecha objetivo.
     * null = la meta no tiene fecha, la fecha está mal escrita o ya se cumplió.
     * Si la fecha ya pasó, devuelve todo lo que falta (hay que ponerlo este mes).
     */
    fun ahorroMensualSugerido(meta: Meta, hoy: LocalDate): Long? {
        if (meta.cumplida || meta.fechaObjetivo.isBlank()) return null
        val fecha = try { LocalDate.parse(meta.fechaObjetivo) } catch (e: DateTimeParseException) { return null }
        // Meses que quedan contando el actual: oct -> dic = 3 meses
        val meses = (ChronoUnit.MONTHS.between(YearMonth.from(hoy), YearMonth.from(fecha)) + 1).coerceAtLeast(1)
        // División redondeando hacia arriba, para que sumando [meses] veces sí se llegue
        return (meta.faltaCentavos + meses - 1) / meses
    }
}
