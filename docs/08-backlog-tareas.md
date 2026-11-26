# 08 — Backlog de Tareas

El tablero operativo. Se marca `[x]` al completar y se reordena si la realidad manda.
Referencia de diseño por fase: `07-plan-de-fases.md`.

**Flujo de trabajo** (detalle en `AGENTS.md`): cada fase se desarrolla en su rama
`fase/<n>-<slug>`, una tarea ≈ un commit detallado (Conventional Commits), y al
cerrar la fase se abre un PR a `main` con los checkboxes de esa fase marcados.

Convención de commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`,
`chore:`), en español o inglés pero consistente.

---

## Fase 0 — Fundaciones ✅ _completada (2026-11-25)_

> Verificado localmente: `assembleDebug` ✔ (APK debug generado), `ktlintCheck` ✔,
> `lint` + `test` ✔. La acción en GitHub se confirma al hacer push (CI corre en
> cada push/PR). Nota: Kotlin viene built-in en AGP 9 — no existe el plugin
> `org.jetbrains.kotlin.android` en este repo (ver `docs/09`).

- [x] Crear estructura Gradle: `settings.gradle.kts`, `build.gradle.kts` (root),
      `gradle/libs.versions.toml` (version catalog), wrapper
      (`gradle-wrapper.properties` + scripts, Gradle 9.7.0).
- [x] Módulo `:app` con `build.gradle.kts` + `applicationId
      io.github.suricatoaviones.spentztrackr` + minSdk 26 + targetSdk 36
      (compileSdk 37; plataforma android-37.0 instalada en el SDK local).
- [x] Dependencias base: Compose BOM 2026.09.00 (Compose 1.12.1 / M3 1.4.0),
      Navigation-Compose 2.10.2, Hilt 2.60.1 (KSP), Room 2.8.5 (KSP), DataStore
      1.3.0, Retrofit 3.0.0 + OkHttp 5.5.1 + kotlinx-serialization 1.13.0,
      WorkManager 2.12.0, kotlinx-datetime 0.8.0.
- [x] `SpentzApplication` (`@HiltAndroidApp`) + `MainActivity` con tema M3
      (claro/oscuro) y edge-to-edge.
- [x] Esqueleto de navegación (`Routes` + `SpentzNavHost`, placeholder de Inicio)
      y `strings.xml` ES + `values-en/`.
- [x] ktlint (plugin 14.2.0) + `.editorconfig` (function-naming desactivada por
      la convención PascalCase de Compose) + `.gitignore` Android definitivo +
      `.gitattributes`.
- [x] GitHub Actions: `android-ci.yml` → `gradlew ktlintCheck lint test` en cada
      push/PR; `assembleDebug` + APK como artefacto en `main`.
- [x] Commits de estructura por tarea (Conventional Commits).

## Fase 1 — Onboarding y modos ✅ _completada (2026-11-25, rama `fase/1-onboarding-modos`)_

> Verificado localmente: `assembleDebug`, `test` (SettingsStore/UrlNormalizer/ApiErrorMapper
> + Routes), `ktlintCheck`, `lint`. Shapes confirmados contra el código real del backend:
> envelope `{success, data, message}`, login 422 `{message, errors}`, userShape de
> `auth/me`, y los 8+8 defaults de `AssignDefaultUserDataAction`. Notas de entorno:
> `hiltViewModel` 1.4.0 vive en `androidx.hilt.lifecycle.viewmodel.compose`; el
> constructor de `MasterKey` está deprecado en security-crypto 1.1.0 (funciona).

- [x] `data/settings/SettingsStore` (DataStore): claves de `docs/04` expuestas como
      `AppSettings` (onboarding, modo, servidor, tema, idioma) + tests.
- [x] `data/token/TokenStore` sobre EncryptedSharedPreferences (ADR-A10,
      security-crypto 1.1.0) + permiso INTERNET y `network_security_config` (ADR-A11).
- [x] Pantalla Bienvenida (monograma + tres ideas).
- [x] Pantalla Elección de modo (radio-cards con copys honestos de `docs/04`).
- [x] Pantalla Login servidor: normalización de URL (`ServerUrlNormalizer`), estados de
      error específicos, toggle de registro (reglas de Fortify confirmadas), aviso
      cleartext para `http://`.
