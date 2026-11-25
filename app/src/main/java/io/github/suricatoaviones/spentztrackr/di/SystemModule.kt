package io.github.suricatoaviones.spentztrackr.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

/** Reloj inyectable: todo lo que necesita "hoy" pasa por aquí ( testeable). */
@Module
@InstallIn(SingletonComponent::class)
object SystemModule {
    // kotlinx-datetime 0.7+ movió Clock a la stdlib (kotlin.time.Clock);
    // la extension todayIn() de kotlinx.datetime lo acepta tal cual.
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.System
}
