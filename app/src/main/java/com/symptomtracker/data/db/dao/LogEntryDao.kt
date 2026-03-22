package com.symptomtracker.data.db.dao

import androidx.room.*
import com.symptomtracker.data.db.entity.LogEntry
import com.symptomtracker.data.db.entity.LogType
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {
    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE type = :type ORDER BY timestamp DESC")
    fun getByType(type: LogType): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE refId = :refId ORDER BY timestamp DESC")
    fun getByRefId(refId: Long): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries WHERE refName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<LogEntry>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 50): List<LogEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: LogEntry): Long

    @Update
    suspend fun update(entry: LogEntry)

    @Delete
    suspend fun delete(entry: LogEntry)

    @Query("DELETE FROM log_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
