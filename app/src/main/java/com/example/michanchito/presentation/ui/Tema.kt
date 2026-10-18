package com.example.michanchito.presentation.ui

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/*
 * PALETA DE LA APP
 *   Morado  = marca (botones, pestaña activa, barras de progreso)
 *   Verde   = dinero que ENTRA (ingresos)
 *   Rojo    = dinero que SALE (gastos, límites superados)
 *
 * Los colores de ingreso/gasto viven en el ColorScheme (tertiary = ingreso, error = gasto),
 * así cambian solos con el tema: en oscuro se aclaran para que se lean bien sobre fondo oscuro.
 * Contraste medido (WCAG): claro 5,0:1 y 5,6:1 sobre blanco; oscuro 9,5:1 y 7,6:1 sobre la tarjeta.
 */
val MoradoChanchito = Color(0xFF6550A4)
private val VerdeIngreso = Color(0xFF187D66)
private val RojoGasto = Color(0xFFBA374B)
private val VerdeIngresoOscuro = Color(0xFF6FD3B0)
private val RojoGastoOscuro = Color(0xFFFF8A96)

/** Color para montos que ENTRAN (según el tema). Uso: MaterialTheme.colorScheme.ingreso */
val ColorScheme.ingreso: Color get() = this.tertiary

/** Color para montos que SALEN (según el tema). Uso: MaterialTheme.colorScheme.gasto */
val ColorScheme.gasto: Color get() = this.error

private val EsquemaClaro = lightColorScheme(
    primary = MoradoChanchito,
    secondary = VerdeIngreso,
    tertiary = VerdeIngreso,
    error = RojoGasto,
    background = Color(0xFFF8F7FC),
    surface = Color.White,
    surfaceContainer = Color(0xFFF0EDF7)
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFF85D8BD),
    tertiary = VerdeIngresoOscuro,
    error = RojoGastoOscuro,
    background = Color(0xFF14121B),
    surface = Color(0xFF1D1A24),
    surfaceContainer = Color(0xFF262230)
)

@Composable
fun MiChanchitoTema(oscuro: Boolean, contenido: @Composable () -> Unit) {
    // Íconos de la barra de estado (hora, batería): oscuros en tema claro y claros en tema oscuro
    val vista = LocalView.current
    if (!vista.isInEditMode) {
        SideEffect {
            val ventana = (vista.context as Activity).window
            WindowCompat.getInsetsController(ventana, vista).apply {
                isAppearanceLightStatusBars = !oscuro
                isAppearanceLightNavigationBars = !oscuro
            }
        }
    }

    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        shapes = Shapes(medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = contenido
    )
}
