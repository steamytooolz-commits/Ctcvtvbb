package com.example.data.repository

import com.example.data.local.ActiveLifeEntity
import com.example.data.local.EpochDao
import com.example.data.local.LegacyHallEntity
import com.example.data.model.LifeState
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LifeRepository(private val dao: EpochDao) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val lifeAdapter = moshi.adapter(LifeState::class.java)

    val savedLives: Flow<List<LifeState>> = dao.observeAllLives().map { entities ->
        entities.mapNotNull { entity ->
            runCatching { lifeAdapter.fromJson(entity.stateJson) }.getOrNull()
        }
    }

    val legacyRecords: Flow<List<LegacyHallEntity>> = dao.observeLegacyHall()

    fun observeDynasty(dynastyId: String): Flow<List<LegacyHallEntity>> =
        dao.observeDynastyTree(dynastyId)

    suspend fun getMostRecentLife(): LifeState? = withContext(Dispatchers.IO) {
        dao.getMostRecentLife()?.let { entity ->
            runCatching { lifeAdapter.fromJson(entity.stateJson) }.getOrNull()
        }
    }

    suspend fun saveLife(state: LifeState) = withContext(Dispatchers.IO) {
        val json = lifeAdapter.toJson(state)
        dao.upsertLife(
            ActiveLifeEntity(
                id = state.id,
                dynastyId = state.dynastyId,
                fullName = state.fullName,
                age = state.age,
                generation = state.generation,
                countryName = state.country.name,
                netWorth = state.netWorth,
                isAlive = state.isAlive,
                stateJson = json,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteLife(id: String) = withContext(Dispatchers.IO) {
        dao.deleteLifeById(id)
    }

    suspend fun recordLegacy(state: LifeState) = withContext(Dispatchers.IO) {
        val record = LegacyHallEntity(
            id = state.id,
            dynastyId = state.dynastyId,
            generation = state.generation,
            fullName = state.fullName,
            countryName = state.country.name,
            ageAtDeath = state.age,
            causeOfDeath = state.causeOfDeath ?: "Old Age",
            finalNetWorth = state.netWorth,
            legacyTitle = state.legacyTitle ?: "Remembered Citizen",
            notableTraits = state.traits.take(4).joinToString(" • ").ifBlank { "Resilient Spirit" },
            careerHighlight = state.business?.let { "Founder of ${it.name}" }
                ?: state.currentJob?.title
                ?: state.degreeMajor?.let { "${it.label} Scholar" }
                ?: "Free Spirit"
        )
        dao.insertLegacyRecord(record)
    }
}
