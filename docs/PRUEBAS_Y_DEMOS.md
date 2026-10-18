# Pruebas y demos

> Copia de `07_Plan_de_pruebas.md` y `09_Guia_de_presentacion.md` de la carpeta `MiChanchito-Documentacion` (esa carpeta es la versión principal).

---

# 07 · Plan de pruebas

> Para los guiones de la presentación ver `09_Guia_de_presentacion.md`.

---

## 1. Estrategia

| Nivel | Qué se prueba | Herramienta | Cantidad |
| --- | --- | --- | --- |
| **Unitarias** | La matemática, los avisos, el ahorro mensual, los 6 meses y el dólar | JUnit 4 en la PC | 9 pruebas |
| **Funcionales (manuales)** | Cada función de la app, paso a paso | Celular o emulador | 32 casos (CP-01 a CP-32) |
| **Flujos completos (happy paths)** | Recorridos reales de un usuario | Celular o emulador | 11 flujos (HP-1 a HP-11) |
| **Caminos alternativos** | Errores esperados y validaciones | Celular o emulador | 6 casos (CA-01 a CA-06) |
| **Verificación en la nube** | Que los datos lleguen bien a Firestore | Consola de Firebase | Dentro de los casos |

**Entorno:** Android Studio, emulador con Google Play (API 34 o superior) o celular Android 9+, cuenta de Google de pruebas, consola de Firebase abierta.

**Formato de montos:** la app muestra `Bs 1.200,00` (punto para miles, coma para decimales).

## 2. Pruebas unitarias (automáticas)

Archivos: `app/src/test/java/com/example/michanchito/CalculosFinanzasTest.kt` y `FuncionesNuevasTest.kt`.
Cómo correrlas: clic derecho en `app/src/test` → **Run Tests**, o `.\gradlew.bat :app:testDebugUnitTest`.

| # | Prueba | Qué verifica |
| --- | --- | --- |
| 1 | `convierteTextoACentavos` | `" 25,50 "` → 2550, `"25.5"` → 2550, `"0.10"` → 10, `"0"` con `permitirCero`, y `aTexto(2550) = "25.50"` |
| 2 | `rechazaMontosInvalidos` | `""`, `"-1"`, `"0"`, `"1.234"`, `"abc"`, `"1e3"` y `"1,000.00"` lanzan error |
| 3 | `saldoTotalReservadoYDisponible` | Las 3 fórmulas con ingresos, gastos y metas |
| 4 | `resumenSoloCuentaElMesPedido` | El resumen ignora movimientos de otros meses y agrupa por categoría |
| 5 | `validaAportesYRetiros` | Los mensajes de `errorAlAportar` y `errorAlRetirar` |
| 6 | `ordenaDelMasNuevoAlMasViejo` | El orden del historial |
| 7 | `avisosDeLimitesDicenCuantoQuedaYCuantosDiasFaltan` | 80 % justo → "Te quedan Bs 80,00. Faltan 16 días…"; superado → "Llevas Bs 180,00 de Bs 150,00…"; otro mes no cuenta; el 31 → "Hoy termina el mes."; el 30 → "Falta 1 día…" |
| 8 | `ahorroMensualParaLlegarALaMeta` | Bs 1.500 en 3 meses = Bs 500; redondeo hacia arriba; null sin fecha / cumplida / fecha inválida; fecha pasada = todo lo que falta |
| 9 | `ultimosSeisMesesYDolar` | 6 meses de mayo a octubre con los totales correctos; Bs 1.045 a 11,84 = US$ 88,26 |

**Resultado esperado:** 9 de 9 en verde. **Resultado obtenido:** ✅ 9 de 9.

## 3. Casos de prueba funcionales

Hazlos **en orden** con una cuenta nueva (o borra tu documento en Firestore → `usuarios`). Cada caso usa los números del anterior.

### 3.1 Acceso

