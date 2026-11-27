# 07 — Plan de Fases

Ocho fases hasta la v1. Cada fase termina con un **aplicación compilable y demostrable**
(nada de "se compilará en la fase siguiente") y con **criterios de salida verificables**.
El orden prioriza: primero las decisiones que moldean todo (dominio del dinero), después
la pantalla de mayor valor (registrar un gasto), y por último lo que adorna (dashboard,
pulido).

El checklist operativo de cada fase está en `08-backlog-tareas.md`.

| Fase | Nombre | Tamaño* | Entregable demostrable |
|---|---|---|---|
| 0 | Fundaciones del proyecto | M | Esqueleto Android corriendo en emulador con tema y navegación vacía; CI en verde |
| 1 | Onboarding y modos | M | Primer arranque completo: elegir modo, conectar a un servidor real o entrar en local |
| 2 | Dominio del dinero y datos | L | Tests del dominio en verde; CRUD de gastos por repositorio en ambos modos (sin UI) |
| 3 | Gastos | L | Registrar, ver, editar y borrar gastos en ambos modos, con comisión y tasa congelada |
| 4 | Ingresos y catálogos | M | CRUD de ingresos, categorías y orígenes |
| 5 | Tasas | M | Sincronización BCV/paralelo, override manual e histórico |
| 6 | Dashboard y ajustes completos | M | Home con totales, presupuesto y últimos movimientos; Ajustes completos |
| 7 | Pulido y release v1 | S | APK firmado, README final, tag `v1.0.0` |

\* Tamaño relativo: S ≤ 2 días de trabajo enfocado, M ≈ 3–5, L ≈ 5–10. Estimaciones para
desarrollo con agencia AI + supervisión humana; ajustar al ritmo real.

---

## Fase 0 — Fundaciones del proyecto

**Objetivo**: que exista un proyecto Android real, abrible en Android Studio, con las
reglas de calidad funcionando desde el commit uno.

- Gradle (Kotlin DSL) + AGP + Kotlin, `:app` único, `applicationId
  io.github.suricatoaviones.spentztrackr`, minSdk 26 (ADR-A09).
- Hilt, Compose BOM, Material 3, Navigation-Compose, Room, DataStore, Retrofit/OkHttp/
  kotlinx-serialization en versiones estables (sin implementar nada todavía).
- `MainActivity` + scaffold vacío + tema M3 (claro/oscuro) + strings ES/EN mínimos.
- ktlint, `.gitignore` Android final, `LICENSE` (ya está), GitHub Actions (build + ktlint
  + tests), `.editorconfig`.

**Criterios de salida**:
- [ ] `git clone` → abrir en Android Studio → ▶ Run → pantalla "Spentz Trackr" en emulador.
- [ ] CI verde en main con un commit vacío de prueba.
- [ ] `docs/` actualizada: marcar Fase 0 completada en `08-backlog-tareas.md`.

## Fase 1 — Onboarding y modos

**Objetivo**: el primer arranque completo según `04-onboarding-y-modos.md`, con DataStore
como fuente de verdad del modo. Todavía sin datos de negocio: el "dashboard" es un stub.

- DataStore + claves de `04` + EncryptedSharedPreferences para el token.
- Pantallas: bienvenida → elección de modo → (remoto: URL + login + estados de error |
  local: setup rápido + inicialización de datos por defecto en Room).
- Cliente Retrofit mínimo con `AuthInterceptor` + interceptor de base URL.
- Root de navegación con gating: `onboarding_completed = false` → onboarding; si no,
  scaffold.
- Cambio de modo en Ajustes (stub funcional, aviso de independencia de datos).

**Criterios de salida**:
- [ ] Flujo local completo contra una BD Room recién creada (categorías default visibles
      vía debug).
- [ ] Flujo remoto completo contra una instancia real del backend (login → token →
      `auth/me` en pantalla).
- [ ] Kill & relanzar la app: recuerda el modo y no repite el onboarding.
- [ ] Cambio de modo local↔remoto sin crash y conservando sesión remota.

## Fase 2 — Dominio del dinero y datos

**Objetivo**: la columna vertebral: reglas de negocio puras con tests, esquema Room
completo y repositorios que delegan por modo. Es la fase más crítica del proyecto; su
calidad hereda todo lo demás.

- `core/money`: `Money` (BigDecimal, 2 dec), `Rate` (4 dec), `CurrencyCode`
  (USD/VES/USDT), converters Room (TEXT canónico).
