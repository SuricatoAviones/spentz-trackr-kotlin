# Spentz Trackr Android — Documentación del Proyecto

Cliente Android nativo (Kotlin + Jetpack Compose) de
[Spentz Trackr](https://github.com/SuricatoAviones/spentz-trackr), el control de finanzas
multi-moneda (USD · Bs · USDT) open source y auto-hospedable.

El `README.md` de la raíz es el punto de entrada; estos documentos son la referencia
técnica y de producto que se escribe **antes** de codear. El backend y la app web tienen
su propia documentación en el repo original (`docs/` de `spentz-trackr`); aquí solo se
documenta el cliente Android y su integración con ellos.

## Índice de documentos

| Documento | Descripción |
|---|---|
| [01-vision-y-alcance.md](01-vision-y-alcance.md) | Visión, decisiones de producto confirmadas y alcance de la v1 |
| [02-arquitectura.md](02-arquitectura.md) | Stack, arquitectura por capas y ADRs de Android |
| [03-modelo-de-datos.md](03-modelo-de-datos.md) | Esquema local (Room/SQLite), equivalencias con el backend y DTOs |
| [04-onboarding-y-modos.md](04-onboarding-y-modos.md) | Primer arranque: modo remoto vs local, cambio de modo y datos |
| [05-integracion-api.md](05-integracion-api.md) | Superficie de la API `/api/v1` usada, auth, throttling y errores |
| [06-diseno-ui.md](06-diseno-ui.md) | Mapa de pantallas, navegación y componentes clave |
| [07-plan-de-fases.md](07-plan-de-fases.md) | Fases de implementación con entregables y criterios de salida |
| [08-backlog-tareas.md](08-backlog-tareas.md) | Checklist de tareas por fase y backlog post-v1 |
| [09-entorno-y-herramientas.md](09-entorno-y-herramientas.md) | Abrir en Android Studio / IntelliJ, emulador, convenciones y CI |
| [10-fdroid.md](10-fdroid.md) | Empaquetado, metadata y publicación en F-Droid |
| [11-hoja-de-ruta-post-v1.md](11-hoja-de-ruta-post-v1.md) | Paridad con la web, fases post-v1, dependencias y criterios |
| [12-widgets.md](12-widgets.md) | Diseño de widgets Glance: catálogo, datos, actualización y pruebas |

## Cómo leer esta documentación

- **Para entender el producto**: empieza por `01`.
- **Para empezar a codear**: `07` (fases) + `08` (tareas). `02` y `03` son la referencia
  técnica constante.
- **Para supervisar el desarrollo**: `07` y `08` funcionan como tablero de progreso.
- **Para montar el entorno**: `09`.

## Convenciones de los documentos

- Los **ADRs de Android** (A-01…) viven en `02-arquitectura.md`. Cuando una decisión viene
  heredada del backend, se referencia el ADR original (ADR-001…ADR-013 de
  `spentz-trackr/docs/04-arquitectura.md`) en lugar de re-explicarla.
- Fechas en formato `YYYY-MM-DD`; montos con coma decimal solo en prosa, `.` en datos.
- El idioma de trabajo es el español, como en el resto del proyecto.
