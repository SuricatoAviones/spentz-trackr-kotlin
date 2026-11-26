# Contribuir a Spentz Trackr Android

Gracias por querer aportar. Este documento resume el flujo; la guía operativa
completa (fases, ramas y commits) está en [`AGENTS.md`](AGENTS.md) y la
documentación de producto/arquitectura en [`docs/`](docs/README.md).

## Antes de empezar

1. Lee [`docs/01-vision-y-alcance.md`](docs/01-vision-y-alcance.md) y
   [`docs/02-arquitectura.md`](docs/02-arquitectura.md).
2. Comprueba que tu cambio respeta las reglas heredadas del backend:
   conversión congelada, USDT 1:1 con USD, comisión `max(mínimo, monto × %)`
   y esquema espejo.
3. Nada de telemetría, analítica, publicidad ni blobs propietarios: el objetivo
   es F-Droid y la licencia es MIT.

## Entorno

- JDK 17 (el JBR de Android Studio sirve).
- Android Studio reciente; `compileSdk 37`, `targetSdk 36`, `minSdk 26`.
- Detalle en [`docs/09-entorno-y-herramientas.md`](docs/09-entorno-y-herramientas.md).

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat ktlintFormat
.\gradlew.bat assembleDebug test ktlintCheck lint
```

En Linux/macOS usa `./gradlew`.

## Flujo de ramas y PRs

- `main` solo recibe merges de fase (o documentación puntual).
- Una rama por fase: `fase/<n>-<slug>` creada desde `main`.
- Commits [Conventional Commits](https://www.conventionalcommits.org/es/):
  `feat:`, `fix:`, `test:`, `refactor:`, `docs:`, `chore:`, `ci:`.
  El asunto explica el **qué**; el cuerpo el **porqué**. Referencia la tarea
  (`docs/08 F<n>-<tarea>`) al final.
- Al cerrar una fase, abre un PR a `main` con:
  - qué se implementó,
  - criterios de salida verificados de `docs/07`,
  - cómo probarlo a mano.
- No se hace merge con CI en rojo.

## Estilo y convenciones

- Kotlin idiomático; `ktlintCheck` manda. Ejecuta `ktlintFormat` antes de commitear.
- UI en Jetpack Compose + Material 3; reutiliza los componentes de
  `core/designsystem` (por ejemplo `SpentzEmptyState`).
- Todo texto visible va por `res/values/strings.xml` (español, default) y
  `res/values-en/strings.xml`; mantén la paridad de claves.
- Dinero siempre `Money`/`Rate` (`BigDecimal`), nunca `Double`.
- Fechas de negocio como `LocalDate`/ISO `YYYY-MM-DD`; en Room, texto.
- Los comentarios explican decisiones, no lo que el código ya dice.

## Tests

- Tests unitarios JVM en `app/src/test` (JUnit4 + coroutines-test + Turbine).
- Tests de UI/Room en `app/src/androidTest` (Compose test + AndroidJUnit4).
- Añade un test cuando cambies reglas de negocio, mapeos o agregaciones.
- Para instrumentación: `.\gradlew.bat connectedDebugAndroidTest` con un
  emulador/teléfono conectado.

## Firma y release

- `keystore.properties` y los `.jks` **no** se commitean (`.gitignore`).
- Copia `keystore.properties.example`, completa tus valores y ejecuta
  `.\gradlew.bat assembleRelease`.
- Sin keystore, el release se genera unsigned; es lo esperado en CI.
- Guarda el keystore y sus contraseñas: sin él no se pueden publicar
  actualizaciones.

## Documentación

La documentación es la especificación. Si el código y `docs/` divergen, corrige
el que esté mal y explícalo en el commit/PR. Mantén actualizados, cuando aplique:

- `docs/07-plan-de-fases.md` (criterios de salida),
- `docs/08-backlog-tareas.md` (checkboxes y deudas),
- `AGENTS.md` (estado de fases),
- `README.md` y `CHANGELOG.md`.

## Comportamiento

Sé respetuoso y constructivo. Los desacuerdos técnicos se resuelven con datos,
tests y referencias a los ADRs; no con ataques personales.

## Licencia

Al contribuir aceptas que tu aporte se publique bajo la licencia MIT del
proyecto.
