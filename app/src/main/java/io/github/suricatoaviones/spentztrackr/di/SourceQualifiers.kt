package io.github.suricatoaviones.spentztrackr.di

import javax.inject.Qualifier

/**
 * Calificadores de fuente de datos (ADR-A01): los delegadores piden las
 * **interfaces** de repositorio con estos qualifiers, y el módulo decide qué
 * implementación (Room local / API remota) va en cada slot. Los tests pueden
 * inyectar fakes sin tocar Room ni Retrofit.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteSource
