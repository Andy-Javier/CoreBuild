package edu.ucne.corebuild.presentation.admin

import edu.ucne.corebuild.domain.model.Component

data class AdminUiState(
    val components: List<Component> = emptyList(),
    val selectedComponent: Component? = null,
    val selectedType: String = "CPU",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isOnline: Boolean = true,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val selectedImageUri: String? = null,
    val isUploadingImage: Boolean = false,
    val formState: ComponentFormState = ComponentFormState(),
    val navigateBack: Boolean = false
)
