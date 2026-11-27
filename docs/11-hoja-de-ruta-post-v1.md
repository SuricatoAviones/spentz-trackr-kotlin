# 11 — Hoja de ruta post-v1 (paridad con Spentz)

Este documento es la referencia canónica de **qué falta para que el cliente Android
alcance la paridad funcional con la app web de Spentz Trackr** y en qué orden se
hará. El backlog resumido vive en
[`08-backlog-tareas.md`](08-backlog-tareas.md); el diseño detallado de widgets en
[`12-widgets.md`](12-widgets.md).

Estado: **v1.0.0 publicada** (2026-11-26). Lo de abajo es post-v1, sin fechas
comprometidas; cada fase se abrirá en su rama `fase/<n>-<slug>` cuando se priorice.

## Cómo leer “paridad”

No todo lo que hace la web debe existir en Android:

- **Paridad funcional**: el usuario puede hacer en el teléfono lo mismo que en la web
  (capturar, consultar, reportar, automatizar).
- **Solo web**: panel de administración multiusuario, PWA instalable, gestión de la
  instancia, 2FA/passkeys del login web (salvo que el backend exponga un flujo API).
- **Solo Android**: widgets, atajos, notificaciones locales, biométrico, cámara,
  almacenamiento app-scoped y modo local sin servidor.

## Matriz de paridad

| Área | Web / backend | Android v1 | Estado | Fase |
|---|---|---|---|---|
| Onboarding y modos | Login web | Local/remoto | ✅ | — |
| Gastos e ingresos simples | ✅ | ✅ | ✅ | — |
| Categorías con presupuesto | ✅ | ✅ | ✅ | — |
| Orígenes de pago | ✅ | ✅ | ✅ | — |
| Tasas BCV/paralelo/manual | ✅ (sync 5 min) | ✅ (~6 h + lazy) | ✅/⚠️ | — |
| Dashboard | ✅ | ✅ | ✅ | — |
| Gastos mixtos (`expense_items`) | ✅ | ❌ | Falta UI | 9 |
| Comprobantes/fotos | ✅ | ❌ | Falta captura/upload | 9 |
| Reportes y estado de cuenta | ✅ | ❌ | Falta pantalla + CSV | 8 |
| Exportación CSV | ✅ | ❌ | Falta FileProvider/share | 8 |
| Metas de ahorro + aportes | ✅ | ❌ | API lista | 11 |
| Pagos recurrentes + `pay` | ✅ | ❌ | API lista | 11 |
| Tarjetas de crédito | ✅ | ❌ | API lista; leer ADR-012 | 12 |
| IA BYOK (chat, escaneo, importación) | ✅ | ❌ | API `ai/*` lista | 13 |
| Notificaciones de presupuesto/vencimientos | Parcial | ❌ | Falta WorkManager + canales | 10/11 |
| Widgets | — | ❌ | Diseño en `12` | 10 |
| Atajos de app / share target | — | ❌ | Quick wins | 10 |
| Sync offline-first remoto | — | ❌ | Solo online | 14 |
| Backup/export/import local | Parcial | ❌ | Falta export completo | 14 |
| Bloqueo biométrico | — | ❌ | BiometricPrompt | Quick win |
| Multi-perfil local | — | ❌ | Solo si hay demanda | Post-v1 |
| 2FA/passkeys | ✅ web | ❌/⚠️ | La API de login no expone el flujo | Backend |

## Dependencias del backend

Antes de algunas fases hay que confirmar o ampliar el backend. No se asume nada que
no esté en la API:

| Necesidad | Estado actual | Qué falta |
|---|---|---|
| Comprobantes | `has_receipt` en gastos; subida por verificar | Confirmar `POST/PUT /expenses` multipart y shape de `receipt` |
| Comisiones y presupuesto remotos | `PUT /user/profile` solo acepta `name/email/default_display_currency/tracking_type` | Exponer `min_commission`, `commission_rate`, `monthly_budget` en perfil/`auth/me` |
| Moneda de visualización | `auth/me` no la devuelve | Incluir `default_display_currency` en `userShape()` |
| Reportes | `GET /reports`, `/reports/monthly-summary`, `/reports/statement` | Verificar shapes contra OpenAPI antes de mapear |
| Metas de ahorro | `savings-goals` + contributions | Verificar reglas de aporte/retiro y moneda |
| Pagos recurrentes | `recurring-payments` + `pay` | Confirmar qué crea `pay` (gasto/ingreso) y cómo avanza la fecha |
| Tarjetas de crédito | `credit-cards` + statements/payments | Leer `docs/12-tarjetas-de-credito.md` del backend y ADR-012 |
| IA | `ai/settings`, `chat`, `receipts/scan`, `statement/analyze`, `statements/scan/import` | Verificar payloads, límites y manejo de claves |
| 2FA/passkeys | Web | Solo si se quiere login API con 2FA; hoy la app usa email/contraseña + Sanctum |

## Fases propuestas

### Fase 8 — Reportes, estado de cuenta y CSV

**Objetivo**: que el usuario pueda responder “¿cómo voy?” y sacar sus datos.

**Alcance**

- `ReportRepository` local/remoto/delegador (ADR-A01) con:
  - resumen mensual (totales y desglose por categoría/origen/moneda),
  - comparativo de 12 meses,
  - estado de cuenta por periodo con neto acumulado.
- Pantalla **Reportes** accesible desde Inicio y Ajustes: selector de periodo,
  tarjetas de totales, gráfico de 12 meses (Compose, sin librería) y lista de
  movimientos con saldo acumulado.
- **CSV** con BOM UTF-8, `FileProvider` y hoja de compartir; columnas:
  `fecha,tipo,descripción,categoría,origen,moneda,monto,comisión,tasa,usd,usdt`.
- Remoto: `GET /reports`, `GET /reports/monthly-summary`,
  `GET /reports/statement`; local: agregaciones SQL + `BigDecimal`.
- Tests: totales del reporte = suma de la lista; CSV bien escapado; estado de
  cuenta cuadra.

**Criterios de salida**

- [ ] Reporte mensual coincide con Dashboard y con la lista del mes.
- [ ] Comparativo de 12 meses con los meses sin movimientos en cero.
- [ ] Estado de cuenta con saldo acumulado correcto y exportable.
- [ ] CSV abre en Excel/LibreOffice con acentos y montos correctos.

**Complejidad**: S/M. **Riesgo**: shapes de reportes remotos.

### Fase 9 — Gastos mixtos y comprobantes

**Objetivo**: paridad real de captura con la web.

**Alcance**

- UI de **porciones extra** (`expense_items`): moneda, monto y tasa por porción;
  alta/edición/borrado; el preview y el congelado suman la porción principal +
  ítems (el dominio y Room ya lo soportan).
- Detalle de gasto con desglose de ítems y total congelado.
- **Comprobante**: cámara o galería, compresión, almacenamiento app-scoped
  (`filesDir/receipts/…`), miniatura, borrado y persistencia de la ruta.
- Remoto: subida multipart si el backend la confirma; si no, se documenta como
  deuda y se mantiene solo local.
- Migración Room para la ruta/URL del comprobante.

**Criterios de salida**

- [ ] Gasto mixto (Bs + USDT) congela cada porción y el total coincide con la
      suma de equivalentes.
- [ ] Editar/eliminar un ítem recalcula una sola vez al guardar.
- [ ] El comprobante sobrevive a reinicios y se borra con el gasto.
- [ ] Sin red, el comprobante local sigue disponible.

**Complejidad**: M. **Riesgo**: multipart no confirmado; tamaño de imágenes.

### Fase 10 — Widgets, atajos y notificaciones

**Objetivo**: que registrar y consultar no requiera abrir la app.

**Alcance**