- `domain/service`: `ConversionService` (congelado por moneda + items), 
  `CommissionCalculator` (`max(mín, %)`), `RatePolicy` (manual > api del día > última
  conocida).
- Room completo de `03-modelo-de-datos.md` (todas las tablas v1, índices, FKs) + DAOs.
- DTOs contra la spec OpenAPI real (`/api/v1.json`) + mapeadores DTO↔dominio.
- Repositorios con delegación por modo (ADR-A01) para gasto/ingreso/categoría/origen/tasa.
- **Tests JVM del dominio** con casos del backend: conversión USD/USDT/Bs, comisión con
  piso, tasa del día, redondeos HALF_UP.

**Criterios de salida**:
- [ ] Cobertura del dominio: todos los servicios con tests que cubran los casos límite
      del `03-modelo-de-datos.md` backend (ves sin tasa → error; comisión 0 explícita;
      tasa manual gana a api).
- [ ] Un test de integración Room (androidTest) inserta un gasto con items y lo lee.
- [ ] Un test con MockWebServer hace login + CRUD de gastos contra respuestas reales.
- [ ] Los repos operan igual en ambos modos (mismo contrato de interfaz, test paramétrico
      con fake local y fake remoto).

## Fase 3 — Gastos (la pantalla estrella)

**Objetivo**: el flujo completo de `06-diseno-ui.md`: lista, detalle, formulario con
comisión y tasa congelada — en ambos modos.

- Lista con agrupación por día + filtros (mes/categoría/moneda) sobre Flows (local) o
  llamadas paginadas (remoto).
- Formulario completo: selector de moneda, método Bs, comisión precargada editable,
  tasa precargada con badge de fuente, equivalente en vivo, validaciones.
- Detalle con tasa aplicada visible y eliminación con confirmación.
- Estados: vacío, carga, error con reintentar, offline (remoto).

**Criterios de salida**:
- [ ] Gasto en Bs con comisión: se guarda y el detalle muestra base, comisión, tasa y
      equivalentes congelados — **idénticos** a lo que la web muestra para los mismos
      datos (verificación manual cruzada local vs web si mismo input).
- [ ] Gasto en USD y USDT sin tasa/comisión: formularios se adaptan (campos Bs ocultos).
- [ ] Filtros funcionan por mes, categoría y moneda en ambos modos.
- [ ] Editar y borrar reflejan exactamente la misma conversión congelada (editar Bs
      recalcula con la tasa editada, como el backend).
- [ ] UI test (Compose) del happy path del formulario.

## Fase 4 — Ingresos y catálogos

**Objetivo**: ingresos CRUD + gestión de categorías y orígenes.

- Ingresos: lista, formulario (sin comisión/método), detalle, filtros por mes/categoría.
- Categorías: crear/editar (nombre, tipo, color, icono, presupuesto mensual), borrar
  bloqueado si tiene movimientos (espejo backend).
- Orígenes de pago: CRUD con los mismos controles.
- Pestaña Ingresos gobernada por `tracking_type` en ambos modos.

**Criterios de salida**:
- [ ] Ingreso en Bs congela su tasa y aparece en el dashboard mensual (cuando llegue F6).
- [ ] Categoría con movimientos no se puede borrar; sin movimientos sí.
- [ ] Crear categoría con presupuesto muestra barra en dashboard (si F6 iniciada, sino
      preparar el modelo).
- [ ] CRUD completo contra la API remota sin errores de validación 422 en el happy path.

## Fase 5 — Tasas

**Objetivo**: la experiencia completa de tasas de `05-integracion-api.md`.

- Modo local: cliente `ve.dolarapi.com`, upsert diario BCV+paralelo, WorkManager periódico,
  refresco de 60 min al abrir formularios, pantalla de tasas con override e histórico.
- Modo remoto: pantalla alimenta de `GET /rates`, override con `PUT /rates`,
  "actualizar ahora" = `POST /rates/sync`.
- Badge de fuente en formularios: BCV / Paralelo / Manual / Estimada.

**Criterios de salida**:
- [x] Con el teléfono en avión, los formularios siguen funcionando con la última tasa
      conocida marcada "estimada" (nunca se bloquea: ADR-002). Cubierto por
      `LocalRateRepositoryTest` (fallback `estimated = true`) y el badge Estimada.