| ID | Objetivo | Pasos | Resultado esperado | ✓ |
| --- | --- | --- | --- | --- |
| CP-01 | Ver el login | Instalar y abrir la app | Logo, "Tu dinero, con más claridad." y **Continuar con Google** | |
| CP-02 | Entrar con Google | Continuar con Google → elegir cuenta | Pantalla **"Empecemos por tu saldo"** | |
| CP-03 | Saldo inicial | Escribir `500` → **Empezar** | Inicio: **Saldo total Bs 500,00**, disponible Bs 500,00, Bs 0,00 reservado. Firestore: `saldoInicialCentavos: 50000` | |

### 3.2 Movimientos

| ID | Objetivo | Pasos | Resultado esperado | ✓ |
| --- | --- | --- | --- | --- |
| CP-04 | Registrar ingreso | **+ Ingreso** → `1000`, 💼 Sueldo, "Sueldo octubre" → Guardar | "Ingreso guardado." Saldo **Bs 1.500,00** | |
| CP-05 | Registrar gasto | **− Gasto** → `300`, 🍽 Alimentación, "Supermercado" → Guardar | "Gasto guardado." Saldo **Bs 1.200,00** | |
| CP-06 | Ver en Firestore | Consola → Firestore → Datos | `movimientos` con 2 documentos; el gasto con `montoCentavos: 30000`, `tipo: "GASTO"`, `categoriaId: "comida"` | |
| CP-07 | Filtrar y buscar | Historial → chip **Gastos**; luego **Todos** y buscar `sueldo` | Solo Supermercado; luego solo el ingreso | |
| CP-08 | Editar | Tocar Supermercado → `320` → Guardar | "Movimiento actualizado." Saldo **Bs 1.180,00** | |
| CP-09 | Eliminar | − Gasto `10` Ocio "Prueba" → tocarlo → **Eliminar movimiento** → Eliminar | "Movimiento eliminado." Saldo **Bs 1.180,00** | |

### 3.3 Metas

| ID | Objetivo | Pasos | Resultado esperado | ✓ |
| --- | --- | --- | --- | --- |
| CP-10 | Crear meta | Metas → **+ Nueva meta** → "Celular", `1500` → Crear | "Celular" 0 %, Bs 0,00 de Bs 1.500,00 | |
| CP-11 | Aportar | **Aportar** → `200` | Meta **13 %**. Saldo total **Bs 1.180,00**, disponible **Bs 980,00**, Bs 200,00 reservado | |
| CP-12 | Retirar | **Retirar** → `50` | Meta **10 %**. Disponible **Bs 1.030,00**, reservado Bs 150,00 | |

### 3.4 Límites y reportes

| ID | Objetivo | Pasos | Resultado esperado | ✓ |
| --- | --- | --- | --- | --- |
| CP-13 | Poner límite | Límites → Alimentación → **Poner límite** → `400` | "Bs 320,00 de Bs 400,00", **80 % usado**. Inicio: "Ya usaste más del 80 % del límite de Alimentación." | |
| CP-14 | Pasarse del límite | − Gasto `100` Alimentación | **Límite superado · 105 %** en rojo. Inicio: "Te pasaste del límite de Alimentación." Saldo **Bs 1.080,00** | |
| CP-15 | Reporte | Reportes | Ingresos Bs 1.000,00 · Gastos Bs 420,00 · **Resultado Bs 580,00** | |
| CP-16 | Otro mes | **‹ Anterior** | Bs 0,00 y "Un mes sin gastos" | |

### 3.5 Perfil, huella y sesión

| ID | Objetivo | Pasos | Resultado esperado | ✓ |
| --- | --- | --- | --- | --- |
| CP-17 | Tema oscuro | Mi perfil → Tema oscuro | Toda la app en oscuro; sigue así al reabrir | |
| CP-18 | Cambiar saldo inicial | Saldo inicial · Cambiar → `600` | Saldo total **Bs 1.180,00** | |
| CP-19 | Sesión recordada | Cerrar la app (de recientes) y abrirla | Entra directo, **sin** pedir Google | |
| CP-20 | Activar huella | Pedir huella al abrir → confirmar | Interruptor encendido | |
| CP-21 | Entrar con huella | Cerrar la app del todo y abrirla | "Tu dinero es personal" + huella → Inicio | |
| CP-22 | Sin internet | Modo avión → − Gasto `15` Transporte | Aparece al instante; saldo **Bs 1.165,00**; en la consola aún no | |
| CP-23 | Sincronizar | Quitar modo avión | El gasto aparece en Firestore | |
| CP-24 | Cerrar sesión | Cerrar sesión → confirmar | Login | |
| CP-25 | Volver a entrar | Misma cuenta | Todos los datos; **no** pide saldo inicial | |
| CP-26 | Privacidad | Entrar con **otra** cuenta | Pide saldo inicial; no ve nada de la primera | |

