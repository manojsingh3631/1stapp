package com.financeapp.calculator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.financeapp.calculator.data.local.entity.CalculationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {

    @Query("SELECT * FROM calculations WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: String): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculations WHERE pendingSync = 1")
    suspend fun getPending(): List<CalculationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CalculationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<CalculationEntity>)

    @Query("DELETE FROM calculations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM calculations WHERE userId = :userId")
    suspend fun clearForUser(userId: String)
}
