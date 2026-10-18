package com.example.michanchito.data.database

import com.example.michanchito.domain.model.Meta
import com.example.michanchito.domain.model.Movimiento
import com.example.michanchito.domain.model.Perfil
import com.example.michanchito.domain.model.Presupuesto
import com.example.michanchito.domain.model.TipoMovimiento
import com.google.firebase.firestore.DocumentSnapshot

/*
 * TRADUCTOR entre la app y Firestore.
 *
 *   aMapa()             data class  ->  Map (JSON) para GUARDAR
 *   DocumentSnapshot.aX()  Map (JSON) ->  data class para LEER
 *
 * Así ves exactamente qué campos tiene cada documento en la consola de Firebase.
 * El "id" NO se guarda como campo: es el nombre del documento.
 */

// ---------------- Movimiento ----------------
fun Movimiento.aMapa(): Map<String, Any> = mapOf(
    "tipo" to tipo.name,                 // "INGRESO" o "GASTO"
    "montoCentavos" to montoCentavos,
    "categoriaId" to categoriaId,
    "fecha" to fecha,
    "descripcion" to descripcion
)

fun DocumentSnapshot.aMovimiento() = Movimiento(
    id = id,
    tipo = if (getString("tipo") == "INGRESO") TipoMovimiento.INGRESO else TipoMovimiento.GASTO,
    montoCentavos = getLong("montoCentavos") ?: 0L,
    categoriaId = getString("categoriaId") ?: "",
    fecha = getString("fecha") ?: "",
    descripcion = getString("descripcion") ?: ""
)

// ---------------- Meta ----------------
fun Meta.aMapa(): Map<String, Any> = mapOf(
    "nombre" to nombre,
    "objetivoCentavos" to objetivoCentavos,
    "ahorradoCentavos" to ahorradoCentavos,
    "fechaObjetivo" to fechaObjetivo
)

fun DocumentSnapshot.aMeta() = Meta(
    id = id,
    nombre = getString("nombre") ?: "",
    objetivoCentavos = getLong("objetivoCentavos") ?: 0L,
    ahorradoCentavos = getLong("ahorradoCentavos") ?: 0L,
    fechaObjetivo = getString("fechaObjetivo") ?: ""
)

// ---------------- Presupuesto ----------------
fun Presupuesto.aMapa(): Map<String, Any> = mapOf(
    "categoriaId" to categoriaId,
    "limiteCentavos" to limiteCentavos
)

fun DocumentSnapshot.aPresupuesto() = Presupuesto(
    categoriaId = id,                    // el id del documento ES la categoría
    limiteCentavos = getLong("limiteCentavos") ?: 0L
)

// ---------------- Perfil (usuarios/{uid}) ----------------
fun DocumentSnapshot.aPerfil() = Perfil(
    nombre = getString("nombre") ?: "",
    correo = getString("correo") ?: "",
    saldoInicialCentavos = getLong("saldoInicialCentavos")   // null si todavía no lo configuró
)
