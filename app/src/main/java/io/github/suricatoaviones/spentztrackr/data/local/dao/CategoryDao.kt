package io.github.suricatoaviones.spentztrackr.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.github.suricatoaviones.spentztrackr.data.local.entity.CategoryEntity
import io.github.suricatoaviones.spentztrackr.domain.model.CategoryType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name")
    fun observeByType(type: CategoryType): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun findById(id: Long): CategoryEntity?

    /** FK RESTRICT: lanza SQLiteConstraintException si tiene movimientos. */
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
