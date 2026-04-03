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
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
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
                    if (dao.getCount() < 10) {
                        refreshAllCategories()
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

    private suspend fun refreshAllCategories() = supervisorScope {
        launch {
            try {
                remoteDataSource.getCpus().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain()
                        domain.toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }
        launch {
            try {
                remoteDataSource.getGpus().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain(dto.id + 1000)
                        domain.toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }
        launch {
            try {
                remoteDataSource.getMotherboards().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain(dto.id + 2000)
                        domain.toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }
        launch {
            try {
                remoteDataSource.getRams().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain(dto.id + 3000)
                        domain.toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }
        launch {
            try {
                remoteDataSource.getPsus().getOrNull()?.let { list ->
                    val entities = list.map { dto ->
                        val domain = dto.toDomain(dto.id + 4000)
                        domain.toEntity()
                    }
                    dao.insertAll(entities)
                }
            } catch (e: Exception) { }
        }
    }
}