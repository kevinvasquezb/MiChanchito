package com.example.michanchito

import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.Presupuesto
import com.example.michanchito.domain.model.TipoMovimiento
import com.example.michanchito.domain.usecase.Avisos
import com.example.michanchito.domain.usecase.CalculosFinanzas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/*
 * Pruebas de las funciones nuevas (v2): avisos de límites, ahorro mensual, gráfico de 6 meses y dólar.
 * Clic derecho en app/src/test -> Run Tests.
 */
class FuncionesNuevasTest {

    private val hoy = LocalDate.of(2026, 10, 15)   // jueves 15 de octubre de 2026

    @Test
    fun avisosDeLimitesDicenCuantoQuedaYCuantosDiasFaltan() {
        val limites = listOf(Presupuesto("comida", 40_000), Presupuesto("ocio", 15_000), Presupuesto("transporte", 30_000))
        val movimientos = listOf(
            Movimiento("a", TipoMovimiento.GASTO, 32_000, "comida", "2026-10-05"),      // 80 % justo
            Movimiento("b", TipoMovimiento.GASTO, 18_000, "ocio", "2026-10-14"),        // pasado
            Movimiento("c", TipoMovimiento.GASTO, 5_000, "transporte", "2026-10-09"),   // tranquilo
            Movimiento("d", TipoMovimiento.GASTO, 99_000, "comida", "2026-09-30")       // otro mes: no cuenta
        )
        val avisos = Avisos.limites(limites, movimientos, hoy)

        assertEquals(2, avisos.size)
        assertEquals("Ya usaste más del 80 % del límite de Alimentación.", avisos[0].titulo)
        assertEquals("Te quedan Bs 80,00. Faltan 16 días para fin de mes.", avisos[0].texto)
        assertEquals("Te pasaste del límite de Ocio.", avisos[1].titulo)
        assertEquals("Llevas Bs 180,00 de Bs 150,00. Faltan 16 días para fin de mes.", avisos[1].texto)

        // Último día del mes y penúltimo: el texto cambia
        assertEquals("Llevas Bs 180,00 de Bs 150,00. Hoy termina el mes.", Avisos.limites(limites, movimientos, LocalDate.of(2026, 10, 31))[1].texto)
        assertEquals("Llevas Bs 180,00 de Bs 150,00. Falta 1 día para fin de mes.", Avisos.limites(limites, movimientos, LocalDate.of(2026, 10, 30))[1].texto)
    }

    @Test
    fun ahorroMensualParaLlegarALaMeta() {
        // Faltan Bs 1.500 y la fecha es el 24 de diciembre: oct, nov y dic = 3 meses -> Bs 500 al mes
        val laptop = Meta("m", "Laptop", objetivoCentavos = 200_000, ahorradoCentavos = 50_000, fechaObjetivo = "2026-12-24")
        assertEquals(50_000L, CalculosFinanzas.ahorroMensualSugerido(laptop, hoy))
        // Redondea hacia arriba: Bs 1.000 en 3 meses = Bs 333,34
        assertEquals(33_334L, CalculosFinanzas.ahorroMensualSugerido(laptop.copy(ahorradoCentavos = 100_000), hoy))
        // Sin fecha, cumplida o fecha mal escrita -> null
        assertNull(CalculosFinanzas.ahorroMensualSugerido(laptop.copy(fechaObjetivo = ""), hoy))
        assertNull(CalculosFinanzas.ahorroMensualSugerido(laptop.copy(ahorradoCentavos = 200_000), hoy))
        assertNull(CalculosFinanzas.ahorroMensualSugerido(laptop.copy(fechaObjetivo = "mañana"), hoy))
        // Fecha ya pasada -> todo lo que falta, este mes
        assertEquals(150_000L, CalculosFinanzas.ahorroMensualSugerido(laptop.copy(fechaObjetivo = "2026-01-01"), hoy))
    }

    @Test
    fun ultimosSeisMesesYDolar() {
        val movimientos = listOf(
            Movimiento("a", TipoMovimiento.INGRESO, 100_000, "sueldo", "2026-10-01"),
            Movimiento("b", TipoMovimiento.GASTO, 30_000, "comida", "2026-08-05"),
            Movimiento("c", TipoMovimiento.GASTO, 10_000, "comida", "2026-04-05")    // fuera de los 6 meses
        )
        val meses = CalculosFinanzas.ultimosMeses(movimientos, YearMonth.of(2026, 10))
        assertEquals(6, meses.size)
        assertEquals(YearMonth.of(2026, 5), meses.first().first)
        assertEquals(YearMonth.of(2026, 10), meses.last().first)
        assertEquals(30_000L, meses[3].second.gastosCentavos)          // agosto
        assertEquals(100_000L, meses.last().second.ingresosCentavos)   // octubre

        // Bs 1.045,00 al dólar de 11,84 = US$ 88,26
        assertEquals(8_826L, Dinero.aCentavosDeDolar(104_500, 1_184))
        assertEquals("US$ 88,26", Dinero.formatearDolares(8_826))
        assertEquals(0L, Dinero.aCentavosDeDolar(104_500, 0))
    }
}
