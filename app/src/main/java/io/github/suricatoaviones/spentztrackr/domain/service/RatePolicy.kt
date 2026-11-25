package io.github.suricatoaviones.spentztrackr.domain.service

import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.ExchangeRate
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource
import kotlinx.datetime.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tasa vigente para un día — espejo de `ExchangeRateService::rateForUser`
 * del backend (docs/03):
 *
 * 1. su última **manual** del día (override del usuario, gana siempre);
 * 2. si no, la **api** del día (proveedor preferido, luego el otro);
 * 3. si no hay tasa del día, la **última conocida** marcada como *estimada*
 *    (la UI nunca la presenta como tasa real);
 * 4. sin nada: null — el formulario ofrece tasa manual.
 */
@Singleton
class RatePolicy
    @Inject
    constructor() {
        data class RateSelection(
            val rate: Rate,
            val provider: RateProvider,
            val estimated: Boolean,
        )

        fun rateFor(
            rates: List<ExchangeRate>,
            date: LocalDate,
            preferredApiProvider: RateProvider = RateProvider.BCV,
        ): RateSelection? {
            val ofDay = rates.filter { it.rateDate == date }

            ofDay
                .filter { it.source == RateSource.MANUAL }
                .maxByOrNull { it.id }
                ?.let { return RateSelection(it.rate, it.provider, estimated = false) }

            val apiOfDay = ofDay.filter { it.source == RateSource.API }
            apiOfDay
                .filter { it.provider == preferredApiProvider }
                .maxByOrNull { it.id }
                ?.let { return RateSelection(it.rate, it.provider, estimated = false) }
            apiOfDay
                .maxByOrNull { it.id }
                ?.let { return RateSelection(it.rate, it.provider, estimated = false) }

            val latest =
                rates
                    .sortedWith(
                        compareByDescending<ExchangeRate> { it.rateDate }.thenByDescending { it.id },
                    ).firstOrNull() ?: return null

            return RateSelection(latest.rate, latest.provider, estimated = true)
        }
    }
