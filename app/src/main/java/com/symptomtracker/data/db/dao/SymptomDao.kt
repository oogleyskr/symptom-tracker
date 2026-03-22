package com.symptomtracker.data.db.dao

import androidx.room.*
import com.symptomtracker.data.db.entity.Symptom
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Query("SELECT * FROM symptoms ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<Symptom>>

    @Query("SELECT * FROM symptoms WHERE id = :id")
    suspend fun getById(id: Long): Symptom?

    @Query("SELECT * FROM symptoms ORDER BY timestamp DESC LIMIT 20")
    suspend fun getRecent(): List<Symptom>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptom: Symptom): Long

    @Update
    suspend fun update(symptom: Symptom)

    @Delete
    suspend fun delete(symptom: Symptom)

    @Query("DELETE FROM symptoms WHERE id = :id")
    suspend fun deleteById(id: Long)
}
