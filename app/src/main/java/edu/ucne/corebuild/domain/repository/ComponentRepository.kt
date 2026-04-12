package edu.ucne.corebuild.domain.repository

import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.util.Resource
import kotlinx.coroutines.flow.Flow

interface ComponentRepository {
    fun getComponents(): Flow<Resource<List<Component>>>
    fun getComponentById(id: Int): Flow<Component?>
    suspend fun addComponent(component: Component): Result<Unit>
    suspend fun updateComponent(component: Component): Result<Unit>
    suspend fun deleteComponent(id: Int, type: String): Result<Unit>
}
