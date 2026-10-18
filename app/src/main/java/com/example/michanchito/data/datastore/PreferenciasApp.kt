package com.example.michanchito.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/*
 * DATASTORE = preferencias pequeñas guardadas EN EL CELULAR (clave -> valor).
 * Reemplaza a SharedPreferences.
 *
 * Guardamos solo 3 cosas del dispositivo:
 *   tema_oscuro         -> true / false
 *   biometria_activada  -> pedir huella al abrir la app
 *   avisos_activados    -> notificación diaria con los límites del mes
 *
 * La SESIÓN no se guarda aquí: Firebase ya la guarda solo.
 * Los DATOS de dinero tampoco: esos van a Firestore.
 */

// Se crea UNA sola vez, a nivel de archivo. Nombre del archivo en el celular: "preferencias"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

class PreferenciasApp(context: Context) {

    private val store = context.applicationContext.dataStore

    private val claveTemaOscuro = booleanPreferencesKey("tema_oscuro")
    private val claveBiometria = booleanPreferencesKey("biometria_activada")
    private val claveAvisos = booleanPreferencesKey("avisos_activados")

    /** Si el archivo se daña, usamos los valores por defecto en vez de cerrar la app. */
    private val datos: Flow<Preferences> = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    // LEER: son Flow, avisan cada vez que cambian
    val temaOscuro: Flow<Boolean> = datos.map { it[claveTemaOscuro] ?: false }
    val biometriaActivada: Flow<Boolean> = datos.map { it[claveBiometria] ?: false }
    val avisosActivados: Flow<Boolean> = datos.map { it[claveAvisos] ?: false }

    // ESCRIBIR: son suspend, se llaman dentro de una corrutina
    suspend fun guardarTemaOscuro(valor: Boolean) {
        store.edit { it[claveTemaOscuro] = valor }
    }

    suspend fun guardarBiometria(valor: Boolean) {
        store.edit { it[claveBiometria] = valor }
    }

    suspend fun guardarAvisos(valor: Boolean) {
        store.edit { it[claveAvisos] = valor }
    }
}
