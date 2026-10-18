package com.example.michanchito.data.api

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.michanchito.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

/*
 * LOGIN CON GOOGLE — los mismos pasos del proyecto de la profe:
 *
 *  1. Opción "Iniciar sesión con Google" con el default_web_client_id
 *  2. Solicitud con esa opción
 *  3. Credential Manager muestra el selector de cuentas
 *  4. Recibimos la credencial
 *  5. Verificamos que sea de Google y sacamos el ID TOKEN
 *  6. Convertimos el token en credencial de Firebase
 *  7. Iniciamos sesión en Firebase -> nos da el usuario con su UID
 *
 * Firebase GUARDA LA SESIÓN SOLO: al abrir la app de nuevo, FirebaseAuth.currentUser ya no es null.
 */
class GoogleAuthHelper(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    /** [context] debe ser la Activity (para poder mostrar la ventana de Google). */
    suspend fun iniciarSesion(): Result<FirebaseUser> {
        return try {
            // 1. default_web_client_id lo GENERA el plugin google-services desde google-services.json
            val opcionGoogle = GetSignInWithGoogleOption
                .Builder(context.getString(R.string.default_web_client_id))
                .build()

            // 2.
            val solicitud = GetCredentialRequest.Builder()
                .addCredentialOption(opcionGoogle)
                .build()

            // 3. y 4. (suspend: espera a que el usuario elija su cuenta)
            val credencial = credentialManager.getCredential(context, solicitud).credential

            // 5.
            if (credencial is CustomCredential &&
                credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val idToken = GoogleIdTokenCredential.createFrom(credencial.data).idToken

                // 6.
                val credencialFirebase = GoogleAuthProvider.getCredential(idToken, null)

                // 7.
                val usuario = auth.signInWithCredential(credencialFirebase).await().user
                    ?: return Result.failure(Exception("Firebase no devolvió el usuario."))
                Result.success(usuario)
            } else {
                Result.failure(Exception("La credencial no es de Google."))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)   // el usuario cerró la ventana: no es un error real
        } catch (e: NoCredentialException) {
            Result.failure(Exception("No hay una cuenta Google en el celular. Agrega una en Ajustes."))
        } catch (e: CancellationException) {
            throw e             // regla de corrutinas: la cancelación siempre se vuelve a lanzar
        } catch (e: Exception) {
            Result.failure(e)   // sin internet, o falta el SHA-1 en Firebase
        }
    }

    /** Cierra la sesión en Firebase y hace que Google vuelva a preguntar la cuenta. */
    suspend fun cerrarSesion() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            // si falla no importa: Firebase ya cerró la sesión
        }
    }
}
