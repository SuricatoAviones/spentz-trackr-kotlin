package io.github.suricatoaviones.spentztrackr.core.money

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Tasa de cambio Bs/USD. `BigDecimal` con 4 decimales (DECIMAL(14,4) del backend),
 * **estrictamente positiva**: una tasa ≤ 0 no tiene sentido económico y el
 * backend la rechaza (`exchange_rate` gt:0).
 */
@JvmInline
value class Rate private constructor(
    val value: BigDecimal,
) : Comparable<Rate> {
    /** Texto canónico a 4 decimales ("28.5000"). */
    fun toCanonicalString(): String = value.setScale(SCALE, RoundingMode.HALF_UP).toPlainString()

    override fun compareTo(other: Rate): Int = value.compareTo(other.value)

    companion object {
        const val SCALE: Int = 4

        /** @throws IllegalArgumentException si la tasa es ≤ 0. */
        fun of(value: BigDecimal): Rate {
            require(value.signum() > 0) { "La tasa de cambio debe ser mayor que 0 (fue: $value)" }
            return Rate(value.setScale(SCALE, RoundingMode.HALF_UP))
        }

        fun parseOrNull(raw: String): Rate? = runCatching { of(BigDecimal(raw.trim())) }.getOrNull()

        fun parse(raw: String): Rate {
            val value =
                runCatching { BigDecimal(raw.trim()) }.getOrNull()
                    ?: throw IllegalArgumentException("Tasa inválida: '$raw'")
            return of(value)
        }
    }
}
