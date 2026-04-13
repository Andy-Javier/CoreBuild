package edu.ucne.corebuild.presentation.admin

import edu.ucne.corebuild.domain.model.Component

sealed interface AdminEvent {
    data object OnLoadComponents : AdminEvent
    data class OnSelectType(val type: String) : AdminEvent
    data class OnFieldChange(
        val field: String, val value: String
    ) : AdminEvent
    data class OnCreateComponent(
        val component: Component
    ) : AdminEvent
    data class OnUpdateComponent(
        val component: Component
    ) : AdminEvent
    data class OnDeleteComponent(
        val id: Int, val type: String
    ) : AdminEvent
    data class OnSelectComponent(
        val component: Component
    ) : AdminEvent
    data object OnShowCreateDialog : AdminEvent
    data object OnDismissDialog : AdminEvent
    data object DismissMessage : AdminEvent
    data class OnImageSelected(val uri: String) : AdminEvent
    data object OnUploadImage : AdminEvent
    data class OnFormFieldChange(val field: String, val value: String) : AdminEvent
    data object OnResetForm : AdminEvent
}
