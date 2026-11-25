package io.github.suricatoaviones.spentztrackr.core.money

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Monto de dinero. `BigDecimal` bajo value class, **nunca Double** (ADR-A03):
 * 2 decimales, redondeo HALF_UP, persistencia como texto canónico.
 */
@JvmInline
value class Money private constructor(
    val value: BigDecimal,
) : Comparable<Money> {
    /** Texto decimal canónico a 2 decimales ("10.50"). Es lo que se persiste. */
    fun toCanonicalString(): String = value.setScale(SCALE, RoundingMode.HALF_UP).toPlainString()

    operator fun plus(other: Money): Money = of(value.add(other.value))

    operator fun minus(other: Money): Money = of(value.subtract(other.value))

    operator fun times(factor: BigDecimal): Money = of(value.multiply(factor))

    override fun compareTo(other: Money): Int = value.compareTo(other.value)

    companion object {
        const val SCALE: Int = 2

        val ZERO: Money = of(BigDecimal.ZERO)

        /** Normaliza a 2 decimales con HALF_UP (igual que `round(x, 2)` de PHP). */
        fun of(value: BigDecimal): Money = Money(value.setScale(SCALE, RoundingMode.HALF_UP))

        fun of(value: Long): Money = of(BigDecimal.valueOf(value))

        fun of(value: Int): Money = of(value.toLong())

        fun parseOrNull(raw: String): Money? = runCatching { of(BigDecimal(raw.trim())) }.getOrNull()

        fun parse(raw: String): Money = parseOrNull(raw) ?: error("Monto inválido: '$raw'")
    }
}