- [x] Override manual del día gana sobre la tasa de la API en formularios y dashboard.
      Cubierto por `RatePolicyTest` y `LocalRateRepositoryTest`; el dashboard lo
      consumirá en la Fase 6.
- [x] Histórico persiste días anteriores (verificable tras cambiar la fecha del emulador).
      Room guarda una fila por `(source, provider, rate_date)`; `observeHistory(30)`
      lo expone en la pantalla de Tasas. Prueba manual en emulador pendiente.

## Fase 6 — Dashboard y ajustes completos

**Objetivo**: la pantalla Inicio responde la pregunta de la app.

- Local: agregaciones SQL del mes (sumas de `usd_amount`/`usdt_amount` congelados),
  presupuesto global y por categoría, últimos movimientos.
- Remoto: `GET /dashboard` + `GET /reports/monthly-summary` mapeados a la misma UI.
- Ajustes completos de `06-diseno-ui.md` (moneda display, comisiones, presupuesto,
  apariencia, idioma, perfil remoto con logout, cambio de modo).
- `PUT /user/profile` persiste cambios remotos; `user_prefs` los locales.

**Criterios de salida**:
- [x] Los totales del dashboard cuadran con la suma manual de la lista del mes (ambos
      modos, test de agregación). Local se cubre con `DashboardAggregatorTest`; remoto
      con `DashboardMappingTest` sobre el shape real de `DashboardController`.
- [x] Cambiar moneda de visualización transforma todo el dashboard al instante. La
      `HomeScreen` observa `UserPreferences` y elige el total primario USD/USDT sin
      recargar; se prueba a mano en el emulador.
- [x] Cambiar comisión mínima/porcentaje cambia el precargado del formulario. Los
      ajustes escriben `user_prefs` y `CommissionSettingsRepository` los lee al abrir
      el formulario (en local y remoto, porque la API v1 no expone comisiones).
- [x] Ajustes de perfil remoto sobreviven cerrar/abrir la app y se reflejan en la web.
      Moneda display y `tracking_type` van por `PUT /user/profile` y se cachean en
      `user_prefs`; `auth/me` no devuelve la moneda display, así que la caché es la
      fuente hasta que el backend amplíe el shape (deuda anotada en `docs/08`).

## Fase 7 — Pulido y release v1

**Objetivo**: digna de publicar.

- Icono de launcher y branding (monograma simple, adapta a `adaptive-icon`).
- Pasada de accesibilidad (talkback en flujos principales), estados vacíos con ilustración
  simple, revisión de contraste.
- Suite de tests estable, `assembleRelease` firmado, README final con capturas,
  CONTRIBUTING.md, CHANGELOG.md inicial (SemVer, como el backend).
- Tag `v1.0.0` + GitHub Release con APK. Evaluar packaging F-Droid (sin blobs).

**Criterios de salida**:
- [x] Instalación limpia → onboarding → registrar gasto → ver dashboard: flujo completo
      verificado en emulador API 37; la pasada en dispositivo físico queda como
      verificación manual del PR.
- [x] `assembleRelease` firmado (keystore local fuera del repo, R8 activo) + CI verde +
      CHANGELOG. Tag [`v1.0.0`](https://github.com/SuricatoAviones/spentz-trackr-kotlin/releases/tag/v1.0.0)
      y GitHub Release publicados con el APK firmado y su checksum SHA-256.
- [x] Backlog post-v1 priorizado en `08-backlog-tareas.md` y evaluación F-Droid en
      [`10-fdroid.md`](10-fdroid.md).

---

## Post-v1

La hoja de ruta completa (matriz de paridad web ↔ Android, dependencias del
backend, alcance, criterios de salida y decisiones abiertas) vive en
[`11-hoja-de-ruta-post-v1.md`](11-hoja-de-ruta-post-v1.md). Los widgets tienen
diseño propio en [`12-widgets.md`](12-widgets.md). Resumen:

| Fase | Tema |
|---|---|
| F8 | Reportes, estado de cuenta y CSV |
| F9 | Gastos mixtos y comprobantes |
| F10 | Widgets, atajos y notificaciones |
| F11 | Pagos recurrentes y metas de ahorro |
| F12 | Tarjetas de crédito |
| F13 | IA BYOK |
| F14 | Backup/export/import y sync offline-first |
| Quick wins | Atajos, share target, biométrico, dynamic color, tablets |

Lo que es solo web (panel de administración, PWA, login 2FA/passkeys sin API)
no se porta al cliente Android; se documenta en la matriz de `11`.
