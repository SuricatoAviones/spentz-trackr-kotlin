package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.domain.model.AppLanguage
import io.github.suricatoaviones.spentztrackr.domain.model.AppTheme
import io.github.suricatoaviones.spentztrackr.domain.model.DisplayCurrency
import io.github.suricatoaviones.spentztrackr.domain.model.TrackingType
import io.github.suricatoaviones.spentztrackr.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

/**
 * Preferencias completas del usuario (Fase 6). La UI observa un único flujo
 * y las escrituras actualizan la fuente activa (Room/DataStore o API).
 */
interface UserPreferencesRepository {
    fun observe(): Flow<UserPreferences>

    suspend fun setDisplayCurrency(currency: DisplayCurrency)

    suspend fun setTrackingType(type: TrackingType)

    suspend fun setMonthlyBudget(budget: Money?)

    suspend fun setCommission(
        minCommission: Money,
        ratePercent: BigDecimal,
    )

    suspend fun setTheme(theme: AppTheme)

    suspend fun setLanguage(language: AppLanguage)
}
