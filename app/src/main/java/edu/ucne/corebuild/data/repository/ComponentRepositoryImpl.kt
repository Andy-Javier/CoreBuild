package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.remote.datasource.RemoteDataSource
import edu.ucne.corebuild.data.sync.SyncManager
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ComponentRepositoryImpl @Inject constructor(
    private val dao: ComponentDao,
    private val syncManager: SyncManager
) : ComponentRepository {

    override fun getComponents(): Flow<List<Component>> = flow {
        coroutineScope {
            launch { syncManager.syncAll() }
        }
        emitAll(
            dao.getComponents()
                .map { entities -> entities.map { it.toDomain() } }
        )
    }.flowOn(Dispatchers.IO)
    .catch { emit(emptyList()) }

    override fun getComponentById(id: Int): Flow<Component?> {
        return dao.getComponentById(id)
            .map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }
}
