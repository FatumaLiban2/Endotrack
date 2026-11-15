package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "symptom_logs",
    indices = [Index(value = ["userId", "epochDay"], unique = true)]
)
data class SymptomLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val epochDay: Long,
    val cycleNumber: Int,
    val cycleDay: Int,
    val painLevel: Int,
    val hasPeriod: Boolean,
    val flowVolume: String,
    val otcMedicationName: String,
    val otcRelievedPain: String,
    val impactFlagsCsv: String,
    val notes: String,
    val featureVectorBinary: String,
    val dailyRiskProbability: Double,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun impactFlagsSet(): Set<String> =
        impactFlagsCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}
