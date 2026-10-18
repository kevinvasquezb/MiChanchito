package com.example.michanchito.presentation.viewModel

import android.app.Application
import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.michanchito.data.api.GoogleAuthHelper
import com.example.michanchito.data.datastore.PreferenciasApp
import com.example.michanchito.data.notificaciones.NotificacionesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * Todo lo que AppNavigation necesita para decidir QUÉ PANTALLA mostrar.
 */
data class AuthEstado(
    val cargando: Boolean = true,            // leyendo DataStore al abrir la app
    val usuario: FirebaseUser? = null,       // null = no hay sesión -> Login
    val temaOscuro: Boolean = false,
    val biometriaActivada: Boolean = false,  // preferencia guardada en DataStore
    val biometriaValidada: Boolean = false,  // ya puso su huella en esta apertura
    val avisosActivados: Boolean = false,    // notificación diaria (DataStore)
    val iniciandoSesion: Boolean = false,    // ruedita en el botón de Google
    val error: String? = null
)

/* AndroidViewModel (y no ViewModel) porque DataStore necesita un Context. */
class AuthViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {

    private val auth = FirebaseAuth.getInstance()
    private val preferencias = PreferenciasApp(aplicacion)

    private val _estado = MutableStateFlow(AuthEstado(usuario = auth.currentUser))
    val estado: StateFlow<AuthEstado> = _estado.asStateFlow()

    /*
     * Igual que en el proyecto de la profe: AuthStateListener avisa cada vez que
     * alguien inicia o cierra sesión, y nosotros actualizamos el estado.
     */
    private val escuchaSesion = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val usuario = firebaseAuth.currentUser
        _estado.update {
            it.copy(
                usuario = usuario,
                biometriaValidada = if (usuario == null) false else it.biometriaValidada
            )
        }
    }

    init {
        auth.addAuthStateListener(escuchaSesion)

        // Leemos las 3 preferencias de DataStore. Al llegar el primer valor ya no estamos "cargando".
        viewModelScope.launch {
            combine(preferencias.temaOscuro, preferencias.biometriaActivada, preferencias.avisosActivados) { oscuro, huella, avisos ->
                Triple(oscuro, huella, avisos)
            }.collect { (oscuro, huella, avisos) ->
                _estado.update { it.copy(temaOscuro = oscuro, biometriaActivada = huella, avisosActivados = avisos, cargando = false) }
                // Si los avisos están activados, nos aseguramos de que el trabajo diario siga programado
                if (avisos) NotificacionesHelper.programarDiarios(aplicacion, reiniciar = false)
            }
        }
    }

    /** [activity] solo se usa para mostrar la ventana de Google; no se guarda. */
    fun iniciarSesionConGoogle(activity: Context) {
        if (_estado.value.iniciandoSesion) return
        // Quien entra con Google ya demostró quién es: no le pedimos huella en esta apertura.
        _estado.update { it.copy(iniciandoSesion = true, error = null, biometriaValidada = true) }

        viewModelScope.launch {
            val resultado = GoogleAuthHelper(activity).iniciarSesion()
            // Si salió bien, el AuthStateListener ya puso el usuario. Aquí solo manejamos el error.
            val error = resultado.exceptionOrNull()?.let { e ->
                if (e is GetCredentialCancellationException) null   // cerró la ventana: sin mensaje
                else "No se pudo iniciar sesión: ${e.message}"
            }
            _estado.update { it.copy(iniciandoSesion = false, error = error) }
        }
    }

    fun cerrarSesion(activity: Context) {
        viewModelScope.launch { GoogleAuthHelper(activity).cerrarSesion() }
        // El AuthStateListener pondrá usuario = null y la app vuelve al Login.
    }

    /** La huella/PIN salió bien. */
    fun onBiometriaValidada() {
        _estado.update { it.copy(biometriaValidada = true) }
    }

    /** Switch de "Pedir huella al abrir" (la pantalla ya pidió la huella antes de activarla). */
    fun cambiarBiometria(activada: Boolean) {
        if (activada) _estado.update { it.copy(biometriaValidada = true) }
        viewModelScope.launch { preferencias.guardarBiometria(activada) }
    }

    fun cambiarTemaOscuro(oscuro: Boolean) {
        viewModelScope.launch { preferencias.guardarTemaOscuro(oscuro) }
    }

    /** Switch "Avisos diarios" (la pantalla ya pidió el permiso de notificaciones antes de activarlo). */
    fun cambiarAvisos(activados: Boolean) {
        val app = getApplication<Application>()
        if (activados) NotificacionesHelper.programarDiarios(app, reiniciar = true)
        else NotificacionesHelper.cancelarDiarios(app)
        viewModelScope.launch { preferencias.guardarAvisos(activados) }
    }

    override fun onCleared() {
        auth.removeAuthStateListener(escuchaSesion)
    }
}
