package io.github.suricatoaviones.spentztrackr.data.repository

import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.data.local.SpentzDatabase
import io.github.suricatoaviones.spentztrackr.data.settings.SettingsStore
import io.github.suricatoaviones.spentztrackr.di.LocalSource
import io.github.suricatoaviones.spentztrackr.di.RemoteSource
import io.github.suricatoaviones.spentztrackr.domain.model.AppMode
import io.github.suricatoaviones.spentztrackr.domain.repository.CommissionSettings
import io.github.suricatoaviones.spentztrackr.domain.repository.CommissionSettingsRepository
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ajustes de comisión — local/remoto/delegador.
 *
 * Local: `user_prefs` (defaults 14.00 Bs / 0.30 %, como el backend).
 * Remoto: **deuda técnica** — la API no expone las comisiones del usuario en
 * `auth/me` (docs/08), así que se usan los defaults; el valor es editable en
 * el formulario de cualquier forma.
 */
@Singleton
class LocalCommissionSettingsRepository
    @Inject
    constructor(
        private val database: SpentzDatabase,
    ) : CommissionSettingsRepository {
        override suspend fun settings(): CommissionSettings {
            val prefs = database.userPrefsDao().find()
            return CommissionSettings(
                minCommission = Money.parse(prefs?.let { it.minCommission } ?: DEFAULT_MIN),
                ratePercent = BigDecimal(prefs?.let { it.commissionRate } ?: DEFAULT_RATE),
            )
        }

        private companion object {
            const val DEFAULT_MIN = "14.00"
            const val DEFAULT_RATE = "0.30"
        }
    }

@Singleton
class RemoteCommissionSettingsRepository
    @Inject
    constructor() : CommissionSettingsRepository {
        override suspend fun settings(): CommissionSettings =
            CommissionSettings(
                minCommission = Money.parse(DEFAULT_MIN),
                ratePercent = BigDecimal(DEFAULT_RATE),
            )

        private companion object {
            const val DEFAULT_MIN = "14.00"
            const val DEFAULT_RATE = "0.30"
        }
    }

@Singleton
class DelegatingCommissionSettingsRepository
    @Inject
    constructor(
        @LocalSource private val local: CommissionSettingsRepository,
        @RemoteSource private val remote: CommissionSettingsRepository,
        private val settings: SettingsStore,
    ) : CommissionSettingsRepository {
        override suspend fun settings(): CommissionSettings =
            if (settings.snapshot().mode == AppMode.REMOTE) remote.settings() else local.settings()
    }
