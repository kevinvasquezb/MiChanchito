package com.example.michanchito.data.api

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/*
 * HUELLA / ROSTRO / PIN con BiometricPrompt (como en clase, con 2 correcciones):
 *
 *  - BIOMETRIC_WEAK or DEVICE_CREDENTIAL: el usuario elige huella, rostro o el PIN/patrón del celular.
 *    (El rostro de muchos celulares es "weak"; con STRONG solo saldría la huella.)
 *  - Con DEVICE_CREDENTIAL NO se pone setNegativeButtonText("Cancelar"):
 *    el botón "Usar PIN" ocupa ese lugar y si lo pones la app se cae (IllegalArgumentException).
 *  - onAuthenticationFailed (huella no reconocida) NO cierra nada: el diálogo deja reintentar.
 *
 * BiometricPrompt necesita una FragmentActivity: por eso MainActivity extiende FragmentActivity.
 */
object BiometricHelper {

    private const val AUTENTICADORES = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    /** ¿El celular tiene huella, rostro o PIN configurado? */
    fun estaDisponible(activity: FragmentActivity): Boolean =
        BiometricManager.from(activity).canAuthenticate(AUTENTICADORES) == BiometricManager.BIOMETRIC_SUCCESS

    fun autenticar(activity: FragmentActivity, alExito: () -> Unit, alError: (String) -> Unit) {
        if (!estaDisponible(activity)) {
            alError("Configura una huella o un bloqueo de pantalla en tu celular.")
            return
        }

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                alExito()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                alError(errString.toString())   // canceló o demasiados intentos
            }
        }

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear Mi Chanchito")
            .setSubtitle("Usa tu huella, rostro o PIN")
            .setAllowedAuthenticators(AUTENTICADORES)
            .build()

        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), callback).authenticate(info)
    }
}
