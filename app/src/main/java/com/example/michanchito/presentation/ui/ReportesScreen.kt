package com.example.michanchito.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.michanchito.domain.model.Categorias
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.usecase.CalculosFinanzas
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/*
 * REPORTES: gráfico de los últimos 6 meses, ingresos, gastos y resultado del mes elegido,
 * y las barras de gasto por categoría.
 * Todo se dibuja con Compose (Box con fondo y LinearProgressIndicator): sin librerías de gráficos.
 */
@Composable
fun ReportesScreen(movimientos: List<Movimiento>) {
    var mesTexto by rememberSaveable { mutableStateOf(YearMonth.from(hoy()).toString()) }
    val mes = YearMonth.parse(mesTexto)
    val resumen = CalculosFinanzas.resumenDelMes(movimientos, mes)

    Pagina("Entiende tus hábitos", "Lo que recibes, lo que gastas y en qué se va tu dinero.") {
        item { SelectorMes(mes) { mesTexto = it.toString() } }
        item { GraficoSeisMeses(movimientos, hasta = mes) }
        item { TotalesDelMes(resumen) }

        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resultado del mes", style = MaterialTheme.typography.labelLarge)
                    Text(
                        Dinero.formatear(resumen.resultadoCentavos),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                        color = if (resumen.resultadoCentavos >= 0) MaterialTheme.colorScheme.ingreso else MaterialTheme.colorScheme.gasto
                    )
                    Text(
                        "Ingresos menos gastos. No incluye el saldo inicial ni las metas.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { TituloSeccion("Gastos por categoría") }
        if (resumen.gastosCentavos == 0L) {
            item { MensajeVacio("▥", "Un mes sin gastos", "Las barras aparecen cuando registras gastos.") }
        } else {
            val ordenadas = resumen.gastosPorCategoria.entries.sortedByDescending { it.value }
            items(ordenadas, key = { it.key }) { (categoriaId, centavos) ->
                val categoria = Categorias.buscar(categoriaId)
                Column {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${categoria.icono} ${categoria.nombre}", modifier = Modifier.weight(1f))
                        Text(Dinero.formatear(centavos), fontWeight = FontWeight.Medium)
                    }
                    // Largo de la barra = parte de este gasto sobre el total del mes
                    BarraProgreso(centavos.toFloat() / resumen.gastosCentavos, MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/**
 * Barras de los últimos 6 meses (terminando en [hasta]): ingresos en verde y gastos en rojo.
 * Cada barra es un Box cuya altura es la fracción del mes más alto. Sin librerías.
 */
@Composable
private fun GraficoSeisMeses(movimientos: List<Movimiento>, hasta: YearMonth) {
    val meses = CalculosFinanzas.ultimosMeses(movimientos, hasta)
    val maximo = meses.maxOf { maxOf(it.second.ingresosCentavos, it.second.gastosCentavos) }.coerceAtLeast(1L)
    val idioma = Locale.forLanguageTag("es-BO")

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Últimos 6 meses", style = MaterialTheme.typography.labelLarge)

            // Las barras: una columna por mes, dos barras por columna
            Row(
                Modifier.fillMaxWidth().height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                meses.forEach { (_, resumen) ->
                    Row(
                        Modifier.weight(1f).fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Barra(resumen.ingresosCentavos.toFloat() / maximo, MaterialTheme.colorScheme.ingreso, Modifier.weight(1f))
                        Barra(resumen.gastosCentavos.toFloat() / maximo, MaterialTheme.colorScheme.gasto, Modifier.weight(1f))
                    }
                }
            }

            // Los nombres de los meses, alineados con las columnas
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                meses.forEach { (mes, _) ->
                    Text(
                        mes.month.getDisplayName(TextStyle.SHORT_STANDALONE, idioma).take(3),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Leyenda(MaterialTheme.colorScheme.ingreso, "Ingresos")
                Leyenda(MaterialTheme.colorScheme.gasto, "Gastos")
            }
        }
    }
}

@Composable
private fun Barra(fraccion: Float, color: Color, modifier: Modifier) {
    Box(
        modifier
            .fillMaxHeight(fraccion.coerceIn(0.02f, 1f))   // 2 % mínimo para que se vea algo aunque sea cero
            .background(color, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
    )
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.size(6.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
