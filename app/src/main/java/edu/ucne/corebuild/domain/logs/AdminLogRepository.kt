package edu.ucne.corebuild.domain.logs

import kotlinx.coroutines.flow.Flow

interface AdminLogRepository {
    suspend fun addLog(log: AdminLog)
    fun getLogs(): Flow<List<AdminLog>>
}
