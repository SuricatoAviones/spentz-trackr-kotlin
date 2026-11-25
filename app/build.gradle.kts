plugins {
    alias(libs.plugins.android.application)
    // AGP 9 trae Kotlin built-in: no se aplica org.jetbrains.kotlin.android.
    // Compose y serialization (Kotlin 2.4.21) fijan la versión del compilador.
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "io.github.suricatoaviones.spentztrackr"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.suricatoaviones.spentztrackr"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            // El minify y las reglas de R8 se deciden en la Fase 7 (release).
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose: el BOM alinea todas las versiones
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)

    // AndroidX base
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room (modo local — el esquema llega en la Fase 2)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore (modo, URL del servidor, token — Fase 1)
    implementation(libs.androidx.datastore.preferences)

    // WorkManager (sincronización de tasas — Fase 5)
    implementation(libs.androidx.work.runtime.ktx)

    // Red (modo remoto — Fase 1)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Token Sanctum en almacenamiento cifrado (ADR-A10)
    implementation(libs.androidx.security.crypto)

    // Fechas (kotlinx-datetime — Fase 2)
    implementation(libs.kotlinx.datetime)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockwebserver)

    // Tests de instrumentación (Room: connectedDebugAndroidTest)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.core)
}
