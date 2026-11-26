package io.github.suricatoaviones.spentztrackr.domain.model

import io.github.suricatoaviones.spentztrackr.core.money.Money
import java.math.BigDecimal

/**
 * Preferencias de negocio y apariencia del usuario (Fase 6).
 *
 * Local: `user_prefs` + DataStore. Remoto: `PUT /user/profile` para moneda de
 * visualización y `tracking_type`; DataStore para tema e idioma. El backend v1
 * no expone comisiones ni presupuesto en la API, así que esos dos se guardan
 * en `user_prefs` del dispositivo en ambos modos (docs/08, deuda técnica).
 */
data class UserPreferences(
    val displayCurrency: DisplayCurrency = DisplayCurrency.USD,
    val trackingType: TrackingType = TrackingType.BOTH,
    /** Presupuesto mensual global en USD; null = sin tope. */
    val monthlyBudget: Money? = null,
    /** Piso de comisión Bs (ADR-006 backend). */
    val minCommission: Money = Money.parse("14.00"),
    /** Porcentaje como lo guarda el backend: 0.30 = 0.30 %. */
    val commissionRate: BigDecimal = BigDecimal("0.30"),
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
)
