package com.example.michanchito.presentation.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.michanchito.data.api.BiometricHelper
import com.example.michanchito.data.notificaciones.NotificacionesHelper
import com.example.michanchito.domain.model.Categoria
import com.example.michanchito.domain.model.Dinero
import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.Presupuesto
import com.example.michanchito.domain.model.TipoMovimiento
import com.example.michanchito.presentation.ui.DialogoAporte
import com.example.michanchito.presentation.ui.DialogoMonto
import com.example.michanchito.presentation.ui.DialogoMovimiento
import com.example.michanchito.presentation.ui.DialogoNuevaMeta
import com.example.michanchito.presentation.ui.InicioScreen
import com.example.michanchito.presentation.ui.LimitesScreen
import com.example.michanchito.presentation.ui.MetasScreen
import com.example.michanchito.presentation.ui.MovimientosScreen
import com.example.michanchito.presentation.ui.PerfilScreen
import com.example.michanchito.presentation.ui.ReportesScreen
import com.example.michanchito.presentation.viewModel.AuthViewModel
import com.example.michanchito.presentation.viewModel.FinanzasViewModel

/** Las rutas de Navigation Compose (texto) y cómo se ven en la barra de abajo. */
object Rutas {
    const val INICIO = "inicio"
    const val MOVIMIENTOS = "movimientos"
    const val METAS = "metas"
    const val LIMITES = "limites"
    const val REPORTES = "reportes"
    const val PERFIL = "perfil"     // sin pestaña: se abre desde la barra superior
}

private data class Pestana(val ruta: String, val titulo: String, val icono: String)

private val pestanas = listOf(
    Pestana(Rutas.INICIO, "Inicio", "⌂"),
    Pestana(Rutas.MOVIMIENTOS, "Historial", "↕"),
    Pestana(Rutas.METAS, "Metas", "◎"),
    Pestana(Rutas.LIMITES, "Límites", "◉"),
    Pestana(Rutas.REPORTES, "Reportes", "▥")
)

/** Qué diálogo de movimiento está abierto (null = ninguno). */
private data class EditorMovimiento(val tipo: TipoMovimiento, val existente: Movimiento? = null)

