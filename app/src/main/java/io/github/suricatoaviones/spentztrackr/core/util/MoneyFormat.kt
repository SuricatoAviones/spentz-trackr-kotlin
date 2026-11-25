package io.github.suricatoaviones.spentztrackr.core.util

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Formateo de montos por locale (docs/06): 2 decimales siempre, agrupación
 * del locale, prefijo por moneda ($ / Bs / USDT).
 */
object MoneyFormat {
    fun format(
        money: Money,
        locale: Locale = Locale.getDefault(),
    ): String {
        val format = NumberFormat.getNumberInstance(locale)
        format.minimumFractionDigits = 2
        format.maximumFractionDigits = 2
        format.isGroupingUsed = true
        return format.format(money.value.setScale(2, RoundingMode.HALF_UP))
    }

    fun withCurrency(
        money: Money,
        currency: CurrencyCode,
        locale: Locale = Locale.getDefault(),
    ): String =
        when (currency) {
            CurrencyCode.USD -> "$" + format(money, locale)
            CurrencyCode.USDT -> format(money, locale) + " USDT"
            CurrencyCode.VES -> format(money, locale) + " Bs"
        }
}
