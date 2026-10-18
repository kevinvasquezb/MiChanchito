package com.example.michanchito.domain.model

/*
 * Firestore: usuarios/{uid}
 *   { "nombre": "Mirko", "correo": "...", "fotoUrl": "...", "saldoInicialCentavos": 50000 }
 *
 * saldoInicialCentavos = null  ->  todavía no configuró su saldo (mostramos esa pantalla).
 */
data class Perfil(
    val nombre: String = "",
    val correo: String = "",
    val saldoInicialCentavos: Long? = null
)
