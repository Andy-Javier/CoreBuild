package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.dao.ComponentDao
import edu.ucne.corebuild.data.local.datasource.ComponentLocalDataSource
import edu.ucne.corebuild.data.local.mapper.toDomain
import edu.ucne.corebuild.data.local.mapper.toEntity
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ComponentRepositoryImpl @Inject constructor(
    private val dao: ComponentDao,
    private val localDataSource: ComponentLocalDataSource
) : ComponentRepository {

    override fun getComponents(): Flow<List<Component>> {
        return dao.getComponents()
            .onStart {
                if (dao.getCount() == 0) {
                    val initialData = localDataSource.getInitialData()
                    dao.insertAll(initialData.map { it.toEntity() })
                }
            }
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getComponentById(id: Int): Flow<Component?> {
        return dao.getComponentById(id).map { it?.toDomain() }
    }
}