### 3.6 Dólar, metas con fecha, gráfico y avisos (v2)

Sigue con la **misma cuenta de CP-01** (saldo Bs 1.165,00 después de CP-23; límite de Alimentación 400 superado). Donde dice «hoy», usa la fecha real del día de la prueba.

| ID | Objetivo | Pasos | Resultado esperado | ✓ |
| --- | --- | --- | --- | --- |
| CP-27 | Dólar en Inicio | Abrir Inicio con internet | Tarjeta **Dólar hoy** con **Oficial (BCB)** y **Paralelo**, cada uno con venta y compra, y **"Tu disponible ≈ US$ X al paralelo"** (X = disponible ÷ venta del paralelo). Texto "Fuente: dolarapi.com…" | |
| CP-28 | Dólar sin internet | Modo avión → **Actualizar** | "No se pudo consultar el dólar. Revisa tu conexión." y botón **Reintentar**. El saldo y todo lo demás siguen igual. Quitar modo avión → Reintentar → vuelve a cargar | |
| CP-29 | Meta con fecha | Metas → **+ Nueva meta** → "Viaje", `600`, fecha objetivo = **último día del mes que viene** → Crear | La tarjeta dice **"Para llegar a tiempo: ahorra Bs 300,00 al mes"** (2 meses: este y el que viene) | |
| CP-30 | Gráfico de 6 meses | Reportes | Tarjeta **Últimos 6 meses** con 6 columnas (verde ingresos, rojo gastos) terminando en el mes actual; el mes actual tiene la barra verde más alta (Bs 1.000) y una roja (Bs 435). **‹ Anterior** desplaza la ventana un mes | |
| CP-31 | Activar avisos | Mi perfil → **Avisos diarios** → (Android 13+) **Permitir** | Switch encendido y aparece **Probar avisos ahora** | |
| CP-32 | Probar avisos | **Probar avisos ahora** → bajar la barra de notificaciones | En segundos llega: **"Te pasaste del límite de Alimentación."** / "Llevas Bs 420,00 de Bs 400,00. Faltan N días para fin de mes." Tocarla abre la app | |

> N = días del mes − día de hoy (el 15 de octubre, N = 16; el último día del mes, "Hoy termina el mes.").

## 4. Caminos alternativos

| ID | Pasos | Resultado esperado | ✓ |
| --- | --- | --- | --- |
| CA-01 | En un monto escribir `abc` o `1.234` | "Escribe un monto válido, con máximo dos decimales." | |
| CA-02 | Aportar más que el disponible | "No tienes suficiente dinero disponible para este aporte." | |
| CA-03 | Aportar más de lo que falta | "El aporte supera lo que falta para la meta." | |
| CA-04 | Fecha de mañana en un gasto | "Usa la fecha de hoy o una anterior." | |
| CA-05 | Cerrar el selector de cuentas sin elegir | No pasa nada, sin error | |
| CA-06 | Negar el permiso de notificaciones (Android 13+) | El switch "Avisos diarios" queda apagado; la app sigue normal | |

## 5. Happy paths (flujos completos)

Agrupan los casos anteriores en recorridos reales. Úsalos para probar rápido toda la app.

