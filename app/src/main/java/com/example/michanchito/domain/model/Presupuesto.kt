package com.example.michanchito.domain.model

/*
 * Un presupuesto es un LÍMITE MENSUAL para una categoría de gasto.
 * No descuenta ni aparta dinero: solo te avisa cuánto llevas gastado.
 *
 * Firestore: usuarios/{uid}/presupuestos/{categoriaId}
 *   { "categoriaId": "comida", "limiteCentavos": 50000 }
 * El id del documento ES la categoría: no puede haber dos límites para "comida".
 */
data class Presupuesto(
    val categoriaId: String,
    val limiteCentavos: Long
)
