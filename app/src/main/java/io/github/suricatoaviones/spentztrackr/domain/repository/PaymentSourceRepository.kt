package io.github.suricatoaviones.spentztrackr.domain.repository

import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSource
import io.github.suricatoaviones.spentztrackr.domain.model.PaymentSourceDraft
import kotlinx.coroutines.flow.Flow

/** Orígenes de pago en cualquier modo. Borrar con gastos: bloqueado. */
interface PaymentSourceRepository {
    fun observe(): Flow<List<PaymentSource>>

    suspend fun save(draft: PaymentSourceDraft): Long

    suspend fun update(
        id: Long,
        draft: PaymentSourceDraft,
    )

    @Throws(EntityInUseException::class)
    suspend fun delete(id: Long)
}
