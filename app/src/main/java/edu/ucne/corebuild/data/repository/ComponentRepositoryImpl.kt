package edu.ucne.corebuild.data.repository

import androidx.paging.*
import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.database.CoreBuildDatabase
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.paging.ComponentRemoteMediator
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
    private val db: CoreBuildDatabase,
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

    @OptIn(ExperimentalPagingApi::class)
    override fun getComponentsStream(): Flow<PagingData<Component>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = ComponentRemoteMediator(dao, db, syncManager),
            pagingSourceFactory = { dao.getComponentsPaged() }
        ).flow
         .map { pagingData -> pagingData.map { it.toDomain() } }
}