- Widgets Glance según [`12-widgets.md`](12-widgets.md):
  - **W1 Registro rápido** (2×1/4×1): “+ Gasto” / “+ Ingreso”.
  - **W2 Resumen del mes** (4×2): gastos, ingresos, neto y presupuesto.
  - **W3 Presupuesto** (2×2/4×2): barra global + restante.
  - **W4 Tasas** (2×1): BCV/paralelo + “actualizado hace X”.
- **Atajos de app** al mantener pulsado el icono: Nuevo gasto, Nuevo ingreso,
  Tasas, Inicio.
- **Notificaciones** (Android 13+ con permiso):
  - presupuesto global/categoría al cruzar el 80 % y el 100 %,
  - tasa desactualizada (> 24 h) en modo local,
  - pagos recurrentes próximos (cuando exista la fase 11).
- Deep links `spentz://…` y manejo en `MainActivity`/`NavHost`.
- Ajuste “ocultar montos en widgets”.

**Criterios de salida**

- [ ] Los widgets se actualizan tras crear/editar/borrar un movimiento y tras
      sincronizar tasas.
- [ ] Los widgets respetan tema claro/oscuro y el ajuste de privacidad.
- [ ] Tocar un widget abre la pantalla correcta.
- [ ] Las notificaciones no se duplican y se pueden desactivar por canal.

**Complejidad**: M. **Riesgo**: limitaciones de actualización de App Widgets.

### Fase 11 — Pagos recurrentes y metas de ahorro

**Objetivo**: automatizar lo que se repite y ahorrar con un objetivo.

**Alcance**

- **Recurrentes**: CRUD, próxima fecha, monto/moneda/categoría/origen,
  “marcar pagado” (crea el gasto/ingreso correspondiente y avanza la fecha),
  historial de pagos y recordatorios.
- **Metas**: CRUD, monto objetivo, fecha, moneda, aportes y retiros, progreso y
  estado (activa/completada). Espejo de `savings-goals`.
- Tablas Room nuevas + migración; dominio y repositorios local/remoto.
- Notificaciones de vencimiento con WorkManager.

**Criterios de salida**

- [ ] Marcar pagado crea el movimiento con la tasa congelada y no duplica.
- [ ] La meta muestra progreso correcto tras aportar/retirar.
- [ ] Las notificaciones de vencidos respetan zona horaria y no spamean.
- [ ] Remoto y local comparten contrato de repositorio.

**Complejidad**: L. **Riesgo**: reglas del backend para `pay` y aportes.

### Fase 12 — Tarjetas de crédito

**Objetivo**: cubrir el modelo de tarjetas con cortes y abonos.

**Alcance**

- Leer primero `docs/12-tarjetas-de-credito.md` y ADR-012 del backend.
- Modelo local espejo: tarjeta, corte/estado de cuenta y abonos; moneda y tasa
  cuando aplique.
- Pantallas: lista de tarjetas, detalle, corte actual, historial de cortes y
  abonos; saldo/consumo disponible.
- Repositorios local/remoto; endpoints `credit-cards`,
  `statements`, `payments`.
- Reportes: incluir tarjetas en el estado de cuenta si el backend lo hace.

**Criterios de salida**

- [ ] Un corte y un abono actualizan el saldo correctamente.
- [ ] Una tarjeta en Bs congela la tasa al registrar el corte/abono.
- [ ] El historial cuadra con la web.

**Complejidad**: L. **Riesgo**: modelo de saldo híbrido; leer ADR antes de codear.

### Fase 13 — IA BYOK

**Objetivo**: asistente con la clave del usuario, sin telemetría.

**Alcance**

- Ajustes de IA: proveedor (OpenAI, Claude, Gemini, Mistral, Groq, DeepSeek,
  OpenRouter, OpenCode, Ollama), clave, modelo y “probar conexión”.
- Clave en almacenamiento cifrado (AndroidX Security) y nunca en logs.
- **Remoto**: usar los endpoints `ai/*` del backend (la clave vive en el servidor).
- **Local**: decidir entre llamar directo al proveedor con consentimiento
  explícito o no ofrecer IA local en v1; documentar la decisión y el aviso de
  privacidad/coste.
