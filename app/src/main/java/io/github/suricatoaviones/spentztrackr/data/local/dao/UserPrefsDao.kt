package io.github.suricatoaviones.spentztrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import io.github.suricatoaviones.spentztrackr.data.local.entity.UserPrefsEntity

@Dao
interface UserPrefsDao {
    @Query("SELECT * FROM user_prefs WHERE id = 1")
    suspend fun find(): UserPrefsEntity?

    @Upsert
    suspend fun upsert(prefs: UserPrefsEntity)
}
