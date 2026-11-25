package io.github.suricatoaviones.spentztrackr.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.suricatoaviones.spentztrackr.data.repository.DelegatingCategoryRepository
import io.github.suricatoaviones.spentztrackr.data.repository.DelegatingCommissionSettingsRepository
import io.github.suricatoaviones.spentztrackr.data.repository.DelegatingExpenseRepository
import io.github.suricatoaviones.spentztrackr.data.repository.DelegatingIncomeRepository
import io.github.suricatoaviones.spentztrackr.data.repository.DelegatingPaymentSourceRepository
import io.github.suricatoaviones.spentztrackr.data.repository.DelegatingRateRepository
import io.github.suricatoaviones.spentztrackr.data.repository.DelegatingTrackingSettingsRepository
import io.github.suricatoaviones.spentztrackr.data.repository.LocalCategoryRepository
import io.github.suricatoaviones.spentztrackr.data.repository.LocalCommissionSettingsRepository
import io.github.suricatoaviones.spentztrackr.data.repository.LocalExpenseRepository
import io.github.suricatoaviones.spentztrackr.data.repository.LocalIncomeRepository
import io.github.suricatoaviones.spentztrackr.data.repository.LocalPaymentSourceRepository
import io.github.suricatoaviones.spentztrackr.data.repository.LocalRateRepository
import io.github.suricatoaviones.spentztrackr.data.repository.LocalTrackingSettingsRepository
import io.github.suricatoaviones.spentztrackr.data.repository.RemoteCategoryRepository
import io.github.suricatoaviones.spentztrackr.data.repository.RemoteCommissionSettingsRepository
import io.github.suricatoaviones.spentztrackr.data.repository.RemoteExpenseRepository
import io.github.suricatoaviones.spentztrackr.data.repository.RemoteIncomeRepository
import io.github.suricatoaviones.spentztrackr.data.repository.RemotePaymentSourceRepository
import io.github.suricatoaviones.spentztrackr.data.repository.RemoteRateRepository
import io.github.suricatoaviones.spentztrackr.data.repository.RemoteTrackingSettingsRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.CategoryRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.CommissionSettingsRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.ExpenseRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.IncomeRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.PaymentSourceRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.RateRepository
import io.github.suricatoaviones.spentztrackr.domain.repository.TrackingSettingsRepository
import javax.inject.Singleton

/**
 * Los repositorios que consume la app son los **delegadores por modo**
 * (ADR-A01): la UI nunca sabe si hay Room o Retrofit detrás. Los slots
 * local/remoto de cada delegador se califican con [LocalSource]/[RemoteSource]
 * — así los tests pueden inyectar fakes por la interfaz.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: DelegatingExpenseRepository): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(impl: DelegatingIncomeRepository): IncomeRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: DelegatingCategoryRepository): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindPaymentSourceRepository(impl: DelegatingPaymentSourceRepository): PaymentSourceRepository

    @Binds
    @Singleton
    abstract fun bindRateRepository(impl: DelegatingRateRepository): RateRepository

    @Binds
    @Singleton
    abstract fun bindCommissionSettingsRepository(impl: DelegatingCommissionSettingsRepository): CommissionSettingsRepository

    @Binds
    @Singleton
    abstract fun bindTrackingSettingsRepository(impl: DelegatingTrackingSettingsRepository): TrackingSettingsRepository

    // Los @Provides del companion de una @Module abstracta forman parte del
    // módulo: no se anota el companion (Hilt lo rechaza como módulo aparte).
    companion object {
        @Provides
        @LocalSource
        fun provideLocalExpenseRepository(impl: LocalExpenseRepository): ExpenseRepository = impl

        @Provides
        @RemoteSource
        fun provideRemoteExpenseRepository(impl: RemoteExpenseRepository): ExpenseRepository = impl

        @Provides
        @LocalSource
        fun provideLocalIncomeRepository(impl: LocalIncomeRepository): IncomeRepository = impl

        @Provides
        @RemoteSource
        fun provideRemoteIncomeRepository(impl: RemoteIncomeRepository): IncomeRepository = impl

        @Provides
        @LocalSource
        fun provideLocalCategoryRepository(impl: LocalCategoryRepository): CategoryRepository = impl

        @Provides
        @RemoteSource
        fun provideRemoteCategoryRepository(impl: RemoteCategoryRepository): CategoryRepository = impl

        @Provides
        @LocalSource
        fun provideLocalPaymentSourceRepository(impl: LocalPaymentSourceRepository): PaymentSourceRepository = impl

        @Provides
        @RemoteSource
        fun provideRemotePaymentSourceRepository(impl: RemotePaymentSourceRepository): PaymentSourceRepository = impl

        @Provides
        @LocalSource
        fun provideLocalRateRepository(impl: LocalRateRepository): RateRepository = impl

        @Provides
        @RemoteSource
        fun provideRemoteRateRepository(impl: RemoteRateRepository): RateRepository = impl

        @Provides
        @LocalSource
        fun provideLocalCommissionSettings(impl: LocalCommissionSettingsRepository): CommissionSettingsRepository = impl

        @Provides
        @RemoteSource
        fun provideRemoteCommissionSettings(impl: RemoteCommissionSettingsRepository): CommissionSettingsRepository = impl

        @Provides
        @LocalSource
        fun provideLocalTrackingSettings(impl: LocalTrackingSettingsRepository): TrackingSettingsRepository = impl

        @Provides
        @RemoteSource
        fun provideRemoteTrackingSettings(impl: RemoteTrackingSettingsRepository): TrackingSettingsRepository = impl
    }
}
