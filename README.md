# Mi Chanchito 🐷

App Android de **finanzas personales**, hecha con Kotlin y Jetpack Compose, para la materia de Programación de Aplicaciones Móviles.

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black)
![Estado](https://img.shields.io/badge/estado-en%20desarrollo-yellow)

## Índice

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Modelo de datos (Firestore)](#modelo-de-datos-firestore)
- [Cómo compilar](#cómo-compilar)
- [Pruebas](#pruebas)
- [Documentación y diagramas](#documentación-y-diagramas)
- [Estado del proyecto](#estado-del-proyecto)

## Características

- 🔑 Login con **Google** (Credential Manager → Firebase Authentication)
- 🔒 **Huella, rostro o PIN** opcional al abrir la app (BiometricPrompt)
- ☁️ Datos en **Cloud Firestore** (NoSQL), con soporte **sin internet**
- ⚙️ Preferencias locales en **DataStore** (tema oscuro, pedir huella, avisos diarios)
- 💵 Cotización del **dólar** oficial y paralelo vía API REST (**Retrofit**), con el disponible convertido a US$
- 🔔 **Avisos diarios** por notificación con **WorkManager** ("te quedan Bs X y faltan Y días")
- 📊 Gráfico de los últimos **6 meses** y sugerencia de ahorro mensual en metas con fecha
- 🏗️ Arquitectura **MVVM** con carpetas inspiradas en Clean Architecture

> Documentación completa (documentos y diagramas explicados) en [`docs/`](docs).

## Arquitectura

MVVM + capas de Clean Architecture:

```
UI (Compose)  →  ViewModel  →  Repositorio  →  Firestore / Retrofit / DataStore
```

- **domain**: modelos y reglas de negocio puras en Kotlin (sin dependencias de Android)
- **data**: acceso a Firebase, API del dólar, DataStore y notificaciones
- **presentation**: pantallas, navegación y ViewModels

## Estructura del proyecto

```
com.example.michanchito
├── MainActivity.kt                       FragmentActivity (la necesita BiometricPrompt)
├── domain/
│   ├── model/        Dinero, Movimiento, Categoria, Meta, Presupuesto, Dolar, Perfil
│   └── usecase/      CalculosFinanzas, Avisos, Fechas  (todas las reglas, Kotlin puro)
├── data/
│   ├── api/              GoogleAuthHelper, BiometricHelper, DolarApi (Retrofit)
│   ├── database/         DocumentosFirestore (data class ↔ JSON)
│   ├── datastore/        PreferenciasApp
│   ├── notificaciones/   NotificacionesHelper, AvisosWorker (WorkManager)
│   └── repository/       FinanzasRepositorio (único archivo que usa Firestore)
└── presentation/
    ├── viewModel/    AuthViewModel, FinanzasViewModel
    ├── navigation/   AppNavigation (el "portero"), PantallasPrincipales (NavHost + pestañas)
    └── ui/           Pantallas, diálogos, componentes y tema
```

## Modelo de datos (Firestore)

Diagrama en [`docs/diagramas/03_base-de-datos.png`](docs/diagramas). Para editarlo en [dbdiagram.io](https://dbdiagram.io) pega [`docs/base-de-datos.dbml`](docs/base-de-datos.dbml).

```
usuarios/{uid}                        nombre, correo, fotoUrl, saldoInicialCentavos
usuarios/{uid}/movimientos/{id}       tipo, montoCentavos, categoriaId, fecha, descripcion
usuarios/{uid}/metas/{id}             nombre, objetivoCentavos, ahorradoCentavos, fechaObjetivo
usuarios/{uid}/presupuestos/{catId}   categoriaId, limiteCentavos
```

Todo el dinero se guarda en **centavos** (`Long`): Bs 25,50 = `2550`. Los saldos no se almacenan, se calculan:

```
saldo total = saldo inicial + ingresos − gastos
reservado   = suma de lo ahorrado en las metas
disponible  = saldo total − reservado
```

El dólar no vive en la base de datos: se consulta a `bo.dolarapi.com`.

## Cómo compilar

Necesitas tu propio `app/google-services.json` de Firebase (no se sube al repo por seguridad).

1. En [Firebase Console](https://console.firebase.google.com) crea un proyecto → **Agregar app Android** con el paquete `com.michanchito.app` (el `applicationId` de `app/build.gradle.kts`).
2. En la terminal del proyecto: `.\gradlew.bat signingReport` → copia el **SHA-1** de `Variant: debug` y agrégalo en Firebase (Configuración del proyecto → Tus apps → Agregar huella digital).
3. **Authentication** → Método de acceso → habilita **Google**.
4. Descarga `google-services.json` y colócalo en `app/`.
5. **Firestore Database** → Crear base de datos → modo **producción** → pestaña **Reglas** → pega el contenido de [`firestore.rules`](firestore.rules) → Publicar.
6. En Android Studio: *Sync Project with Gradle Files* → ▶ Run.

## Pruebas

```bash
.\gradlew.bat :app:testDebugUnitTest
```

9 pruebas unitarias sobre la lógica de negocio (cálculos financieros, avisos, ahorro mensual, gráfico de 6 meses, dólar).

## Documentación y diagramas

En [`docs/`](docs) hay documentos de avance (`AVANCE.md`), pruebas (`PRUEBAS_Y_DEMOS.md`) y 14 diagramas (casos de uso, arquitectura, Firestore, flujos y secuencias) en [`docs/diagramas/`](docs/diagramas), cada uno con su fuente `.mmd` (editable en [mermaid.live](https://mermaid.live)) y su `.png`.

## Estado del proyecto

Proyecto académico en desarrollo activo (~85 % completado). Ver el detalle de avances y pendientes en [`docs/AVANCE.md`](docs/AVANCE.md).

---

Hecho con 🐷 para la materia de Programación de Aplicaciones Móviles.
