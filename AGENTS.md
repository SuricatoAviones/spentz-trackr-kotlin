# AGENTS.md

Guía operativa para agentes (humanos o IA) que trabajan en este repositorio.
La referencia de producto, arquitectura y fases vive en [`docs/`](docs/README.md);
este archivo define **cómo** se trabaja aquí.

## Reglas de oro

1. **La documentación es la especificación.** Si el código y `docs/` divergen, se
   corrige el que esté mal y se explica en el commit — nunca divergen en silencio.
2. **Las reglas de negocio se heredan del backend**
   ([SuricatoAviones/spentz-trackr](https://github.com/SuricatoAviones/spentz-trackr),
   ADR-001…013): conversión congelada al escribir, USDT 1:1 con USD, comisión
   `max(mínimo, monto × %)`, esquema espejo. No se re-discuten aquí; los ADRs
   propios de Android están en `docs/02-arquitectura.md`.
3. **Sin telemetría, sin analítica, sin blobs propietarios** (objetivo: F-Droid).
   Licencia MIT.
4. **Bilingüe ES/EN**: todo texto de UI va por `res/values/strings.xml` (español,
   default) y `res/values-en/strings.xml`. Paridad de claves obligatoria.
5. **El proyecto se desarrolla en español**; la app es bilingüe.

## Flujo de trabajo: ramas, commits y PRs

- **Una rama por fase**: `fase/<n>-<slug>`, creada desde `main` al iniciar la
  fase (ej. `fase/1-onboarding-modos`, `fase/2-dominio-datos`). `main` solo
  recibe merges de fase (y documentación/planificación de forma puntual).
- **Commits detallados por tarea**: cada tarea de `docs/08-backlog-tareas.md`
  produce al menos un commit [Conventional Commits](https://www.conventionalcommits.org/es/):
  - Prefijos: `feat:` funcionalidad · `fix:` corrección · `test:` solo tests ·
    `refactor:` · `docs:` · `chore:` build/tooling · `ci:`.
  - El asunto explica el **qué**; el cuerpo (cuando aporte) el **porqué** y las
    decisiones. Referencia la tarea al final del asunto: `(docs/08 F3-5)`.
- **Al terminar la fase → Pull Request a `main`**:
  1. La rama `fase/<n>-<slug>` debe estar en verde:
     `gradlew ktlintCheck lint test` y `gradlew assembleDebug`.
  2. El PR se titula `Fase <n>: <nombre>` y su descripción incluye: qué se
     implementó, qué criterios de salida de `docs/07-plan-de-fases.md` quedaron
     verificados (y cuáles quedan para la siguiente) y cómo probarla a mano.
  3. Los checkboxes de `docs/08-backlog-tareas.md` se marcan **dentro del PR**
     (no después del merge).
  4. Merge → la siguiente fase abre su rama desde el `main` actualizado.
  - Si no hay credenciales de GitHub disponibles para abrir el PR, se avisa y
    se deja la rama lista con un resumen para que el humano lo abra.
- **Calidad mínima en cada PR**: el CI
  (`.github/workflows/android-ci.yml`) corre ktlint + lint + tests en cada
  push/PR y `assembleDebug` + artefacto APK en `main`; nada se hace merge con CI
  en rojo.

## Skills locales (`.agents/skills/`)

`.agents/`, `.ai/` y `.cursor/` están en `.gitignore`: son tooling local, no
parte del proyecto. `.agents/skills/` contiene skills por superficie — **cargar
la correspondiente antes de tocar cada área**:

| Superficie | Skill a usar |
|---|---|
| Gradle, version catalog, plugins | `android-gradle-build-logic` |
| UI Jetpack Compose | `android-compose-foundations` |
| Kotlin base (idioms, nullability, sellados) | `android-kotlin-core` |
| Coroutines / Flow | `android-coroutines-flow` |
| Inyección con Hilt | `android-di-hilt` |
| Red: Retrofit + OkHttp | `android-networking-retrofit-okhttp` |
| Tests unitarios | `android-testing-unit` |
| Capas / repositorios / use cases | `android-architecture-clean` |

## Comandos frecuentes

```powershell
.\gradlew assembleDebug       # compilar APK debug (requiere SDK; ver docs/09)
.\gradlew ktlintFormat       # autocorregir formato
.\gradlew ktlintCheck        # verificar estilo
.\gradlew lint               # Android Lint
.\gradlew test               # tests unitarios (JVM)
```

JDK del entorno local: el JBR de Android Studio
(`C:\Program Files\Android\Android Studio\jbr`, Java 25) vía `JAVA_HOME`.
El bytecode target es Java 17 (compatible con CI y cualquier JDK 17+).

## Estado (mantener esta sección al día en cada fase)

- **Fase 0 — Fundaciones**: ✅ completada (2026-11-25) y en `main`.
- **Fase 1 — Onboarding y modos**: ✅ completada (2026-11-25) y en `main` (PR #1).
- **Fase 2 — Dominio del dinero y datos**: ✅ completada (2026-11-26) en rama
  `fase/2-dominio-datos` → PR a `main`. Núcleo `Money`/`Rate` (BigDecimal, ADR-A03),
  `ConversionService`/`CommissionCalculator`/`RatePolicy` con tests (paridad con el
  backend verificada), Room v2 (4 tablas nuevas con FKs/índices), DTOs contra shapes
  reales + API completa, repositorios con delegación por modo y qualifiers. Verificado:
  `assembleDebug`, `test` (10 suites), `ktlintCheck`, `lint`, androidTest compilado.
- **Siguiente**: Fase 3 — Gastos (la pantalla estrella) en rama `fase/3-gastos`.
