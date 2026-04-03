package edu.ucne.corebuild.domain.use_case

import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetComponentByIdUseCase @Inject constructor(
    private val repository: ComponentRepository
) {
    operator fun invoke(id: Int): Flow<Component?> =
        repository.getComponentById(id)
}