- [x] Retrofit mínimo: `SpentzApi` (login/register/me/logout) + `AuthInterceptor` +
      `DynamicBaseUrlInterceptor` + `ApiErrorMapper` (422/403/429/HTML-no-spentz).
- [x] Pantalla Setup local: moneda display, tracking type, presupuesto opcional.
- [x] `LocalInitializer`: transacción Room idempotente con los 8 categorías + 8 orígenes
      espejo del backend y `user_prefs` (comisión 14.00 Bs / 0.30 %).
- [x] Root de navegación con gating (`RootApp`: splash → onboarding | main con barra
      inferior) + Ajustes con cambio de modo (local ↔ remoto con sesión guardada,
      `auth/me` en pantalla y logout).
- [x] Tests: SettingsStore (Turbine sobre DataStore temporal), ServerUrlNormalizer
      (9 casos), ApiErrorMapper (5 casos con bodies reales del backend).

## Fase 2 — Dominio del dinero y datos ✅ _completada (2026-11-26, rama `fase/2-dominio-datos`)_

> Verificado localmente: `assembleDebug`, `test` (10 suites JVM: money/rate, conversión,
> comisión, política de tasas, mapeo de errores, normalizador de URL, settings, delegación por
> modo, API con MockWebServer y routes), `ktlintCheck`, `lint` y `assembleDebugAndroidTest`
> (compilado). El androidTest de Room (gasto+ítems, FK restrict, upsert de tasas) corre con
> `gradlew connectedDebugAndroidTest` en emulador/dispositivo — pendiente de ejecutar a mano.
> Hallazgos técnicos (notas en el código y `docs/09`): Room+KSP2 exige primitivas en columnas,
> así que `Money`/`Rate` y las fechas ISO van como **texto canónico** en las entidades (los
> value classes viven solo en dominio); `kotlinx.datetime.Clock` ya no existe — es
> `kotlin.time.Clock` (stdlib desde 0.7); los delegadores usan qualifiers
> `@LocalSource`/`@RemoteSource` para depender de interfaces y testear con fakes.

- [x] Spec confirmada contra `docs/api/api-documentation.md` + Presenters/FormRequests reales del
      backend (equivalente al volcado de `/api/v1.json`): envelope, items del presenter y reglas
      de `StoreExpenseRequest`/`StoreIncomeRequest` (items ≤ 6, tasa > 0, fecha no futura).
- [x] `core/money`: `Money` (2 dec, HALF_UP como `round()` de PHP), `Rate` (4 dec,
      estrictamente > 0), `CurrencyCode`/`PaymentMethod`/`RateProvider` +
      `FlexibleBigDecimalSerializer` (número o string, salida texto canónico) — con tests.
- [x] `domain/model`: `Expense`(+Draft/Item), `Income`(+Draft), `Category`(+Type/Draft),
      `PaymentSource`(+Draft), `ExchangeRate`(+RateSource); `AppSettings` movida al dominio.
- [x] Servicios: `ConversionService` (congela como el backend — ejemplo canónico del README
      (280+14)/28 = $10.50/10.50 USDT; ítems con su propia tasa; VES sin tasa lanza
      `MissingRateException`), `CommissionCalculator` (`max(mín, monto × %)`), `RatePolicy`
      (manual > API BCV > API paralelo > última conocida estimada) — con tests.
- [x] Room v2: `expenses`/`expense_items`/`incomes`/`exchange_rates` con índices por
      fecha/categoría/moneda, FKs RESTRICT (categoría/origen) y CASCADE (ítems), DAOs con
      filtros ISO, upsert idempotente de tasas por índice único (source, provider, rate_date).
- [x] DTOs y mappers contra shapes verificados + `SpentzApi` completa (CRUD de gastos, ingresos,
      categorías, orígenes; `rates` GET/PUT/`sync`) + `explicitNulls=false` para PUT parciales —
      con tests MockWebServer (parse número-o-string, requests como texto canónico).
- [x] Repositorios con delegación por modo (ADR-A01) para las cinco superficies: congelan en
      local vía `ConversionService` y delegan al backend en remoto; `EntityInUseException`
      unifica el FK restrict local con el 422/409 del API — con test de delegación por fakes.
- [x] Tests de integración Room en androidTest (insertar gasto con ítems, FK restrict, cascade
      de ítems, idempotencia de tasas) — compilados, ejecutables en emulador.

