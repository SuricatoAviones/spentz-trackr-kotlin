# Spentz Trackr — Android (Kotlin)

**Cliente Android nativo de [Spentz Trackr](https://github.com/SuricatoAviones/spentz-trackr)**, el control de
gastos e ingresos multi-moneda (USD · Bs · USDT) open source y auto-hospedable.

Kotlin · Jetpack Compose · Material 3 · Room (SQLite) · Retrofit · Hilt.

---

## ¿Qué es?

La app Android de Spentz Trackr. Al primer arranque eliges cómo usarla:

- **Modo remoto** — te conectas a tu propia instancia del backend (la URL donde lo tienes
  hospedado). La app habla con la API REST `/api/v1` (tokens Sanctum) y tus datos viven en
  tu servidor, igual que en la web.
- **Modo local** — la app funciona sola, sin servidor: todo se guarda en SQLite en el
  teléfono, con la misma lógica de negocio (conversión congelada, comisiones Bs, tasas
  BCV/paralelo desde [ve.dolarapi.com](https://ve.dolarapi.com)).

El modo se puede cambiar después en **Ajustes**.

## Estado

🚧 **En desarrollo — Fase 3 (gastos) completada** en la rama
`fase/3-gastos` (PR a `main`): la app ya **hace cosas de verdad** — pestaña
Gastos con lista agrupada por día y filtros (mes/categoría/moneda), formulario
con comisión precargada editable y tasa con badge BCV/Paralelo/Manual,
**equivalente congelado en vivo** (el ejemplo canónico del backend:
`(280 Bs + 14) / 28 = $10.50 / 10.50 USDT`), detalle con edición/borrado y
FAB de alta rápida. Todo sobre el dominio de dinero de la Fase 2, en ambos
modos. Plan en [`docs/07-plan-de-fases.md`](docs/07-plan-de-fases.md),
tablero en [`docs/08-backlog-tareas.md`](docs/08-backlog-tareas.md) y flujo
en [`AGENTS.md`](AGENTS.md).

| Documento | Descripción |
|---|---|
| [`docs/README.md`](docs/README.md) | Índice de la documentación |
| [`docs/01-vision-y-alcance.md`](docs/01-vision-y-alcance.md) | Visión, decisiones de producto y alcance v1 |
| [`docs/02-arquitectura.md`](docs/02-arquitectura.md) | Arquitectura técnica y ADRs |
| [`docs/03-modelo-de-datos.md`](docs/03-modelo-de-datos.md) | Esquema Room local y DTOs de la API |
| [`docs/04-onboarding-y-modos.md`](docs/04-onboarding-y-modos.md) | Primer arranque, modo remoto/local y cambio de modo |
| [`docs/05-integracion-api.md`](docs/05-integracion-api.md) | Endpoints, auth Sanctum y manejo de errores |
| [`docs/06-diseno-ui.md`](docs/06-diseno-ui.md) | Pantallas, navegación y componentes |
| [`docs/07-plan-de-fases.md`](docs/07-plan-de-fases.md) | Plan de fases con criterios de salida |
| [`docs/08-backlog-tareas.md`](docs/08-backlog-tareas.md) | Backlog de tareas detallado |
| [`docs/09-entorno-y-herramientas.md`](docs/09-entorno-y-herramientas.md) | Android Studio / IntelliJ, emulador y convenciones |

## Cómo abrir el proyecto

Resumen rápido — el detalle paso a paso está en
[`docs/09-entorno-y-herramientas.md`](docs/09-entorno-y-herramientas.md):

1. **Android Studio**: `File → Open…` y selecciona esta carpeta
   (`spentz-trackr-kotlin`). Espera el *Gradle sync* (la primera vez descarga
   dependencias; ya es un proyecto Gradle completo desde la Fase 0).
2. Ejecuta con ▶ (*Run*) sobre un emulador del **Device Manager** o un teléfono con
   *Depuración USB* activada. Verás el placeholder de Inicio de la Fase 0.
3. **IntelliJ IDEA** sirve para revisar código y Markdown, pero para **compilar y correr**
   un proyecto Android usa Android Studio (es IntelliJ + las herramientas Android).

Tu SDK local ya tiene las plataformas `android-36.1` y `android-37.0` instaladas
(detalle del entorno verificado en
[`docs/09-entorno-y-herramientas.md`](docs/09-entorno-y-herramientas.md)).

## Relación con el proyecto web

Este repo es el **cliente Android**. El backend (Laravel), la web app y su documentación
viven en [SuricatoAviones/spentz-trackr](https://github.com/SuricatoAviones/spentz-trackr).
Las reglas de negocio son las mismas y sus decisiones (ADRs) se respetan aquí; los ADRs
propios de Android están en [`docs/02-arquitectura.md`](docs/02-arquitectura.md).

El proyecto se desarrolla en español; la aplicación es bilingüe (ES/EN).

## Contribuir

Issues y pull requests bienvenidos. Lee [CONTRIBUTING.md](CONTRIBUTING.md) antes de
empezar cuando exista; mientras tanto, el punto de partida son los documentos de
[`docs/`](docs/README.md).

## Seguridad

¿Encontraste un fallo? **No abras un issue público.** Usa el canal de
[SECURITY.md](https://github.com/SuricatoAviones/spentz-trackr/blob/main/SECURITY.md)
del proyecto.

## Licencia

[MIT](LICENSE).