| Flujo | Cubre | Qué demuestra |
| --- | --- | --- |
| HP-1 Primer uso | CP-01 a CP-03 | Firebase Auth, Credential Manager, AuthStateListener, el portero, `merge` |
| HP-2 Ingresos y gastos | CP-04 a CP-06 | Validación, centavos, `set`, listener en tiempo real, cálculo del saldo |
| HP-3 Buscar, editar, eliminar | CP-07 a CP-09 | Filtros, `set` con id existente, `delete`, recálculo |
| HP-4 Metas | CP-10 a CP-12 | Aportar no es gastar, `FieldValue.increment`, validaciones |
| HP-5 Límites y reportes | CP-13 a CP-16 | Categoría como id, `resumenDelMes`, alertas 80 % / 100 % |
| HP-6 Tema, saldo, huella | CP-17 a CP-21 | DataStore, BiometricPrompt, sesión recordada |
| HP-7 Sin internet | CP-22 a CP-23 | Caché offline, escrituras sin `await` |
| HP-8 Salir y privacidad | CP-24 a CP-26 | `desconectar()`, cerrar sesión, reglas por UID |
| HP-9 Dólar | CP-27, CP-28 | Retrofit + corrutinas, manejo de error sin internet |
| HP-10 Metas con fecha | CP-29 | `ahorroMensualSugerido` (redondeo hacia arriba, meses contando el actual) |
| HP-11 Gráfico y avisos | CP-30 a CP-32, CA-06 | `ultimosMeses`, WorkManager, permiso `POST_NOTIFICATIONS`, `Avisos.limites` |

## 6. Matriz de trazabilidad (requisito → prueba)

| Requisito | Pruebas |
| --- | --- |
| RF-01, RF-02 | CP-02, CP-19, CP-25 |
| RF-03, RF-04 | CP-20, CP-21 |
| RF-05, RF-06 | CP-03, CP-18 |
| RF-07, RF-08 | CP-04, CP-05, CP-08, CP-09, CA-01, CA-04 |
| RF-09, RF-10 | CP-07, Unit 6 |
| RF-11 | CP-03, CP-11, Unit 3 |
| RF-12, RF-13 | CP-10 a CP-12, CA-02, CA-03, Unit 5 |
| RF-14, RF-15 | CP-13, CP-14 |
| RF-16, RF-17 | CP-15, CP-16, Unit 4 |
| RF-18 | CP-17 |
| RF-19 | CP-24 |
| RNF-01 | CP-26 |
| RNF-04 | CP-22, CP-23 |
| RNF-05 | Unit 1, Unit 2 |
| RF-21, RF-22 | CP-27, CP-28, Unit 9 |
| RF-23, RF-24 | CP-31, CP-32, CA-06, Unit 7 |
| RF-25 | CP-30, Unit 9 |
| RF-26 | CP-29, Unit 8 |
| RNF-14 | CP-27 (la petición no lleva datos del usuario) |
| RNF-15 | CP-17 con tema oscuro: montos verdes y rojos legibles |

## 7. Hoja de resultados

| Fecha | Dispositivo | Cuenta | Casos OK | Casos con error | Notas |
| --- | --- | --- | --- | --- | --- |
| | | | / 32 | | |

### Registro de errores encontrados

| # | Caso | Qué pasó | Captura | Corregido |
| --- | --- | --- | --- | --- |
| 1 | — | Porcentaje truncado (420 de 400 mostraba 104 %) | — | ✅ `roundToInt` en `LimitesScreen` y `Componentes` |
| 2 | — | `google-services.json is missing` al compilar | — | ✅ JSON en `app/` |
| 3 | — | Verde y rojo poco legibles en tema oscuro (3,4:1 y 3,1:1) | — | ✅ Colores semánticos en el `ColorScheme`, aclarados en oscuro (v2) |


---

# 09 · Guía de presentación y defensa

> Qué diagrama mostrar en cada momento, datos de demo, guion cronometrado y respuestas a las preguntas más probables.

---

## 1. Orden sugerido de diapositivas (12–15 min)

