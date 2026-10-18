package com.example.michanchito.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.michanchito.domain.model.Categoria
import com.example.michanchito.domain.model.Categorias
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.Presupuesto
import com.example.michanchito.domain.model.TipoMovimiento
import com.example.michanchito.domain.usecase.CalculosFinanzas
import java.time.YearMonth
import kotlin.math.roundToInt

/*
 * LÍMITES (presupuestos): un tope mensual por categoría de gasto.
 * Muestra cuánto llevas gastado en el mes elegido contra ese tope.
 */
@Composable
fun LimitesScreen(
    presupuestos: List<Presupuesto>,
    movimientos: List<Movimiento>,
    alEditar: (Categoria, Presupuesto?) -> Unit,
    alQuitar: (Categoria) -> Unit
) {
    // Guardamos el mes como texto ("2026-10") para que sobreviva al girar la pantalla
    var mesTexto by rememberSaveable { mutableStateOf(YearMonth.from(hoy()).toString()) }
    val mes = YearMonth.parse(mesTexto)
    val resumen = CalculosFinanzas.resumenDelMes(movimientos, mes)

    Pagina("Límites por categoría", "Decide cuánto quieres gastar cada mes. Un límite no aparta ni descuenta dinero.") {
        item { SelectorMes(mes) { mesTexto = it.toString() } }

        items(Categorias.deTipo(TipoMovimiento.GASTO), key = { it.id }) { categoria ->
            val limite = presupuestos.firstOrNull { it.categoriaId == categoria.id }
            val gastado = resumen.gastosPorCategoria[categoria.id] ?: 0L

            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${categoria.icono} ${categoria.nombre}",
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        if (limite != null) TextButton(onClick = { alQuitar(categoria) }) { Text("Quitar") }
                        TextButton(onClick = { alEditar(categoria, limite) }) { Text(if (limite == null) "Poner límite" else "Cambiar") }
                    }

                    if (limite == null) {
                        Text("Gastaste ${Dinero.formatear(gastado)} (sin límite)", style = MaterialTheme.typography.bodySmall)
                    } else {
                        val uso = gastado.toFloat() / limite.limiteCentavos.coerceAtLeast(1)
                        val color = if (uso >= 1f) MaterialTheme.colorScheme.gasto else MaterialTheme.colorScheme.primary
                        Text("${Dinero.formatear(gastado)} de ${Dinero.formatear(limite.limiteCentavos)}")
                        BarraProgreso(uso, color)
                        Text(
                            if (uso >= 1f) "Límite superado · ${(uso * 100).roundToInt()} %" else "${(uso * 100).roundToInt()} % usado",
                            style = MaterialTheme.typography.bodySmall, color = color
                        )
                    }
                }
            }
        }
    }
}
