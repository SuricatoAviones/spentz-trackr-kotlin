# 12 — Widgets (Jetpack Glance)

Diseño canónico de los widgets de Spentz Trackr Android. Forma parte de la
**Fase 10** de [`11-hoja-de-ruta-post-v1.md`](11-hoja-de-ruta-post-v1.md).

Estado: **propuesta de diseño**, sin implementar. La v1.0.0 no incluye widgets.

## Objetivos

- Registrar un gasto/ingreso en **un toque** desde el launcher.
- Ver el estado del mes y del presupuesto **sin abrir la app**.
- Consultar las tasas del día.
- Respetar privacidad, batería y modo local/remoto.
- Mantener F-Droid: nada de Google Play Services ni telemetría.

## Catálogo de widgets

| ID | Widget | Tamaños | Contenido | Acción | Prioridad |
|---|---|---|---|---|---|
| **W1** | Registro rápido | 2×1, 4×1 | Botones “+ Gasto” y “+ Ingreso” | Abre el formulario | **P0** |
| **W2** | Resumen del mes | 4×2, 4×4 | Gastos, ingresos, neto y mes en la moneda display | Abre Inicio | **P0** |
| **W3** | Presupuesto | 2×2, 4×2 | Barra global, gastado/tope, restante o excedido | Abre Inicio o Ajustes | **P1** |
| **W4** | Tasas | 2×1, 4×1 | BCV, paralelo, “actualizado hace X” | Abre Tasas / refresca | **P1** |
| **W5** | Últimos movimientos | 4×2, 4×4 | 3–5 movimientos con color de categoría | Abre el detalle | P2 |
| **W6** | Categoría configurable | 2×2, 4×2 | Gasto/presupuesto de una categoría elegida | Abre la categoría | P2 |
| **W7** | Metas / vencimientos | 4×2 | Progreso de una meta o próximo pago recurrente | Abre meta/pago | P3 (tras F11) |

### W1 — Registro rápido

- Dos botones grandes (objetivo táctil mínimo 48 dp).
- No pide datos: abre `spentz://expense/new` o `spentz://income/new`.
- En el formulario se precargan fecha, tasa del día y moneda display.
- En modo remoto sin red: permite abrir el formulario; el guardado se resolverá
  cuando exista sync offline-first (F14). Hasta entonces muestra el error normal.
- No muestra montos: seguro para pantalla de bloqueo.

### W2 — Resumen del mes

- Mes actual localizado.
- Dos columnas: gastos e ingresos, con la moneda display en grande y la otra
  como secundaria (mismo criterio que el Dashboard).
- Neto (`ingresos − gastos`) en una línea.
- Si el mes está vacío: “Aún no hay movimientos este mes”.
- Si el snapshot está viejo: “Actualizado hace X” y punto de aviso.

### W3 — Presupuesto

- Barra de progreso global; color:
  - normal: `primary`,
  - ≥ 80 %: `tertiary`,
  - > 100 %: `error`.
- Texto “$X de $Y” y “Te quedan $Z” / “Excedido por $Z”.
- Sin presupuesto: “Define un presupuesto” + botón a Ajustes.
- Opcional futuro: variante por categoría (W6).

### W4 — Tasas

- Tarjetas BCV y paralelo con valor y “hace X min”.
- Badge **Estimada** si la última tasa no es del día.
- Acción “Actualizar”:
  - local: encola `RatesSyncWorker` y actualiza al terminar,
  - remoto: `POST /rates/sync` con throttle, o solo abre Tasas si no hay red.
- Si no hay tasas: “Abre la app para sincronizar”.

### W5 — Últimos movimientos

- Últimos 3–5 movimientos del mes (gastos e ingresos según tracking).
- Punto/icono con color de categoría, descripción, fecha corta y monto original.
- Tap → detalle del gasto/ingreso.
- No mostrar notas ni datos sensibles.

### W6 — Categoría configurable

- Configuración al añadir el widget (`GlanceAppWidget` con
  `ConfigurationActivity` o pantalla de ajustes del widget).
- Muestra gasto del mes vs presupuesto de esa categoría.
- Si la categoría se borra: estado “Categoría no disponible”.

### W7 — Metas / vencimientos

- Solo cuando existan F11.
- Una meta configurable con barra de progreso, o el próximo pago recurrente.
- Acción: abrir meta/pago o marcarlo pagado (con confirmación en la app, no en
  el widget).

## UX y privacidad

- **Ajuste “Ocultar montos en widgets”** en Ajustes → Apariencia. Si está activo,
  todos los widgets muestran `••••` y solo dejan ver el estado (sin cifras).
