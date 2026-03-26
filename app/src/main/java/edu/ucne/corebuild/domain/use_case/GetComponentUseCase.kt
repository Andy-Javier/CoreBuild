package edu.ucne.corebuild.domain.use_case

import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetComponentUseCase @Inject constructor(
    private val repository: ComponentRepository
) {
    operator fun invoke(id: Int): Flow<Component?> {
        return repository.getComponentById(id)
    }
}
