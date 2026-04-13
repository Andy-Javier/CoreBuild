package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.AdminLogDao
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.local.mapper.toEntity
import edu.ucne.corebuild.domain.logs.AdminLog
import edu.ucne.corebuild.domain.logs.AdminLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdminLogRepositoryImpl @Inject constructor(
    private val dao: AdminLogDao
) : AdminLogRepository {
    override suspend fun addLog(log: AdminLog) {
        dao.insert(log.toEntity())
    }

    override fun getLogs(): Flow<List<AdminLog>> =
        dao.getAllLogs().map { it.map { e -> e.toDomain() } }
}
