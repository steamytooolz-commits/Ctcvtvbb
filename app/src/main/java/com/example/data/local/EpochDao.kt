package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EpochDao {
    @Query("SELECT * FROM active_lives ORDER BY updatedAt DESC")
    fun observeAllLives(): Flow<List<ActiveLifeEntity>>

    @Query("SELECT * FROM active_lives ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getMostRecentLife(): ActiveLifeEntity?

    @Query("SELECT * FROM active_lives WHERE id = :id LIMIT 1")
    suspend fun getLifeById(id: String): ActiveLifeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLife(entity: ActiveLifeEntity)

    @Query("DELETE FROM active_lives WHERE id = :id")
    suspend fun deleteLifeById(id: String)

    @Query("SELECT * FROM legacy_hall ORDER BY completedAt DESC")
    fun observeLegacyHall(): Flow<List<LegacyHallEntity>>

    @Query("SELECT * FROM legacy_hall WHERE dynastyId = :dynastyId ORDER BY generation ASC")
    fun observeDynastyTree(dynastyId: String): Flow<List<LegacyHallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLegacyRecord(record: LegacyHallEntity)
}