- Funciones: chat sobre finanzas, escaneo de factura → borrador de gasto,
  análisis/importación de estado de cuenta → movimientos propuestos que el
  usuario confirma.
- Nada se guarda como movimiento sin confirmación humana.

**Criterios de salida**

- [ ] La clave no aparece en logs, backups ni crash reports.
- [ ] Una factura escaneada propone un borrador editable, no guarda sola.
- [ ] Si el proveedor falla, la app sigue funcionando sin IA.
- [ ] Se avisa de que las imágenes/textos salen del teléfono.

**Complejidad**: L. **Riesgo**: coste, privacidad, límites de proveedores.

### Fase 14 — Backup, export/import y sync offline-first

**Objetivo**: no perder datos y poder usar el modo remoto sin conexión.

Dividir en dos entregas para no mezclar riesgos:

1. **Backup/export/import local** (M): JSON + CSV + adjuntos en un ZIP,
   `FileProvider`, restauración con validación de versión y modo; opción de
   exportar solo movimientos o todo.
2. **Sync offline-first** (XL): Room como caché del remoto, cola de escrituras,
   IDs locales↔remotos, resolución de conflictos, tombstones, adjuntos,
   reintentos y estados “pendiente/sincronizado/error”. Proyecto propio con
   ADR y pruebas de integración.

**Criterios de salida**

- [ ] Exportar e importar en un teléfono limpio reproduce los datos.
- [ ] En remoto sin red se puede consultar y registrar; al volver la red se
      sincroniza sin duplicar.
- [ ] Los conflictos tienen una política documentada y visible.

**Complejidad**: M (backup) / XL (sync). **Riesgo**: IDs y conflictos.

## Quick wins (no son fases completas)

| Mejora | Esfuerzo | Nota |
|---|---|---|
| Atajos de app (long-press) | S | Nuevo gasto/ingreso, Tasas, Inicio |
| Share target de imágenes/PDF | S/M | Base para comprobantes e IA |
| Bloqueo biométrico | S | BiometricPrompt + ajuste |
| Ajuste de frecuencia de tasas | S | Evaluar 30/60 min frente a batería |
| Dynamic color / Material You | S | Opcional, sin Play Services |
| Tablets y plegables | M | `WindowSizeClass`, dos paneles |
| Baseline profiles / startup | M | Arranque y scroll |
| Tests de captura y CI con emulador | M | Suite más estable |
| Enlaces `Acerca de` | S | Repo, licencia, changelog, F-Droid |

## Decisiones abiertas

| Pregunta | Opciones | Impacto |
|---|---|---|
| ¿IA local? | Solo remoto / BYOK directo con consentimiento | Privacidad, coste, complejidad |
| ¿Widgets en remoto? | Solo local / caché de último snapshot | UX y complejidad de caché |
| ¿Sync o backup primero? | Backup JSON primero / sync completo | Riesgo y tiempo |
| ¿2FA en API? | Mantener email/contraseña / ampliar backend | Seguridad de instancias con 2FA |
| ¿Frecuencia de tasas local? | 6 h / 60 min / 30 min | Frescura vs batería |
| ¿Multi-perfil local? | No / sí | Solo si hay demanda |

## Definition of Done por fase

Cada fase post-v1 repite el flujo de `AGENTS.md`:

- Rama `fase/<n>-<slug>` desde `main`.
- Commits convencionales por tarea, referencia `docs/08 F<n>-<tarea>`.
- `ktlintCheck`, `lint`, `test`, `assembleDebug`, `assembleRelease` y
  `assembleDebugAndroidTest` en verde.
- Documentación actualizada: `07`, `08`, este documento y `12` si toca widgets.
- Capturas nuevas si hay UI.
- PR con criterios de salida verificados y cómo probarlo a mano.
