package com.example.michanchito

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.michanchito.presentation.navigation.AppNavigation

/*
 * Punto de entrada de la app.
 * FragmentActivity (y no ComponentActivity) porque BiometricPrompt la necesita.
 * FragmentActivity igual hereda de ComponentActivity, así que setContent { } funciona.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}
