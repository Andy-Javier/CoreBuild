package edu.ucne.corebuild.domain.repository

import edu.ucne.corebuild.domain.model.Component
import kotlinx.coroutines.flow.Flow

interface ComponentRepository {
    fun getComponents(): Flow<List<Component>>
    fun getComponentById(id: Int): Flow<Component?>
}
