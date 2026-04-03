package edu.ucne.corebuild.domain.repository

import androidx.paging.PagingData
import edu.ucne.corebuild.domain.model.Component
import kotlinx.coroutines.flow.Flow

interface ComponentRepository {
    fun getComponents(): Flow<List<Component>>
    fun getComponentById(id: Int): Flow<Component?>
    fun getComponentsStream(): Flow<PagingData<Component>>
}
