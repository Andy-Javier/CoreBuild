package edu.ucne.corebuild.presentation.admin.logs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.corebuild.domain.logs.AdminLog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LogsScreen(
    viewModel: LogsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LogsBody(state, viewModel::onEvent, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsBody(
    state: LogsUiState,
    onEvent: (LogsEvent) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de cambios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = state.filter,
                onValueChange = { onEvent(LogsEvent.OnFilterChange(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Filtrar logs...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.filter.isNotEmpty()) {
                        IconButton(onClick = { onEvent(LogsEvent.ClearFilter) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("Todos", "CREATE", "UPDATE", "DELETE")
                items(filters) { filter ->
                    FilterChip(
                        selected = if (filter == "Todos") state.filter.isEmpty() else state.filter == filter,
                        onClick = {
                            if (filter == "Todos") onEvent(LogsEvent.ClearFilter)
                            else onEvent(LogsEvent.OnFilterChange(filter))
                        },
                        label = { Text(filter) }
                    )
                }
            }

            if (state.filteredLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay registros", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.filteredLogs) { log ->
                        LogCard(log)
                    }
                }
            }
        }
    }
}

@Composable
fun LogCard(log: AdminLog) {
    val actionColor = when (log.action) {
        "CREATE" -> Color(0xFF4CAF50)
        "UPDATE" -> Color(0xFFFFC107)
        "DELETE" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val actionIcon = when (log.action) {
        "CREATE" -> Icons.Default.Add
        "UPDATE" -> Icons.Default.Edit
        "DELETE" -> Icons.Default.Delete
        else -> Icons.Default.History
    }

    val sdf = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault())
    val formattedDate = sdf.format(Date(log.timestamp))

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = actionIcon,
                contentDescription = log.action,
                tint = actionColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = log.componentName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = log.componentType,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = log.userEmail,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
