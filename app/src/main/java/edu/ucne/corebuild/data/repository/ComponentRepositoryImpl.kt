package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.local.mapper.toEntity
import edu.ucne.corebuild.data.remote.datasource.RemoteDataSource
import edu.ucne.corebuild.data.remote.dto.*
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ComponentRepositoryImpl @Inject constructor(
    private val dao: ComponentDao,
    private val remoteDataSource: RemoteDataSource
) : ComponentRepository {

    override fun getComponents(): Flow<List<Component>> {
        return dao.getComponents()
            .onStart {
                withContext(Dispatchers.IO) {
                    if (dao.getCount() == 0) {
                        refreshComponents()
                    }
                }
            }
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override fun getComponentById(id: Int): Flow<Component?> {
        return dao.getComponentById(id)
            .map { it?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    private suspend fun refreshComponents() {
        try {
            val cpus = remoteDataSource.getCpus().getOrNull()?.map { it.toDomain().toEntity() } ?: emptyList()
            val gpus = remoteDataSource.getGpus().getOrNull()?.map { it.toDomain().toEntity() } ?: emptyList()
            val motherboards = remoteDataSource.getMotherboards().getOrNull()?.map { it.toDomain().toEntity() } ?: emptyList()
            val rams = remoteDataSource.getRams().getOrNull()?.map { it.toDomain().toEntity() } ?: emptyList()
            val psus = remoteDataSource.getPsus().getOrNull()?.map { it.toDomain().toEntity() } ?: emptyList()

            val allEntities = cpus + gpus + motherboards + rams + psus
            if (allEntities.isNotEmpty()) {
                dao.insertAll(allEntities)
            }
        } catch (e: Exception) {
            // Log error or handle failure
        }
    }
}
