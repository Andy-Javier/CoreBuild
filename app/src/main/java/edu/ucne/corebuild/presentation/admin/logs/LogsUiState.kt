package edu.ucne.corebuild.presentation.admin.logs

import edu.ucne.corebuild.domain.logs.AdminLog

data class LogsUiState(
    val logs: List<AdminLog> = emptyList(),
    val filteredLogs: List<AdminLog> = emptyList(),
    val filter: String = "",
    val isLoading: Boolean = false
)
