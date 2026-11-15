package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val ageRange: String = "25–34",
    val ageNumeric: Int = 28,
    val cycleLengthDays: Int = 28,
    val periodDurationDays: Int = 5,
    val journeyReason: String = "I'm waiting for a diagnosis",
    val helpGoalsCsv: String = "Track my symptoms day by day,Spot patterns across my cycle,Prepare for doctor's appointments",
    val baselineHistoryCsv: String = "",
    val lastPeriodStartEpochDay: Long,
    val hasCompletedProfile: Boolean = false,
    val acceptedDisclaimer: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun baselineHistorySet(): Set<String> =
        baselineHistoryCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    fun helpGoalsSet(): Set<String> =
        helpGoalsCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}
