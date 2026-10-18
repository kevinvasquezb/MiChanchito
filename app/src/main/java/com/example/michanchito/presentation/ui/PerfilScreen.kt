package com.example.michanchito.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Perfil

/*
 * MI PERFIL: datos de la cuenta, tema oscuro, huella, avisos diarios, saldo inicial y cerrar sesión.
 */
@Composable
fun PerfilScreen(
    perfil: Perfil,
    temaOscuro: Boolean,
    biometriaActivada: Boolean,
    biometriaDisponible: Boolean,
    avisosActivados: Boolean,
    alCambiarTema: (Boolean) -> Unit,
    alCambiarBiometria: (Boolean) -> Unit,
    alCambiarAvisos: (Boolean) -> Unit,
    alProbarAvisos: () -> Unit,
    alCambiarSaldoInicial: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    Pagina("Tu espacio personal", perfil.correo) {
        item { Text(perfil.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }

        item {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FilaInterruptor("Tema oscuro", "Más cómodo de noche", temaOscuro, true, alCambiarTema)
                    HorizontalDivider()
                    FilaInterruptor(
                        "Pedir huella al abrir",
                        if (biometriaDisponible) "Huella, rostro o PIN del celular" else "Configura una huella o bloqueo en tu celular",
                        biometriaActivada,
                        biometriaDisponible || biometriaActivada,
                        alCambiarBiometria
                    )
                    HorizontalDivider()
                    FilaInterruptor(
                        "Avisos diarios",
                        "Una notificación a las 9:00 si estás cerca de un límite o te pasaste",
                        avisosActivados,
                        true,
                        alCambiarAvisos
                    )
                    if (avisosActivados) {
                        TextButton(onClick = alProbarAvisos) { Text("Probar avisos ahora") }
                        Text(
                            "Solo llega un aviso si algún límite está al 80 % o superado.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(onClick = alCambiarSaldoInicial, modifier = Modifier.fillMaxWidth()) {
                Text("Saldo inicial: ${Dinero.formatear(perfil.saldoInicialCentavos ?: 0)} · Cambiar")
            }
        }
        item { Aviso("Moneda: bolivianos (Bs). Mi Chanchito registra tu dinero a mano; no se conecta a tu banco.") }
        item { OutlinedButton(onClick = alCerrarSesion, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") } }
    }
}

@Composable
private fun FilaInterruptor(titulo: String, descripcion: String, activado: Boolean, habilitado: Boolean, alCambiar: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Medium)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = activado, onCheckedChange = alCambiar, enabled = habilitado)
    }
}
