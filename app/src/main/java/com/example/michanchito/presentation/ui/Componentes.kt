package com.example.michanchito.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.michanchito.domain.model.Categorias
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.usecase.CalculosFinanzas
import com.example.michanchito.domain.usecase.Fechas
import com.example.michanchito.domain.usecase.ResumenMes
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Piezas de interfaz que se repiten en varias pantallas.
 */

/** La fecha de hoy en Bolivia (la misma que usan los avisos y el Worker). */
fun hoy(): LocalDate = Fechas.hoy()

/** Estructura de cada pestaña: título, subtítulo y una lista que se puede deslizar. */
@Composable
fun Pagina(titulo: String, subtitulo: String, contenido: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(titulo, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        contenido()
    }
}

@Composable
fun PantallaCargando(mensaje: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(20.dp))
        Text(mensaje)
    }
}

/** Tarjeta que se muestra cuando una lista está vacía. */
@Composable
fun MensajeVacio(simbolo: String, titulo: String, mensaje: String) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(28.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(simbolo, style = MaterialTheme.typography.headlineLarge)
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun TituloSeccion(titulo: String, accion: String? = null, alPulsarAccion: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (accion != null) TextButton(onClick = alPulsarAccion) { Text(accion) }
    }
}

/** La tarjeta morada de Inicio: saldo total, disponible y reservado. */
@Composable
fun TarjetaSaldo(saldoTotal: Long, disponible: Long, reservado: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFF57418F), Color(0xFF7962B8))), RoundedCornerShape(28.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("SALDO TOTAL", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
        Text(Dinero.formatear(saldoTotal), style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.Bold)
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 6.dp))
        Text("Disponible para gastar", color = Color.White.copy(alpha = 0.9f))
        Text(Dinero.formatear(disponible), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text("${Dinero.formatear(reservado)} reservado en metas", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
    }
}

/** Dos cuadritos: ingresos y gastos de un mes. */
@Composable
fun TotalesDelMes(resumen: ResumenMes) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CuadroTotal("↗ Ingresos", resumen.ingresosCentavos, MaterialTheme.colorScheme.ingreso, Modifier.weight(1f))
        CuadroTotal("↘ Gastos", resumen.gastosCentavos, MaterialTheme.colorScheme.gasto, Modifier.weight(1f))
    }
}

@Composable
private fun CuadroTotal(titulo: String, centavos: Long, color: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelLarge, color = color)
            Text(Dinero.formatear(centavos), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** Una fila de la lista de movimientos. */
@Composable
fun FilaMovimiento(movimiento: Movimiento, alPulsar: () -> Unit) {
    val categoria = Categorias.buscar(movimiento.categoriaId)
    val color = if (movimiento.esGasto) MaterialTheme.colorScheme.gasto else MaterialTheme.colorScheme.ingreso
    Card(onClick = alPulsar, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(44.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) { Text(categoria.icono) }
            Column(Modifier.weight(1f)) {
                Text(
                    movimiento.descripcion.ifBlank { categoria.nombre },
                    style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${categoria.nombre} · ${movimiento.fecha}",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                (if (movimiento.esGasto) "− " else "+ ") + Dinero.formatear(movimiento.montoCentavos),
                color = color, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.widthIn(max = 150.dp)
            )
        }
    }
}

/** Nombre, barra y montos de una meta. */
@Composable
fun ProgresoMeta(meta: Meta) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(meta.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(
            if (meta.cumplida) "¡Lograda!" else "${(meta.progreso * 100).roundToInt()} %",
            color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge
        )
    }
    BarraProgreso(meta.progreso, MaterialTheme.colorScheme.primary)
    Text(
        "${Dinero.formatear(meta.ahorradoCentavos)} de ${Dinero.formatear(meta.objetivoCentavos)}",
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (meta.fechaObjetivo.isNotBlank()) {
        Text("Fecha objetivo: ${meta.fechaObjetivo}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    // "Necesitas ahorrar Bs X al mes": solo si la meta tiene fecha y todavía no se cumplió
    CalculosFinanzas.ahorroMensualSugerido(meta, hoy())?.let { mensual ->
        val fecha = leerFecha(meta.fechaObjetivo)
        val texto = if (fecha != null && fecha < hoy()) "La fecha objetivo ya pasó: te faltan ${Dinero.formatear(meta.faltaCentavos)}."
        else "Para llegar a tiempo: ahorra ${Dinero.formatear(mensual)} al mes."
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun BarraProgreso(progreso: Float, color: Color) {
    LinearProgressIndicator(
        progress = { progreso.coerceIn(0f, 1f) },
        color = color,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(8.dp)
    )
}

/** "‹ Anterior   octubre 2026   Siguiente ›" */
@Composable
fun SelectorMes(mes: YearMonth, alCambiar: (YearMonth) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { alCambiar(mes.minusMonths(1)) }) { Text("‹ Anterior") }
        Text(
            mes.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-BO"))),
            style = MaterialTheme.typography.labelLarge
        )
        TextButton(onClick = { alCambiar(mes.plusMonths(1)) }) { Text("Siguiente ›") }
    }
}

/** Recuadro de aviso (lila normal, rojo si es error). */
@Composable
fun Aviso(texto: String, esError: Boolean = false) {
    Surface(
        color = if (esError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            texto, Modifier.fillMaxWidth().padding(12.dp), style = MaterialTheme.typography.bodySmall,
            color = if (esError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
