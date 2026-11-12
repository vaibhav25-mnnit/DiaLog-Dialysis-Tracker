package com.example.dialysisdiary.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DialysisSessionDao {

    @Query("SELECT * FROM dialysis_sessions ORDER BY startTimeMillis ASC")
    fun getAllSessions(): Flow<List<DialysisSessionEntity>>

    @Query("SELECT * FROM dialysis_sessions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY startTimeMillis ASC")
    fun getSessionsForDateRange(startMillis: Long, endMillis: Long): Flow<List<DialysisSessionEntity>>

    @Query("SELECT * FROM dialysis_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): DialysisSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: DialysisSessionEntity): Long

    @Update
    suspend fun updateSession(session: DialysisSessionEntity)

    @Delete
    suspend fun deleteSession(session: DialysisSessionEntity)

    @Query("DELETE FROM dialysis_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)
}
