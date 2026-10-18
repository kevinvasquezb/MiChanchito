package com.example.michanchito.presentation.navigation

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.michanchito.presentation.ui.BiometriaScreen
import com.example.michanchito.presentation.ui.LoginScreen
import com.example.michanchito.presentation.ui.MiChanchitoTema
import com.example.michanchito.presentation.ui.PantallaCargando
import com.example.michanchito.presentation.ui.PantallaError
import com.example.michanchito.presentation.ui.SaldoInicialScreen
import com.example.michanchito.presentation.viewModel.AuthViewModel
import com.example.michanchito.presentation.viewModel.FinanzasViewModel

/*
 * EL "PORTERO" DE LA APP (mismo patrón que AppNavigation de la profe: un when).
 *
 *   ¿leyendo preferencias?               -> ruedita
 *   ¿no hay usuario?                     -> Login con Google
 *   ¿huella activada y no la puso?       -> Pantalla de huella
 *   ¿falló la lectura de Firestore?      -> error con "Reintentar"
 *   ¿todavía no sabemos su saldo?        -> ruedita
 *   ¿no tiene saldo inicial?             -> "Empecemos por tu saldo"
 *   si no                                -> la app con sus 5 pestañas
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    finanzasViewModel: FinanzasViewModel = viewModel()
) {
    val auth by authViewModel.estado.collectAsStateWithLifecycle()
    val finanzas by finanzasViewModel.estado.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as FragmentActivity

    // Cuando cambia el usuario, conectamos (o desconectamos) sus datos de Firestore
    val usuario = auth.usuario
    LaunchedEffect(usuario?.uid) {
        if (usuario != null) finanzasViewModel.conectar(usuario) else finanzasViewModel.desconectar()
    }

    val cerrarSesion = {
        finanzasViewModel.desconectar()   // primero dejamos de escuchar Firestore
        authViewModel.cerrarSesion(activity)
    }

    MiChanchitoTema(oscuro = auth.temaOscuro) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
                auth.cargando -> PantallaCargando("Preparando Mi Chanchito…")

                usuario == null -> LoginScreen(
                    cargando = auth.iniciandoSesion,
                    error = auth.error,
                    alPulsarGoogle = { authViewModel.iniciarSesionConGoogle(activity) }
                )

                auth.biometriaActivada && !auth.biometriaValidada -> BiometriaScreen(
                    alDesbloquear = { authViewModel.onBiometriaValidada() },
                    alCerrarSesion = cerrarSesion
                )

                finanzas.errorCarga != null -> PantallaError(
                    mensaje = finanzas.errorCarga ?: "",
                    alReintentar = { finanzasViewModel.reintentar() },
                    alCerrarSesion = cerrarSesion
                )

                finanzas.cargando -> PantallaCargando("Cargando tus datos…")

                finanzas.perfil.saldoInicialCentavos == null -> SaldoInicialScreen(
                    alGuardar = { finanzasViewModel.guardarSaldoInicial(it) },
                    alCerrarSesion = cerrarSesion
                )

                else -> PantallasPrincipales(
                    authViewModel = authViewModel,
                    finanzasViewModel = finanzasViewModel,
                    alCerrarSesion = cerrarSesion
                )
            }
        }
    }
}
