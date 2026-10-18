package com.example.michanchito.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.michanchito.domain.model.Categoria
import com.example.michanchito.domain.model.Categorias
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.TipoMovimiento
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.ZoneOffset

/*
 * Formularios (diálogos) de la app. Cada uno VALIDA lo que se escribió y,
 * si está bien, llama a una función que le pasa la pantalla (que a su vez llama al ViewModel).
 */

// ============================ CAMPOS REUTILIZABLES ============================

/** Campo para escribir dinero (teclado numérico). */
@Composable
fun CampoMonto(valor: String, alCambiar: (String) -> Unit, etiqueta: String = "Monto", error: String? = null) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text("$etiqueta (Bs)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = error != null,
        supportingText = { Text(error ?: "Por ejemplo: 25,50") }
    )
}

/** Chips para elegir la categoría. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectorCategoria(categorias: List<Categoria>, seleccionada: String, alCambiar: (String) -> Unit) {
    Text("Categoría", style = MaterialTheme.typography.labelLarge)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categorias.forEach { categoria ->
            FilterChip(
                selected = seleccionada == categoria.id,
                onClick = { alCambiar(categoria.id) },
                label = { Text("${categoria.icono} ${categoria.nombre}") }
            )
        }
    }
}

/** Convierte "2026-10-15" en fecha. Si el texto está vacío o mal escrito, devuelve null (no se cae). */
fun leerFecha(texto: String): LocalDate? =
    try { LocalDate.parse(texto) } catch (e: DateTimeParseException) { null }

/** Botón que abre un calendario. La fecha se maneja como texto "yyyy-MM-dd". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(valor: String, alCambiar: (String) -> Unit, etiqueta: String = "Fecha", opcional: Boolean = false) {
    var abierto by rememberSaveable { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        OutlinedButton(onClick = { abierto = true }, modifier = Modifier.weight(1f)) {
            Text("$etiqueta: ${valor.ifBlank { "Sin fecha" }}")
        }
        if (opcional && valor.isNotBlank()) TextButton(onClick = { alCambiar("") }) { Text("Quitar") }
    }

    if (abierto) {
        val inicial = leerFecha(valor) ?: hoy()
        // El DatePicker trabaja en milisegundos UTC
        val estado = rememberDatePickerState(
            initialSelectedDateMillis = inicial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { abierto = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { millis ->
                        alCambiar(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    abierto = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { abierto = false }) { Text("Cancelar") } }
        ) { DatePicker(estado) }
    }
}

// ============================ MOVIMIENTO ============================

/**
 * Crear o editar un ingreso/gasto.
 * [existente] = null -> nuevo.  Si no es null -> editar (y aparece "Eliminar").
 */
