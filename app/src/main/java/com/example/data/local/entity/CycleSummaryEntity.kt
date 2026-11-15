package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cycle_summaries",
    indices = [Index(value = ["userId", "cycleNumber"], unique = true)]
)
data class CycleSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val cycleNumber: Int,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val lengthDays: Int,
    val averagePain: Double,
    val peakPain: Int,
    val bleedingDays: Int,
    val aggregatedRiskProbability: Double,
    val topDriverSummary: String
)
