package com.example.michanchito.data.repository

import com.example.michanchito.data.database.aMapa
import com.example.michanchito.data.database.aMeta
import com.example.michanchito.data.database.aMovimiento
import com.example.michanchito.data.database.aPerfil
import com.example.michanchito.data.database.aPresupuesto
import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.Perfil
import com.example.michanchito.domain.model.Presupuesto
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions

/*
 * REPOSITORIO = el ÚNICO archivo que habla con Firestore.
 * Ni las pantallas ni los ViewModels usan FirebaseFirestore directamente.
 *
 *   usuarios/{uid}                        nombre, correo, fotoUrl, saldoInicialCentavos
 *   usuarios/{uid}/movimientos/{id}       tipo, montoCentavos, categoriaId, fecha, descripcion
 *   usuarios/{uid}/metas/{id}             nombre, objetivoCentavos, ahorradoCentavos, fechaObjetivo
 *   usuarios/{uid}/presupuestos/{catId}   categoriaId, limiteCentavos
 *
 * LEER: con addSnapshotListener (tiempo real). Firestore vuelve a llamar cada vez que algo cambia.
 * ESCRIBIR: sin .await(). Firestore guarda primero en el celular y sube solo cuando hay internet,
 *           así la app funciona offline. Si el servidor rechaza algo, avisamos con [alError].
 */
class FinanzasRepositorio(
    uid: String,
    private val alError: (String) -> Unit
) {
    private val db = FirebaseFirestore.getInstance()

    private val usuario = db.collection("usuarios").document(uid)
    private val movimientos = usuario.collection("movimientos")
    private val metas = usuario.collection("metas")
    private val presupuestos = usuario.collection("presupuestos")

    // =========================== PERFIL ===========================

    /** Al iniciar sesión: crea o actualiza usuarios/{uid}. merge = no borra el saldo inicial. */
    fun guardarDatosDeGoogle(usuarioGoogle: FirebaseUser) {
        val datos = mapOf(
            "nombre" to (usuarioGoogle.displayName ?: "Usuario"),
            "correo" to (usuarioGoogle.email ?: ""),
            "fotoUrl" to (usuarioGoogle.photoUrl?.toString() ?: "")
        )
        usuario.set(datos, SetOptions.merge()).addOnFailureListener { avisar("guardar tu perfil", it) }
    }

    fun guardarSaldoInicial(centavos: Long) {
        usuario.set(mapOf("saldoInicialCentavos" to centavos), SetOptions.merge())
            .addOnFailureListener { avisar("guardar el saldo inicial", it) }
    }

    /**
     * Escucha usuarios/{uid}.
     * [conocido] = ya sabemos de verdad si tiene saldo inicial. Si el dato viene SOLO de la
     * copia del celular y no trae saldo, esperamos al servidor para no pedirlo por error.
     * MetadataChanges.INCLUDE hace que también nos avise cuando el servidor confirma.
     * Si falla (reglas sin publicar, base no creada), llamamos a [alFallar] para mostrar el error.
     */
    fun escucharPerfil(
        alCambiar: (perfil: Perfil, conocido: Boolean) -> Unit,
        alFallar: (String) -> Unit
    ): ListenerRegistration =
        usuario.addSnapshotListener(MetadataChanges.INCLUDE) { documento, error ->
            if (error != null) {
                alFallar("No se pudieron cargar tus datos: ${error.localizedMessage ?: "revisa tu conexión"}")
                return@addSnapshotListener
            }
            if (documento == null) return@addSnapshotListener
            val perfil = if (documento.exists()) documento.aPerfil() else Perfil()
            val conocido = perfil.saldoInicialCentavos != null || !documento.metadata.isFromCache
            alCambiar(perfil, conocido)
        }

    // =========================== MOVIMIENTOS ===========================

    fun escucharMovimientos(alCambiar: (List<Movimiento>) -> Unit): ListenerRegistration =
        movimientos.addSnapshotListener { resultado, error ->
            if (error != null) { avisar("leer tus movimientos", error); return@addSnapshotListener }
            alCambiar(resultado?.documents?.map { it.aMovimiento() } ?: emptyList())
        }

    /** Un id nuevo inventado por Firestore (sin escribir nada todavía). */
    fun nuevoIdMovimiento(): String = movimientos.document().id

    /** Crea o edita: set() con el mismo id reemplaza el documento completo. */
    fun guardarMovimiento(movimiento: Movimiento) {
        movimientos.document(movimiento.id).set(movimiento.aMapa())
            .addOnFailureListener { avisar("guardar el movimiento", it) }
    }

    fun borrarMovimiento(id: String) {
        movimientos.document(id).delete().addOnFailureListener { avisar("borrar el movimiento", it) }
    }

    // =========================== METAS ===========================

    fun escucharMetas(alCambiar: (List<Meta>) -> Unit): ListenerRegistration =
        metas.addSnapshotListener { resultado, error ->
            if (error != null) { avisar("leer tus metas", error); return@addSnapshotListener }
            alCambiar(resultado?.documents?.map { it.aMeta() } ?: emptyList())
        }

    fun crearMeta(nombre: String, objetivoCentavos: Long, fechaObjetivo: String) {
        val documento = metas.document()   // id automático
        val meta = Meta(documento.id, nombre, objetivoCentavos, 0, fechaObjetivo)
        documento.set(meta.aMapa()).addOnFailureListener { avisar("crear la meta", it) }
    }

    /**
     * Aportar (+) o retirar (−) dinero de una meta.
     * FieldValue.increment suma directo en el servidor: si dos celulares aportan a la vez,
     * no se pierde ningún aporte.
     */
    fun cambiarAhorroDeMeta(id: String, diferenciaCentavos: Long) {
        metas.document(id).update("ahorradoCentavos", FieldValue.increment(diferenciaCentavos))
            .addOnFailureListener { avisar("actualizar la meta", it) }
    }

    fun borrarMeta(id: String) {
        metas.document(id).delete().addOnFailureListener { avisar("borrar la meta", it) }
    }

    // =========================== PRESUPUESTOS ===========================

    fun escucharPresupuestos(alCambiar: (List<Presupuesto>) -> Unit): ListenerRegistration =
        presupuestos.addSnapshotListener { resultado, error ->
            if (error != null) { avisar("leer tus límites", error); return@addSnapshotListener }
            alCambiar(resultado?.documents?.map { it.aPresupuesto() } ?: emptyList())
        }

    /** El id del documento es la categoría: guardar otra vez reemplaza el límite anterior. */
    fun guardarPresupuesto(presupuesto: Presupuesto) {
        presupuestos.document(presupuesto.categoriaId).set(presupuesto.aMapa())
            .addOnFailureListener { avisar("guardar el límite", it) }
    }

    fun borrarPresupuesto(categoriaId: String) {
        presupuestos.document(categoriaId).delete().addOnFailureListener { avisar("quitar el límite", it) }
    }

    // ===========================================================

    private fun avisar(accion: String, error: Exception) {
        alError("No se pudo $accion: ${error.localizedMessage ?: "revisa tu conexión"}")
    }
}
