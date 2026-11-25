// Plugins declarados con `apply false`: cada módulo aplica lo que usa.
// Las versiones viven en gradle/libs.versions.toml (version catalog).
//
// Nota (AGP 9): Kotlin viene "built-in" en AGP, así que NO se declara
// org.jetbrains.kotlin.android. La versión del compilador la decide el KGP
// del classpath: los plugins de Compose y serialization (2.4.21) la suben.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ktlint) apply false
}
