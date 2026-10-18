plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Lee app/google-services.json y genera R.string.default_web_client_id (lo usa el login con Google)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.michanchito"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        // applicationId = el nombre de la app para Android y para Firebase.
        // OJO: debe ser EXACTAMENTE el paquete que registres en Firebase.
        // (El namespace de arriba es solo la carpeta del código; pueden ser distintos.)
        applicationId = "com.michanchito.app"
        minSdk = 28          // Android 9 o superior: java.time y la ventana de huella del sistema
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ---------- Android + Compose ----------
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)    // collectAsStateWithLifecycle()
    implementation(libs.androidx.lifecycle.viewmodel.compose)  // viewModel() en un @Composable
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)           // NavHost, rutas
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)   // Icons.AutoMirrored.Filled.ArrowBack (flecha de regreso)

    // ---------- Firebase (el BoM elige las versiones) ----------
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)        // Authentication
    implementation(libs.firebase.firestore)   // Base de datos NoSQL
    implementation(libs.kotlinx.coroutines.play.services) // .await()

    // ---------- Login con Google (Credential Manager) ----------
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    // ---------- Huella / PIN y preferencias ----------
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore.preferences)

    // ---------- API REST del dólar (Retrofit + Gson) ----------
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)

    // ---------- Notificaciones diarias (WorkManager) ----------
    implementation(libs.androidx.work.runtime)

    // ---------- Pruebas de la lógica (sin Android) ----------
    testImplementation(libs.junit)
}
