package edu.ucne.corebuild.presentation.admin.logs

sealed interface LogsEvent {
    data object OnLoad : LogsEvent
    data class OnFilterChange(val filter: String) : LogsEvent
    data object ClearFilter : LogsEvent
}