- Los widgets no muestran descripciones largas ni notas.
- En la pantalla de bloqueo, Android decide si se ven; la opción de ocultar
  montos es la red de seguridad de la app.
- Tema claro/oscuro según el sistema; opcional color dinámico Android 12+.
- Sin permisos nuevos: los widgets usan los datos que la app ya tiene.
- Acciones destructivas (borrar, marcar pagado) **no** se hacen desde el widget;
  abren la app y piden confirmación.

## Arquitectura propuesta

```
app/src/main/java/.../
├── widget/
│   ├── WidgetEntryPoint.kt          # acceso Hilt desde Glance
│   ├── WidgetUpdater.kt             # updateAll / update(ids)
│   ├── WidgetSnapshotRepository.kt  # caché local/remota
│   ├── WidgetDeepLinks.kt           # spentz://…
│   ├── quickadd/QuickAddWidget.kt
│   ├── summary/SummaryWidget.kt
│   ├── budget/BudgetWidget.kt
│   ├── rates/RatesWidget.kt
│   ├── recent/RecentWidget.kt
│   └── category/CategoryWidget.kt
└── res/
    ├── xml/quick_add_widget_info.xml
    ├── xml/summary_widget_info.xml
    ├── …
    └── drawable/widget_preview_*.xml
```

### Componentes

- **Glance**: `GlanceAppWidget` + `GlanceAppWidgetReceiver` por widget.
- **`appwidget-provider`**: `minWidth`, `minHeight`, `targetCellWidth/Height`,
  `resizeMode`, `widgetCategory`, `updatePeriodMillis=0` (las actualizaciones
  reales las hace WorkManager/eventos) y `previewLayout`/`description`.
- **Responsive**: `SizeMode.Responsive` con conjuntos de tamaños; un mismo widget
  se adapta a 2×1/4×1 o 2×2/4×2.
- **Hilt**: los receivers no se inyectan directamente; usar
  `EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)`
  para obtener `WidgetSnapshotRepository`/`DashboardRepository`.
- **No red en `provideGlance`**: solo lectura de caché/Room; si no hay datos,
  estado vacío.

### Datos

Contratos internos (dominio, no expuestos a la UI):

```kotlin
data class WidgetSummary(
    val month: YearMonth,
    val displayCurrency: DisplayCurrency,
    val expensesUsd: Money,
    val expensesUsdt: Money,
    val incomesUsd: Money,
    val incomesUsdt: Money,
    val globalBudget: Money?,
    val updatedAt: Long,
    val stale: Boolean,
)

data class WidgetBudget(
    val spentUsd: Money,
    val budgetUsd: Money?,
    val updatedAt: Long,
)

data class WidgetRate(
    val bcv: Rate?,
    val paralelo: Rate?,
    val manual: Rate?,
    val updatedAt: Long,
    val estimated: Boolean,
)

data class WidgetMovement(
    val id: Long,
    val type: DashboardMovementType,
    val description: String,
    val amount: Money,
    val currency: CurrencyCode,
    val date: LocalDate,
    val categoryName: String,
    val categoryColor: String,
)
```

- **Local**: leer de Room con las mismas consultas/agregaciones que el
  Dashboard (`DashboardAggregator`).
- **Remoto**: guardar el último snapshot bueno en una tabla `widget_cache`
  (o DataStore serializado) tras cada carga/actualización de Dashboard. El
  widget nunca llama a la API; si no hay caché, muestra “Abre la app para
  sincronizar”.
- **Modo activo**: el snapshot guarda `mode`; al cambiar de modo se invalida y
  se actualiza.

### Actualización

| Disparador | Qué actualiza |
|---|---|
| Guardar/editar/borrar gasto o ingreso | W1 no; W2/W3/W5/W6 |
| Guardar tasa / sync worker | W4 |
| Dashboard carga datos remotos | Caché de W2/W3/W5 |
| Cambio de modo, moneda display, tema o idioma | Todos |
| WorkManager periódico (30–60 min) | W2/W3/W4 |
| Arranque / cambio de día | W2/W3 |
| Ajuste de privacidad | Todos |

- `WidgetUpdater.updateAll(context)` tras escrituras; usar `goAsync`/WorkManager
  si la actualización es costosa.
- Trabajo periódico con `ExistingPeriodicWorkPolicy.KEEP`, constraints de red
  solo para tasas, y sin despertar el dispositivo innecesariamente.
- Evitar `updatePeriodMillis` corto: el sistema lo limita a 30 min.

### Deep links

Rutas mínimas (se añaden al `NavHost` y a `MainActivity`):

