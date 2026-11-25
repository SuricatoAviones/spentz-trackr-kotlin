package io.github.suricatoaviones.spentztrackr.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal

/**
 * Decimal tolerante (ADR-A03): Laravel serializa DECIMAL como string pero
 * algunos endpoints mandan números (`"amount": 150.50` vs `"150.50"`), y la
 * validación de requests acepta ambos. Se lee cualquiera de las dos formas
 * y **se escribe como texto canónico** (sin notación científica).
 */
object FlexibleBigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleBigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        val jsonDecoder =
            decoder as? JsonDecoder
                ?: throw SerializationException("FlexibleBigDecimal requiere un JsonDecoder")
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonPrimitive ->
                element.content.toBigDecimalOrNull()
                    ?: throw SerializationException("Decimal inválido: '${element.content}'")
            else -> throw SerializationException("Se esperaba un número o string decimal")
        }
    }

    override fun serialize(
        encoder: Encoder,
        value: BigDecimal,
    ) {
        encoder.encodeString(value.toPlainString())
    }

    private fun String.toBigDecimalOrNull(): BigDecimal? = runCatching { BigDecimal(trim()) }.getOrNull()
}
