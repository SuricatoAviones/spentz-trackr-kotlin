package io.github.suricatoaviones.spentztrackr.navigation

/**
 * Rutas de Compose Navigation.
 *
 * Fase 4: ingresos (lista/form/detalle) y catálogos (categorías/orígenes)
 * accesibles desde Ajustes.
 */
object Routes {
    // Tabs principales
    const val HOME = "home"
    const val EXPENSES = "expenses"
    const val SETTINGS = "settings"

    // Gastos
    const val EXPENSE_FORM = "expense-form?editing={editing}"
    const val EXPENSE_DETAIL = "expense-detail/{id}"

    fun expenseForm(editing: Long? = null): String = if (editing == null) "expense-form" else "expense-form?editing=$editing"

    fun expenseDetail(id: Long): String = "expense-detail/$id"

    // Ingresos
    const val INCOMES = "incomes"
    const val INCOME_FORM = "income-form?editing={editing}"
    const val INCOME_DETAIL = "income-detail/{id}"

    fun incomeForm(editing: Long? = null): String = if (editing == null) "income-form" else "income-form?editing=$editing"

    fun incomeDetail(id: Long): String = "income-detail/$id"

    // Catálogos (desde Ajustes)
    const val CATEGORIES = "categories"
    const val CATEGORY_FORM = "category-form?editing={editing}&type={type}"

    fun categoryForm(
        editing: Long? = null,
        type: String = "expense",
    ): String = if (editing == null) "category-form?type=$type" else "category-form?editing=$editing&type=$type"

    const val SOURCES = "sources"

    // Tasas (desde Ajustes, Fase 5)
    const val RATES = "rates"

    // Onboarding (Fase 1)
    const val WELCOME = "welcome"
    const val MODE_SELECTION = "mode-selection"
    const val LOCAL_SETUP = "local-setup"
    const val SERVER_LOGIN = "server-login/{fromSettings}"

    fun serverLogin(fromSettings: Boolean): String = "server-login/$fromSettings"

    /** Nombre del argumento de ruta de [SERVER_LOGIN]. */
    const val ARG_FROM_SETTINGS = "fromSettings"

    /** Argumentos de formularios y detalles. */
    const val ARG_EDITING = "editing"
    const val ARG_EXPENSE_ID = "id"
    const val ARG_CATEGORY_TYPE = "type"
}