| # | Diapositiva | Qué mostrar | Material |
| --- | --- | --- | --- |
| 1 | Portada | Logo, nombre, materia, integrantes | Ícono de la app |
| 2 | El problema | Gastos en efectivo, ahorro mezclado con lo que se gasta | `01_Descripcion_del_proyecto.md` §1 |
| 3 | La solución | Saldo total, reservado, disponible | Captura de Inicio |
| 4 | Casos de uso | Qué puede hacer el usuario | `diagramas/01_casos-de-uso.png` |
| 5 | Tecnologías | Kotlin, Compose, Firebase Auth, Firestore, Credential Manager, BiometricPrompt, DataStore, Retrofit, WorkManager | `03_Arquitectura_y_codigo.md` §1 |
| 6 | Arquitectura | MVVM + Clean | `diagramas/02_arquitectura.png` |
| 7 | Base de datos | Árbol NoSQL sin ciclos | `diagramas/03_base-de-datos.png` y `04_firestore-arbol.png` |
| 8 | Login | Credential Manager → ID token → Firebase | `diagramas/07_secuencia-login-google.png` |
| 9 | Flujo de la app | El portero | `diagramas/05_flujo-app.png` |
| 10 | Cómo se guarda un dato | Tiempo real y offline | `diagramas/09_secuencia-guardar-gasto.png` |
| 11 | La matemática | Saldos calculados | `diagramas/12_calculo-saldo.png` |
| 12 | API REST: el dólar | Retrofit + corrutinas | `diagramas/13_secuencia-dolar.png` |
| 13 | Avisos diarios | WorkManager + notificaciones | `diagramas/14_secuencia-avisos.png` |
| 14 | Seguridad | Reglas + huella | `diagramas/11_despliegue.png` + reglas |
| 15 | **Demo en vivo** | Ver sección 3 | Celular + consola de Firebase |
| 16 | Pruebas | 9 unitarias + 32 casos + 11 flujos | `07_Plan_de_pruebas.md` |
| 17 | Diseño | Paleta y contraste medido | `13_Diseno_UX_UI.md` |
| 18 | Conclusiones y mejoras | Qué aprendimos, qué sigue | `10_Avance_y_pendientes.md` |

Cada diagrama tiene su `.md` con un párrafo "Qué decir al presentarlo".

## 2. Datos de demo

Usa una **cuenta de Google solo para la demo**. Pon **fechas del mes actual** (siempre hoy o antes).

**Saldo inicial:** `800`

| Tipo | Día | Categoría | Monto | Descripción |
| --- | --- | --- | --- | --- |
| Ingreso | 1 | 💼 Sueldo | 3000 | Sueldo del mes |
| Ingreso | 8 | ✨ Ingreso extra | 450 | Venta de ropa |
| Ingreso | 12 | 🎁 Regalo | 200 | Cumpleaños |
| Gasto | 2 | 🏠 Hogar | 900 | Alquiler |
| Gasto | 5 | 🍽 Alimentación | 620 | Supermercado |
| Gasto | 9 | 🚌 Transporte | 150 | Micro y taxi |
| Gasto | 10 | 📚 Estudios | 350 | Material de clases |
| Gasto | 13 | 💚 Salud | 120 | Farmacia |
| Gasto | 14 | 🍽 Alimentación | 85 | Almuerzos |
| Gasto | 14 | 🎬 Ocio | 180 | Cine con amigos |

| Meta | Objetivo | Fecha objetivo | Aportar |
| --- | --- | --- | --- |
| Laptop | 6000 | **30 de abril del año siguiente** | 700 |
| Fondo de emergencia | 2000 | **31 de diciembre de este año** | 300 |

| Límite | Monto |
| --- | --- |
| 🍽 Alimentación | 800 |
| 🎬 Ocio | 150 |
| 🚌 Transporte | 300 |

### Lo que debe mostrar la app

