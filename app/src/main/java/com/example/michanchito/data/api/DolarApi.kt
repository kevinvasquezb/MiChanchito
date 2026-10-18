package com.example.michanchito.data.api

import com.example.michanchito.domain.model.Cotizacion
import com.example.michanchito.domain.model.Dolar
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.math.roundToLong

/*
 * CONSUMO DE UNA API REST con Retrofit (igual que un servicio web en clase).
 *
 * API: https://bo.dolarapi.com   (open source, sin API key)
 *   GET /v1/dolares/oficial   -> dólar oficial del Banco Central de Bolivia
 *   GET /v1/dolares/binance   -> dólar paralelo (Binance P2P)
 *
 * Respuesta (JSON):
 *   { "moneda": "USD", "casa": "oficial", "nombre": "Oficial",
 *     "compra": 11.68, "venta": 11.68, "fechaActualizacion": "2026-10-15T00:00:00.000Z" }
 *
 * Retrofit convierte ese JSON en CotizacionDto (Gson empareja los campos por nombre).
 * Después lo traducimos a Cotizacion (domain), con los precios en centavos.
 */

/** Tal cual llega de la API. "Dto" = objeto de transferencia de datos. */
data class CotizacionDto(
    val nombre: String? = null,
    val compra: Double? = null,
    val venta: Double? = null,
    val fechaActualizacion: String? = null
)

interface DolarApi {
    @GET("v1/dolares/oficial")
    suspend fun oficial(): CotizacionDto

    @GET("v1/dolares/binance")
    suspend fun binance(): CotizacionDto
}

object DolarServicio {

    private val api: DolarApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://bo.dolarapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DolarApi::class.java)
    }

    /** Pide las dos cotizaciones. Lanza excepción si no hay internet o la API falla: quien llama decide qué hacer. */
    suspend fun consultar(): Dolar = Dolar(
        oficial = api.oficial().aCotizacion("Oficial"),
        paralelo = api.binance().aCotizacion("Paralelo")
    )

    private fun CotizacionDto.aCotizacion(nombrePorDefecto: String) = Cotizacion(
        nombre = nombre ?: nombrePorDefecto,
        compraCentavos = ((compra ?: 0.0) * 100).roundToLong(),
        ventaCentavos = ((venta ?: 0.0) * 100).roundToLong(),
        fecha = (fechaActualizacion ?: "").take(10)   // "2026-10-15T00:00:00.000Z" -> "2026-10-15"
    )
}
