package com.example.michanchito.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.michanchito.domain.model.Categorias
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.TipoMovimiento
import com.example.michanchito.domain.usecase.CalculosFinanzas

/*
 * MOVIMIENTOS: lista completa con filtro (todos / ingresos / gastos) y buscador.
 * Tocar uno abre el diálogo para editarlo o eliminarlo.
 */
@Composable
fun MovimientosScreen(
    movimientos: List<Movimiento>,
    alAgregar: (TipoMovimiento) -> Unit,
    alEditar: (Movimiento) -> Unit
) {
    var filtro by rememberSaveable { mutableStateOf<TipoMovimiento?>(null) }   // null = todos
    var busqueda by rememberSaveable { mutableStateOf("") }

    val visibles = CalculosFinanzas.ordenarRecientes(movimientos).filter { movimiento ->
        val pasaFiltro = filtro == null || movimiento.tipo == filtro
        val categoria = Categorias.buscar(movimiento.categoriaId).nombre
        val pasaBusqueda = movimiento.descripcion.contains(busqueda, ignoreCase = true) ||
                categoria.contains(busqueda, ignoreCase = true)
        pasaFiltro && pasaBusqueda
    }

    Pagina("Ingresos y gastos", "Toca un movimiento para corregirlo o eliminarlo.") {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { alAgregar(TipoMovimiento.INGRESO) }, modifier = Modifier.weight(1f)) { Text("+ Ingreso") }
                OutlinedButton(onClick = { alAgregar(TipoMovimiento.GASTO) }, modifier = Modifier.weight(1f)) { Text("− Gasto") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(filtro == null, { filtro = null }, label = { Text("Todos") })
                FilterChip(filtro == TipoMovimiento.INGRESO, { filtro = TipoMovimiento.INGRESO }, label = { Text("Ingresos") })
                FilterChip(filtro == TipoMovimiento.GASTO, { filtro = TipoMovimiento.GASTO }, label = { Text("Gastos") })
            }
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Buscar por descripción o categoría") }
            )
        }
        if (visibles.isEmpty()) {
            item { MensajeVacio("↕", "Sin movimientos aquí", "Registra uno o cambia el filtro.") }
        } else {
            items(visibles, key = { it.id }) { movimiento -> FilaMovimiento(movimiento) { alEditar(movimiento) } }
        }
    }
}
