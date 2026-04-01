package edu.ucne.corebuild.presentation.comparator

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import edu.ucne.corebuild.domain.model.Component

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparatorScreen(
    onMenuClick: () -> Unit,
    viewModel: ComparatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparador PRO") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Selector de tipo (CPU/GPU)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.selectedType == "CPU",
                    onClick = { viewModel.onEvent(ComparatorEvent.SelectType("CPU")) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) { Text("Procesadores") }
                SegmentedButton(
                    selected = uiState.selectedType == "GPU",
                    onClick = { viewModel.onEvent(ComparatorEvent.SelectType("GPU")) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) { Text("Gráficas") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selectores de componentes
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ComponentSelector(
                    label = "Componente 1",
                    selected = uiState.selectedComponent1,
                    options = uiState.filteredComponents,
                    onSelect = { viewModel.onEvent(ComparatorEvent.SelectComponent1(it)) },
                    modifier = Modifier.weight(1f)
                )
                ComponentSelector(
                    label = "Componente 2",
                    selected = uiState.selectedComponent2,
                    options = uiState.filteredComponents,
                    onSelect = { viewModel.onEvent(ComparatorEvent.SelectComponent2(it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Área de comparación
            if (uiState.selectedComponent1 != null && uiState.selectedComponent2 != null) {
                ComparisonMatrix(uiState.selectedComponent1!!, uiState.selectedComponent2!!)
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Text("Selecciona dos componentes para comparar", color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonMatrix(c1: Component, c2: Component) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            WinnerHeader(c1, c2)
        }

        // Definir métricas según el tipo
        val metrics = when {
            c1 is Component.CPU && c2 is Component.CPU -> listOf(
                Metric("Núcleos", c1.cores.toDouble(), c2.cores.toDouble(), "${c1.cores}", "${c2.cores}"),
                Metric("Hilos", c1.threads.toDouble(), c2.threads.toDouble(), "${c1.threads}", "${c2.threads}"),
                Metric("Precio", c1.price, c2.price, "$${c1.price}", "$${c2.price}", inverse = true)
            )
            c1 is Component.GPU && c2 is Component.GPU -> listOf(
                Metric("VRAM", extractValue(c1.vram), extractValue(c2.vram), c1.vram, c2.vram),
                Metric("Precio", c1.price, c2.price, "$${c1.price}", "$${c2.price}", inverse = true)
            )
            else -> emptyList()
        }

        items(metrics) { metric ->
            ComparisonBar(metric)
        }
    }
}

@Composable
fun WinnerHeader(c1: Component, c2: Component) {
    val winner = when {
        c1.price > c2.price -> c1
        c2.price > c1.price -> c2
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (winner != null) "El mejor rendimiento estimado: ${winner.name}" else "Rendimiento equilibrado",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ComparisonBar(metric: Metric) {
    val color1 = if (metric.v1 == metric.v2) Color.Gray else if ((metric.v1 > metric.v2 && !metric.inverse) || (metric.v1 < metric.v2 && metric.inverse)) Color(0xFF4CAF50) else Color(0xFFE57373)
    val color2 = if (metric.v1 == metric.v2) Color.Gray else if ((metric.v2 > metric.v1 && !metric.inverse) || (metric.v2 < metric.v1 && metric.inverse)) Color(0xFF4CAF50) else Color(0xFFE57373)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(metric.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Lado 1
            Box(modifier = Modifier.weight(1f).height(12.dp).clip(CircleShape).background(color1))
            
            // Valores
            Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(metric.t1, fontWeight = FontWeight.Bold, color = color1, fontSize = 12.sp)
                Text(" vs ", color = MaterialTheme.colorScheme.outline, fontSize = 10.sp)
                Text(metric.t2, fontWeight = FontWeight.Bold, color = color2, fontSize = 12.sp)
            }

            // Lado 2
            Box(modifier = Modifier.weight(1f).height(12.dp).clip(CircleShape).background(color2))
        }
    }
}

@Composable
fun ComponentSelector(
    label: String,
    selected: Component?,
    options: List<Component>,
    onSelect: (Component) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = selected?.name ?: "Seleccionar",
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected != null) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.45f)) {
            options.forEach { component ->
                DropdownMenuItem(
                    text = { Text(component.name, fontSize = 12.sp) },
                    onClick = {
                        onSelect(component)
                        expanded = false
                    }
                )
            }
        }
    }
}

data class Metric(val label: String, val v1: Double, val v2: Double, val t1: String, val t2: String, val inverse: Boolean = false)

fun extractValue(text: String): Double = text.filter { it.isDigit() }.toDoubleOrNull() ?: 0.0
