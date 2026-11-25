# 06 — Diseño de UI

Jetpack Compose + Material 3, un solo Activity con `Navigation-Compose`. Pantallas
declarativas con state hoisting estricto: los ViewModels exponen `StateFlow<UiState>`,
las composables no conocen repositorios.

## Mapa de navegación

```mermaid
flowchart TD
    O[Onboarding] --> H
    subgraph SCAFFOLD[Barra inferior: Inicio · Gastos · (Ingresos) · Ajustes]
        H[Inicio / Dashboard]
        E[Gastos: lista + filtros]
        I[Ingresos: lista] 
        T[Ajustes]
        EF[Formulario de gasto]
        ED[Detalle de gasto]
        IF[Formulario de ingreso]
        CAT[Categorías y orígenes]
        RT[Tasas]
        MD[Cambio de modo / login servidor]
    end
    H -.FAB.-> EF
    E --> ED --> EF
    T --> CAT
    T --> RT
    T --> MD
    E --> EF
    I --> IF
```

- La pestaña **Ingresos** solo aparece si `tracking_type` ∈ {income, both}; la de
  **Gastos** si ∈ {expenses, both} (uno de los dos siempre visible; si el tipo es
  "expenses" la de ingresos se oculta).
- **FAB global** (＋) en Inicio: abre el formulario de gasto — la acción más frecuente de
  la app (métrica de éxito: < 20 s para registrar).

## Pantallas de la v1

### Onboarding (3–4 pantallas)
Definidas en `04-onboarding-y-modos.md`: bienvenida → elección de modo (radio-cards) →
login del servidor (remoto) o setup rápido (local).

### Dashboard (`Inicio`)
- **Tarjeta de totales del mes**: gasto e ingreso en USD y USDT (según
  `default_display_currency` se destaca una), mes en curso con nombre localizado.
- **Presupuesto**: barra de progreso del presupuesto global del mes
  (gastado/total, color según %).
- **Próximos cortes / vencimientos**: fuera de v1 (recurrentes/tarjetas).
- **Últimos movimientos**: lista compacta (descripción, categoría con su color, monto
  con moneda) — toca y abre el detalle.
- **Mini gráfico** de gasto por categoría del mes (chips de progreso o barras simples;
  sin librería de charts: Compose `Canvas`/`LinearProgressIndicator`).
- Estados: vacío ("Aún no hay movimientos este mes — registra el primero"),
  carga, error (remoto).

### Gastos
- **Lista** agrupada por día (header con fecha localizada y subtotal del día), cada fila:
  icono de la categoría con su color, descripción, método/origen, monto en la moneda
  original + equivalente USD.
- **Filtros** en un `ModalBottomSheet`: mes (selector), categoría (chips), moneda
  (todas/USD/Bs/USDT).
- **Detalle**: todos los campos, comisión y tasa congelada visible ("Tasa aplicada:
  28.5000 Bs/USD (BCV)"), editar/eliminar (confirmación).

### Formulario de gasto — la pantalla más importante de la app
Diseño de **una pantalla con secciones**, no wizard (menos clicks, el wizard cansa con
uso frecuente):

1. **Monto grande** + selector de moneda (segmented: USD · Bs · USDT).
2. Si **Bs**: selector de método (`pago móvil` / `transferencia` / sin método) →
   **comisión precargada** con la regla `max(mín, %)` y **editable** (campo con botón
   "sin comisión"); **tasa precargada** (BCV/paralelo del día o manual) con badge de
   fuente y edición inline; el equivalente USD/USDT se ve **en vivo**.
3. **Categoría** (chips horizontales con icono/color) y **origen de pago** (dropdown).
4. **Fecha** (default hoy, selector Material), **descripción** y **nota** (opcional).
5. Botón guardar fijo abajo; validaciones en línea (monto > 0, tasa > 0 si Bs).

- La tasa **nunca** bloquea: si no hay red ni tasa local, el campo queda libre para
  escritura manual con aviso "no hay tasa del día".
- Equivalente congelado mostrado antes de guardar: "Se registrará como $10.50 /
  10.50 USDT" — sin sorpresas.

### Ingresos
Lista + detalle + formulario, sin método/comisión; mismo patrón de tasa congelada en Bs.

### Categorías y orígenes (`Ajustes → Catálogos`)
Listas con edición (nombre, color de una paleta fija, icono de un set fijo, presupuesto
mensual para categorías, tipo gasto/ingreso al crear). Borrar con gasto/ingreso asociado
→ **bloqueado** con mensaje (espejo del backend FK restrict).

### Tasas (`Ajustes → Tasas`)
- Tarjeta **BCV** y tarjeta **Paralelo** del día (tasa, fuente, "hace X min").
- **Override manual**: "Usar mi tasa de hoy" (queda como `manual`/`user`).
- **Histórico**: lista por fecha (últimos 30 días en v1).
- Botón **Actualizar ahora** (local: dolarapi; remoto: `POST /rates/sync`).

### Ajustes
- Perfil/estado: remoto → servidor + email ("Sesión: user@x · https://…") con logout;
  local → "Modo local — datos en este teléfono".
- **Cambiar de modo** → flujo de `04-onboarding-y-modos.md`.
- Moneda de visualización, tipo de seguimiento, presupuesto mensual, comisión mínima y
  porcentaje.
- Apariencia (claro/oscuro/sistema), idioma (sistema/es/en), Acerca de (versión, licencia,
  repo).

## Tema y identidad

- Material 3, **esquema por defecto generado de una semilla verde** (finanzas) hasta que
  el proyecto web defina paleta oficial; modo oscuro completo.
- Tipografía por defecto M3 (`Typography` con ajustes de tamaño si hace falta).
- Iconos: Material Symbols; iconos de categorías = set fijo de ~24 símbolos mapeados a
  nombres (almacenados como string, igual que el backend).
- Formato de montos: `NumberFormat` con locale del teléfono; Bs con separador de miles y
  sin decimales ocultos (2 siempre); USD/USDT con símbolo según locale.

## i18n y accesibilidad

- Español por defecto (`values/`), inglés en `values-en/` — paridad de claves verificada
  por test (ADR-A13).
- `contentDescription` en todos los elementos con significado (chips de categoría, FAB,
  barras de presupuesto), touch targets ≥ 48 dp, contraste AA mínimo.
- Navegación por teclado/scroll restaurable (state de lista persistido por ruta).