| Dónde | Valor |
| --- | --- |
| Saldo total | **Bs 2.045,00** (800 + 3.650 − 2.405) |
| Disponible | **Bs 1.045,00** |
| Reservado | **Bs 1.000,00** |
| Este mes | Ingresos Bs 3.650,00 · Gastos Bs 2.405,00 |
| Alertas | "Ya usaste más del 80 % del límite de Alimentación." · "Te pasaste del límite de Ocio." |
| Inicio · Dólar hoy | Oficial y paralelo del día, y "Tu disponible ≈ US$ X" (X = 1.045 ÷ venta del paralelo; con venta 11,89 → **US$ 87,89**) |
| Metas | Laptop **12 %** · "ahorra **Bs 757,15** al mes" (faltan 5.300 en 7 meses, de octubre a abril) · Fondo de emergencia **15 %** · "ahorra **Bs 566,67** al mes" (faltan 1.700 en 3 meses) |
| Reportes · Últimos 6 meses | Solo el mes actual tiene barras (verde 3.650, roja 2.405); los 5 anteriores vacíos |
| Límites | Alimentación **88 %** · Ocio **superado · 120 %** · Transporte **50 %** |
| Reportes | Resultado **Bs 1.245,00** · barras: Hogar 900, Alimentación 705, Estudios 350, Ocio 180, Transporte 150, Salud 120 |

Si presentas en otro mes que octubre, los meses del ahorro sugerido cambian: Laptop = 5.300 ÷ (meses de hoy a abril, contando el actual); Fondo = 1.700 ÷ (meses de hoy a diciembre).

### Capturas para las diapositivas

Login · Huella · Inicio (claro, con la tarjeta del dólar) · Inicio (oscuro) · Historial · Diálogo de nuevo gasto · Metas (con "ahorra Bs X al mes") · Límites · Reportes (con el gráfico de 6 meses) · Mi perfil · una notificación en la barra · Consola de Firestore (`usuarios → uid → movimientos`).

## 3. Guion de la demo en vivo (6 minutos)

**Antes:** datos de demo cargados, huella activada, **avisos activados**, app cerrada, consola de Firestore abierta en la PC.

| Min | Haces | Dices |
| --- | --- | --- |
| 0:00 | Abres la app → huella → Inicio | "Entramos con Google una sola vez; Firebase nos da un UID. La huella es un candado del celular: se valida con BiometricPrompt y nunca sale del teléfono." |
| 0:45 | Muestras la tarjeta morada y la del dólar | "Saldo total, disponible y reservado no se guardan: se calculan en `CalculosFinanzas`. Debajo, el dólar oficial y paralelo vienen de una API REST con Retrofit; mi disponible son US$ 88." |
| 1:30 | **− Gasto** `25,50` Alimentación → Guardar; miras la consola | "El dinero va en centavos: 25,50 se guarda como 2550. Miren Firestore: aparece en tiempo real gracias a `addSnapshotListener`." |
| 2:30 | Metas → Aportar `100` a Laptop | "Aportar no es gastar: el saldo total no cambia, solo baja el disponible. Se suma en el servidor con `FieldValue.increment`. Y como la meta tiene fecha, me dice cuánto ahorrar al mes." |
| 3:15 | Límites y luego Inicio | "Cada categoría tiene un tope mensual. El documento se llama igual que la categoría, así no puede haber dos límites para lo mismo." |
| 3:45 | Reportes | "Seis meses de ingresos contra gastos, resultado del mes y barras por categoría, todo dibujado con Compose, sin librerías." |
| 4:15 | Modo avión → − Gasto `10` → quitas modo avión | "Funciona sin internet: Firestore guarda en el celular y sincroniza solo." |
| 5:00 | Mi perfil → tema oscuro → **Probar avisos ahora** → bajas la barra | "Tema, huella y avisos son preferencias del celular en DataStore. Los avisos salen con WorkManager a las 9:00 aunque la app esté cerrada: miren, 'te pasaste del límite de Ocio, faltan N días para fin de mes'." |
| 5:30 | Diapositiva de arquitectura | "MVVM: pantalla → ViewModel → repositorio → Firestore. Cada usuario solo puede leer su `usuarios/{uid}`." |

**Plan B sin internet en el aula:** la app abre con los datos que ya tenía. Muestra la parte offline y usa capturas de la consola.

### Demos cortas (si piden algo puntual)

| Demo | Pasos |
| --- | --- |
| **Primer uso (2 min)** | Cuenta nueva → Google → saldo `500` → Inicio vacío → + Ingreso `1000` → ver `usuarios/(uid)` en Firestore |
| **Seguridad (2 min)** | Mostrar reglas → cerrar sesión → otra cuenta no ve nada → activar huella → cerrar y abrir → "Entrar con otra cuenta" |
| **Sin internet (1 min)** | Modo avión → gasto → no está en la consola → quitar modo avión → aparece |