/*
 * La app por dentro: barra superior + NavHost con las pantallas + barra de pestañas abajo.
 * Aquí también viven los DIÁLOGOS (formularios), porque se abren desde varias pantallas.
 * Ruta sin pestaña: Mi perfil (botón de la barra superior). Se abre ENCIMA de la pantalla actual
 * y muestra una flecha de regreso en la barra superior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallasPrincipales(
    authViewModel: AuthViewModel,
    finanzasViewModel: FinanzasViewModel,
    alCerrarSesion: () -> Unit
) {
    val auth by authViewModel.estado.collectAsStateWithLifecycle()
    val estado by finanzasViewModel.estado.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as FragmentActivity

    val nav = rememberNavController()
    val rutaActual = nav.currentBackStackEntryAsState().value?.destination?.route ?: Rutas.INICIO
    val snackbar = remember { SnackbarHostState() }

    // Diálogos abiertos (null / false = cerrado)
    var editor by remember { mutableStateOf<EditorMovimiento?>(null) }
    var creandoMeta by rememberSaveable { mutableStateOf(false) }
    var aporte by remember { mutableStateOf<Pair<Meta, Boolean>?>(null) }          // (meta, ¿retirar?)
    var limite by remember { mutableStateOf<Pair<Categoria, Presupuesto?>?>(null) }
    var cambiandoSaldo by rememberSaveable { mutableStateOf(false) }
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    // Permiso de notificaciones (Android 13+). Si lo concede, recién activamos los avisos.
    val pedirPermisoAvisos = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) authViewModel.cambiarAvisos(true)
    }

    // Muestra los avisos del ViewModel en un Snackbar, uno por uno
    LaunchedEffect(Unit) {
        finanzasViewModel.avisos.collect { mensaje -> snackbar.showSnackbar(mensaje) }
    }

    // Ir a una PESTAÑA sin apilar pantallas repetidas (vuelve al estado guardado de esa pestaña)
    fun irA(ruta: String) {
        nav.navigate(ruta) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Abrir una pantalla SECUNDARIA (Mi perfil) encima de la actual: "atrás" vuelve a donde estabas
    fun abrir(ruta: String) {
        nav.navigate(ruta) { launchSingleTop = true }
    }

    // ¿Estamos en una pantalla secundaria? Entonces la barra superior muestra la flecha de regreso y su título
    val esSecundaria = rutaActual !in pestanas.map { it.ruta }
    val tituloBarra = when (rutaActual) {
        Rutas.PERFIL -> "Mi perfil"
        else -> "Mi Chanchito"
    }
    fun volver() {
        if (!nav.popBackStack()) irA(Rutas.INICIO)   // si no hay a dónde volver, a Inicio
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tituloBarra, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (esSecundaria) {
                        IconButton(onClick = { volver() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (rutaActual != Rutas.PERFIL) {
                        TextButton(onClick = { abrir(Rutas.PERFIL) }) { Text("Mi perfil") }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            NavigationBar {
                pestanas.forEach { pestana ->
                    NavigationBarItem(
                        selected = rutaActual == pestana.ruta,
                        onClick = { irA(pestana.ruta) },
                        icon = { Text(pestana.icono, style = MaterialTheme.typography.titleLarge) },
                        label = { Text(pestana.titulo) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { espacio ->
        NavHost(nav, startDestination = Rutas.INICIO, modifier = Modifier.padding(espacio)) {
            composable(Rutas.INICIO) {
                InicioScreen(
                    nombre = estado.perfil.nombre,
                    estado = estado,
                    alAgregar = { editor = EditorMovimiento(it) },
                    alEditar = { editor = EditorMovimiento(it.tipo, it) },
                    alVerMovimientos = { irA(Rutas.MOVIMIENTOS) },
                    alVerMetas = { irA(Rutas.METAS) },
                    alRecargarDolar = { finanzasViewModel.cargarDolar() }
                )
            }
            composable(Rutas.MOVIMIENTOS) {
                MovimientosScreen(
                    movimientos = estado.movimientos,
                    alAgregar = { editor = EditorMovimiento(it) },
                    alEditar = { editor = EditorMovimiento(it.tipo, it) }
                )
            }
            composable(Rutas.METAS) {
                MetasScreen(
                    metas = estado.metas,
                    alCrear = { creandoMeta = true },
                    alAportar = { aporte = it to false },
                    alRetirar = { aporte = it to true },
                    alBorrar = { finanzasViewModel.borrarMeta(it.id) }
                )
            }
            composable(Rutas.LIMITES) {
                LimitesScreen(
                    presupuestos = estado.presupuestos,
                    movimientos = estado.movimientos,
                    alEditar = { categoria, actual -> limite = categoria to actual },
                    alQuitar = { finanzasViewModel.borrarPresupuesto(it.id) }
                )
            }
            composable(Rutas.REPORTES) {
                ReportesScreen(movimientos = estado.movimientos)
            }
            composable(Rutas.PERFIL) {
                PerfilScreen(
                    perfil = estado.perfil,
                    temaOscuro = auth.temaOscuro,
                    biometriaActivada = auth.biometriaActivada,
                    biometriaDisponible = BiometricHelper.estaDisponible(activity),
                    avisosActivados = auth.avisosActivados,
                    alCambiarTema = { authViewModel.cambiarTemaOscuro(it) },
                    alCambiarBiometria = { activar ->
                        if (!activar) authViewModel.cambiarBiometria(false)
                        else BiometricHelper.autenticar(   // primero comprobamos que la huella funciona
                            activity,
                            alExito = { authViewModel.cambiarBiometria(true) },
                            alError = { /* canceló: el switch queda apagado */ }
                        )
                    },
                    alCambiarAvisos = { activar ->
                        when {
                            !activar -> authViewModel.cambiarAvisos(false)
                            NotificacionesHelper.tienePermiso(activity) -> authViewModel.cambiarAvisos(true)
                            Build.VERSION.SDK_INT >= 33 -> pedirPermisoAvisos.launch(Manifest.permission.POST_NOTIFICATIONS)
                            else -> authViewModel.cambiarAvisos(true)
                        }
                    },
                    alProbarAvisos = { NotificacionesHelper.probarAhora(activity) },
                    alCambiarSaldoInicial = { cambiandoSaldo = true },
                    alCerrarSesion = { confirmarSalida = true }
                )
            }
        }
    }

    // ===================== DIÁLOGOS =====================

    editor?.let { seleccion ->
        DialogoMovimiento(
            tipoInicial = seleccion.tipo,
            existente = seleccion.existente,
            alGuardar = { tipo, monto, categoria, fecha, descripcion ->
                finanzasViewModel.guardarMovimiento(seleccion.existente?.id, tipo, monto, categoria, fecha, descripcion)
            },
            alBorrar = { seleccion.existente?.let { finanzasViewModel.borrarMovimiento(it.id) } },
            alCerrar = { editor = null }
        )
    }

    if (creandoMeta) {
        DialogoNuevaMeta(
            alGuardar = { nombre, objetivo, fecha -> finanzasViewModel.crearMeta(nombre, objetivo, fecha) },
            alCerrar = { creandoMeta = false }
        )
    }

    aporte?.let { (metaElegida, retirar) ->
        // Usamos la versión más nueva de la meta (por si cambió mientras el diálogo estaba abierto)
        val meta = estado.metas.firstOrNull { it.id == metaElegida.id } ?: metaElegida
        DialogoAporte(
            meta = meta,
            retirar = retirar,
            disponible = estado.disponible,
            alConfirmar = { monto ->
                if (retirar) finanzasViewModel.retirarDeMeta(meta, monto)
                else finanzasViewModel.aportarAMeta(meta, monto)
            },
            alCerrar = { aporte = null }
        )
    }

    limite?.let { (categoria, actual) ->
        DialogoMonto(
            titulo = "Límite de ${categoria.icono} ${categoria.nombre}",
            explicacion = "¿Cuánto quieres gastar como máximo cada mes en esta categoría?",
            textoInicial = actual?.let { Dinero.aTexto(it.limiteCentavos) } ?: "",
            alGuardar = { finanzasViewModel.guardarPresupuesto(categoria.id, it) },
            alCerrar = { limite = null }
        )
    }

    if (cambiandoSaldo) {
        DialogoMonto(
            titulo = "Cambiar saldo inicial",
            explicacion = "Es el dinero que tenías al empezar. Tu saldo total se recalcula solo.",
            textoInicial = Dinero.aTexto(estado.saldoInicial),
            permitirCero = true,
            alGuardar = { finanzasViewModel.guardarSaldoInicial(it) },
            alCerrar = { cambiandoSaldo = false }
        )
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar tu sesión?") },
            text = { Text("Tus datos quedan guardados en tu cuenta para cuando vuelvas.") },
            confirmButton = { TextButton(onClick = { confirmarSalida = false; alCerrarSesion() }) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } }
        )
    }
}
