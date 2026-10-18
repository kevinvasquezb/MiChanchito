package com.example.michanchito.domain.model

/*
 * Una meta de ahorro APARTA dinero de tu saldo. No es un gasto:
 * el saldo total no cambia, solo baja lo "disponible".
 *
 * Firestore: usuarios/{uid}/metas/{id}
 *   { "nombre": "Celular", "objetivoCentavos": 150000,
 *     "ahorradoCentavos": 20000, "fechaObjetivo": "2026-12-24" }
 */
data class Meta(
    val id: String,
    val nombre: String,
    val objetivoCentavos: Long,
    val ahorradoCentavos: Long = 0,
    val fechaObjetivo: String = ""   // "" = sin fecha
) {
    /** Progreso de 0.0 a 1.0 para la barra. */
    val progreso: Float
        get() = (ahorradoCentavos.toFloat() / objetivoCentavos.coerceAtLeast(1)).coerceIn(0f, 1f)

    val faltaCentavos: Long get() = (objetivoCentavos - ahorradoCentavos).coerceAtLeast(0)
    val cumplida: Boolean get() = ahorradoCentavos >= objetivoCentavos
}