@Composable
fun DialogoMovimiento(
    tipoInicial: TipoMovimiento,
    existente: Movimiento?,
    alGuardar: (tipo: TipoMovimiento, monto: Long, categoriaId: String, fecha: String, descripcion: String) -> Unit,
    alBorrar: () -> Unit,
    alCerrar: () -> Unit
) {
    var tipo by rememberSaveable { mutableStateOf(existente?.tipo ?: tipoInicial) }
    var monto by rememberSaveable { mutableStateOf(existente?.let { Dinero.aTexto(it.montoCentavos) } ?: "") }
    var categoria by rememberSaveable { mutableStateOf(existente?.categoriaId ?: Categorias.deTipo(tipo).first().id) }
    var fecha by rememberSaveable { mutableStateOf(existente?.fecha?.takeIf { leerFecha(it) != null } ?: hoy().toString()) }
    var descripcion by rememberSaveable { mutableStateOf(existente?.descripcion ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var errorFecha by remember { mutableStateOf<String?>(null) }
    var confirmarBorrado by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(if (existente != null) "Editar movimiento" else "Nuevo ${tipo.etiqueta.lowercase()}") },
        text = {
            Column(
                Modifier.heightIn(max = 500.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. ¿Ingreso o gasto? Al cambiar, elegimos la primera categoría de ese tipo
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TipoMovimiento.entries.forEach { opcion ->
                        FilterChip(
                            selected = opcion == tipo,
                            onClick = { tipo = opcion; categoria = Categorias.deTipo(opcion).first().id },
                            label = { Text(opcion.etiqueta) }
                        )
                    }
                }
                // 2. Monto, 3. categoría, 4. fecha, 5. descripción
                CampoMonto(monto, { monto = it; error = null }, error = error)
                SelectorCategoria(Categorias.deTipo(tipo), categoria) { categoria = it }
                CampoFecha(fecha, { fecha = it; errorFecha = null })
                errorFecha?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it.take(200) },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                if (existente != null) {
                    TextButton(onClick = { confirmarBorrado = true }) {
                        Text("Eliminar movimiento", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val dia = leerFecha(fecha)
                if (dia == null || dia > hoy()) {
                    errorFecha = "Usa la fecha de hoy o una anterior."
                    return@Button
                }
                try {
                    alGuardar(tipo, Dinero.aCentavos(monto), categoria, fecha, descripcion)
                    alCerrar()
                } catch (e: IllegalArgumentException) {
                    error = e.message   // monto inválido
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
    )

    if (confirmarBorrado) {
        AlertDialog(
            onDismissRequest = { confirmarBorrado = false },
            title = { Text("¿Eliminar este movimiento?") },
            text = { Text("Tu saldo se recalcula solo.") },
            confirmButton = { Button(onClick = { alBorrar(); alCerrar() }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmarBorrado = false }) { Text("Conservar") } }
        )
    }
}

// ============================ METAS ============================

@Composable
fun DialogoNuevaMeta(alGuardar: (nombre: String, objetivo: Long, fecha: String) -> Unit, alCerrar: () -> Unit) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var monto by rememberSaveable { mutableStateOf("") }
    var fecha by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Nueva meta de ahorro") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it.take(80) },
                    label = { Text("¿Para qué quieres ahorrar?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                CampoMonto(monto, { monto = it; error = null }, "Objetivo", error)
                CampoFecha(fecha, { fecha = it }, "Fecha objetivo", opcional = true)
                Text("Crear una meta no mueve dinero. Los aportes apartan una parte de tu saldo.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    require(nombre.isNotBlank()) { "Ponle un nombre a tu meta." }
                    alGuardar(nombre, Dinero.aCentavos(monto), fecha)
                    alCerrar()
                } catch (e: IllegalArgumentException) {
                    error = e.message
                }
            }) { Text("Crear meta") }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
    )
}

/**
 * Aportar o retirar de una meta.
 * [alConfirmar] devuelve un mensaje de error (no alcanza el dinero, etc.) o null si se guardó.
 */
@Composable
fun DialogoAporte(meta: Meta, retirar: Boolean, disponible: Long, alConfirmar: (Long) -> String?, alCerrar: () -> Unit) {
    var monto by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(if (retirar) "Retirar de ${meta.nombre}" else "Aportar a ${meta.nombre}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (retirar) "Ahorrado: ${Dinero.formatear(meta.ahorradoCentavos)}. Lo que retires vuelve a estar disponible."
                    else "Disponible: ${Dinero.formatear(disponible)}. El aporte se aparta; no cuenta como gasto."
                )
                CampoMonto(monto, { monto = it; error = null }, error = error)
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    val problema = alConfirmar(Dinero.aCentavos(monto))
                    if (problema == null) alCerrar() else error = problema
                } catch (e: IllegalArgumentException) {
                    error = e.message
                }
            }) { Text(if (retirar) "Retirar" else "Aportar") }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
    )
}

// ============================ MONTO SIMPLE ============================

/** Diálogo que solo pide un monto: límite de una categoría o cambiar el saldo inicial. */
@Composable
fun DialogoMonto(
    titulo: String,
    explicacion: String,
    textoInicial: String = "",
    permitirCero: Boolean = false,
    alGuardar: (Long) -> Unit,
    alCerrar: () -> Unit
) {
    var monto by rememberSaveable { mutableStateOf(textoInicial) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(explicacion, style = MaterialTheme.typography.bodySmall)
                CampoMonto(monto, { monto = it; error = null }, error = error)
            }
        },
        confirmButton = {
            Button(onClick = {
                try {
                    alGuardar(Dinero.aCentavos(monto, permitirCero))
                    alCerrar()
                } catch (e: IllegalArgumentException) {
                    error = e.message
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCerrar) { Text("Cancelar") } }
    )
}
