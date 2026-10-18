# Mi Chanchito 🐷

App Android de finanzas personales (Kotlin + Jetpack Compose) para la materia de Programación de Aplicaciones Móviles.

- Login con **Google** (Credential Manager → Firebase Authentication)
- **Huella, rostro o PIN** opcional al abrir (BiometricPrompt)
- Datos en **Cloud Firestore** (NoSQL), funcionan sin internet
- Preferencias en **DataStore** (tema oscuro, pedir huella, avisos diarios)
- **Dólar** oficial y paralelo desde una API REST (**Retrofit**) y el disponible en US$
- **Avisos diarios** por notificación con **WorkManager** ("te quedan Bs X y faltan Y días")
- Gráfico de **6 meses** y "ahorra Bs X al mes" en metas con fecha
- Arquitectura **MVVM** con carpetas de Clean Architecture

> **Documentación completa** (13 documentos y 14 diagramas explicados): carpeta `MiChanchito-Documentacion`, aparte del código.

## Antes de compilar

Necesitas `app/google-services.json` de tu proyecto Firebase. Pasos:

1. Firebase Console → crear proyecto → **Agregar app Android** con el paquete `com.michanchito.app` (el `applicationId` de `app/build.gradle.kts`).
2. En la terminal de Android Studio: `.\gradlew.bat signingReport` → copia el **SHA1** de `Variant: debug`.
   Agrega en Firebase el SHA-1 de **cada PC** que va a compilar (Configuración del proyecto → Tus apps → Agregar huella digital).
3. **Authentication** → Método de acceso → **Google** → Habilitar.
4. **Recién ahora** descarga `google-services.json` y ponlo en la carpeta `app/`.
5. **Firestore Database** → Crear base de datos → modo **producción** → pestaña **Reglas** → pega `firestore.rules` → Publicar.
6. Sync Project with Gradle Files → ▶ Run.

## Estructura

```
com.example.michanchito
├── MainActivity.kt                       FragmentActivity (la necesita BiometricPrompt)
├── domain/
│   ├── model/        Dinero, Movimiento, Categoria, Meta, Presupuesto, Dolar, Perfil
│   └── usecase/      CalculosFinanzas, Avisos, Fechas  (todas las reglas, Kotlin puro)
├── data/
│   ├── api/          GoogleAuthHelper, BiometricHelper, DolarApi (Retrofit)
│   ├── database/     DocumentosFirestore  (data class ↔ JSON)
│   ├── datastore/    PreferenciasApp
│   ├── notificaciones/ NotificacionesHelper, AvisosWorker  (WorkManager)
│   └── repository/   FinanzasRepositorio  (ÚNICO archivo que usa Firestore)
└── presentation/
    ├── viewModel/    AuthViewModel, FinanzasViewModel
    ├── navigation/   AppNavigation (el "portero"), PantallasPrincipales (NavHost + pestañas)
    └── ui/           Pantallas, diálogos, componentes y tema
```

## Base de datos (Firestore)

Diagrama: `docs/diagramas/03_base-de-datos.png`. Para editarlo en https://dbdiagram.io pega `docs/base-de-datos.dbml`.

```
usuarios/{uid}                        nombre, correo, fotoUrl, saldoInicialCentavos
usuarios/{uid}/movimientos/{id}       tipo, montoCentavos, categoriaId, fecha, descripcion
usuarios/{uid}/metas/{id}             nombre, objetivoCentavos, ahorradoCentavos, fechaObjetivo
usuarios/{uid}/presupuestos/{catId}   categoriaId, limiteCentavos
```

Todo el dinero va en **centavos** (`Long`): Bs 25,50 = `2550`, por eso los campos terminan en `Centavos` (dice la unidad, como `timeoutMillis`). Los saldos no se guardan: se calculan. El dólar no está en la base: viene de `bo.dolarapi.com`.

```
saldo total = saldo inicial + ingresos − gastos
reservado   = suma de lo ahorrado en las metas
disponible  = saldo total − reservado
```

## Pruebas

`.\gradlew.bat :app:testDebugUnitTest` (o clic derecho en `app/src/test` → Run Tests): 9 pruebas (matemática, avisos, ahorro mensual, 6 meses, dólar).

## Diagramas

`docs/diagramas/`: 14 diagramas, cada uno con su `.mmd` (texto Mermaid que se edita en https://mermaid.live) y su `.png` para las diapositivas. La explicación de cada uno está en `MiChanchito-Documentacion/diagramas/*.md`.