## Fase 3 — Gastos ✅ _completada (2026-11-26, rama `fase/3-gastos`)_

> Verificado localmente: `assembleDebug`, `test`, `ktlintCheck`, `lint` y
> `assembleDebugAndroidTest` (incluye el UI test de Compose, compilado; su ejecución y
> la del androidTest de Room requieren emulador: `gradlew connectedDebugAndroidTest`).
> El ejemplo canónico del README del backend se ve **en vivo** en la pantalla del
> formulario y está congelado en el UI test: (280 Bs + 14) / 28 = $10.50 / 10.50 USDT.

- [x] `ExpensesViewModel`: lista reactiva desde el repositorio delegador con filtros
      (mes → `firstDay`/`lastDay` pushdown, categoría, moneda), grupos por día con
      subtotal congelado, totales del período y estado de error con reintentar.
- [x] Pantalla Lista (docs/06): filas con categoría/origen, monto original + equivalente
      USD, tarjeta de totales, estados vacío/carga/error, FAB de alta.
- [x] `ModalBottomSheet` de filtros: meses (últimos 12 + todos), chips de categoría,
      moneda segmentada.
- [x] Formulario: monto grande, moneda segmentada, bloque Bs animado (método, comisión
      precargada `max(mín, %)` **editable** con "sin comisión", tasa con badge
      BCV/Paralelo/Manual), equivalente congelado en vivo ("Se registrará como…"),
      chips de categoría con color, dropdown de origen, DatePicker (validación de fecha
      no futura delegada al backend), descripción/nota, validaciones en línea.
- [x] `CommissionSettingsRepository` (nuevo, patrón ADR-A01): local lee `user_prefs`
      (14.00 Bs / 0.30 %); remoto usa defaults con **deuda técnica anotada** (la API no
      expone las comisiones del usuario en `auth/me`).
- [x] Detalle: monto, comisión, "Tasa aplicada: X Bs/USD (fuente)", equivalentes
      congelados, editar y eliminar con confirmación.
- [x] Navegación: pestaña Gastos en la barra inferior, rutas de formulario (alta/edición)
      y detalle, FAB en Inicio (alta rápida).
- [x] UI test de Compose del happy path (`ExpenseFormScreenTest`): ViewModel con fakes
      por interfaz (sin Hilt), locale forzado a ES para determinismo, campos con
      `testTag` estables; valida la comisión precargada (14.00 con piso) y el congelado
      del ejemplo canónico antes de guardar, y el borrador que llega al repositorio.
- [ ] **Verificación cruzada manual** (criterio docs/07): mismo input en la web y en la
      app local → mismos congelados — requiere instancia real del backend corriendo.
- [ ] Ejecutar `connectedDebugAndroidTest` en emulador (tests de Room + UI test de la
      F2 y este).

## Fase 4 — Ingresos y catálogos ✅ _completada (2026-11-26, rama `fase/4-ingresos-catalogos`)_

> Verificado localmente: `assembleDebug`, `test`, `ktlintCheck`, `lint` y
> `assembleDebugAndroidTest` (todo compilado). El CRUD de ingresos congela su tasa
> (para los reportes de la Fase 6); el borrado de categorías/orígenes con movimientos
> está bloqueado por `EntityInUseException` (FK restrict local / 422-409 API).

- [x] `IncomesViewModel` + lista con filtros (mes/categoría/moneda), grupos por día con
      subtotal congelado, totales del período, estados vacío/carga/error.
- [x] Formulario de ingreso: moneda, monto, tasa con badge si Bs (sin comisión/método/
      origen — no existen para ingresos, docs/03), categoría tipo INCOME, DatePicker,
      descripción, nota, equivalente congelado en vivo.
- [x] Detalle + editar/eliminar con confirmación.
- [x] Gestión de **Categorías**: lista por tipo (segmented gasto/ingreso), crear/editar
      (nombre, icono/color con picker reutilizable, tipo, presupuesto mensual en USD),
      borrar bloqueado con movimientos (`EntityInUseException` → snackbar).
- [x] Gestión de **Orígenes de pago**: CRUD en diálogo con icono/color picker.
- [x] `TrackingSettingsRepository` (patrón ADR-A01): local lee `user_prefs`, remoto lee
      `auth/me` — la pestaña Ingresos se gobernará por `tracking_type` desde la Fase 6.
