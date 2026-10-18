package com.example.michanchito.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Meta

/*
 * METAS: crear, aportar, retirar y eliminar.
 * Aportar APARTA dinero (no es gasto): baja "disponible", pero el saldo total no cambia.
 */
@Composable
fun MetasScreen(
    metas: List<Meta>,
    alCrear: () -> Unit,
    alAportar: (Meta) -> Unit,
    alRetirar: (Meta) -> Unit,
    alBorrar: (Meta) -> Unit
) {
    var metaABorrar by remember { mutableStateOf<Meta?>(null) }

    Pagina("Metas que avanzan", "Apartar para una meta mantiene tu saldo total y reduce tu disponible.") {
        item { Button(onClick = alCrear, modifier = Modifier.fillMaxWidth()) { Text("+ Nueva meta") } }

        if (metas.isEmpty()) {
            item { MensajeVacio("◎", "¿Qué quieres lograr?", "Un viaje, tus estudios o un fondo de emergencia.") }
        } else {
            items(metas.sortedBy { it.nombre }, key = { it.id }) { meta ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(20.dp)) {
                        ProgresoMeta(meta)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!meta.cumplida) Button(onClick = { alAportar(meta) }) { Text("Aportar") }
                            if (meta.ahorradoCentavos > 0) OutlinedButton(onClick = { alRetirar(meta) }) { Text("Retirar") }
                        }
                        TextButton(onClick = { metaABorrar = meta }) {
                            Text("Eliminar meta", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    metaABorrar?.let { meta ->
        AlertDialog(
            onDismissRequest = { metaABorrar = null },
            title = { Text("¿Eliminar \"${meta.nombre}\"?") },
            text = { Text("${Dinero.formatear(meta.ahorradoCentavos)} vuelven a estar disponibles.") },
            confirmButton = { Button(onClick = { alBorrar(meta); metaABorrar = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { metaABorrar = null }) { Text("Cancelar") } }
        )
    }
}
