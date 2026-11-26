# 10 — F-Droid y publicación

La app está pensada para publicarse en F-Droid sin blobs ni servicios
propietarios. Este documento evalúa el estado de la v1 y deja la receta técnica.

## ¿Por qué es apta?

| Requisito | Estado |
|---|---|
| Código fuente abierto | MIT ([LICENSE](../LICENSE)) |
| Sin telemetría/analítica/crash reporting | ✅ sin SDKs de terceros |
| Sin Google Play Services | ✅ no se usa |
| Dependencias FOSS | AndroidX, Compose, Room, Retrofit, OkHttp, Hilt, WorkManager, kotlinx |
| Sin publicidad ni compras | ✅ |
| Permisos mínimos | solo `INTERNET` |
| Sin `google-services.json` ni blobs | ✅ |

**Anti-features**: ninguna. La única red externa en modo local es
`ve.dolarapi.com` (tasas) y en remoto la URL del propio servidor.

## Metadata incluida

La metadata para F-Droid está en formato fastlane:

```
fastlane/metadata/android/
├── en-US/
│   ├── title.txt
│   ├── short_description.txt
│   ├── full_description.txt
│   └── changelogs/1.txt
└── es-ES/
    ├── title.txt
    ├── short_description.txt
    ├── full_description.txt
    └── changelogs/1.txt
```

F-Droid usa el `changelog` correspondiente al `versionCode` (`1.txt` para la
v1.0.0). Las capturas de F-Droid se pueden añadir después en
`images/phoneScreenshots/`.

## Receta para fdroiddata

El APK de F-Droid se compila **desde el código fuente y lo firma F-Droid**, no
con el keystore del repositorio. Un borrador de metadata:

```yaml
Categories:
  - Money
License: MIT
AuthorName: Suricato Aviones
SourceCode: https://github.com/SuricatoAviones/spentz-trackr-kotlin
IssueTracker: https://github.com/SuricatoAviones/spentz-trackr-kotlin/issues
AutoName: Spentz Trackr

RepoType: git
Repo: https://github.com/SuricatoAviones/spentz-trackr-kotlin

Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    subdir: app
    gradle:
      - yes
```

Notas:

- `assembleRelease` funciona sin `keystore.properties` (genera unsigned); F-Droid
  firma el resultado.
- El proyecto necesita JDK 17 y Android SDK 37; el buildserver de F-Droid lo
  resuelve con `gradle: - yes` y la configuración del contenedor.
- Reproducible builds: no se garantizan todavía. Si se quiere, hay que fijar
  versiones de dependencias y evitar rutas absolutas; ya se usa un version
  catalog, así que es un buen punto de partida.

## Publicación en GitHub Releases

El repositorio publica el APK firmado con su propio keystore en
`Releases`/`v1.0.0`. La firma de GitHub Releases **no** es la misma que la de
F-Droid (comportamiento normal): los usuarios deben elegir una fuente e instalar
actualizaciones desde ella.

Pasos:

1. `.\gradlew.bat assembleRelease` con `keystore.properties` local.
2. Verifica la firma: `apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk`.
3. Crea el tag `v1.0.0` sobre `main` y publica el APK + `CHANGELOG.md` en
   GitHub Releases.
4. Guarda el keystore y las contraseñas fuera del repositorio.

## Checklist antes de enviar a F-Droid

- [ ] `versionCode` incrementado y `CHANGELOG` actualizado.
- [ ] `assembleRelease` compila con R8 y sin warnings bloqueantes.
- [ ] `ktlintCheck lint test` en verde.
- [ ] Permisos revisados (solo `INTERNET`).
- [ ] Sin dependencias propietarias nuevas.
- [ ] Metadata fastlane actualizada.
- [ ] Capturas y textos ES/EN revisados.
