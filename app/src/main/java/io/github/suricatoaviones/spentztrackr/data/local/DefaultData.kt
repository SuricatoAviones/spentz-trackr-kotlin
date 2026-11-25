package io.github.suricatoaviones.spentztrackr.data.local

/**
 * Set inicial de categorías y orígenes de pago.
 *
 * **Espejo exacto** de `AssignDefaultUserDataAction` del backend
 * (`DEFAULT_CATEGORIES` / `DEFAULT_PAYMENT_SOURCES`): si cambia allí, cambia
 * aquí. Todos los defaults son de tipo "expense", como en el backend.
 */
object DefaultData {
    data class CatalogItem(
        val name: String,
        val icon: String,
        val color: String,
    )

    val categories: List<CatalogItem> =
        listOf(
            CatalogItem("Alimentación", "shopping-cart", "#10B981"),
            CatalogItem("Transporte", "car", "#3B82F6"),
            CatalogItem("Servicios", "zap", "#F59E0B"),
            CatalogItem("Salud", "heart-pulse", "#EF4444"),
            CatalogItem("Ocio", "gamepad-2", "#8B5CF6"),
            CatalogItem("Ropa", "shirt", "#EC4899"),
            CatalogItem("Educación", "graduation-cap", "#06B6D4"),
            CatalogItem("Otros", "tag", "#6B7280"),
        )

    val paymentSources: List<CatalogItem> =
        listOf(
            CatalogItem("Efectivo", "banknote", "#F59E0B"),
            CatalogItem("Zelle", "zap", "#8B5CF6"),
            CatalogItem("PayPal", "wallet", "#3B82F6"),
            CatalogItem("Binance", "bitcoin", "#EC4899"),
            CatalogItem("Pago Móvil", "smartphone", "#14B8A6"),
            CatalogItem("Banco de Venezuela", "landmark", "#10B981"),
            CatalogItem("Banesco", "landmark", "#EF4444"),
            CatalogItem("Mercantil", "landmark", "#06B6D4"),
        )
}
