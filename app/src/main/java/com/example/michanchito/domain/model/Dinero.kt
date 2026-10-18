package com.example.michanchito.domain.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/*
 * TODO EL DINERO SE GUARDA EN CENTAVOS (Long).
 *   Bs 25,50  ->  2550 centavos
 * ¿Por qué? Con Double, 0.1 + 0.2 da 0.30000000000000004.
 * Con centavos enteros las sumas siempre son exactas.
 */
object Dinero {

    const val MAXIMO = 999_999_999_999L   // Bs 9.999.999.999,99

    /**
     * Convierte lo que escribe el usuario a centavos.
     * "25,50" o "25.50" -> 2550.  Lanza IllegalArgumentException con un mensaje si no es válido.
     */
    fun aCentavos(texto: String, permitirCero: Boolean = false): Long {
        val limpio = texto.trim().replace(',', '.')
        require(limpio.matches(Regex("\\d{1,10}(\\.\\d{1,2})?"))) {
            "Escribe un monto válido, con máximo dos decimales."
        }
        val centavos = BigDecimal(limpio).movePointRight(2).longValueExact()
        val minimo = if (permitirCero) 0L else 1L
        require(centavos in minimo..MAXIMO) { "El monto debe ser mayor que cero." }
        return centavos
    }

    /** 120000 -> "Bs 1.200,00" (formato boliviano). */
    fun formatear(centavos: Long): String {
        val formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-BO"))
        formato.minimumFractionDigits = 2
        formato.maximumFractionDigits = 2
        return "Bs " + formato.format(BigDecimal.valueOf(centavos, 2))
    }

    /** 2550 -> "25.50" (para volver a poner el monto en un campo de texto al editar). */
    fun aTexto(centavos: Long): String = BigDecimal.valueOf(centavos, 2).toPlainString()

    /**
     * Cuántos dólares son [centavosBs] si el dólar se vende a [ventaCentavos] (Bs por dólar, en centavos).
     * Devuelve centavos de dólar.  Ej.: Bs 104.500 a 11,84 -> 8.826 centavos = US$ 88,26
     */
    fun aCentavosDeDolar(centavosBs: Long, ventaCentavos: Long): Long {
        if (ventaCentavos <= 0) return 0
        return BigDecimal.valueOf(centavosBs).multiply(BigDecimal(100))
            .divide(BigDecimal.valueOf(ventaCentavos), 0, RoundingMode.HALF_UP)
            .toLong()
    }

    /** 8826 -> "US$ 88,26" */
    fun formatearDolares(centavos: Long): String {
        val formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-BO"))
        formato.minimumFractionDigits = 2
        formato.maximumFractionDigits = 2
        return "US$ " + formato.format(BigDecimal.valueOf(centavos, 2))
    }
}