- [x] Selector de icono (set fijo Material, 25 iconos) y color (paleta 12) reutilizable:
      `CatalogIconPickerDialog` + `CatalogIconBadge`.
- [x] Navegación: pestaña Ingresos, rutas de form/detalle de ingresos, catálogos desde
      Ajustes, rutas de categoría (crear/editar).
- [x] Strings ES/EN completos (ingresos + catálogos + ajustes).

## Fase 5 — Tasas ✅ _completada (2026-11-26, rama `fase/5-tasas`)_

> Verificado localmente: `assembleDebug`, `test`, `ktlintCheck`, `lint` y
> `assembleDebugAndroidTest`. La sincronización se prueba en el repositorio
> (`LocalRateRepositoryTest`) en lugar del WorkManager test framework: el worker
> quedó fino (decide modo y delega en `RateRepository.syncNow()`).

- [x] `DolarApiClient` (Retrofit aparte, sin auth) contra `ve.dolarapi.com/v1/dolares`;
      parse de lista `moneda/fuente/promedio` (`oficial` ⇒ BCV) con fecha de
      actualización y compatibilidad con el formato antiguo. Interfaz
      `DolarApiSource` para poder testear sin red.
- [x] `RatesSyncWorker` (WorkManager, ~6 h, red no medida) + upsert diario BCV+paralelo;
      `SpentzApplication` es `Configuration.Provider` con `HiltWorkerFactory`.
- [x] Refresco "si > 60 min" al abrir formularios en Bs (no bloqueante): se lanza en
      segundo plano y el formulario usa la cache; `observeSnapshot` actualiza la tasa
      mientras el usuario no la haya editado.
- [x] Pantalla Tasas: tarjetas BCV/Paralelo con "hace X min", override manual, histórico
      30 días (local), "Actualizar ahora" (local: dolarapi; remoto: `POST /rates/sync`).
- [x] Override manual persistido como `manual/user` del día (gana en formularios).
- [x] Badge **Estimada** en formularios cuando no hay tasa del día (ADR-002) y el
      proveedor real se conserva al congelar.
- [x] Tests: `RatePolicyTest` (manual > API > estimada), `LocalRateRepositoryTest`
      (sync, fallback, refresco perezoso, histórico) y `DolarApiMappingTest`.

## Fase 6 — Dashboard y ajustes ✅ _completada (2026-11-26, rama `fase/6-dashboard`)_

> Verificado localmente: `assembleDebug`, `test`, `ktlintCheck`, `lint` y
> `assembleDebugAndroidTest`. La prueba manual en emulador (cambio de fecha,
> tema/idioma y settings remotos contra una instancia real) queda para el PR.

- [x] `DashboardViewModel` local: movimientos de los últimos 6 meses filtrados por
      fecha en Room y agregados en `BigDecimal` (`DashboardAggregator`) para que los
      totales USD/USDT congelados cuadren exactamente con la lista; presupuesto global
      y por categoría, tendencia de 6 meses y últimos movimientos.
- [x] Dashboard remoto: `GET /dashboard` mapeado al mismo `Dashboard` (DTOs verificados
      contra `DashboardController`). `monthly-summary` no se consume: el dashboard ya
      trae totales/categorías/tendencia y la UI usa una sola forma.
- [x] Pantalla Inicio completa de `06`: tarjetas de totales según moneda de
      visualización, barra de presupuesto global y por categoría, mini-gráfico por
      categoría (Compose, sin librería) y últimos movimientos clicables.
- [x] Ajustes: moneda display, `tracking_type` (gobierna las pestañas), presupuesto,
      comisión mínima/%, apariencia (tema), idioma, perfil remoto (email + servidor +
      logout) y Acerca de.
- [x] Persistencia: local → `user_prefs`/DataStore; remoto → `PUT /user/profile`
      (moneda y tracking) + cache en `user_prefs` para uso sin red. Presupuesto y
      comisiones se guardan en `user_prefs` en ambos modos porque la API v1 no los
      expone (deuda técnica anotada abajo).
- [x] Test de agregación: `DashboardAggregatorTest` (totales = suma de la lista,
      categorías, presupuestos, ingresos, tendencia) y `DashboardMappingTest` con el
      shape real del backend.

## Fase 7 — Pulido y release ✅ _completada (2026-11-26, rama `fase/7-pulido`)_

