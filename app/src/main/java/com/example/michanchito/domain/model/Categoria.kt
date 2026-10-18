package com.example.michanchito.domain.model

/*
 * Las categorías son FIJAS: viven en el código, no en Firestore.
 * Así no hace falta una colección ni pantallas para crearlas.
 * Para agregar una, solo añádela a la lista.
 */
data class Categoria(
    val id: String,
    val nombre: String,
    val icono: String,
    val tipo: TipoMovimiento
)

object Categorias {

    val todas = listOf(
        Categoria("sueldo", "Sueldo", "💼", TipoMovimiento.INGRESO),
        Categoria("extra", "Ingreso extra", "✨", TipoMovimiento.INGRESO),
        Categoria("regalo", "Regalo", "🎁", TipoMovimiento.INGRESO),
        Categoria("otros_ingresos", "Otros ingresos", "↗", TipoMovimiento.INGRESO),
        Categoria("comida", "Alimentación", "🍽", TipoMovimiento.GASTO),
        Categoria("transporte", "Transporte", "🚌", TipoMovimiento.GASTO),
        Categoria("hogar", "Hogar", "🏠", TipoMovimiento.GASTO),
        Categoria("estudios", "Estudios", "📚", TipoMovimiento.GASTO),
        Categoria("salud", "Salud", "💚", TipoMovimiento.GASTO),
        Categoria("ocio", "Ocio", "🎬", TipoMovimiento.GASTO),
        Categoria("otros_gastos", "Otros gastos", "↘", TipoMovimiento.GASTO)
    )

    /** Solo las de un tipo (para el selector del formulario). */
    fun deTipo(tipo: TipoMovimiento): List<Categoria> = todas.filter { it.tipo == tipo }

    /** Busca por id. Si no existe (por ejemplo, la borraste del código) devuelve una genérica. */
    fun buscar(id: String): Categoria =
        todas.firstOrNull { it.id == id } ?: Categoria(id, "Sin categoría", "•", TipoMovimiento.GASTO)
}
