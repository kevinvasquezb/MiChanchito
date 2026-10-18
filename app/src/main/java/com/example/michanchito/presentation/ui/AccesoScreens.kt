package com.example.michanchito.presentation.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.michanchito.R
import com.example.michanchito.data.api.BiometricHelper
import com.example.michanchito.domain.model.Dinero

/*
 * Las pantallas ANTES de entrar a la app:
 *   1. LoginScreen        -> no hay sesión
 *   2. BiometriaScreen    -> hay sesión y el usuario activó la huella
 *   3. PantallaError      -> no se pudo leer Firestore (reglas, base no creada, sin internet)
 *   4. SaldoInicialScreen -> primera vez: ¿cuánto dinero tienes hoy?
 */

@Composable
fun LoginScreen(cargando: Boolean, error: String?, alPulsarGoogle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()   // deja espacio para las barras del sistema y el teclado
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.chanchito_logo),
            contentDescription = "Logo de Mi Chanchito",
            modifier = Modifier.size(112.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(28.dp))
        Text("MI CHANCHITO", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text("Tu dinero,\ncon más claridad.", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "Registra lo que entra y sale, aparta dinero para tus metas y conoce cuánto puedes gastar.",
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(36.dp))

        Button(
            onClick = alPulsarGoogle,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp)
        ) {
            if (cargando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            else Text("Continuar con Google")
        }

        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun BiometriaScreen(alDesbloquear: () -> Unit, alCerrarSesion: () -> Unit) {
    val activity = LocalActivity.current as FragmentActivity
    var mensaje by remember { mutableStateOf<String?>(null) }
    var yaSePidio by rememberSaveable { mutableStateOf(false) }   // para no pedirla 2 veces al girar

    fun pedirHuella() {
        BiometricHelper.autenticar(activity, alExito = alDesbloquear, alError = { mensaje = it })
    }

    // Al entrar, pedimos la huella automáticamente (una sola vez)
    LaunchedEffect(Unit) {
        if (yaSePidio) return@LaunchedEffect
        yaSePidio = true
        if (BiometricHelper.estaDisponible(activity)) pedirHuella()
        else alDesbloquear()   // el celular ya no tiene huella ni PIN: lo dejamos pasar
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔒", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(12.dp))
        Text("Tu dinero es personal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("Desbloquea Mi Chanchito para continuar.")
        Spacer(Modifier.height(24.dp))
        Button(onClick = { pedirHuella() }) { Text("Desbloquear") }
        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp)) }
        TextButton(onClick = alCerrarSesion) { Text("Entrar con otra cuenta") }
    }
}

@Composable
fun SaldoInicialScreen(alGuardar: (Long) -> Unit, alCerrarSesion: () -> Unit) {
    var texto by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()   // deja espacio para las barras del sistema y el teclado
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Empecemos por tu saldo", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "¿Cuánto dinero tienes hoy? Ese es tu saldo inicial. Después, cada ingreso y gasto lo actualiza.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        CampoMonto(texto, { texto = it; error = null }, "Saldo inicial", error)
        Text("Puedes empezar con 0. El saldo inicial no cuenta como ingreso del mes.", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                try {
                    alGuardar(Dinero.aCentavos(texto, permitirCero = true))
                } catch (e: IllegalArgumentException) {
                    error = e.message
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Empezar") }
        TextButton(onClick = alCerrarSesion, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}

@Composable
fun PantallaError(mensaje: String, alReintentar: () -> Unit, alCerrarSesion: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("No pudimos cargar tus datos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(mensaje)
        Spacer(Modifier.height(8.dp))
        Text(
            "Revisa tu conexión. Si recién configuraste Firebase, revisa que la base Firestore exista y que las reglas estén publicadas.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = alReintentar, modifier = Modifier.fillMaxWidth()) { Text("Reintentar") }
        TextButton(onClick = alCerrarSesion, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
    }
}
