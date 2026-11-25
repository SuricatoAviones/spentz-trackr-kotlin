# 08 — Backlog de Tareas

El tablero operativo. Se marca `[x]` al completar y se reordena si la realidad manda.
Referencia de diseño por fase: `07-plan-de-fases.md`.

Convención de commits: Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`,
`chore:`), en español o inglés pero consistente.

---

## Fase 0 — Fundaciones ✅ _pendiente_

- [ ] Crear estructura Gradle: `settings.gradle.kts`, `build.gradle.kts` (root),
      `gradle/libs.versions.toml` (version catalog), wrapper (`gradle-wrapper.properties`).
- [ ] Módulo `:app` con `build.gradle.kts` + `applicationId
      io.github.suricatoaviones.spentztrackr` + minSdk 26 + target última.
- [ ] Dependencias base: Compose BOM, Material 3, Navigation-Compose, Hilt, Room,
      DataStore, Retrofit + OkHttp + kotlinx-serialization, WorkManager, kotlinx-datetime.
- [ ] `SpentzApplication` (`@HiltAndroidApp`) + `MainActivity` con tema M3 (claro/oscuro).
- [ ] Esqueleto de navegación con rutas vacías y `strings.xml` ES + `values-en/`.
- [ ] ktlint + `.editorconfig` + `.gitignore` Android definitivo (build/, .gradle/,
      local.properties, .idea/, *.apk…).
- [ ] GitHub Actions: `android-ci.yml` → `gradlew lint ktlintCheck test` (JVM) y
      `assembleDebug` en main.
- [ ] Commits de estructura iniciales + verificar CI verde.

## Fase 1 — Onboarding y modos ✅ _pendiente_

- [ ] `data/settings/AppSettingsStore` (DataStore): todas las claves de `04`.
- [ ] Token: `TokenStore` sobre EncryptedSharedPreferences (ADR-A10).
- [ ] Pantalla Bienvenida (una pantalla, tres bullets, botón Empezar).
- [ ] Pantalla Elección de modo (radio-cards Local/Remoto, copys de `04`).
- [ ] Pantalla Login servidor: campo URL con normalización + email/contraseña + estados
      de error (inalcanzable / credenciales / no es spentz-trackr) + toggle registro.
- [ ] Retrofit: `SpentzApi` mínima (`auth/login`, `auth/register`, `auth/me`) +
      `AuthInterceptor` + `BaseUrlInterceptor` + parse 422.
- [ ] Pantalla Setup local: moneda display, tracking_type, presupuesto opcional.
- [ ] `LocalInitializer`: transacción Room con categorías default (set del
      `AssignDefaultUserDataAction` del backend), orígenes default, `user_prefs`
      (comisión 14.00 / 0.30%).
- [ ] Root de navegación con gating onboarding/mode + cambio de modo en Ajustes (stub).
- [ ] Tests: store de settings (Turbine), normalización de URL, mapeo de errores HTTP.

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
