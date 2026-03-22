package com.symptomtracker.data.db.dao

import androidx.room.*
import com.symptomtracker.data.db.entity.SideEffect
import kotlinx.coroutines.flow.Flow

@Dao
interface SideEffectDao {
    @Query("SELECT * FROM side_effects WHERE logEntryId = :logEntryId")
    fun getByLogEntry(logEntryId: Long): Flow<List<SideEffect>>

    @Query("SELECT * FROM side_effects ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<SideEffect>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sideEffect: SideEffect): Long

    @Delete
    suspend fun delete(sideEffect: SideEffect)
}
