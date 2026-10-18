package com.example.michanchito.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.michanchito.domain.model.Cotizacion
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Dolar
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.TipoMovimiento
import com.example.michanchito.domain.usecase.Avisos
import com.example.michanchito.domain.usecase.CalculosFinanzas
import com.example.michanchito.presentation.viewModel.FinanzasEstado
import java.time.YearMonth

/*
 * INICIO: saldo, dólar, avisos, botones rápidos, este mes, últimos movimientos y metas.
 * No guarda nada: solo muestra el estado y avisa (con lambdas) qué tocó el usuario.
 */
@Composable
fun InicioScreen(
    nombre: String,
    estado: FinanzasEstado,
    alAgregar: (TipoMovimiento) -> Unit,
    alEditar: (Movimiento) -> Unit,
    alVerMovimientos: () -> Unit,
    alVerMetas: () -> Unit,
    alRecargarDolar: () -> Unit
) {
    val resumen = CalculosFinanzas.resumenDelMes(estado.movimientos, YearMonth.from(hoy()))

    // Los mismos avisos que manda la notificación diaria (domain/usecase/Avisos.kt)
    val avisos = Avisos.limites(estado.presupuestos, estado.movimientos, hoy())

    Pagina("Tu dinero, a la vista", "Hola, ${nombre.substringBefore(' ').ifBlank { "bienvenido" }}.") {
        item { TarjetaSaldo(estado.saldoTotal, estado.disponible, estado.reservado) }

        item {
            TarjetaDolar(
                dolar = estado.dolar,
                cargando = estado.cargandoDolar,
                error = estado.errorDolar,
                disponibleCentavos = estado.disponible,
                alRecargar = alRecargarDolar
            )
        }

        if (estado.disponible < 0) {
            item { Aviso("Tu disponible es negativo: gastaste dinero que tenías apartado en metas.", esError = true) }
        }
        items(avisos) { aviso -> Aviso("${aviso.titulo} ${aviso.texto}", esError = true) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { alAgregar(TipoMovimiento.INGRESO) }, modifier = Modifier.weight(1f)) { Text("+ Ingreso") }
                OutlinedButton(onClick = { alAgregar(TipoMovimiento.GASTO) }, modifier = Modifier.weight(1f)) { Text("− Gasto") }
            }
        }

        item {
            TituloSeccion("Este mes")
            TotalesDelMes(resumen)
        }

        item { TituloSeccion("Movimientos recientes", "Ver todos", alVerMovimientos) }
        val recientes = CalculosFinanzas.ordenarRecientes(estado.movimientos).take(4)
        if (recientes.isEmpty()) {
            item { MensajeVacio("↗", "Tu historia empieza aquí", "Agrega tu primer ingreso o gasto.") }
        } else {
            items(recientes, key = { it.id }) { movimiento -> FilaMovimiento(movimiento) { alEditar(movimiento) } }
        }

        item { TituloSeccion("Tus metas", "Ver metas", alVerMetas) }
        val metas = estado.metas.take(2)
        if (metas.isEmpty()) {
            item { MensajeVacio("◎", "Dale un propósito a tu ahorro", "Crea una meta y aparta dinero poco a poco.") }
        } else {
            items(metas, key = { it.id }) { meta ->
                Card(
                    onClick = alVerMetas,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(20.dp)) { ProgresoMeta(meta) }
                }
            }
        }
    }
}

/**
 * Tarjeta del dólar: oficial (BCB) y paralelo (Binance P2P) desde dolarapi.com,
 * y cuánto vale tu disponible en dólares al precio de venta del paralelo.
 */
@Composable
private fun TarjetaDolar(dolar: Dolar?, cargando: Boolean, error: Boolean, disponibleCentavos: Long, alRecargar: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dólar hoy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (cargando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else TextButton(onClick = alRecargar) { Text(if (dolar == null) "Reintentar" else "Actualizar") }
            }

            when {
                dolar != null -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ColumnaCotizacion("Oficial (BCB)", dolar.oficial, Modifier.weight(1f))
                        ColumnaCotizacion("Paralelo", dolar.paralelo, Modifier.weight(1f))
                    }
                    HorizontalDivider()
                    val enDolares = Dinero.aCentavosDeDolar(disponibleCentavos, dolar.paralelo.ventaCentavos)
                    Text(
                        "Tu disponible ≈ ${Dinero.formatearDolares(enDolares)} al paralelo",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Fuente: dolarapi.com (BCB y Binance P2P) · ${dolar.paralelo.fecha}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                error -> Text(
                    "No se pudo consultar el dólar. Revisa tu conexión.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> Text("Consultando…", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ColumnaCotizacion(titulo: String, cotizacion: Cotizacion, modifier: Modifier) {
    Column(modifier) {
        Text(titulo, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(Dinero.formatear(cotizacion.ventaCentavos), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("compra ${Dinero.formatear(cotizacion.compraCentavos)}", style = MaterialTheme.typography.bodySmall)
    }
}
