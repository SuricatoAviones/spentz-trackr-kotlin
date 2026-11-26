# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y [SemVer](https://semver.org/lang/es/).

## [Unreleased]

## [1.0.0] — 2026-11-26

Primera versión pública del cliente Android de Spentz Trackr.

### Añadido

- **Onboarding** con elección de modo local o remoto, login/registro contra el
  backend (Sanctum) y setup rápido local (moneda, seguimiento y presupuesto).
- **Gastos** en USD, Bs y USDT con comisión Bs (`max(mínimo, monto × %)`),
  método de pago, origen, categoría, fecha/nota y equivalente congelado.
- **Ingresos** con tasa congelada y catálogo propio de categorías.
- **Categorías** con presupuesto, icono/color y borrado bloqueado por
  movimientos.
- **Orígenes de pago** con CRUD y protección de borrado.
- **Tasas** BCV/paralelo desde `ve.dolarapi.com`, sincronización WorkManager
  (~6 h), refresco perezoso al abrir formularios, override manual del día,
  histórico de 30 días y badge **Estimada** sin red (ADR-002).
- **Dashboard** con totales del mes, presupuesto global y por categoría,
  mini-gráfico por categoría y últimos movimientos (Room en local,
  `GET /dashboard` en remoto).
- **Ajustes completos**: moneda de visualización, tipo de seguimiento,
  presupuesto, comisiones, tema claro/oscuro/sistema, idioma ES/EN/sistema,
  perfil remoto (email + servidor + logout) y Acerca de.
- **Bilingüe** ES/EN con paridad de claves.
- **Splash API**, icono adaptativo (incluido monochrome) y estados vacíos
  ilustrados.
- **Accesibilidad**: descripciones en controles, semántica de progreso y
  objetivos táctiles revisados.
- **Release**: R8/ProGuard activo, firma externa por `keystore.properties`,
  metadata de F-Droid (fastlane) y CI con `assembleRelease` de validación.

### Cambiado

- `versionName` pasa a `1.0.0`.
- Las listas principales animan la colocación de sus filas.
- El tema y el idioma se aplican en vivo desde Ajustes.

### Corregido

- Las filas de gastos e ingresos ahora abren su detalle al tocarlas.
- El RadioButton del onboarding no duplica la acción de la tarjeta.
- La lectura de configuración para el override de idioma usa
  `LocalConfiguration` (lint `LocalContextConfigurationRead`).

### Seguridad

- Sin telemetría, analítica ni crash reporting.
- Token Sanctum en almacenamiento cifrado (AndroidX Security).
- `http://` avisado en la UI; se recomienda HTTPS.

[Unreleased]: https://github.com/SuricatoAviones/spentz-trackr-kotlin/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/SuricatoAviones/spentz-trackr-kotlin/releases/tag/v1.0.0
