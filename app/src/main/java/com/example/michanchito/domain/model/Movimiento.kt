package com.example.michanchito.domain.model

/** Un movimiento es dinero que ENTRA (ingreso) o SALE (gasto). */
enum class TipoMovimiento(val etiqueta: String) {
    INGRESO("Ingreso"),
    GASTO("Gasto")
}

/*
 * Firestore: usuarios/{uid}/movimientos/{id}
 *   { "tipo": "GASTO", "montoCentavos": 2550, "categoriaId": "comida",
 *     "fecha": "2026-10-15", "descripcion": "Almuerzo" }
 */
data class Movimiento(
    val id: String,
    val tipo: TipoMovimiento,
    val montoCentavos: Long,       // siempre positivo
    val categoriaId: String,       // "comida", "sueldo", ...
    val fecha: String,             // "yyyy-MM-dd" (texto: fácil de leer y de ordenar)
    val descripcion: String = ""
) {
    val esGasto: Boolean get() = tipo == TipoMovimiento.GASTO
}
