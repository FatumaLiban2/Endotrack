package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "model_weights")
data class ModelWeightsEntity(
    @PrimaryKey val userId: Long,
    val weightsCsv: String,
    val calibrationUpdatesCount: Int = 0,
    val lastCalibrationOutcome: String = "Clinical Baseline (886-Patient Model)",
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
