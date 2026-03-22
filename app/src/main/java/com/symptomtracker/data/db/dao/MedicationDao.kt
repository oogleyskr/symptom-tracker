package com.symptomtracker.data.db.dao

import androidx.room.*
import com.symptomtracker.data.db.entity.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE active = 1 ORDER BY name ASC")
    fun getActiveFlow(): Flow<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY name ASC")
    fun getAllFlow(): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getById(id: Long): Medication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)

    @Query("UPDATE medications SET active = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
}
