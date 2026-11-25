package io.github.suricatoaviones.spentztrackr.navigation

/**
 * Rutas de Compose Navigation.
 *
 * Fase 3: pestaña Gastos + formulario (crear/editar) y detalle.
 * El formulario se abre sin editing (alta) o con id (edición).
 */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"

    const val EXPENSES = "expenses"
    const val EXPENSE_FORM = "expense-form?editing={editing}"
    const val EXPENSE_DETAIL = "expense-detail/{id}"

    fun expenseForm(editing: Long? = null): String = if (editing == null) "expense-form" else "expense-form?editing=$editing"

    fun expenseDetail(id: Long): String = "expense-detail/$id"

    // Onboarding (Fase 1)
    const val WELCOME = "welcome"
    const val MODE_SELECTION = "mode-selection"
    const val LOCAL_SETUP = "local-setup"
    const val SERVER_LOGIN = "server-login/{fromSettings}"

    fun serverLogin(fromSettings: Boolean): String = "server-login/$fromSettings"

    /** Nombre del argumento de ruta de [SERVER_LOGIN]. */
    const val ARG_FROM_SETTINGS = "fromSettings"

    /** Argumentos del formulario y el detalle. */
    const val ARG_EDITING = "editing"
    const val ARG_EXPENSE_ID = "id"
}
