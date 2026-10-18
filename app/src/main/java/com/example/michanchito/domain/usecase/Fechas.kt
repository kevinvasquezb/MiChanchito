package com.example.michanchito.domain.usecase

import java.time.LocalDate
import java.time.ZoneId

/** Fechas de la app. Siempre en la hora de Bolivia, así el "hoy" es el mismo en todas partes. */
object Fechas {
    val ZONA: ZoneId = ZoneId.of("America/La_Paz")

    fun hoy(): LocalDate = LocalDate.now(ZONA)
}