## 4. Preguntas probables y respuestas

### Sobre Firebase y el login

| Pregunta | Respuesta |
| --- | --- |
| ¿Cómo funciona el login? | Credential Manager muestra las cuentas de Google, Google devuelve un ID token, se lo damos a Firebase con `signInWithCredential` y nos da el usuario con su UID |
| ¿Qué es el ID token? | Un "carnet" firmado por Google que dice quién es la persona. Firebase lo verifica |
| ¿Qué es el SHA-1 y para qué sirve? | La huella de la llave que firma la app. Google la usa para confirmar que la app que pide el login es la registrada |
| ¿Qué es `default_web_client_id`? | El id del cliente web OAuth. Lo genera el plugin google-services desde el JSON, en `res (generated)` |
| ¿Por qué hay dos carpetas `res`? | `res` es nuestra; `res (generated)` la crea el plugin a partir de `google-services.json` |
| ¿Cómo sabe la app que ya inicié sesión? | Firebase guarda la sesión; `currentUser` no es null y el `AuthStateListener` avisa |
| ¿Qué pasa al cerrar sesión? | Se quitan los listeners de Firestore, `signOut()` y `clearCredentialState()`; el listener pone `usuario = null` y el portero muestra el login |

### Sobre la base de datos

| Pregunta | Respuesta |
| --- | --- |
| ¿Por qué NoSQL? | Tiempo real, offline incluido, sin servidor propio y gratis a esta escala |
| ¿Cómo relacionan los datos sin llaves foráneas? | Con subcolecciones: la ruta `usuarios/{uid}/movimientos` ya dice de quién es |
| ¿Tiene ciclos? | No, es un árbol: todo cuelga de `usuarios/{uid}` |
| ¿Por qué no hay tabla de categorías? | Son 11 fijas, viven en `Categoria.kt` |
| ¿Por qué centavos? | `Double` redondea mal (`0.1 + 0.2`). Con `Long` es exacto |
| ¿Por qué no guardan el saldo? | Se calcula de los datos reales; si se guardara podría desincronizarse |
| ¿Qué hace `merge`? | Actualiza solo los campos enviados sin borrar los demás (no borra el saldo inicial al reentrar) |
| ¿Qué hace `increment`? | Suma en el servidor; dos aportes simultáneos no se pierden |
| ¿Cómo funciona offline? | Firestore guarda en una caché local; escribimos sin `await()` y sincroniza al volver la red |

### Sobre la arquitectura

| Pregunta | Respuesta |
| --- | --- |
| ¿Qué es MVVM? | La vista (Compose) observa el estado del ViewModel; el ViewModel pide datos al repositorio. La vista nunca toca la base |
| ¿Por qué dos ViewModels? | Uno para la sesión (login, huella, tema) y otro para el dinero (Firestore) |
| ¿Qué es el "portero"? | Un `when` en `AppNavigation` que decide la pantalla según el estado |
| ¿Por qué `StateFlow`? | La pantalla se redibuja sola cuando cambia el valor |
| ¿Qué pasa al girar la pantalla? | El ViewModel sobrevive y los formularios usan `rememberSaveable` |
| ¿Dónde está la lógica? | En `CalculosFinanzas` (domain), sin Android, por eso se prueba con JUnit |

### Sobre la huella y DataStore

| Pregunta | Respuesta |
| --- | --- |
| ¿Se puede entrar solo con la huella? | No. La huella desbloquea una sesión de Google que ya existe |
| ¿Dónde se guarda la huella? | En ningún lado nuestro. El sistema la valida y solo responde sí o no |
| ¿Por qué no hay botón Cancelar? | Usamos `DEVICE_CREDENTIAL` (PIN); ese botón lo ocupa "Usar PIN" |
| ¿Por qué `FragmentActivity`? | BiometricPrompt la necesita |
| ¿Qué guarda DataStore? | Solo `tema_oscuro` y `biometria_activada`. Es el reemplazo moderno de SharedPreferences |

