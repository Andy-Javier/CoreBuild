package edu.ucne.corebuild.presentation.admin.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ucne.corebuild.domain.logs.AdminLog
import edu.ucne.corebuild.domain.logs.AdminLogRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val adminLogRepository: AdminLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            adminLogRepository.getLogs().collect { logs ->
                _uiState.update {
                    it.copy(
                        logs = logs,
                        filteredLogs = filterLogs(logs, it.filter)
                    )
                }
            }
        }
    }

    fun onEvent(event: LogsEvent) {
        when (event) {
            LogsEvent.OnLoad -> { /* No action needed */ }
            is LogsEvent.OnFilterChange -> {
                _uiState.update {
                    it.copy(
                        filter = event.filter,
                        filteredLogs = filterLogs(it.logs, event.filter)
                    )
                }
            }
            LogsEvent.ClearFilter ->
                _uiState.update {
                    it.copy(filter = "", filteredLogs = it.logs)
                }
        }
    }

    private fun filterLogs(
        logs: List<AdminLog>,
        filter: String
    ): List<AdminLog> {
        if (filter.isBlank()) return logs
        return logs.filter {
            it.action.contains(filter, ignoreCase = true) ||
            it.componentName.contains(filter, ignoreCase = true) ||
            it.userEmail.contains(filter, ignoreCase = true)
        }
    }
}
