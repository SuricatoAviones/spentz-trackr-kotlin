package io.github.suricatoaviones.spentztrackr.data.repository

import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.data.local.SpentzDatabase
import io.github.suricatoaviones.spentztrackr.data.local.entity.UserPrefsEntity
import io.github.suricatoaviones.spentztrackr.data.remote.SpentzApi
import io.github.suricatoaviones.spentztrackr.data.remote.dto.ProfileRequestDto
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.di.LocalSource
import io.github.suricatoaviones.spentztrackr.di.RemoteSource
import io.github.suricatoaviones.spentztrackr.domain.model.AppLanguage
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.model.AppSettings
import io.github.suricatoaviones.spentztrackr.domain.model.AppTheme
import io.github.suricatoaviones.spentztrackr.domain.model.DisplayCurrency
import io.github.suricatoaviones.spentztrackr.domain.model.TrackingType
import io.github.suricatoaviones.spentztrackr.domain.model.UserPreferences
import io.github.suricatoaviones.spentztrackr.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferencias — local/remoto/delegador (ADR-A01).
 *
 * En ambos modos `user_prefs` guarda el presupuesto y las comisiones (el
 * backend v1 no los expone en la API). En remoto, moneda de visualización y
 * `tracking_type` se envían con `PUT /user/profile` y se cachean en Room para
 * que la UI no parpadee sin red.
 */
@Singleton
class LocalUserPreferencesRepository
    @Inject
    constructor(
        private val database: SpentzDatabase,
        private val settings: SettingsStore,
    ) : UserPreferencesRepository {
        override fun observe(): Flow<UserPreferences> =
            combine(database.userPrefsDao().observe(), settings.settings) { prefs, app ->
                prefs.toPreferences(app)
            }

        override suspend fun setDisplayCurrency(currency: DisplayCurrency) = update { it.copy(defaultDisplayCurrency = currency.value) }

        override suspend fun setTrackingType(type: TrackingType) = update { it.copy(trackingType = type.value) }

        override suspend fun setMonthlyBudget(budget: Money?) = update { it.copy(monthlyBudget = budget?.toCanonicalString()) }

        override suspend fun setCommission(
            minCommission: Money,
            ratePercent: BigDecimal,
        ) = update {
            it.copy(
                minCommission = minCommission.toCanonicalString(),
                commissionRate = ratePercent.toPlainString(),
            )
        }

        override suspend fun setTheme(theme: AppTheme) = settings.setTheme(theme)

        override suspend fun setLanguage(language: AppLanguage) = settings.setLanguage(language)

        private suspend fun update(transform: (UserPrefsEntity) -> UserPrefsEntity) {
            val current = database.userPrefsDao().find() ?: UserPrefsEntity()
            database.userPrefsDao().upsert(transform(current))
        }
    }

@Singleton
class RemoteUserPreferencesRepository
    @Inject
    constructor(
        private val api: SpentzApi,
        private val database: SpentzDatabase,
        private val settings: SettingsStore,
    ) : UserPreferencesRepository {
        private val refreshMutex = Mutex()
        private var refreshedFromServer = false

        override fun observe(): Flow<UserPreferences> =
            flow {
                if (!refreshedFromServer) {
                    refreshMutex.withLock {
                        if (!refreshedFromServer) {
                            refreshFromServer()
                            refreshedFromServer = true
                        }
                    }
                }
                emitAll(
                    combine(database.userPrefsDao().observe(), settings.settings) { prefs, app ->
                        prefs.toPreferences(app)
                    },
                )
            }

        override suspend fun setDisplayCurrency(currency: DisplayCurrency) {
            update { it.copy(defaultDisplayCurrency = currency.value) }
            api.updateProfile(ProfileRequestDto(defaultDisplayCurrency = currency.value))
        }

        override suspend fun setTrackingType(type: TrackingType) {
            update { it.copy(trackingType = type.value) }
            api.updateProfile(ProfileRequestDto(trackingType = type.value))
        }

        override suspend fun setMonthlyBudget(budget: Money?) = update { it.copy(monthlyBudget = budget?.toCanonicalString()) }

        override suspend fun setCommission(
            minCommission: Money,
            ratePercent: BigDecimal,
        ) = update {
            it.copy(
                minCommission = minCommission.toCanonicalString(),
                commissionRate = ratePercent.toPlainString(),
            )
        }

        override suspend fun setTheme(theme: AppTheme) = settings.setTheme(theme)

        override suspend fun setLanguage(language: AppLanguage) = settings.setLanguage(language)

        /** `auth/me` trae `tracking_type`; la moneda display no viene en v1. */
        private suspend fun refreshFromServer() {
            val user = runCatching { api.me().data }.getOrNull() ?: return
            val trackingType = user.trackingType ?: return
            update { it.copy(trackingType = trackingType) }
        }

        private suspend fun update(transform: (UserPrefsEntity) -> UserPrefsEntity) {
            val current = database.userPrefsDao().find() ?: UserPrefsEntity()
            database.userPrefsDao().upsert(transform(current))
        }
    }

@Singleton
class DelegatingUserPreferencesRepository
    @Inject
    constructor(
        @LocalSource private val local: UserPreferencesRepository,
        @RemoteSource private val remote: UserPreferencesRepository,
        private val settings: SettingsStore,
    ) : UserPreferencesRepository {
        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        override fun observe(): Flow<UserPreferences> =
            settings.settings
                .map { it.mode }
                .distinctUntilChanged()
                .flatMapLatest { mode ->
                    if (mode == AppMode.REMOTE) remote.observe() else local.observe()
                }

        override suspend fun setDisplayCurrency(currency: DisplayCurrency) = active().setDisplayCurrency(currency)

        override suspend fun setTrackingType(type: TrackingType) = active().setTrackingType(type)

        override suspend fun setMonthlyBudget(budget: Money?) = active().setMonthlyBudget(budget)

        override suspend fun setCommission(
            minCommission: Money,
            ratePercent: BigDecimal,
        ) = active().setCommission(minCommission, ratePercent)

        override suspend fun setTheme(theme: AppTheme) = active().setTheme(theme)

        override suspend fun setLanguage(language: AppLanguage) = active().setLanguage(language)

        private suspend fun active(): UserPreferencesRepository = if (settings.snapshot().mode == AppMode.REMOTE) remote else local
    }

private fun UserPrefsEntity?.toPreferences(app: AppSettings): UserPreferences =
    UserPreferences(
        displayCurrency =
            DisplayCurrency.entries.firstOrNull { it.value == this?.defaultDisplayCurrency }
                ?: DisplayCurrency.USD,
        trackingType = TrackingType.fromValue(this?.trackingType),
        monthlyBudget = this?.monthlyBudget?.let(Money::parseOrNull),
        minCommission = this?.minCommission?.let(Money::parseOrNull) ?: Money.parse(DEFAULT_MIN_COMMISSION),
        commissionRate = this?.commissionRate?.toBigDecimalOrNull() ?: DEFAULT_COMMISSION_RATE,
        theme = app.theme,
        language = app.language,
    )

private const val DEFAULT_MIN_COMMISSION = "14.00"
private val DEFAULT_COMMISSION_RATE: BigDecimal = BigDecimal("0.30")
