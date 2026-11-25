# 09 — Entorno y Herramientas

Guía para abrir, correr y supervisar el proyecto en **Android Studio** e **IntelliJ
IDEA**, con las convenciones de trabajo.

## Estado actual: ¿qué puedo abrir hoy?

Este repo está **en planificación** (Fases 0–7 en `07-plan-de-fases.md`). Hoy contiene
documentación y no es todavía un proyecto Gradle. Por eso:

- **Ahora mismo**: puedes abrir la carpeta en Android Studio o IntelliJ como proyecto de
  documentos (markdown) para leer los docs. No habrá nada que *ejecutar* todavía.
- **Después de la Fase 0** (creación del esqueleto Android): la carpeta se convierte en un
  proyecto Gradle completo y todo lo de abajo aplica: sync, ▶ Run, Logcat, emulador.

## Android Studio — el entorno principal

**Usa Android Studio para TODO el ciclo** (es IntelliJ IDEA + plugin Android + SDK
manager + emuladores): abrir, editar, compilar, correr, depurar. No necesitas IntelliJ
aparte.

### Requisitos

- Android Studio actual (cualquier versión de los últimos 12 meses; trae su propio JBR —
  no hace falta instalar Java aparte).
- ~10 GB libres (SDK + emuladores + Gradle caches).
- En Windows: si el SDK lo reclama, acepta la instalación de **Hyper-V / HAXM** para
  acelerar el emulador.

### Abrir el proyecto (tras la Fase 0)

1. `File → Open…` → selecciona la carpeta `spentz-trackr-kotlin` → *Open* (confía en el
   proyecto si pregunta).
2. Espera el **Gradle sync** (barra inferior / status bar). La primera vez descarga
   dependencias: varios minutos es normal. El proyecto está listo cuando aparece
   *“Gradle sync finished”* y el árbol `app/` se despliega con sus carpetas Kotlin.
3. Si el sync falla: revisa la pestaña *Build*; lo típico es SDK platform faltante →
   `File → Settings → Languages & Frameworks → Android SDK` → instala la API level que
   pida `targetSdk` (o pulsa el enlace del error, AS lo ofrece).

### Ejecutar la app

1. **Emulador**: `View → Tool Windows → Device Manager` (o el icono de teléfono) →
   `Create Device` → un Pixel reciente → imagen recomendada (con Google Play) → Finish.
   Luego selecciónalo en el dropdown de dispositivos de la barra.
2. **Teléfono físico**: ajustes → información del teléfono → pulsa 7× *Número de
   compilación* para activar opciones de desarrollador → **Depuración USB** ON → conecta
   y acepta el diálogo del teléfono. Aparecerá en el mismo dropdown.
3. Pulsa **▶ Run** (Shift+F10). Compila, instala y lanza la app.

### Supervisar mientras se desarrolla

- **Logcat** (`View → Tool Windows → Logcat`): todo el `Log.d/…` de la app en vivo,
  filtrable por package `io.github.suricatoaviones.spentztrackr`. Ahí verán errores de
  red, Room, etc.
- **Git integrado**: pestaña *Commit* para ver diffs, *Git → Log* para el historial.
  Cuando el agente de desarrollo hace commits, `Git → Pull` (o auto-sync con
  `settings → Git → “Pull automatically”`) los trae. Con el proyecto abierto, AS recarga
  los archivos cambiados al vuelo.
- **Ver el progreso**: `docs/08-backlog-tareas.md` es el tablero (checkboxes por fase).
- **Probar en emulador** tras cada fase es la mejor supervisión: correr el flujo de esa
  fase contra sus criterios de salida en `07-plan-de-fases.md`.

### Tips de emulador para esta app

- Cambiar la fecha del emulador (`adb shell date` o ajustes del AVD) sirve para probar el
  histórico de tasas y el “estimada”.
- Sin red: activa **Airplane mode** en el emulador para probar los estados offline
  (criterio de salida de la Fase 5).
- Modo remoto en desarrollo: el backend puede correr en el mismo PC con Docker
  (`localhost:8080`); desde el emulador se alcanza como `http://10.0.2.2:8080` (así se
  prueba cleartext HTTP, ADR-A11).

## IntelliJ IDEA — cuándo usarlo

- **Ultimate** puede abrir y compilar proyectos Android (plugin Android incluido); sirve
  para revisar código o Markdown cómodamente si es tu editor de cabecera.
- **Community** no soporta el desarrollo Android completo.
- Recomendación práctica: **deja IntelliJ para otros proyectos**; este repo vive mejor en
  Android Studio. Si aun así lo abres en IntelliJ y el Gradle sync de Android falla, no es
  un bug del proyecto: es la limitación de edición/Community.

## Comandos Gradle (desde la terminal del repo)

```
gradlew assembleDebug      # compilar APK debug
gradlew lint ktlintCheck    # calidad estática
gradlew test               # tests JVM (dominio, viewmodels)
gradlew connectedAndroidTest # tests de instrumentación (emulador/dispositivo conectido)
```

(En PowerShell de Windows: `.\gradlew …`.) Los mismos comandos corren en CI.

## CI — GitHub Actions

- En cada push/PR: `lint` + `ktlintCheck` + `test` (rápidos, JVM).
- En `main`: además `assembleDebug` — el APK queda como artefacto descargable.
- No usamos emulador en CI en v1 (los androidTest corren en local); se evalúa post-v1.

## Convenciones del repo

- **Commits**: Conventional Commits (`feat:`, `fix:`, `docs:`, `test:`, `chore:`).
- **Formato**: ktlint (se corre en CI; `gradlew ktlintFormat` arregla en local).
- **Paquetes**: estructura de `02-arquitectura.md`; el dominio no importa nada de
  `data/` ni de Android.
- **Idioma**: código y commits pueden ir en español o inglés (consistencia por
  contribución); strings de UI **siempre** vía `strings.xml` (ES default + `values-en/`).
- **Reglas de negocio**: si algo se ve raro respecto a conversiones/comisiones, la
  especificación es `docs/03-modelo-de-datos.md` del backend y sus ADRs, no la intuición.
