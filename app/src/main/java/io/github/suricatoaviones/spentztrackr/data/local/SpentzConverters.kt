package io.github.suricatoaviones.spentztrackr.data.local

import androidx.room.TypeConverter
import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.PaymentMethod
import io.github.suricatoaviones.spentztrackr.core.money.RateProvider
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import io.github.suricatoaviones.spentztrackr.domain.model.RateSource

/**
 * Converters Room ↔ SQLite (ADR-A03): enums como su valor string, fechas
 * como ISO `YYYY-MM-DD`. El dinero no aparece aquí: va como texto canónico
 * directo en las columnas (Room + KSP2 no procesa value classes en campos).
 */
class SpentzConverters {
    @TypeConverter
    fun currencyToText(currency: CurrencyCode?): String? = currency?.value

    @TypeConverter
    fun textToCurrency(value: String?): CurrencyCode? = CurrencyCode.fromValue(value)

    @TypeConverter
    fun paymentMethodToText(method: PaymentMethod?): String? = method?.value

    @TypeConverter
    fun textToPaymentMethod(value: String?): PaymentMethod? = PaymentMethod.fromValue(value)

    @TypeConverter
    fun rateProviderToText(provider: RateProvider?): String? = provider?.value

    @TypeConverter
    fun textToRateProvider(value: String?): RateProvider? = RateProvider.fromValue(value)

    @TypeConverter
    fun rateSourceToText(source: RateSource?): String? = source?.value

    @TypeConverter
    fun textToRateSource(value: String?): RateSource? = RateSource.fromValue(value)

    @TypeConverter
    fun categoryTypeToText(type: CategoryType?): String? = type?.value

    @TypeConverter
    fun textToCategoryType(value: String?): CategoryType? = CategoryType.fromValue(value)
}
