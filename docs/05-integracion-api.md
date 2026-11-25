# 05 — Integración con la API

El modo remoto habla con la **API REST v1** del backend, autenticada con tokens
**Sanctum** (ADR-009 del backend: expiración 90 días, `throttle:api` 100/min,
`throttle:api.auth` 5/min por IP). La superficie exacta está en la spec OpenAPI del
servidor: **`GET {url}/api/v1.json`** (Scramble) — referencia autorativa para definir los
DTOs en la Fase 2.

## Endpoints usados en v1

| Función | Endpoint | Método | Notas |
|---|---|---|---|
| Login (token) | `/api/v1/auth/login` | POST | `throttle:api.auth` (5/min) |
| Registro | `/api/v1/auth/register` | POST | Opcional en la UI de onboarding |
| Logout | `/api/v1/auth/logout` | DELETE | Revoca el token |
| Mi perfil | `/api/v1/auth/me` | GET | Valida sesión; trae `tracking_type`, `default_display_currency`, presupuesto |
| Actualizar perfil | `/api/v1/user/profile` | PUT | Ajustes: comisiones, presupuesto, moneda display |
| Gastos | `/api/v1/expenses` | GET | Filtros de mes/categoría/moneda como query params (confirmar en OpenAPI) |
| Gasto | `/api/v1/expenses/{id}` | POST / GET / PUT / DELETE | |
| Ingresos | `/api/v1/incomes`, `/api/v1/incomes/{id}` | CRUD | |
| Categorías | `/api/v1/categories`, `/api/v1/categories/{id}` | GET/POST/PUT/DELETE | |
| Orígenes | `/api/v1/sources`, `/api/v1/sources/{id}` | CRUD | |
| Dashboard | `/api/v1/dashboard` | GET | Totales ya calculados por el backend |
| Tasas | `/api/v1/rates` | GET | Tasas vigentes (BCV/paralelo + override) |
| Sincronizar tasas | `/api/v1/rates/sync` | POST | Fuerza sync contra dolarapi; con `throttle:rates.sync` |
| Tasa manual | `/api/v1/rates` | PUT | Override del día |
| Reporte mensual | `/api/v1/reports/monthly-summary` | GET | (v1: solo si el dashboard lo consume) |

> **Tarea de la Fase 2**: volcar `/api/v1.json` de una instancia real y definir los DTOs
> **contra esa spec**, no contra este documento. Los shapes de respuesta vienen de los
> Presenters del backend y pueden tener campos adicionales (`ignoreUnknownKeys = true`
> nos hace tolerantes a los que no usamos).

## Reglas del cliente HTTP

- **Base URL**: se compone en runtime desde `server_url` (DataStore): `OkHttp` con un
  `Interceptor` que reescribe la URL de cada request — la URL es decisión del usuario en
  el primer arranque, no de compilación.
- **Auth**: `AuthInterceptor` añade `Authorization: Bearer {token}`. Ante **401** se
  dispara el flujo de re-login (`04-onboarding-y-modos.md`). Ante 429 se reintenta con
  backoff (máx. 2 reintentos; la UI muestra estado de carga).
- **Timeouts**: connect 15 s, read 30 s. Sin reintentos ciegos en escrituras (POST/PUT)
  para no duplicar un gasto.
- **JSON**: kotlinx-serialization con `ignoreUnknownKeys`, `coerceInputValues`. Fechas
  `"YYYY-MM-DD"`; montos y tasas llegan como string decimal (Laravel serializa DECIMAL
  como string) — parse directo a `BigDecimal`.
- **Errores de validación Laravel (422)**: shape
  `{ "message": "...", "errors": { "campo": ["msg1", …] } }` — mapear a errores de campo
  del formulario.
- **CORS**: no aplica — es un mecanismo de navegador; el backend restringe CORS para la
  web, pero las apps nativas no lo atraviesan. No hay que pedirle nada al operador.
- **Cleartext**: permitido con advertencia en UI (ADR-A11); `network_security_config`
  permite `http` solo hacia URLs definidas en runtime.

## Reglas de negocio que el backend ya garantiza (y la app respeta)

En modo remoto **no recalculamos nada**: el congelado de USD/USDT, la comisión y la
tasa del día los resuelve el servidor con las mismas Actions que la web (ADR-007/ADR-009
backend). El formulario remoto:

- precarga la tasa con `GET /rates` (nunca llama a dolarapi directo);
- muestra el equivalente USD/USDT al vuelo usando `ConversionService` **solo como vista
  previa** (el valor autorativo es el que devuelve el servidor tras el POST);
- envía `amount` (base, sin comisión) + `commission` por separado cuando la moneda es Bs.

## Sincronización de tasas — modo local

La app es su propio "scheduler":

- **Al abrir el formulario** de gasto/ingreso en Bs: si la tasa del día tiene más de
  60 minutos, se refresca en background (espejo de `ensureFreshRate` del backend; el
  formulario nunca bloquea por la red — muestra la última conocida como *estimada*).
- **WorkManager periódico** (cada ~6 h, requiere red no medida): mantiene el histórico
  diario BCV + paralelo.
- **Sincronización manual** en la pantalla de Tasas: "Actualizar ahora".
- Payload de `ve.dolarapi.com/v1/dolares`: lista de objetos con
  `moneda`/`fuente`/`promedio` (`fuente = "oficial"` ⇒ BCV, `"paralelo"` ⇒ paralelo).
  Se guardan ambas fuentes; la del formulario se elige (BCV por defecto).
- Fallo de la API: se conserva y usa la última tasa conocida, marcada como estimada.
  Igual que el backend (ADR-002): la app nunca se bloquea por dolarapi.

## Versiones y compatibilidad

- La API es versionada (`/api/v1`): el cliente asume v1 y no hace negociación. Si el
  servidor responde 404 en `/api/v1/auth/login` durante el onboarding, se asume que no es
  un spentz-trackr (o es una versión antigua sin API) y se le dice al usuario.
- Futuro: exponer la versión del backend en `auth/me` o un endpoint `/version` es una
  mejora del backend (issue para el repo web, no de la app).
