package edu.ucne.corebuild.presentation.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.presentation.components.toPrice

@Composable
fun AdminScreen(
    viewModel: AdminViewModel = hiltViewModel(),
    onLogsClick: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AdminBody(
        state = state,
        onEvent = viewModel::onEvent,
        onLogsClick = onLogsClick,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBody(
    state: AdminUiState,
    onEvent: (AdminEvent) -> Unit,
    onLogsClick: () -> Unit,
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
                onClick = { if (state.isOnline) onEvent(AdminEvent.OnShowCreateDialog) },
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
                            onEdit = { onEvent(AdminEvent.OnSelectComponent(component)) },
                            onDelete = { onEvent(AdminEvent.OnDeleteComponent(component.id, component.category)) }
                        )
                    }
                }
            }
        }

        if (state.showEditDialog && state.selectedComponent != null) {
            ComponentDialog(
                title = "Editar Componente",
                component = state.selectedComponent,
                uiState = state,
                onEvent = onEvent,
                onDismiss = { onEvent(AdminEvent.OnDismissDialog) },
                onSave = { onEvent(AdminEvent.OnUpdateComponent(it)) }
            )
        }

        if (state.showCreateDialog) {
            ComponentDialog(
                title = "Nuevo Componente",
                component = null,
                uiState = state,
                onEvent = onEvent,
                onDismiss = { onEvent(AdminEvent.OnDismissDialog) },
                onSave = { onEvent(AdminEvent.OnCreateComponent(it)) }
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDialog(
    title: String,
    component: Component? = null,
    uiState: AdminUiState,
    onEvent: (AdminEvent) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Component) -> Unit
) {
    var name by remember { mutableStateOf(component?.name ?: "") }
    var price by remember { mutableStateOf(component?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(component?.category ?: "Procesador") }
    
    val categories = listOf("Procesador", "Tarjeta Gráfica", "Placa Base", "Memoria RAM", "Fuente de Poder")
    var expanded by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onEvent(AdminEvent.OnImageSelected(it.toString())) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Selector de Imagen
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imageLauncher.launch("image/*") }
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.selectedImageUri != null) {
                        AsyncImage(
                            model = uiState.selectedImageUri,
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Seleccionar imagen")
                    }
                }

                if (uiState.selectedImageUri != null && !uiState.selectedImageUri.startsWith("http")) {
                    Button(
                        onClick = { onEvent(AdminEvent.OnUploadImage) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isUploadingImage
                    ) {
                        if (uiState.isUploadingImage) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Subir a Cloudinary")
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Precio (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = price.toDoubleOrNull() ?: 0.0
                    val newComponent = when (category) {
                        "Procesador" -> Component.CPU(id = component?.id ?: 0, name = name, description = "", price = priceVal, brand = "", socket = "", generation = "", cores = 0, threads = 0, baseClock = "", boostClock = "", tdp = "")
                        "Tarjeta Gráfica" -> Component.GPU(id = component?.id ?: 0, name = name, description = "", price = priceVal, brand = "", chipset = "", vram = "", vramType = "", consumptionWatts = "")
                        "Placa Base" -> Component.Motherboard(id = component?.id ?: 0, name = name, description = "", price = priceVal, brand = "", socket = "", chipset = "", format = "", ramType = "")
                        "Memoria RAM" -> Component.RAM(id = component?.id ?: 0, name = name, description = "", price = priceVal, brand = "", type = "", capacity = "", configuration = "", speed = "", latency = "")
                        else -> Component.PSU(id = component?.id ?: 0, name = name, description = "", price = priceVal, brand = "", wattage = 0, certification = "", modularity = "")
                    }
                    onSave(newComponent)
                },
                enabled = !uiState.isSaving && name.isNotBlank() && price.toDoubleOrNull() != null
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
