# 10 · Avance y pendientes

> **Corte:** 15 de octubre de 2026, **versión 2**.
> **Estado general:** la app compila (v1 probada en el celular), el login con Google funciona y los datos se guardan en Firestore. La v2 agrega dólar, avisos, gráfico de 6 meses y ahorro mensual; **falta compilarla y probarla en el celular**. **≈ 85 % del proyecto.**

---

## 1. Avance por área

| Área | Avance | Qué falta |
| --- | --- | --- |
| Funciones de la app (5 pestañas, login, huella, perfil) | **100 %** | — |
| Funciones v2 (dólar, avisos, gráfico, ahorro mensual) | **100 % programadas** | Compilar en Android Studio y probar HP-9 a HP-11 |
| Firebase (Auth con Google, Firestore, reglas) | **100 %** | — (la v2 no necesita cambios en Firebase) |
| Código simple, en español y comentado | **100 %** | — |
| Pruebas unitarias | **100 %** (9 de 9) | — |
| Documentación (esta carpeta) | **95 %** | Agregar capturas nuevas |
| Pruebas manuales en el celular | **~10 %** | Correr los 11 happy paths (`07_Plan_de_pruebas.md`) |
| Presentación | **0 %** | Armar diapositivas con los diagramas y capturas |
| **Total** | **≈ 85 %** | |

**La app no necesita pantallas nuevas.** Lo que falta es probar, sacar capturas y presentar.

## 2. Qué está terminado

### Funciones

| Pantalla | Estado |
| --- | --- |
| Login con Google | ✅ Probado |
| Huella / rostro / PIN | ✅ Hecho (falta probar en el celular) |
| Saldo inicial | ✅ |
| Inicio (saldo, alertas, este mes, recientes, metas) | ✅ |
| Historial (filtro, buscador, editar, eliminar) | ✅ |
| Metas (crear, aportar, retirar, eliminar) | ✅ |
| Límites (por categoría, otros meses) | ✅ |
| Reportes (resultado, barras, otros meses) | ✅ |
| Mi perfil (tema, huella, avisos, saldo inicial, cerrar sesión) | ✅ |
| Uso sin internet | ✅ |
| Pantalla de error con Reintentar | ✅ |
| **Dólar en Inicio** (oficial + paralelo, disponible en US$) · v2 | ✅ Programado, falta probar |
| **Avisos diarios** (WorkManager, permiso, "te quedan Bs X y faltan Y días") · v2 | ✅ Programado, falta probar |
| **Gráfico de 6 meses** · v2 | ✅ Programado, falta probar |
| **Ahorro mensual sugerido en metas con fecha** · v2 | ✅ Programado, falta probar |
| **Colores legibles en tema oscuro** · v2 | ✅ |

### Firebase

| Servicio | Estado |
| --- | --- |
| App `com.michanchito.app` con tu SHA-1 | ✅ |
| Authentication → Google | ✅ |
| Firestore en São Paulo, modo producción | ✅ |
| Reglas publicadas | ✅ |

## 3. Historia del proyecto

| Etapa | Qué se hizo |
| --- | --- |
| Versión original ("Centavo") | Diseño de pantallas y una base de datos SQL con tablas de usuarios, categorías y saldos |
| Revisión | Se detectó: base SQL para un proyecto que usa Firestore, saldo guardado (riesgo de desincronizar), categorías como tabla, login sin Firebase |
| Refactorización | Login con Google + Firebase Auth (método de clase), Firestore NoSQL en árbol, MVVM + Clean, montos en centavos, saldos calculados, huella opcional, DataStore |
| Firebase propio | Proyecto nuevo con paquete `com.michanchito.app` para no chocar con el Firebase anterior |
| Revisiones de calidad | Ver tabla de correcciones |
| Versión 2 (15/10/2026) | Investigación de apps parecidas y APIs públicas (`12_Ideas_de_mejora_y_APIs.md`); se implementaron 4 ideas (dólar, avisos, gráfico, ahorro mensual); revisión de paleta (`13_Diseno_UX_UI.md`) |
| Simplificación (16/10/2026) | Se quitaron movimientos fijos y deudas para que el proyecto sea más fácil de explicar y defender; la base vuelve a 3 subcolecciones |

### Correcciones importantes

