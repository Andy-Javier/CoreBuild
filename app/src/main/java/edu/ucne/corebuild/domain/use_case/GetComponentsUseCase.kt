package edu.ucne.corebuild.domain.use_case

import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import edu.ucne.corebuild.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetComponentsUseCase @Inject constructor(
    private val repository: ComponentRepository
) {
    operator fun invoke(): Flow<Resource<List<Component>>> {
        return repository.getComponents()
    }
}
