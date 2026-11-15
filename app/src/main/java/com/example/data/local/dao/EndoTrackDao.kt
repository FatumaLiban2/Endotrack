package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CycleSummaryEntity
import com.example.data.local.entity.ModelWeightsEntity
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EndoTrackDao {

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserByIdFlow(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserByIdOnce(userId: Long): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM symptom_logs WHERE userId = :userId ORDER BY epochDay DESC")
    fun getLogsForUserFlow(userId: Long): Flow<List<SymptomLogEntity>>

    @Query("SELECT * FROM symptom_logs WHERE userId = :userId ORDER BY epochDay DESC")
    suspend fun getLogsForUserOnce(userId: Long): List<SymptomLogEntity>

    @Query("SELECT * FROM symptom_logs WHERE userId = :userId AND epochDay = :epochDay LIMIT 1")
    suspend fun getLogForDay(userId: Long, epochDay: Long): SymptomLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: SymptomLogEntity): Long

    @Query("DELETE FROM symptom_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)

    @Query("SELECT * FROM cycle_summaries WHERE userId = :userId ORDER BY cycleNumber DESC")
    fun getCycleSummariesFlow(userId: Long): Flow<List<CycleSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCycleSummary(summary: CycleSummaryEntity)

    @Query("DELETE FROM cycle_summaries WHERE userId = :userId")
    suspend fun deleteCycleSummariesForUser(userId: Long)

    @Query("SELECT * FROM model_weights WHERE userId = :userId LIMIT 1")
    fun getWeightsForUserFlow(userId: Long): Flow<ModelWeightsEntity?>

    @Query("SELECT * FROM model_weights WHERE userId = :userId LIMIT 1")
    suspend fun getWeightsForUserOnce(userId: Long): ModelWeightsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeights(weights: ModelWeightsEntity)
}
