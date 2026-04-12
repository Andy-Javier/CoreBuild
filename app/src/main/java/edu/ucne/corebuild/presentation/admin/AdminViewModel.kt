package edu.ucne.corebuild.presentation.admin

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import edu.ucne.corebuild.core.network.NetworkManager
import edu.ucne.corebuild.data.remote.image.ImageUploader
import edu.ucne.corebuild.domain.auth.AuthManager
import edu.ucne.corebuild.domain.logs.AdminLog
import edu.ucne.corebuild.domain.logs.AdminLogRepository
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.domain.repository.ComponentRepository
import edu.ucne.corebuild.domain.repository.UserRepository
import edu.ucne.corebuild.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val componentRepository: ComponentRepository,
    private val networkManager: NetworkManager,
    private val authManager: AuthManager,
    private val userRepository: UserRepository,
    private val adminLogRepository: AdminLogRepository,
    private val imageUploader: ImageUploader,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private var currentUserEmail: String = ""

    init {
        loadComponents()
        checkConnectivity()
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.getLoggedUser().collect { user ->
                currentUserEmail = user?.email ?: "admin@corebuild.com"
            }
        }
    }

    private fun loadComponents() {
        viewModelScope.launch {
            componentRepository.getComponents().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(components = resource.data ?: emptyList(), isLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    private fun checkConnectivity() {
        _uiState.update {
            it.copy(isOnline = networkManager.isOnline())
        }
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            AdminEvent.OnLoadComponents -> loadComponents()
            is AdminEvent.OnSelectType ->
                _uiState.update { it.copy(selectedType = event.type) }
            is AdminEvent.OnCreateComponent -> createComponent(event.component)
            is AdminEvent.OnUpdateComponent -> updateComponent(event.component)
            is AdminEvent.OnDeleteComponent ->
                deleteComponent(event.id, event.type)
            is AdminEvent.OnSelectComponent ->
                _uiState.update {
                    it.copy(
                        selectedComponent = event.component,
                        showEditDialog = true,
                        selectedImageUri = event.component.imageUrl
                    )
                }
            AdminEvent.OnShowCreateDialog -> 
                _uiState.update { it.copy(showCreateDialog = true, selectedImageUri = null) }
            AdminEvent.OnDismissDialog ->
                _uiState.update {
                    it.copy(
                        showCreateDialog = false,
                        showEditDialog = false,
                        selectedComponent = null,
                        selectedImageUri = null
                    )
                }
            AdminEvent.DismissMessage ->
                _uiState.update {
                    it.copy(
                        successMessage = null,
                        errorMessage = null
                    )
                }
            is AdminEvent.OnImageSelected ->
                _uiState.update { it.copy(selectedImageUri = event.uri) }
            AdminEvent.OnUploadImage -> uploadImage()
            else -> {}
        }
    }

    private fun uploadImage() {
        val uriStr = _uiState.value.selectedImageUri ?: return
        if (uriStr.startsWith("http")) return // Ya es una URL de red

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true) }
            imageUploader.uploadImage(context, Uri.parse(uriStr))
                .fold(
                    onSuccess = { url ->
                        _uiState.update {
                            it.copy(
                                isUploadingImage = false,
                                selectedImageUri = url,
                                successMessage = "Imagen subida correctamente"
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isUploadingImage = false,
                                errorMessage = "Error subiendo imagen: ${e.message}"
                            )
                        }
                    }
                )
        }
    }

    private fun createComponent(component: Component) {
        if (!networkManager.isOnline()) {
            _uiState.update {
                it.copy(errorMessage = "Se requiere conexión para crear componentes")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val componentWithImage = component.withImageUrl(_uiState.value.selectedImageUri)
            componentRepository.addComponent(componentWithImage)
                .fold(
                    onSuccess = {
                        adminLogRepository.addLog(
                            AdminLog(
                                userEmail = currentUserEmail,
                                action = "CREATE",
                                componentName = component.name,
                                componentType = component.category
                            )
                        )
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                successMessage = "Componente creado correctamente",
                                showCreateDialog = false,
                                selectedImageUri = null
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = e.message ?: "Error al crear"
                            )
                        }
                    }
                )
        }
    }

    private fun updateComponent(component: Component) {
        if (!networkManager.isOnline()) {
            _uiState.update {
                it.copy(errorMessage = "Se requiere conexión para editar componentes")
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val componentWithImage = component.withImageUrl(_uiState.value.selectedImageUri)
            componentRepository.updateComponent(componentWithImage)
                .fold(
                    onSuccess = {
                        adminLogRepository.addLog(
                            AdminLog(
                                userEmail = currentUserEmail,
                                action = "UPDATE",
                                componentName = component.name,
                                componentType = component.category
                            )
                        )
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                successMessage = "Componente actualizado",
                                showEditDialog = false,
                                selectedComponent = null,
                                selectedImageUri = null
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = e.message ?: "Error al actualizar"
                            )
                        }
                    }
                )
        }
    }

    private fun deleteComponent(id: Int, type: String) {
        if (!networkManager.isOnline()) {
            _uiState.update {
                it.copy(errorMessage = "Se requiere conexión para eliminar componentes")
            }
            return
        }
        viewModelScope.launch {
            val componentName = _uiState.value.components.find { it.id == id }?.name ?: "ID: $id"
            componentRepository.deleteComponent(id, type)
                .fold(
                    onSuccess = {
                        adminLogRepository.addLog(
                            AdminLog(
                                userEmail = currentUserEmail,
                                action = "DELETE",
                                componentName = componentName,
                                componentType = type
                            )
                        )
                        _uiState.update {
                            it.copy(successMessage = "Componente eliminado")
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(errorMessage = e.message ?: "Error al eliminar")
                        }
                    }
                )
        }
    }
}
