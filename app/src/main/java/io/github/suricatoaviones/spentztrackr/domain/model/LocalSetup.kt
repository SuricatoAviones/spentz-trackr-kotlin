package io.github.suricatoaviones.spentztrackr.domain.model

/**
 * Preferencias iniciales del modo local que se piden en el onboarding
 * (`docs/04-onboarding-y-modos.md`, rama local). Todo tiene default y se
 * puede cambiar luego en Ajustes.
 */
data class LocalSetup(
    val displayCurrency: DisplayCurrency = DisplayCurrency.USD,
    val trackingType: TrackingType = TrackingType.BOTH,
    /**
     * Presupuesto mensual global en USD, como decimal canónico ("500.00"),
     * o null si no se define. Dinero siempre como texto (ADR-A03).
     */
    val monthlyBudget: String? = null,
)
