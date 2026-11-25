package io.github.suricatoaviones.spentztrackr.feature.expenses

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.suricatoaviones.spentztrackr.R

/** Texto visible del badge de tasa (BCV/Paralelo/Manual), compartido con ingresos. */
@Composable
fun badgeText(badge: RateBadge?): String =
    when (badge) {
        RateBadge.BCV -> stringResource(R.string.expense_badge_bcv)
        RateBadge.PARALELO -> stringResource(R.string.expense_badge_paralelo)
        RateBadge.MANUAL -> stringResource(R.string.expense_badge_manual)
        null -> ""
    }
