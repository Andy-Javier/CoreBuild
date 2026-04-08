package edu.ucne.corebuild.domain.repository

import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.util.Resource
import kotlinx.coroutines.flow.Flow

interface ComponentRepository {
    fun getComponents(): Flow<Resource<List<Component>>>
    fun getComponentById(id: Int): Flow<Component?>
}
