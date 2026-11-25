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

## Fase 2 — Dominio del dinero y datos ✅ _pendiente_

- [ ] **Volcar `/api/v1.json` de una instancia real** y definir DTOs contra la spec
      (tarea de verificación — anotar divergencias con `05`).
- [ ] `core/money`: `Money`, `Rate`, `CurrencyCode` (USD/VES/USDT), `PaymentMethod`,
      `RateProvider`; TypeConverters TEXT canónico; formateo por locale.
- [ ] `domain/model`: `Expense`, `ExpenseItem`, `Income`, `Category`, `PaymentSource`,
      `ExchangeRate`, `AppSettings` — puros, sin Android.
- [ ] `ConversionService.convert(currency, amount, rate)` + variantes con items.
- [ ] `CommissionCalculator.calculate(amount)` = `max(min, amount × %)`.
- [ ] `RatePolicy.rateFor(date)`: manual del día → api del día → última conocida
      (estimada); selección BCV/paralelo.
- [ ] Room: entidades, `SpentzDatabase`, DAOs con queries de listados/agregación,
      índices y FKs de `03`.
- [ ] `remote/dto` + mapeadores ↔ dominio; `remote/api/SpentzApi` completa (CRUD de
      `05`).
- [ ] Repositorios delegadores por modo (ADR-A01) para gasto, ingreso, categoría, origen,
      tasa, settings.
- [ ] **Tests dominio** (JVM): cada moneda, congelado con items, comisión (piso activo,
      porcentaje activo, cero explícito), RatePolicy (manual > api > estimada), redondeos
      HALF_UP 2/4 decimales, rechazo de rate ≤ 0.
- [ ] Tests Room (androidTest): insertar gasto con items y leerlo; FK restrict de
      categoría con movimientos.
- [ ] Tests API (MockWebServer): login, 422 de validación, 401, throttling simulado.

## Fase 3 — Gastos ✅ _pendiente_

- [ ] `ExpensesViewModel`: lista con filtros (mes/categoría/moneda), operaciones CRUD.
- [ ] Pantalla Lista: agrupada por día, subtotales, estados vacío/carga/error/offline.
- [ ] `ModalBottomSheet` de filtros.
- [ ] Pantalla Formulario (la de `06`): monto + moneda segmentada; si Bs → método,
      comisión precargada editable + "sin comisión", tasa precargada con badge fuente
      (BCV/Paralelo/Manual/Estimada) y edición; equivalente USD/USDT en vivo; categoría
      (chips), origen (dropdown), fecha, descripción, nota.
- [ ] Pantalla Detalle: todos los campos + tasa aplicada + editar/eliminar.
- [ ] FAB desde Dashboard al formulario.
- [ ] Remoto: precarga de tasa con `GET /rates` (nunca dolarapi directo en remoto).
- [ ] UI test Compose: alta de gasto en Bs con comisión → aparece en lista con
      equivalentes correctos.
- [ ] Verificación cruzada: mismo input en la web y en la app (local) → mismos
      `usd_amount`/`usdt_amount` congelados.

## Fase 4 — Ingresos y catálogos ✅ _pendiente_

- [ ] `IncomesViewModel` + lista + filtros mes/categoría.
- [ ] Formulario de ingreso (moneda, monto, tasa si Bs, categoría, fecha, descripción).
- [ ] Detalle + editar/eliminar.
- [ ] Gestión de Categorías: lista separada gasto/ingreso, crear/editar (nombre, icono,
      color, tipo, presupuesto), borrar con bloqueo por movimientos.
- [ ] Gestión de Orígenes: CRUD.
- [ ] Pestaña Ingresos condicionada a `tracking_type` (ambos modos).
- [ ] Selector de icono (set fijo Material) y color (paleta fija) reutilizable.

## Fase 5 — Tasas ✅ _pendiente_

- [ ] `DolarApiClient` (Retrofit aparte, sin auth) contra `ve.dolarapi.com/v1/dolares`;
      parse de lista `moneda/fuente/promedio` con compatibilidad de formato antiguo.
- [ ] `RatesSyncWorker` (WorkManager, ~6 h, red no medida) + upsert diario BCV+paralelo.
- [ ] Refresco "si > 60 min" al abrir formularios en Bs (no bloqueante).
- [ ] Pantalla Tasas: tarjetas BCV/Paralelo, override manual, histórico 30 días,
      "Actualizar ahora" (local: dolarapi; remoto: `POST /rates/sync`).
- [ ] Override manual persistido como `manual/user` del día (gana en formularios).
- [ ] Test: RatePolicy con datos sembrados; worker con WorkManager test framework.

## Fase 6 — Dashboard y ajustes ✅ _pendiente_

- [ ] `DashboardViewModel` local: agregaciones SQL del mes (USD y USDT congelados),
      presupuesto global + por categoría, últimos movimientos.
- [ ] Dashboard remoto: `GET /dashboard` (+`monthly-summary`) mapeado a la misma UI.
- [ ] Pantalla Dashboard completa de `06`: tarjetas de totales, barra de presupuesto,
      mini-gráfico por categoría (Compose, sin librería), últimos movimientos.
- [ ] Ajustes: moneda display, tracking_type, presupuesto, comisión mínima/%, apariencia,
      idioma, perfil remoto (email + servidor + logout), Acerca de.
- [ ] Persistencia de settings: local → `user_prefs`/DataStore; remoto → `PUT
      /user/profile`.
- [ ] Test de agregación: totales del dashboard = suma de la lista (ambos modos).

## Fase 7 — Pulido y release ✅ _pendiente_

- [ ] Icono de launcher (adaptive icon) + splash API.
- [ ] Pasada de accesibilidad: talkback en onboarding, formulario de gasto, dashboard.
- [ ] Estados vacíos definitivos + micro-animaciones (animateItemPlacement en listas).
- [ ] `assembleRelease` firmado + proguard/rules verificadas.
- [ ] README final (capturas, requisitos, cómo conectar al backend), CONTRIBUTING.md,
      CHANGELOG.md (SemVer).
- [ ] Prueba en dispositivo físico + instalación limpia de release.
- [ ] Tag `v1.0.0` + GitHub Release (APK). Evaluar metadata F-Droid.

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
