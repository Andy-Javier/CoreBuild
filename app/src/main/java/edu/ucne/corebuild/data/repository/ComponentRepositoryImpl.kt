package edu.ucne.corebuild.data.repository

import edu.ucne.corebuild.data.local.datasource.ComponentLocalDataSource
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ComponentRepositoryImpl @Inject constructor(
    private val localDataSource: ComponentLocalDataSource
) : ComponentRepository {
    override fun getComponents(): Flow<List<Component>> = localDataSource.getComponents()

    override fun getComponentById(id: Int): Flow<Component?> = localDataSource.getComponentById(id)
}
