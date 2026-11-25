package io.github.suricatoaviones.spentztrackr

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application de Spentz Trackr.
 *
 * `@HiltAndroidApp` genera el componente raíz de Hilt. Todas las dependencias
 * (DataStore, Room, Retrofit) se cablean aquí a partir de la Fase 1.
 */
@HiltAndroidApp
class SpentzApplication : Application()