```
spentz://home
spentz://expense/new
spentz://income/new
spentz://expense/{id}
spentz://income/{id}
spentz://rates
spentz://settings
spentz://settings/appearance
```

- `ActionCallback` de Glance lanza un `Intent`/`PendingIntent` con la URI.
- En `MainActivity`, `onNewIntent` + `handleDeepLink(intent)`.
- Si la app está cerrada, el deep link debe funcionar tras el splash/onboarding.
- En modo local/remoto, la ruta es la misma; el repositorio decide la fuente.

### Acciones

- **W1**: abrir formulario (no guarda desde el widget).
- **W4**: `ActionCallback` → `RatesSyncWorker` (local) o `POST /rates/sync`
  (remoto); mostrar estado “Actualizando…” con `GlanceStateDefinition` o
  actualizar al terminar.
- **W2/W3/W5/W6**: abrir pantalla correspondiente.
- **W7**: abrir meta/pago; “marcar pagado” solo dentro de la app con
  confirmación.

## Modo remoto y offline

- El widget es **read-only** salvo W4 (refresco). No intenta autenticarse ni
  hacer POST desde `provideGlance`.
- La caché se rellena cuando el usuario abre la app o cuando un worker remoto
  autorizado actualiza el Dashboard.
- Si el token expiró, el widget muestra el último snapshot con “Sincroniza en la
  app”; no abre un login desde el widget.
- Cuando llegue F14 (sync offline-first), el widget podrá reflejar la cola de
  escrituras pendientes (“1 cambio sin sincronizar”).

## Accesibilidad

- `contentDescription` en cada acción (“Nuevo gasto”, “Nuevo ingreso”,
  “Actualizar tasas”).
- Textos con `maxLines` y elipsis; no depender solo del color (usar también
  texto/icono para “excedido”).
- Objetivos táctiles ≥ 48 dp.
- Soporte de fuente grande: tamaños responsivos y textos que no se solapen.
- En W3, `stateDescription` con “gastado X de Y”.

## Rendimiento y batería

- Nada de red en `provideGlance`.
- Snapshots pequeños (no listas completas); W5 limita a 5 movimientos.
- Una sola actualización por evento; agrupar con `WorkManager` si hay ráfagas.
- No usar `AlarmManager` exacto para widgets.
- Medir con Macrobenchmark/Perfetto si hay quejas de batería.

## Pruebas

- **Unitarias**: contratos de snapshot, formateo de montos/fechas, cálculo de
  porcentaje, estados vacío/estimada/excedido.
- **Instrumentación**: `GlanceAppWidget` con el artefacto de testing si está
  disponible; si no, prueba manual en launcher.
- **Manual** (checklist):
  - añadir widget en 2×1, 4×2 y 4×4,
  - crear gasto → se actualiza,
  - cambiar tema/idioma → se actualiza,
  - modo avión → muestra último snapshot,
  - activar “ocultar montos” → `••••`,
  - tocar cada acción → abre la pantalla correcta,
  - rotar/reiniciar launcher → sigue funcionando.
- **CI**: tests unitarios; los widgets no requieren emulador en CI v1.

## F-Droid

- `androidx.glance:glance-appwidget` es AndroidX/FOSS; no usa Play Services.
- Sin telemetría ni crash reporting.
- Color dinámico es opcional; si no está disponible, se usa la paleta de marca.
- Los widgets se incluyen en el APK de F-Droid sin cambios de firma.

## Orden de implementación

| Entrega | Contenido | Criterio |
|---|---|---|
| **W1** | Registro rápido + deep links + `WidgetUpdater` | Crear gasto/ingreso desde widget abre el formulario correcto |
| **W2** | Resumen del mes + caché local | Totales coinciden con Dashboard |
| **W3** | Presupuesto + privacidad | Barra y “ocultar montos” funcionan |
| **W4** | Tasas + refresco | BCV/paralelo reales y “hace X” |
| **W5** | Últimos movimientos | Abre detalle correcto |
| **W6** | Categoría configurable | Configuración persiste |
| **W7** | Metas/vencimientos | Tras F11 |

## Decisiones abiertas

- ¿Los widgets se ofrecen también en modo remoto con caché, o solo en local en
  la primera entrega?
- ¿W4 refresca desde el widget o solo abre Tasas? (coste/throttle)
- ¿Snapshots en Room (`widget_cache`) o DataStore JSON? Room facilita consultas
  y limpieza; DataStore evita migración.
- ¿Un widget combinado “Resumen + presupuesto” o dos separados? Empezar
  separados y medir uso.
- ¿Configuración de W6 dentro del widget (`ConfigurationActivity`) o en Ajustes?
