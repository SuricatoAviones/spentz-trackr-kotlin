package io.github.suricatoaviones.spentztrackr.domain.service

import io.github.suricatoaviones.spentztrackr.core.money.CurrencyCode
import io.github.suricatoaviones.spentztrackr.core.money.Money
import io.github.suricatoaviones.spentztrackr.core.money.Rate
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseDraft
import io.github.suricatoaviones.spentztrackr.domain.model.ExpenseItemDraft
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Reglas de conversión congelada — los casos límite del
 * `ExpenseConversionService` del backend (docs/03) y el ejemplo canónico del
 * README: `Gasto 280 Bs + comisión 14, tasa 28,00 ⇒ $10.50 / 10.50 USDT`.
 */
class ConversionServiceTest {
    private val conversion = ConversionService()

    @Test
    fun `usd se registra por su valor nominal`() {
        val frozen = conversion.convert(CurrencyCode.USD, Money.parse("25.50"))

        assertEquals("25.50", frozen.usd.toCanonicalString())
        assertEquals("25.50", frozen.usdt.toCanonicalString())
    }

    @Test
    fun `usdt es uno a uno con usd`() {
        val frozen = conversion.convert(CurrencyCode.USDT, Money.parse("10.00"))

        assertEquals("10.00", frozen.usd.toCanonicalString())
        assertEquals("10.00", frozen.usdt.toCanonicalString())
    }

    @Test
    fun `ves con comision congela el ejemplo del readme`() {
        // (280 + 14) / 28 = 10.50 — y queda congelado para siempre.
        val frozen =
            conversion.convert(
                currency = CurrencyCode.VES,
                amount = Money.parse("280"),
                commission = Money.parse("14.00"),
                rate = Rate.parse("28.0000"),
            )

        assertEquals("10.50", frozen.usd.toCanonicalString())
        assertEquals("10.50", frozen.usdt.toCanonicalString())
    }

    @Test
    fun `ves sin comision divide directo`() {
        val frozen =
            conversion.convert(
                currency = CurrencyCode.VES,
                amount = Money.parse("100"),
                rate = Rate.parse("3"),
            )

        assertEquals("33.33", frozen.usd.toCanonicalString())
    }

    @Test
    fun `ves redondea half up a dos decimales`() {
        // 10.01 / 0.4 = 25.025 ⇒ 25.03 (HALF_UP, como round() de PHP).
        val frozen =
            conversion.convert(
                currency = CurrencyCode.VES,
                amount = Money.parse("10.01"),
                rate = Rate.parse("0.4"),
            )

        assertEquals("25.03", frozen.usd.toCanonicalString())
    }

    @Test
    fun `ves sin tasa lanza missing rate`() {
        assertThrows(ConversionService.MissingRateException::class.java) {
            conversion.convert(
                currency = CurrencyCode.VES,
                amount = Money.parse("100"),
                rate = null,
            )
        }
    }

    @Test
    fun `el total del gasto suma la porcion principal y los items`() {
        val draft =
            ExpenseDraft(
                categoryId = 1L,
                paymentSourceId = 1L,
                currency = CurrencyCode.VES,
                paymentMethod = null,
                commission = Money.parse("14.00"),
                amount = Money.parse("280"),
                exchangeRate = Rate.parse("28"),
                description = "Mercado",
                spentAt = LocalDate.parse("2026-11-25"),
                items =
                    listOf(
                        // Ítem en USD: se suma nominal.
                        ExpenseItemDraft(currency = CurrencyCode.USD, amount = Money.parse("5")),
                        // Ítem en Bs: congela con su propia tasa (56 / 28 = 2).
                        ExpenseItemDraft(
                            currency = CurrencyCode.VES,
                            amount = Money.parse("56"),
                            exchangeRate = Rate.parse("28"),
                        ),
                    ),
            )

        val total = conversion.convertExpense(draft)

        // 10.50 (principal) + 5.00 (ítem USD) + 2.00 (ítem Bs) = 17.50
        assertEquals("17.50", total.usd.toCanonicalString())
        assertEquals("17.50", total.usdt.toCanonicalString())
    }

    @Test
    fun `item en bs sin tasa lanza missing rate`() {
        assertThrows(ConversionService.MissingRateException::class.java) {
            conversion.convertItem(
                ExpenseItemDraft(currency = CurrencyCode.VES, amount = Money.parse("56")),
            )
        }
    }
}
