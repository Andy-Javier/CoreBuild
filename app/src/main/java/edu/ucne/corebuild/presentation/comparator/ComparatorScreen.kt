package edu.ucne.corebuild.presentation.comparator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import edu.ucne.corebuild.domain.model.Component

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparatorScreen(
    viewModel: ComparatorViewModel = hiltViewModel(),
    onMenuClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparador") },
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
                .padding(16.dp)
        ) {
            // Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedType == "CPU",
                    onClick = { viewModel.onEvent(ComparatorEvent.SelectType("CPU")) },
                    label = { Text("Procesadores") }
                )
                FilterChip(
                    selected = state.selectedType == "GPU",
                    onClick = { viewModel.onEvent(ComparatorEvent.SelectType("GPU")) },
                    label = { Text("Gráficas") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selectors
            ComponentDropdown(
                label = "Componente 1",
                selectedComponent = state.selectedComponent1,
                components = state.filteredComponents,
                onSelect = { viewModel.onEvent(ComparatorEvent.SelectComponent1(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComponentDropdown(
                label = "Componente 2",
                selectedComponent = state.selectedComponent2,
                components = state.filteredComponents,
                onSelect = { viewModel.onEvent(ComparatorEvent.SelectComponent2(it)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Comparison Table
            if (state.selectedComponent1 != null && state.selectedComponent2 != null) {
                ComparisonContent(state.selectedComponent1!!, state.selectedComponent2!!)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Selecciona dos componentes para comparar", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentDropdown(
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
fun ComparisonContent(c1: Component, c2: Component) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComparisonHeader(c1, Modifier.weight(1f))
                VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                ComparisonHeader(c2, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item { ComparisonRow("Precio", "$${c1.price}", "$${c2.price}") }
        item { ComparisonRow("Marca", getBrand(c1), getBrand(c2)) }

        if (c1 is Component.CPU && c2 is Component.CPU) {
            item { ComparisonRow("Núcleos", c1.cores.toString(), c2.cores.toString()) }
            item { ComparisonRow("Hilos", c1.threads.toString(), c2.threads.toString()) }
            item { ComparisonRow("Reloj Boost", c1.boostClock, c2.boostClock) }
            item { ComparisonRow("Socket", c1.socket, c2.socket) }
        }

        if (c1 is Component.GPU && c2 is Component.GPU) {
            item { ComparisonRow("VRAM", c1.vram, c2.vram) }
            item { ComparisonRow("Tipo VRAM", c1.vramType, c2.vramType) }
            item { ComparisonRow("Consumo", "${c1.consumptionWatts}W", "${c2.consumptionWatts}W") }
            item { ComparisonRow("Fuente Rec.", c1.recommendedPSU ?: "N/A", c2.recommendedPSU ?: "N/A") }
        }
    }
}

@Composable
fun ComparisonHeader(component: Component, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = component.imageUrl ?: "https://via.placeholder.com/150",
            contentDescription = component.name,
            modifier = Modifier
                .size(100.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = component.name,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun ComparisonRow(label: String, val1: String, val2: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(text = val1, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(text = val2, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

fun getBrand(c: Component): String {
    return when (c) {
        is Component.CPU -> c.brand
        is Component.GPU -> c.brand
        is Component.Motherboard -> c.brand
        is Component.RAM -> c.brand
        is Component.PSU -> c.brand
    }
}
