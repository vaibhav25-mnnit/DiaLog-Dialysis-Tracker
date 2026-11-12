package com.example.dialysisdiary.data.repository

import com.example.dialysisdiary.data.local.DialysisSessionDao
import com.example.dialysisdiary.data.local.DialysisSessionEntity
import kotlinx.coroutines.flow.Flow

class DialysisSessionRepository(private val dao: DialysisSessionDao) {

    val allSessions: Flow<List<DialysisSessionEntity>> = dao.getAllSessions()

    fun getSessionsForDateRange(startMillis: Long, endMillis: Long): Flow<List<DialysisSessionEntity>> {
        return dao.getSessionsForDateRange(startMillis, endMillis)
    }

    suspend fun getSessionById(id: Long): DialysisSessionEntity? {
        return dao.getSessionById(id)
    }

    suspend fun insertSession(session: DialysisSessionEntity): Long {
        return dao.insertSession(session)
    }

    suspend fun updateSession(session: DialysisSessionEntity) {
        dao.updateSession(session)
    }

    suspend fun deleteSession(session: DialysisSessionEntity) {
        dao.deleteSession(session)
    }

    suspend fun deleteSessionById(id: Long) {
        dao.deleteSessionById(id)
    }
}
