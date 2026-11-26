package io.github.suricatoaviones.spentztrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.suricatoaviones.spentztrackr.data.local.entity.UserPrefsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPrefsDao {
    @Query("SELECT * FROM user_prefs WHERE id = 1")
    suspend fun find(): UserPrefsEntity?

    @Query("SELECT * FROM user_prefs WHERE id = 1")
    fun observe(): Flow<UserPrefsEntity?>

    @Upsert
    suspend fun upsert(prefs: UserPrefsEntity)
}
