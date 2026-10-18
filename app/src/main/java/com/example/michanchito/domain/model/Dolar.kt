package com.example.michanchito.domain.model

/*
 * Cotización del dólar en bolivianos. Viene de internet (dolarapi.com), no de Firestore.
 * Los precios también van en CENTAVOS: Bs 11,84 por dólar = 1184.
 */
data class Cotizacion(
    val nombre: String,        // "Oficial" (BCB) o "Binance" (paralelo)
    val compraCentavos: Long,  // a cuánto te compran tus dólares
    val ventaCentavos: Long,   // a cuánto te venden dólares
    val fecha: String          // fecha de actualización que entrega la API
)

data class Dolar(
    val oficial: Cotizacion,
    val paralelo: Cotizacion
)