| Problema | Solución |
| --- | --- |
| Activar la huella bloqueaba al usuario en la misma sesión | `cambiarBiometria(true)` marca la huella como validada |
| Pantalla de carga infinita si Firestore fallaba | `errorCarga` + `PantallaError` con Reintentar |
| El teclado tapaba los campos | `safeDrawingPadding` |
| Una fecha mal escrita podía cerrar la app | `leerFecha` devuelve `null` en vez de fallar |
| Snackbars repetidos o perdidos | `Channel` para los avisos |
| Íconos de la barra de estado invisibles en tema oscuro | `WindowCompat` en `MiChanchitoTema` |
| Porcentajes truncados (420 de 400 = 104 %) | `roundToInt` (ahora 105 %) |
| Posible cierre en Android 8 con la ventana de huella | `minSdk = 28` |
| Ícono de la app no adaptativo | Ícono adaptativo con el logo |
| Verde y rojo poco legibles en tema oscuro (v2) | Colores semánticos en el `ColorScheme`, aclarados en oscuro |
| Mi perfil no tenía flecha de regreso (v2, reportado por Mirko) | Barra superior con título de la pantalla y `navigationIcon` ← que hace `popBackStack()`; la pantalla se apila sobre la actual |

## 4. Qué falta para terminar (en orden)

| # | Tarea | Tiempo | Cómo |
| --- | --- | --- | --- |
| 0 | **Compilar la v2**: copiar el proyecto nuevo (con tu `google-services.json` en `app/`), Sync, Run, y correr las 9 pruebas | ~15 min | Si algo no compila, copia el error en el chat |
| 1 | Correr los **11 happy paths** | ~1,5 horas | `07_Plan_de_pruebas.md`; marcar ✅/❌ |
| 2 | Cargar los **datos de demo** | ~10 min | `09_Guia_de_presentacion.md` §2, con una cuenta solo para la demo |
| 3 | Sacar **capturas** de cada pantalla | ~15 min | Botón de encendido + bajar volumen |
| 4 | Armar la **presentación** | — | Orden de diapositivas en `09_Guia_de_presentacion.md` §1 |
| 5 | Generar el **APK** | ~5 min | Build → Generate App Bundles or APKs → **Generate APKs** |
| 6 | **Ensayar** la demo de 6 minutos | ~20 min | Dos veces, con cronómetro |

**Si tu compañero quiere compilar en su PC:** te manda su SHA-1 (`.\gradlew.bat signingReport`) y lo agregas en Firebase → Configuración → Tus apps → Agregar huella digital. Le pasas tu `google-services.json` por privado. Si solo quiere usar la app, el APK de tu PC le funciona tal cual.

## 5. Mejoras posibles (solo si el docente pide más)

| Mejora | Dificultad | Qué tocaría |
| --- | --- | --- |
| Ofrecer "¿activar huella?" justo después del primer login | Baja | `AuthViewModel` + un `AlertDialog` en `AppNavigation` |
| Pedir huella también al volver de segundo plano | Baja | Observar `ON_STOP` y poner `biometriaValidada = false` |
| Editar nombre u objetivo de una meta | Baja | Un diálogo más + `update` en el repositorio |
| Mostrar la foto de Google en Mi perfil | Baja | Ya se guarda `fotoUrl`; falta la librería Coil |
| Íconos vectoriales en la barra de pestañas | Baja | Ver `13_Diseno_UX_UI.md` |
| Widget de pantalla de inicio con el disponible | Media | Glance |
| Precios de referencia de la canasta familiar (curados desde el OAP) | Media | Ver `12_Ideas_de_mejora_y_APIs.md`, idea 9 |
| Exportar historial a CSV | Media | FileProvider + Intent de compartir |
| Validar campos en las reglas de Firestore | Media | Ejemplo en `06_Seguridad.md` §8 |
| Pruebas de interfaz (Compose UI tests) | Media | Carpeta `androidTest` |
| Inyección de dependencias (Hilt) | Media | Módulos para repositorio y helpers |

## 6. Riesgos para la entrega

| Riesgo | Probabilidad | Plan |
| --- | --- | --- |
| No hay internet en el aula | Media | La app funciona offline; capturas de la consola como respaldo |
| El celular de la demo no tiene huella configurada | Baja | Configurar PIN antes; la ventana acepta PIN |
| El login falla en otra PC (código 10) | Media | Agregar su SHA-1 o usar el APK de tu PC |
| Los números de la demo no coinciden | Baja | Revisar que todas las fechas sean del mes actual |
| La v2 no compila a la primera | Baja-media | El código fue revisado dos veces, pero no se compiló aquí (no hay SDK). Un error de compilación se corrige en minutos: copia el mensaje en el chat |
| dolarapi.com no responde en la demo | Baja | La tarjeta muestra Reintentar; el resto sigue. Ten una captura del dólar como respaldo |
