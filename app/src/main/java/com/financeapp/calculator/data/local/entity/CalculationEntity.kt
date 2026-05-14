package com.financeapp.calculator.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculations")
data class CalculationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String,
    val inputsJson: String,
    val resultsJson: String,
    val savedName: String?,
    val createdAt: String,
    /** When true, the row hasn't been synced to the backend yet. */
    val pendingSync: Boolean = false
)
