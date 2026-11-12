package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_lives")
data class ActiveLifeEntity(
    @PrimaryKey val id: String,
    val dynastyId: String,
    val fullName: String,
    val age: Int,
    val generation: Int,
    val countryName: String,
    val netWorth: Long,
    val isAlive: Boolean,
    val stateJson: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "legacy_hall")
data class LegacyHallEntity(
    @PrimaryKey val id: String,
    val dynastyId: String,
    val generation: Int,
    val fullName: String,
    val countryName: String,
    val ageAtDeath: Int,
    val causeOfDeath: String,
    val finalNetWorth: Long,
    val legacyTitle: String,
    val notableTraits: String,
    val careerHighlight: String,
    val completedAt: Long = System.currentTimeMillis()
)
