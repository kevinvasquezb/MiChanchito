package com.example.michanchito

import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.TipoMovimiento
import com.example.michanchito.domain.usecase.CalculosFinanzas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.YearMonth

/*
 * Pruebas de la matemática de la app (corren en la PC, sin celular):
 * clic derecho en la carpeta "test" -> Run Tests.
 */
class CalculosFinanzasTest {

    private fun ingreso(centavos: Long, fecha: String = "2026-10-03") =
        Movimiento("i$centavos$fecha", TipoMovimiento.INGRESO, centavos, "sueldo", fecha)

    private fun gasto(centavos: Long, categoria: String = "comida", fecha: String = "2026-10-04") =
        Movimiento("g$centavos$categoria$fecha", TipoMovimiento.GASTO, centavos, categoria, fecha)

    @Test
    fun convierteTextoACentavos() {
        assertEquals(2550L, Dinero.aCentavos(" 25,50 "))
        assertEquals(2550L, Dinero.aCentavos("25.5"))
        assertEquals(10L, Dinero.aCentavos("0.10"))
        assertEquals(0L, Dinero.aCentavos("0", permitirCero = true))
        assertEquals("25.50", Dinero.aTexto(2550))
    }

    @Test
    fun rechazaMontosInvalidos() {
        listOf("", "-1", "0", "1.234", "abc", "1e3", "1,000.00").forEach { texto ->
            assertThrows(IllegalArgumentException::class.java) { Dinero.aCentavos(texto) }
        }
    }

    @Test
    fun saldoTotalReservadoYDisponible() {
        // Saldo inicial Bs 500 + ingreso Bs 1.000 − gasto Bs 300 = Bs 1.200
        val movimientos = listOf(ingreso(100_000), gasto(30_000))
        val metas = listOf(Meta("m", "Emergencia", objetivoCentavos = 50_000, ahorradoCentavos = 20_000))

        assertEquals(120_000L, CalculosFinanzas.saldoTotal(50_000, movimientos))
        assertEquals(20_000L, CalculosFinanzas.reservado(metas))
        assertEquals(100_000L, CalculosFinanzas.disponible(50_000, movimientos, metas))
    }

    @Test
    fun resumenSoloCuentaElMesPedido() {
        val movimientos = listOf(
            ingreso(100_000, "2026-10-01"),
            gasto(30_000, "comida", "2026-10-05"),
            gasto(5_000, "comida", "2026-10-20"),
            gasto(10_000, "transporte", "2026-10-21"),
            gasto(99_999, "comida", "2026-09-30")   // septiembre: no cuenta
        )
        val resumen = CalculosFinanzas.resumenDelMes(movimientos, YearMonth.of(2026, 10))

        assertEquals(100_000L, resumen.ingresosCentavos)
        assertEquals(45_000L, resumen.gastosCentavos)
        assertEquals(55_000L, resumen.resultadoCentavos)
        assertEquals(35_000L, resumen.gastosPorCategoria["comida"])
        assertEquals(10_000L, resumen.gastosPorCategoria["transporte"])
    }

    @Test
    fun validaAportesYRetiros() {
        val meta = Meta("m", "Celular", objetivoCentavos = 50_000, ahorradoCentavos = 40_000)

        assertNull(CalculosFinanzas.errorAlAportar(10_000, meta, disponible = 20_000))
        assertNotNull(CalculosFinanzas.errorAlAportar(10_001, meta, disponible = 20_000))   // pasa el objetivo
        assertNotNull(CalculosFinanzas.errorAlAportar(5_000, meta, disponible = 1_000))     // no alcanza el dinero

        assertNull(CalculosFinanzas.errorAlRetirar(40_000, meta))
        assertNotNull(CalculosFinanzas.errorAlRetirar(40_001, meta))
    }

    @Test
    fun ordenaDelMasNuevoAlMasViejo() {
        val ordenados = CalculosFinanzas.ordenarRecientes(
            listOf(gasto(1, fecha = "2026-10-01"), gasto(2, fecha = "2026-10-09"), gasto(3, fecha = "2026-10-05"))
        )
        assertEquals(listOf("2026-10-09", "2026-10-05", "2026-10-01"), ordenados.map { it.fecha })
    }
}
