package io.github.suricatoaviones.spentztrackr.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * DTOs de catálogos (categorías y orígenes) y tasas — shapes verificados
 * contra `CategoryController`/`PaymentSourceController` y
 * `docs/api/api-documentation.md`.
 */

@Serializable
data class CategoryDto(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val type: String? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val budget: BigDecimal? = null,
    @SerialName("is_system") val isSystem: Boolean = false,
)

@Serializable
data class CategoriesListDataDto(
    val categories: List<CategoryDto> = emptyList(),
)

@Serializable
data class CategoryRequestDto(
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val type: String,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val budget: BigDecimal? = null,
)

// ---------- Orígenes de pago ----------

@Serializable
data class SourceDto(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    @SerialName("is_system") val isSystem: Boolean = false,
)

@Serializable
data class SourcesListDataDto(
    val sources: List<SourceDto> = emptyList(),
)

@Serializable
data class SourceRequestDto(
    val name: String,
    val icon: String? = null,
    val color: String? = null,
)

// ---------- Tasas ----------

/** `GET /rates` → `{bcv: "30.5000", paralelo: "…", manual: "…"}`. */
@Serializable
data class RatesDataDto(
    val rates: Map<String, String>? = null,
)

@Serializable
data class ManualRateRequestDto(
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val rate: BigDecimal,
)

@Serializable
data class ManualRateDataDto(
    val id: Long? = null,
    @Serializable(with = FlexibleBigDecimalSerializer::class)
    val rate: BigDecimal? = null,
)

// ---------- Común ----------

/** `apiCreated`/update/delete devuelven `data: {id}`. */
@Serializable
data class IdDto(
    val id: Long,
)
