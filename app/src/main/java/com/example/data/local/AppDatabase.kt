package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.EndoTrackDao
import com.example.data.local.entity.CycleSummaryEntity
import com.example.data.local.entity.ModelWeightsEntity
import com.example.data.local.entity.SymptomLogEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SymptomLogEntity::class,
        CycleSummaryEntity::class,
        ModelWeightsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun endoTrackDao(): EndoTrackDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "endotrack_clinical.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
