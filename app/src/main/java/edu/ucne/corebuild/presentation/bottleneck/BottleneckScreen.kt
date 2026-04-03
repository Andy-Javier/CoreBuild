package edu.ucne.corebuild.presentation.bottleneck

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.ucne.corebuild.domain.model.Component
import edu.ucne.corebuild.presentation.components.PerformanceBar
import edu.ucne.corebuild.ui.theme.CoreBuildTheme

@Composable
fun BottleneckScreen(
    viewModel: BottleneckViewModel = hiltViewModel(),
    onMenuClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BottleneckBody(
        state = state,
        onEvent = viewModel::onEvent,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottleneckBody(
    state: BottleneckUiState,
    onEvent: (BottleneckEvent) -> Unit,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora de Cuello de Botella") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selecciona tus componentes para calcular el balance del sistema.",
                style = MaterialTheme.typography.bodyMedium
            )

            // CPU Selector
            BottleneckDropdown(
                label = "Procesador (CPU)",
                selectedComponent = state.selectedCpu,
                components = state.cpus,
                onSelect = { onEvent(BottleneckEvent.SelectCpu(it as Component.CPU)) }
            )

            // GPU Selector
            BottleneckDropdown(
                label = "Tarjeta Gráfica (GPU)",
                selectedComponent = state.selectedGpu,
                components = state.gpus,
                onSelect = { onEvent(BottleneckEvent.SelectGpu(it as Component.GPU)) }
            )

            // Resolution Selector
            ResolutionSelector(
                selectedResolution = state.selectedResolution,
                onResolutionSelect = { onEvent(BottleneckEvent.SelectResolution(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results Section
            if (state.selectedCpu != null && state.selectedGpu != null) {
                ResultCard(state.bottleneckPercentage, state.status)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottleneckDropdown(
    label: String,
    selectedComponent: Component?,
    components: List<Component>,
    onSelect: (Component) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedComponent?.name ?: "Seleccionar...",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            components.forEach { component ->
                DropdownMenuItem(
                    text = { Text(component.name) },
                    onClick = {
                        onSelect(component)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResolutionSelector(
    selectedResolution: String,
    onResolutionSelect: (String) -> Unit
) {
    val resolutions = listOf("1080p", "1440p", "4K")
    Column {
        Text(text = "Resolución", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            resolutions.forEach { res ->
                FilterChip(
                    selected = selectedResolution == res,
                    onClick = { onResolutionSelect(res) },
                    label = { Text(res) }
                )
            }
        }
    }
}

@Composable
fun ResultCard(percentage: Double, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val performanceValue = ((100f - percentage) / 100f).toFloat()
            
            Text(
                text = "${String.format("%.1f", percentage)}%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (percentage < 10) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
            Text(
                text = status,
                style = MaterialTheme.typography.titleLarge,
                color = if (percentage < 10) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            PerformanceBar(value = performanceValue)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Un cuello de botella inferior al 10% se considera ideal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BottleneckScreenPreview() {
    CoreBuildTheme {
        BottleneckBody(
            state = BottleneckUiState(),
            onEvent = {},
            onMenuClick = {}
        )
    }
}
