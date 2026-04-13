package edu.ucne.corebuild.presentation.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.presentation.components.toPrice

@Composable
fun AdminScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onLogsClick: () -> Unit,
    onAddNewClick: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AdminBody(
        state = state,
        onEvent = viewModel::onEvent,
        onLogsClick = onLogsClick,
        onAddNewClick = onAddNewClick,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBody(
    state: AdminUiState,
    onEvent: (AdminEvent) -> Unit,
    onLogsClick: () -> Unit,
    onAddNewClick: () -> Unit,
    onBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage, state.errorMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(AdminEvent.DismissMessage)
        }
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(AdminEvent.DismissMessage)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Admin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onLogsClick) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    onEvent(AdminEvent.OnResetForm)
                    onAddNewClick() 
                },
                containerColor = if (state.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (state.isOnline) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Componente")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (!state.isOnline) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sin conexión — Solo lectura",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val grouped = state.components.groupBy { it.category }
                grouped.forEach { (category, components) ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(components, key = { it.id }) { component ->
                        ComponentAdminCard(
                            component = component,
                            onEdit = { 
                                // Para editar, idealmente usaríamos el mismo formulario pero 
                                // por ahora mantenemos la consistencia del flujo de navegación.
                                // onEvent(AdminEvent.OnSelectComponent(component)) 
                            },
                            onDelete = { onEvent(AdminEvent.OnDeleteComponent(component.id, component.category)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentAdminCard(
    component: Component,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = component.price.toPrice(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = component.category,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showConfirmDelete = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar este componente?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirmDelete = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