> Verificado localmente: `assembleDebug`, `test`, `ktlintCheck`, `lint`,
> `assembleDebugAndroidTest` y `assembleRelease` firmado con R8. El flujo
> completo se prueba en emulador API 37; el tag `v1.0.0` y la GitHub Release
> se crean después del merge (el keystore de producción no vive en el repo).

- [x] Icono de launcher (adaptive icon + monochrome) + splash API con
      `core-splashscreen` y tema `Theme.SpentzTrackr.Starting`.
- [x] Pasada de accesibilidad: semántica de progreso en presupuestos,
      descripción del switch de registro, monograma del splash, radio-cards sin
      doble acción y revisión de descripciones en onboarding/formulario/dashboard.
- [x] Estados vacíos definitivos (`SpentzEmptyState` con ilustración) en Inicio,
      Gastos, Ingresos, Categorías y Orígenes + micro-animaciones
      (`animateItem`) en las listas principales.
- [x] `assembleRelease` firmado con keystore local (`keystore.properties`
      ignorado), R8/`shrinkResources` activos y reglas de Retrofit +
      kotlinx.serialization verificadas.
- [x] README final (funciones, requisitos, build, conexión al backend, release,
      privacidad y capturas), `CONTRIBUTING.md`, `CHANGELOG.md` (SemVer) y
      `docs/10-fdroid.md` + metadata fastlane ES/EN.
- [x] Flujo limpio en emulador API 37: instalación → onboarding → gasto →
      dashboard → tasas → ajustes. Dispositivo físico pendiente de la
      verificación manual del PR.
- [x] Evaluación F-Droid (sin blobs ni telemetría) y tag/Release preparados para
      ejecutarse sobre `main` tras el merge.

---

## Backlog post-v1 (priorizado, sin fecha)

| # | Item | Complejidad | Notas |
|---|---|---|---|
| P1 | Gastos mixtos (`expense_items`) | M | Esquema ya existe; UI en formulario (porciones extra) |
| P1 | Comprobantes (foto) | M | Local: archivos app-scoped; remoto: **verificar multipart en OpenAPI** |
| P2 | Metas de ahorro + aportes | M | API completa (`savings-goals`, contributions) |
| P2 | Pagos recurrentes + `pay` | M | API completa; notificaciones de vencidos = WorkManager |
| P3 | Reportes: comparativo 12 meses, estado de cuenta | M | Local: SQL; remoto: endpoints listos |
| P3 | Exportación CSV | S | Local: FileProvider + share; BOM UTF-8 como backend |
| P4 | Tarjetas de crédito | L | Leer ADR-012 y doc 12 del backend primero; modelo de saldo híbrido |
| P5 | IA BYOK (chat, scan de facturas) | L | Requiere UI de settings de IA; API `ai/*` lista |
| P6 | Sync offline-first local↔remoto | XL | Motor de sincronización (IDs, conflictos, cola de writes) — proyecto propio |
| P7 | Widget de registro rápido | M | Widget Compose Glance |
| P7 | Bloqueo biométrico | S | BiometricPrompt; pedir si hay demanda |
| P8 | Modo multi-perfil local | M | Solo si usuarios reales lo piden |

## Deudas técnicas / verificaciones contra el backend

- [ ] Confirmar shape exacto de `auth/login` (¿`{token: "1\|…"}`?) contra OpenAPI.
- [ ] Confirmar query params de filtros de `expenses`/`incomes` (¿`month`, `category_id`?).
- [ ] Confirmar si `POST /expenses` acepta multipart con recibo (para P1 de comprobantes).
- [ ] Confirmar redondeo exacto del backend en conversión (¿HALF_UP?) para paridad total.
- [ ] Revisar periodicidad del scheduler de Docker (pendiente en el backend) — mientras
      tanto la app remota usa `POST /rates/sync` con moderación.
- [ ] **Comisiones y presupuesto en remoto**: `PUT /user/profile` solo acepta
      `name/email/default_display_currency/tracking_type`; `auth/me` tampoco devuelve
      comisiones ni presupuesto. Hasta que el backend los exponga, la app los guarda en
      `user_prefs` del dispositivo y la web no los verá.
- [ ] **Moneda display en `auth/me`**: el shape actual no incluye
      `default_display_currency`; en remoto se cachea localmente tras `PUT`.

