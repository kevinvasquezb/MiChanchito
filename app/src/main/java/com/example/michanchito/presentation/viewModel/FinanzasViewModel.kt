package com.example.michanchito.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.michanchito.data.api.DolarServicio
import com.example.michanchito.data.repository.FinanzasRepositorio
import com.example.michanchito.domain.model.Dolar
import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.Perfil
import com.example.michanchito.domain.model.Presupuesto
import com.example.michanchito.domain.model.TipoMovimiento
import com.example.michanchito.domain.usecase.CalculosFinanzas
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * TODO lo que muestran las pantallas de dinero, en un solo objeto.
 * Los saldos NO se guardan: se calculan aquí con CalculosFinanzas.
 */
data class FinanzasEstado(
    val cargando: Boolean = true,                 // todavía no sabemos si tiene saldo inicial
    val errorCarga: String? = null,               // no se pudo leer usuarios/{uid}
    val perfil: Perfil = Perfil(),
    val movimientos: List<Movimiento> = emptyList(),
    val metas: List<Meta> = emptyList(),
    val presupuestos: List<Presupuesto> = emptyList(),
    val dolar: Dolar? = null,                 // viene de internet (dolarapi.com), no de Firestore
    val cargandoDolar: Boolean = false,
    val errorDolar: Boolean = false
) {
    val saldoInicial: Long get() = perfil.saldoInicialCentavos ?: 0L
    val saldoTotal: Long get() = CalculosFinanzas.saldoTotal(saldoInicial, movimientos)
    val reservado: Long get() = CalculosFinanzas.reservado(metas)
    val disponible: Long get() = saldoTotal - reservado
}

/*
 *   Pantalla --(acción)--> ViewModel --> Repositorio --> Firestore
 *   Pantalla <--(estado)-- ViewModel <-- listener en tiempo real <--
 */
class FinanzasViewModel : ViewModel() {

    private var repositorio: FinanzasRepositorio? = null
    private var usuarioActual: FirebaseUser? = null
    private val escuchas = mutableListOf<ListenerRegistration>()

    private val _estado = MutableStateFlow(FinanzasEstado())
    val estado: StateFlow<FinanzasEstado> = _estado.asStateFlow()

    /**
     * Mensajes cortos para el Snackbar (errores de Firestore o confirmaciones).
     * Un Channel entrega cada mensaje UNA vez, aunque se repita el mismo texto.
     */
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    private fun avisar(mensaje: String) {
        _avisos.trySend(mensaje)
    }

    // ======================= CONEXIÓN =======================

    /** Hay usuario: guardamos sus datos de Google y empezamos a escuchar SUS documentos. */
    fun conectar(usuario: FirebaseUser) {
        if (usuario.uid == usuarioActual?.uid) return   // ya estábamos escuchando a esta persona
        desconectar()
        usuarioActual = usuario

        val repo = FinanzasRepositorio(usuario.uid, alError = { mensaje -> avisar(mensaje) })
        repositorio = repo
        repo.guardarDatosDeGoogle(usuario)

        escuchas += repo.escucharPerfil(
            alCambiar = { perfil, conocido ->
                _estado.update { it.copy(perfil = perfil, cargando = !conocido, errorCarga = null) }
            },
            alFallar = { mensaje -> _estado.update { it.copy(errorCarga = mensaje) } }
        )
        escuchas += repo.escucharMovimientos { lista -> _estado.update { it.copy(movimientos = lista) } }
        escuchas += repo.escucharMetas { lista -> _estado.update { it.copy(metas = lista) } }
        escuchas += repo.escucharPresupuestos { lista -> _estado.update { it.copy(presupuestos = lista) } }

        cargarDolar()
    }

    /** Cerró sesión: dejamos de escuchar y borramos los datos de la pantalla. */
    fun desconectar() {
        escuchas.forEach { it.remove() }
        escuchas.clear()
        repositorio = null
        usuarioActual = null
        _estado.value = FinanzasEstado()
    }

    /** Botón "Reintentar" de la pantalla de error: vuelve a conectar al mismo usuario. */
    fun reintentar() {
        val usuario = usuarioActual ?: return
        desconectar()
        conectar(usuario)
    }

    override fun onCleared() = desconectar()

    // ======================= SALDO INICIAL =======================

    fun guardarSaldoInicial(centavos: Long) {
        repositorio?.guardarSaldoInicial(centavos)
    }

    // ======================= MOVIMIENTOS =======================

    /** Si [idExistente] es null crea uno nuevo; si no, lo reemplaza (editar). */
    fun guardarMovimiento(
        idExistente: String?,
        tipo: TipoMovimiento,
        montoCentavos: Long,
        categoriaId: String,
        fecha: String,
        descripcion: String
    ) {
        val repo = repositorio ?: return
        val movimiento = Movimiento(
            id = idExistente ?: repo.nuevoIdMovimiento(),
            tipo = tipo,
            montoCentavos = montoCentavos,
            categoriaId = categoriaId,
            fecha = fecha,
            descripcion = descripcion.trim()
        )
        repo.guardarMovimiento(movimiento)
        avisar(if (idExistente == null) "${tipo.etiqueta} guardado." else "Movimiento actualizado.")
    }

    fun borrarMovimiento(id: String) {
        repositorio?.borrarMovimiento(id)
        avisar("Movimiento eliminado.")
    }

    // ======================= METAS =======================

    fun crearMeta(nombre: String, objetivoCentavos: Long, fechaObjetivo: String) {
        repositorio?.crearMeta(nombre.trim(), objetivoCentavos, fechaObjetivo)
    }

    /** Devuelve un mensaje de error si no se puede, o null si se guardó. */
    fun aportarAMeta(meta: Meta, montoCentavos: Long): String? {
        val error = CalculosFinanzas.errorAlAportar(montoCentavos, meta, _estado.value.disponible)
        if (error == null) repositorio?.cambiarAhorroDeMeta(meta.id, montoCentavos)
        return error
    }

    fun retirarDeMeta(meta: Meta, montoCentavos: Long): String? {
        val error = CalculosFinanzas.errorAlRetirar(montoCentavos, meta)
        if (error == null) repositorio?.cambiarAhorroDeMeta(meta.id, -montoCentavos)
        return error
    }

    /** Al borrar la meta, lo ahorrado vuelve solo a "disponible" (porque reservado se calcula). */
    fun borrarMeta(id: String) {
        repositorio?.borrarMeta(id)
    }

    // ======================= DÓLAR (API REST) =======================

    /** Pide las cotizaciones a dolarapi.com. Sin internet: la tarjeta muestra "Reintentar" y la app sigue igual. */
    fun cargarDolar() {
        if (_estado.value.cargandoDolar) return
        _estado.update { it.copy(cargandoDolar = true, errorDolar = false) }
        viewModelScope.launch {
            try {
                val dolar = DolarServicio.consultar()
                _estado.update { it.copy(dolar = dolar, cargandoDolar = false) }
            } catch (e: Exception) {
                _estado.update { it.copy(cargandoDolar = false, errorDolar = true) }
            }
        }
    }

    // ======================= LÍMITES (PRESUPUESTOS) =======================

    fun guardarPresupuesto(categoriaId: String, limiteCentavos: Long) {
        repositorio?.guardarPresupuesto(Presupuesto(categoriaId, limiteCentavos))
    }

    fun borrarPresupuesto(categoriaId: String) {
        repositorio?.borrarPresupuesto(categoriaId)
    }
}
