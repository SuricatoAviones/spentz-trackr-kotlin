package io.github.suricatoaviones.spentztrackr.core.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Fechas localizadas sobre java.time (nativo desde minSdk 26). kotlinx-datetime
 * se queda para el dominio; aquí solo se formatea para la UI.
 */
object DateText {
    /** "25 de noviembre de 2026" según locale. */
    fun formatLong(
        date: LocalDate,
        locale: Locale = Locale.getDefault(),
    ): String = toJava(date).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale))

    /** "noviembre 2026" según locale. */
    fun formatMonth(
        month: YearMonth,
        locale: Locale = Locale.getDefault(),
    ): String =
        java.time.YearMonth
            .of(month.year, month.month.ordinal + 1)
            .format(DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale))

    private fun toJava(date: LocalDate): java.time.LocalDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
}