### Sobre las funciones nuevas (v2)

| Pregunta | Respuesta |
| --- | --- |
| ¿Cómo consumen la API del dólar? | Con Retrofit: una interfaz con dos `@GET` `suspend`, Gson convierte el JSON a una data class, y lo llamamos desde `viewModelScope.launch`. Sin internet atrapamos la excepción y mostramos Reintentar |
| ¿Por qué Retrofit y no HttpURLConnection? | Es el estándar de la industria: 15 líneas en vez de 60, y maneja hilos y JSON solo |
| ¿Qué pasa si dolarapi.com se cae? | Solo la tarjeta del dólar muestra "Reintentar"; el resto no depende de ella |
| ¿Cómo llegan las notificaciones si la app está cerrada? | WorkManager programa un `PeriodicWorkRequest` de 24 h; Android lo ejecuta aunque la app no esté abierta. El Worker lee Firestore (caché si no hay internet), calcula los avisos y los muestra |
| ¿Por qué no AlarmManager? | Desde Android 12 pide permisos especiales para alarmas exactas y gasta más batería; WorkManager es lo recomendado para "una vez al día" |
| ¿Y el permiso? | En Android 13+ se pide `POST_NOTIFICATIONS` con `rememberLauncherForActivityResult` cuando el usuario enciende el switch |
| ¿Cómo calculan "faltan Y días"? | Días del mes menos el día de hoy |
| ¿Cómo calculan "ahorra Bs X al mes"? | Lo que falta ÷ meses hasta la fecha (contando el actual), redondeado hacia arriba |
| ¿Por qué los avisos de Inicio y los de la notificación dicen lo mismo? | Porque los calcula la misma función, `Avisos.limites`, en `domain`. Una sola fuente de verdad |
| ¿El gráfico usa alguna librería? | No: cada barra es un `Box` con altura proporcional al mes más alto |
| ¿Por qué los campos de dinero terminan en "Centavos"? | Porque guardamos enteros de centavos para que las sumas sean exactas; el sufijo deja clara la unidad, como `timeoutMillis` |

### Sobre pruebas y calidad

| Pregunta | Respuesta |
| --- | --- |
| ¿Cómo probaron? | 9 pruebas JUnit (matemática, avisos, ahorro mensual, 6 meses, dólar) + 32 casos manuales + 11 flujos completos |
| ¿Qué pasa si escribo letras en el monto? | Muestra "Escribe un monto válido…" y no se cierra el diálogo |
| ¿Y si falla Firestore? | Pantalla "No pudimos cargar tus datos" con Reintentar |

### Preguntas difíciles

| Pregunta | Respuesta honesta |
| --- | --- |
| ¿Qué mejorarían? | Pedir huella al volver de segundo plano, validar campos en las reglas, exportar a CSV, widget de pantalla de inicio, pruebas de interfaz, precios de referencia de la canasta familiar |
| ¿Escala a muchos movimientos? | Para uso personal sí. Si creciera, se filtraría por mes con una consulta en Firestore |
| ¿Por qué no Hilt? | Para mantenerlo simple; con dos ViewModels no hace falta. Es una mejora posible |
| ¿Qué pasa si dos celulares editan a la vez? | Gana la última escritura; los aportes no se pierden gracias a `increment` |

## 5. Checklist del día de la presentación

- [ ] APK instalado en el celular de la demo (`Generate APKs`).
- [ ] Datos de demo cargados y revisados contra la tabla de valores.
- [ ] Huella activada en el celular de la demo.
- [ ] Avisos diarios activados (permiso concedido) y probados con "Probar avisos ahora".
- [ ] El dólar carga (hay internet).
- [ ] Batería cargada, modo no molestar activado.
- [ ] Consola de Firebase abierta e iniciada sesión en la PC.
- [ ] Diapositivas con los PNG de `diagramas/`.
- [ ] Ensayado el guion 2 veces con cronómetro.
- [ ] Plan B: capturas de todo por si no hay internet.
